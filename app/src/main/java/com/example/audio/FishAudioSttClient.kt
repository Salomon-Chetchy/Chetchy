package com.example.audio

import android.content.Context
import com.example.BuildConfig
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed interface FishSttResult {
  data class Success(
    val transcript: String,
    val durationSeconds: Double? = null,
    val languageCode: String? = null,
  ) : FishSttResult

  data class MissingApiKey(val message: String) : FishSttResult

  data class Error(val message: String) : FishSttResult
}

interface FishAudioSttClient {
  suspend fun transcribeAudioFile(
    file: File,
    language: String? = null,
  ): FishSttResult
}

object FishAudioConfig {
  const val ASR_ENDPOINT = "https://api.fish.audio/v1/asr"
  const val ASR_MODEL = "transcribe-1-pro"
  private val PLACEHOLDER_KEYS = setOf("", "MY_FISH_AUDIO_API_KEY", "YOUR_API_KEY", "null")

  fun resolveApiKey(configured: String = BuildConfig.FISH_AUDIO_API_KEY): String? {
    val trimmed = configured.trim()
    return if (trimmed in PLACEHOLDER_KEYS) null else trimmed
  }

  fun resolveEffectiveApiKey(
    userApiKey: String?,
    buildConfigKey: String = BuildConfig.FISH_AUDIO_API_KEY,
  ): String? =
    userApiKey?.let { resolveApiKey(it) } ?: resolveApiKey(buildConfigKey)
}

object FishAudioPreferences {
  private const val PREFS_NAME = "chetchy_fish_audio_prefs"
  private const val KEY_API_KEY = "fish_audio_api_key"

  fun getApiKey(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_API_KEY, "")?.trim().orEmpty()
  }

  fun saveApiKey(context: Context, apiKey: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val clean = apiKey.trim()
    if (clean.isEmpty()) {
      prefs.edit().remove(KEY_API_KEY).apply()
    } else {
      prefs.edit().putString(KEY_API_KEY, clean).apply()
    }
  }
}

private val fishControlTagRegex = Regex("<\\|[^|]+\\|>")

fun cleanFishTranscriptText(rawText: String): String =
  rawText
    .replace(fishControlTagRegex, " ")
    .replace(Regex("\\s+"), " ")
    .trim()

fun parseFishAudioSttResponse(rawJson: String): FishSttResult {
  if (rawJson.isBlank()) {
    return FishSttResult.Error("Fish Audio returned an empty response.")
  }
  return try {
    val obj = JSONObject(rawJson)
    val errMsg =
      obj.optString("message").takeIf { it.isNotBlank() && !obj.has("text") }
        ?: obj.optString("detail").takeIf { it.isNotBlank() && !obj.has("text") }
        ?: obj.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
    if (!errMsg.isNullOrBlank()) {
      return FishSttResult.Error(errMsg)
    }

    val transcript = cleanFishTranscriptText(obj.optString("text", ""))
    if (transcript.isBlank()) {
      return FishSttResult.Error("No speech detected in the voice message.")
    }

    val duration = if (obj.has("duration") && !obj.isNull("duration")) obj.optDouble("duration") else null
    val languageCode = obj.optString("language_code", "").ifBlank { null }
    FishSttResult.Success(
      transcript = transcript,
      durationSeconds = duration,
      languageCode = languageCode,
    )
  } catch (e: Exception) {
    FishSttResult.Error("Invalid Fish Audio response (${e.localizedMessage ?: "parse error"}).")
  }
}

class HttpFishAudioSttClient(
  private val apiKeyProvider: () -> String? = { FishAudioConfig.resolveApiKey() },
  private val endpointUrl: String = FishAudioConfig.ASR_ENDPOINT,
  private val modelHeader: String = FishAudioConfig.ASR_MODEL,
) : FishAudioSttClient {

  override suspend fun transcribeAudioFile(
    file: File,
    language: String?,
  ): FishSttResult =
    withContext(Dispatchers.IO) {
      val apiKey = apiKeyProvider()?.trim().orEmpty()
      if (apiKey.isEmpty()) {
        return@withContext FishSttResult.MissingApiKey(
          "Fish Audio API key is not configured. Add it in Settings → Fish Audio Voice STT."
        )
      }
      if (!file.exists() || file.length() <= 0L) {
        return@withContext FishSttResult.Error("Recorded audio file is missing or empty.")
      }

      val boundary = "----ChetchyFishBoundary${UUID.randomUUID().toString().replace("-", "")}"
      val lineEnd = "\r\n"
      val twoHyphens = "--"

      var connection: HttpURLConnection? = null
      try {
        connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
          requestMethod = "POST"
          connectTimeout = 30_000
          readTimeout = 45_000
          doInput = true
          doOutput = true
          useCaches = false
          setRequestProperty("Authorization", "Bearer $apiKey")
          setRequestProperty("model", modelHeader)
          setRequestProperty("Accept", "application/json")
          setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        DataOutputStream(connection.outputStream).use { out ->
          // Part 1: ignore_timestamps=true for fast transcript response
          out.writeBytes("$twoHyphens$boundary$lineEnd")
          out.writeBytes("Content-Disposition: form-data; name=\"ignore_timestamps\"$lineEnd$lineEnd")
          out.writeBytes("true$lineEnd")

          // Optional language hint
          if (!language.isNullOrBlank()) {
            out.writeBytes("$twoHyphens$boundary$lineEnd")
            out.writeBytes("Content-Disposition: form-data; name=\"language\"$lineEnd$lineEnd")
            out.writeBytes("${language.trim()}$lineEnd")
          }

          // Part 2: audio file bytes
          val mimeType =
            when (file.extension.lowercase()) {
              "mp3" -> "audio/mpeg"
              "wav" -> "audio/wav"
              "ogg", "opus" -> "audio/ogg"
              "webm" -> "audio/webm"
              else -> "audio/mp4"
            }
          out.writeBytes("$twoHyphens$boundary$lineEnd")
          out.writeBytes(
            "Content-Disposition: form-data; name=\"audio\"; filename=\"${file.name}\"$lineEnd"
          )
          out.writeBytes("Content-Type: $mimeType$lineEnd$lineEnd")

          FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            while (true) {
              val read = input.read(buffer)
              if (read <= 0) break
              out.write(buffer, 0, read)
            }
          }
          out.writeBytes(lineEnd)
          out.writeBytes("$twoHyphens$boundary$twoHyphens$lineEnd")
          out.flush()
        }

        val status = connection.responseCode
        val stream =
          if (status in 200..299) {
            connection.inputStream
          } else {
            connection.errorStream
          }
        val body =
          stream?.use { s ->
            BufferedReader(InputStreamReader(s)).readText()
          }.orEmpty()

        if (status == 401 || status == 403) {
          return@withContext FishSttResult.MissingApiKey(
            "Fish Audio rejected the API key (HTTP $status). Update your key in Settings → Fish Audio Voice STT."
          )
        }
        if (status !in 200..299) {
          val errFromJson =
            runCatching {
              val obj = JSONObject(body)
              obj.optString("message").ifBlank { obj.optString("detail") }
            }.getOrNull()
          return@withContext FishSttResult.Error(
            errFromJson?.takeIf { it.isNotBlank() } ?: "Fish Audio request failed (HTTP $status)."
          )
        }

        parseFishAudioSttResponse(body)
      } catch (e: Exception) {
        FishSttResult.Error("Voice transcription error: ${e.localizedMessage ?: "network error"}")
      } finally {
        connection?.disconnect()
      }
    }
}
