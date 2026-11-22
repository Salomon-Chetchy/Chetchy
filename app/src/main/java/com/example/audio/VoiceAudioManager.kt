package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RecordedVoiceNote(
  val filePath: String,
  val durationMillis: Long,
  val waveformBars: List<Float>,
)

data class VoicePlaybackState(
  val playingMessageId: String? = null,
  val isPlaying: Boolean = false,
  val progress: Float = 0f,
  val positionMs: Long = 0L,
  val durationMs: Long = 0L,
)

fun formatVoiceDuration(durationMs: Long): String {
  val totalSeconds = (durationMs.coerceAtLeast(0L) / 1000L).toInt()
  val mins = totalSeconds / 60
  val secs = totalSeconds % 60
  return String.format(Locale.US, "%d:%02d", mins, secs)
}

fun normalizeWaveformBars(
  samples: List<Float>,
  targetBarCount: Int = 26,
): List<Float> {
  if (targetBarCount <= 0) return emptyList()
  if (samples.isEmpty()) {
    return List(targetBarCount) { idx ->
      when (idx % 4) {
        0 -> 0.28f
        1 -> 0.56f
        2 -> 0.40f
        else -> 0.70f
      }
    }
  }
  if (samples.size == targetBarCount) {
    return samples.map { it.coerceIn(0.18f, 1.0f) }
  }
  return List(targetBarCount) { barIdx ->
    val mapped = (barIdx.toFloat() / targetBarCount.toFloat()) * samples.size
    val idx = mapped.roundToInt().coerceIn(0, samples.lastIndex)
    samples[idx].coerceIn(0.18f, 1.0f)
  }
}

class VoiceRecorderController {
  private var recorder: MediaRecorder? = null
  private var currentOutputFile: File? = null
  private var startedAtMs: Long = 0L
  private val capturedAmplitudes = mutableListOf<Float>()

  val isRecording: Boolean
    get() = recorder != null

  fun startRecording(context: Context): Boolean {
    stopSilently()
    val voiceDir = File(context.filesDir, "voice_notes").apply { mkdirs() }
    val outFile = File(voiceDir, "voice_${UUID.randomUUID()}.m4a")

    val mediaRecorder =
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          MediaRecorder(context)
        } else {
          @Suppress("DEPRECATION")
          MediaRecorder()
        }
      } catch (_: Exception) {
        return false
      }

    return try {
      mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
      mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
      mediaRecorder.setAudioSamplingRate(24000)
      mediaRecorder.setAudioEncodingBitRate(48000)
      mediaRecorder.setOutputFile(outFile.absolutePath)
      mediaRecorder.prepare()
      mediaRecorder.start()

      recorder = mediaRecorder
      currentOutputFile = outFile
      startedAtMs = System.currentTimeMillis()
      capturedAmplitudes.clear()
      true
    } catch (_: Exception) {
      runCatching { mediaRecorder.reset() }
      runCatching { mediaRecorder.release() }
      runCatching { outFile.delete() }
      recorder = null
      currentOutputFile = null
      false
    }
  }

  fun sampleNormalizedAmplitude(): Float {
    val rec = recorder ?: return 0.2f
    val rawAmp = runCatching { rec.maxAmplitude }.getOrDefault(0)
    // Scale 0..32767 aggressively for natural speech visibility
    val normalized = (kotlin.math.sqrt((rawAmp / 32767.0).coerceIn(0.0, 1.0))).toFloat().coerceIn(0.18f, 1.0f)
    if (capturedAmplitudes.size < 400) {
      capturedAmplitudes.add(normalized)
    }
    return normalized
  }

  fun elapsedMillis(): Long =
    if (startedAtMs > 0L) (System.currentTimeMillis() - startedAtMs).coerceAtLeast(0L) else 0L

  fun stopAndSave(): RecordedVoiceNote? {
    val rec = recorder ?: return null
    val outFile = currentOutputFile
    val elapsed = elapsedMillis().coerceAtLeast(450L)
    recorder = null
    currentOutputFile = null
    startedAtMs = 0L

    val stoppedCleanly =
      runCatching {
        rec.stop()
        true
      }.getOrDefault(false)
    runCatching { rec.reset() }
    runCatching { rec.release() }

    if (!stoppedCleanly || outFile == null || !outFile.exists() || outFile.length() < 128L) {
      runCatching { outFile?.delete() }
      return null
    }

    val bars = normalizeWaveformBars(capturedAmplitudes)
    return RecordedVoiceNote(
      filePath = outFile.absolutePath,
      durationMillis = elapsed,
      waveformBars = bars,
    )
  }

  fun cancelRecording() {
    val outFile = currentOutputFile
    stopSilently()
    runCatching { outFile?.delete() }
  }

  private fun stopSilently() {
    val rec = recorder ?: return
    recorder = null
    startedAtMs = 0L
    runCatching { rec.stop() }
    runCatching { rec.reset() }
    runCatching { rec.release() }
    currentOutputFile = null
    capturedAmplitudes.clear()
  }
}

class VoicePlaybackController(
  private val scope: CoroutineScope,
) {
  private var player: MediaPlayer? = null
  private var progressJob: Job? = null

  private val _state = MutableStateFlow(VoicePlaybackState())
  val state: StateFlow<VoicePlaybackState> = _state.asStateFlow()

  fun togglePlayback(
    messageId: String,
    audioPath: String?,
    fallbackDurationMs: Long,
  ) {
    if (audioPath.isNullOrBlank()) return
    val current = _state.value
    if (current.playingMessageId == messageId && player != null) {
      if (current.isPlaying) {
        pause()
      } else {
        resume()
      }
      return
    }

    startNewPlayback(messageId = messageId, audioPath = audioPath, fallbackDurationMs = fallbackDurationMs)
  }

  private fun startNewPlayback(
    messageId: String,
    audioPath: String,
    fallbackDurationMs: Long,
  ) {
    stop()
    val file = File(audioPath)
    if (!file.exists()) return

    val mediaPlayer = MediaPlayer()
    try {
      mediaPlayer.setDataSource(audioPath)
      mediaPlayer.prepare()
      val actualDuration = mediaPlayer.duration.toLong().takeIf { it > 0L } ?: fallbackDurationMs.coerceAtLeast(1000L)
      mediaPlayer.setOnCompletionListener {
        stop()
      }
      mediaPlayer.start()
      player = mediaPlayer
      _state.value =
        VoicePlaybackState(
          playingMessageId = messageId,
          isPlaying = true,
          progress = 0f,
          positionMs = 0L,
          durationMs = actualDuration,
        )
      startProgressPolling(messageId, actualDuration)
    } catch (_: Exception) {
      runCatching { mediaPlayer.release() }
      _state.value = VoicePlaybackState()
    }
  }

  private fun pause() {
    val p = player ?: return
    runCatching {
      if (p.isPlaying) p.pause()
    }
    progressJob?.cancel()
    _state.value = _state.value.copy(isPlaying = false)
  }

  private fun resume() {
    val p = player ?: return
    val msgId = _state.value.playingMessageId ?: return
    val dur = _state.value.durationMs.coerceAtLeast(1000L)
    runCatching { p.start() }
    _state.value = _state.value.copy(isPlaying = true)
    startProgressPolling(msgId, dur)
  }

  private fun startProgressPolling(messageId: String, totalDurationMs: Long) {
    progressJob?.cancel()
    progressJob =
      scope.launch(Dispatchers.Main) {
        while (isActive) {
          val p = player ?: break
          val pos = runCatching { p.currentPosition.toLong() }.getOrDefault(0L).coerceIn(0L, totalDurationMs)
          val ratio = (pos.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
          _state.value =
            _state.value.copy(
              playingMessageId = messageId,
              isPlaying = runCatching { p.isPlaying }.getOrDefault(false),
              progress = ratio,
              positionMs = pos,
              durationMs = totalDurationMs,
            )
          delay(60L)
        }
      }
  }

  fun stop() {
    progressJob?.cancel()
    progressJob = null
    val p = player
    player = null
    if (p != null) {
      runCatching {
        if (p.isPlaying) p.stop()
      }
      runCatching { p.reset() }
      runCatching { p.release() }
    }
    _state.value = VoicePlaybackState()
  }
}
