package com.example

import com.example.ai.ChatHistoryTurnInput
import com.example.ai.ParsedAiReply
import com.example.ai.PdfTextExtractor
import com.example.ai.PuterAiEngine
import com.example.ai.PuterAiResult
import com.example.ai.PuterChatOrchestrator
import com.example.ai.PuterChatTurn
import com.example.ai.PuterImageResult
import com.example.ai.PuterModels
import com.example.audio.FishAudioConfig
import com.example.audio.FishAudioSttClient
import com.example.audio.FishSttResult
import com.example.audio.cleanFishTranscriptText
import com.example.audio.formatVoiceDuration
import com.example.audio.normalizeWaveformBars
import com.example.ui.ChatsViewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.Deflater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PuterAiMediaTest {
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `PuterModels resolveModelForTurns keeps vision models and upgrades deepseek on photo turns`() {
    val textTurns = listOf(PuterChatTurn(role = "user", content = "Hi"))
    val photoTurns =
      listOf(
        PuterChatTurn(
          role = "user",
          content = "What is this?",
          imageDataUrl = "data:image/jpeg;base64,abc123",
        )
      )

    assertEquals("deepseek/deepseek-chat", PuterModels.resolveModelForTurns("deepseek/deepseek-chat", textTurns))
    assertEquals("gpt-4o-mini", PuterModels.resolveModelForTurns("deepseek/deepseek-chat", photoTurns))
    assertEquals("gpt-5-nano", PuterModels.resolveModelForTurns("gpt-5-nano", photoTurns))
    assertEquals("google/gemini-2.5-flash-lite", PuterModels.resolveModelForTurns("google/gemini-2.5-flash-lite", photoTurns))
  }

  @Test
  fun `detectDirectImageGenerationPrompt distinguishes explicit requests from visual curiosity`() {
    assertEquals("a volcano", PuterChatOrchestrator.detectDirectImageGenerationPrompt("generate an image of a volcano"))
    assertEquals("a snowy owl", PuterChatOrchestrator.detectDirectImageGenerationPrompt("show me an image of a snowy owl"))
    assertEquals("neon skyline", PuterChatOrchestrator.detectDirectImageGenerationPrompt("/imagine neon skyline"))
    assertEquals("a cyber cat", PuterChatOrchestrator.detectDirectImageGenerationPrompt("Can you draw me a cyber cat please?"))

    // Curiosity questions should go to chat AI first so AI can ask the user to validate before generating
    assertNull(PuterChatOrchestrator.detectDirectImageGenerationPrompt("can i see how a volcano looks like"))
    assertNull(PuterChatOrchestrator.detectDirectImageGenerationPrompt("how does a volcano look?"))
    assertNull(PuterChatOrchestrator.detectDirectImageGenerationPrompt("what does an aurora borealis look like"))
    assertNull(PuterChatOrchestrator.detectDirectImageGenerationPrompt("show me how to write kotlin"))
  }

  @Test
  fun `parseAiReplyDirectives extracts generation and validation offer directives cleanly`() {
    val genParsed =
      PuterChatOrchestrator.parseAiReplyDirectives(
        "Here you go!\n[GENERATE_IMAGE: dramatic erupting volcano at dusk]"
      )
    assertTrue(genParsed is ParsedAiReply.GenerateImage)
    assertEquals("dramatic erupting volcano at dusk", (genParsed as ParsedAiReply.GenerateImage).prompt)
    assertEquals("Here you go!", genParsed.companionText)

    val askParsed =
      PuterChatOrchestrator.parseAiReplyDirectives(
        "A volcano is a mountain vent that releases lava. Want me to generate an image? [ASK_GENERATE_IMAGE: glowing volcano erupting]"
      )
    assertTrue(askParsed is ParsedAiReply.AskImageValidation)
    val ask = askParsed as ParsedAiReply.AskImageValidation
    assertFalse(ask.text.contains("ASK_GENERATE_IMAGE"))
    assertEquals("glowing volcano erupting", ask.offeredPrompt)
  }

  @Test
  fun `ChatsViewModel handles direct image prompt without special buttons`() = runTest {
    var capturedPrompt: String? = null
    val fakeEngine =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult = PuterAiResult.Success("ok")

        override suspend fun generateImage(
          prompt: String,
          authToken: String?,
        ): PuterImageResult {
          capturedPrompt = prompt
          return PuterImageResult.Success("https://cdn.puter.site/img/volcano.png")
        }
      }

    val vm = ChatsViewModel(puterAiEngine = fakeEngine)
    vm.openChatWithContact("aria_coach")
    vm.updateChatInput("show me an image of a volcano")
    vm.sendMessage(imagePersister = { it })
    advanceUntilIdle()

    assertEquals("a volcano", capturedPrompt)
    val messages = vm.uiState.value.activeMessages
    assertTrue(messages.any { it.isOutgoing && it.text == "show me an image of a volcano" })
    val generatedMsg = messages.last()
    assertFalse(generatedMsg.isOutgoing)
    assertEquals("AI Image", generatedMsg.mediaLabel)
    assertEquals("https://cdn.puter.site/img/volcano.png", generatedMsg.imageUri)
    assertNull(vm.uiState.value.aiGeneratingImageContactId)
  }

  @Test
  fun `ChatsViewModel asks validation first on visual question then generates on yes`() = runTest {
    var generatedPrompt: String? = null
    val fakeEngine =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult =
          PuterAiResult.Success(
            "A volcano is a mountain with molten lava inside. Would you like me to generate an image of how a volcano looks? [ASK_GENERATE_IMAGE: realistic erupting volcano with glowing lava]"
          )

        override suspend fun generateImage(
          prompt: String,
          authToken: String?,
        ): PuterImageResult {
          generatedPrompt = prompt
          return PuterImageResult.Success("https://cdn.puter.site/img/validated_volcano.png")
        }
      }

    val vm = ChatsViewModel(puterAiEngine = fakeEngine)
    vm.openChatWithContact("aria_coach")

    // Step 1: User asks "can i see how a volcano looks like" -> AI asks to validate
    vm.updateChatInput("can i see how a volcano looks like")
    vm.sendMessage(imagePersister = { it })
    advanceUntilIdle()

    assertNull(generatedPrompt)
    val validationReply = vm.uiState.value.activeMessages.last()
    assertFalse(validationReply.isOutgoing)
    assertTrue(validationReply.text.contains("Would you like me to generate an image"))
    assertFalse(validationReply.text.contains("ASK_GENERATE_IMAGE"))
    assertEquals("realistic erupting volcano with glowing lava", validationReply.offeredImagePrompt)

    // Step 2: User validates with "yes" -> AI generates image
    vm.updateChatInput("yes")
    vm.sendMessage(imagePersister = { it })
    advanceUntilIdle()

    assertEquals("realistic erupting volcano with glowing lava", generatedPrompt)
    val imageMsg = vm.uiState.value.activeMessages.last()
    assertEquals("AI Image", imageMsg.mediaLabel)
    assertEquals("https://cdn.puter.site/img/validated_volcano.png", imageMsg.imageUri)
  }

  @Test
  fun `ChatsViewModel handles LLM GENERATE_IMAGE directive naturally`() = runTest {
    var generatedPrompt: String? = null
    val fakeEngine =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult =
          PuterAiResult.Success(
            "Here is that mountain view for you!\n[GENERATE_IMAGE: serene sunrise over snowy alps]"
          )

        override suspend fun generateImage(
          prompt: String,
          authToken: String?,
        ): PuterImageResult {
          generatedPrompt = prompt
          return PuterImageResult.Success("https://cdn.puter.site/img/alps.png")
        }
      }

    val vm = ChatsViewModel(puterAiEngine = fakeEngine)
    vm.openChatWithContact("aria_coach")
    vm.updateChatInput("could you visualize a peaceful sunrise on the alps")
    vm.sendMessage(imagePersister = { it })
    advanceUntilIdle()

    assertEquals("serene sunrise over snowy alps", generatedPrompt)
    val imageMsg = vm.uiState.value.activeMessages.last()
    assertEquals("AI Image", imageMsg.mediaLabel)
    assertEquals("Here is that mountain view for you!", imageMsg.text)
    assertEquals("https://cdn.puter.site/img/alps.png", imageMsg.imageUri)
  }

  @Test
  fun `buildHistoryTurns remembers uploaded image for 10 follow-up user messages then prunes`() {
    val photoDataUrl = "data:image/jpeg;base64,photo_payload"
    val messages = mutableListOf<ChatHistoryTurnInput>()

    // User message #0: uploaded photo with no text caption
    messages.add(
      ChatHistoryTurnInput(
        id = "u0",
        text = "",
        isOutgoing = true,
        mediaLabel = "Photo",
        imageDataUrl = photoDataUrl,
      )
    )
    messages.add(
      ChatHistoryTurnInput(
        id = "a0",
        text = "I see a red bicycle in this image. What would you like me to do with it?",
        isOutgoing = false,
      )
    )

    // First turn (0 follow-ups after upload): imageDataUrl + no-caption prompt must be included
    val firstTurn =
      PuterChatOrchestrator.buildHistoryTurns(
        systemInstruction = "sys",
        messages = messages,
        currentUserMessageId = "u0",
      )
    val photoTurnImmediate = firstTurn.find { it.imageDataUrl == photoDataUrl }
    assertNotNull(photoTurnImmediate)
    assertTrue(photoTurnImmediate!!.content.contains("uploaded this image without text instructions"))

    // Add 10 follow-up user messages (u1..u10)
    for (i in 1..10) {
      messages.add(ChatHistoryTurnInput(id = "u$i", text = "Follow up $i", isOutgoing = true))
      if (i < 10) {
        messages.add(ChatHistoryTurnInput(id = "a$i", text = "Reply $i", isOutgoing = false))
      }
    }

    val turnsAt10FollowUps =
      PuterChatOrchestrator.buildHistoryTurns(
        systemInstruction = "sys",
        messages = messages,
        currentUserMessageId = "u10",
      )
    assertTrue(turnsAt10FollowUps.any { it.imageDataUrl == photoDataUrl })

    // Add 11th follow-up user message (exceeds 10-user-message memory window)
    messages.add(ChatHistoryTurnInput(id = "a10", text = "Reply 10", isOutgoing = false))
    messages.add(ChatHistoryTurnInput(id = "u11", text = "Follow up 11", isOutgoing = true))

    val turnsAt11FollowUps =
      PuterChatOrchestrator.buildHistoryTurns(
        systemInstruction = "sys",
        messages = messages,
        currentUserMessageId = "u11",
      )
    assertFalse(turnsAt11FollowUps.any { it.imageDataUrl == photoDataUrl })
  }

  @Test
  fun `PdfTextExtractor extracts text from uncompressed and FlateDecode PDF streams`() {
    val uncompressedStream = "BT /F1 12 Tf 72 720 Td (Quarterly Budget Report) Tj T* [(Total ) -250 (Revenue: ) -220 ($48,200)] TJ ET"
    val uncompressedPdf =
      ("%PDF-1.4\n1 0 obj\n<< /Length ${uncompressedStream.length} >>\nstream\n$uncompressedStream\nendstream\nendobj\n%%EOF")
        .toByteArray(Charsets.ISO_8859_1)

    val extractedPlain = PdfTextExtractor.extractTextFromPdfBytes(uncompressedPdf)
    assertTrue(extractedPlain.contains("Quarterly Budget Report"))
    assertTrue(extractedPlain.contains("Total Revenue: $48,200"))

    val compressedSource = "BT /F1 12 Tf 72 720 Td (Chetchy Project Notes) Tj T* (Milestone Alpha Complete) Tj ET"
      .toByteArray(Charsets.ISO_8859_1)
    val deflater = Deflater()
    deflater.setInput(compressedSource)
    deflater.finish()
    val compressedOut = ByteArrayOutputStream()
    val buf = ByteArray(256)
    while (!deflater.finished()) {
      val n = deflater.deflate(buf)
      if (n > 0) compressedOut.write(buf, 0, n)
    }
    deflater.end()
    val compressedBytes = compressedOut.toByteArray()

    val header = "%PDF-1.5\n2 0 obj\n<< /Filter /FlateDecode /Length ${compressedBytes.size} >>\nstream\n"
      .toByteArray(Charsets.ISO_8859_1)
    val footer = "\nendstream\nendobj\n%%EOF".toByteArray(Charsets.ISO_8859_1)
    val compressedPdf = header + compressedBytes + footer

    val extractedFlate = PdfTextExtractor.extractTextFromPdfBytes(compressedPdf)
    assertTrue(extractedFlate.contains("Chetchy Project Notes"))
    assertTrue(extractedFlate.contains("Milestone Alpha Complete"))
  }

  @Test
  fun `buildHistoryTurns keeps uploaded document content for 10 follow-up messages then prunes`() {
    val docText = "Meeting Agenda:\n1. Launch Chetchy v2\n2. Review analytics"
    val messages = mutableListOf<ChatHistoryTurnInput>()

    messages.add(
      ChatHistoryTurnInput(
        id = "u0",
        text = "",
        isOutgoing = true,
        mediaLabel = "Document",
        documentName = "agenda.txt",
        documentSizeLabel = "2 KB",
        documentExtractedText = docText,
      )
    )
    messages.add(
      ChatHistoryTurnInput(
        id = "a0",
        text = "Your document lists the meeting agenda. What would you like me to do?",
        isOutgoing = false,
      )
    )

    val immediateTurns =
      PuterChatOrchestrator.buildHistoryTurns(
        systemInstruction = "sys",
        messages = messages,
        currentUserMessageId = "u0",
      )
    val docTurnImmediate = immediateTurns.find { it.content.contains("agenda.txt") }
    assertNotNull(docTurnImmediate)
    assertTrue(docTurnImmediate!!.content.contains("without text instructions"))
    assertTrue(docTurnImmediate.content.contains("Launch Chetchy v2"))

    for (i in 1..10) {
      messages.add(ChatHistoryTurnInput(id = "u$i", text = "Follow up $i", isOutgoing = true))
      if (i < 10) {
        messages.add(ChatHistoryTurnInput(id = "a$i", text = "Reply $i", isOutgoing = false))
      }
    }

    val turnsAt10FollowUps =
      PuterChatOrchestrator.buildHistoryTurns(
        systemInstruction = "sys",
        messages = messages,
        currentUserMessageId = "u10",
      )
    assertTrue(turnsAt10FollowUps.any { it.content.contains("Launch Chetchy v2") })

    messages.add(ChatHistoryTurnInput(id = "a10", text = "Reply 10", isOutgoing = false))
    messages.add(ChatHistoryTurnInput(id = "u11", text = "Follow up 11", isOutgoing = true))

    val turnsAt11FollowUps =
      PuterChatOrchestrator.buildHistoryTurns(
        systemInstruction = "sys",
        messages = messages,
        currentUserMessageId = "u11",
      )
    assertFalse(turnsAt11FollowUps.any { it.content.contains("Launch Chetchy v2") })
  }

  @Test
  fun `PuterModels resolveModelForTurns upgrades to vision model for scanned PDF page images`() {
    val scannedPdfTurns =
      listOf(
        PuterChatTurn(
          role = "user",
          content = "Read this PDF",
          extraImageDataUrls = listOf("data:image/jpeg;base64,page1"),
        )
      )
    assertEquals("gpt-4o-mini", PuterModels.resolveModelForTurns("deepseek/deepseek-chat", scannedPdfTurns))
  }

  @Test
  fun `FishAudioConfig and voice helpers clean control tags and format durations`() {
    assertNull(FishAudioConfig.resolveApiKey(""))
    assertNull(FishAudioConfig.resolveApiKey("MY_FISH_AUDIO_API_KEY"))
    assertEquals("sk-test-token", FishAudioConfig.resolveApiKey(" sk-test-token "))

    assertEquals(
      "Hello Aria, how many sets today?",
      cleanFishTranscriptText("<|speaker:0|> Hello Aria,   how many sets today? <|laughter|>"),
    )
    assertEquals("0:00", formatVoiceDuration(0L))
    assertEquals("0:08", formatVoiceDuration(8_400L))
    assertEquals("1:05", formatVoiceDuration(65_000L))

    val bars = normalizeWaveformBars(listOf(0.05f, 0.5f, 1.5f), targetBarCount = 12)
    assertEquals(12, bars.size)
    assertTrue(bars.all { it in 0.18f..1.0f })
  }

  @Test
  fun `ChatsViewModel sendVoiceMessage transcribes via Fish Audio STT in background for Puter AI`() = runTest {
    var capturedTurns: List<PuterChatTurn> = emptyList()
    val fakeAi =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult {
          capturedTurns = messages
          return PuterAiResult.Success("Great plan! Do 3 sets of 12 push-ups today.")
        }
      }
    val fakeStt =
      object : FishAudioSttClient {
        override suspend fun transcribeAudioFile(
          file: File,
          language: String?,
        ): FishSttResult = FishSttResult.Success(transcript = "What workout should I do today?")
      }

    val vm = ChatsViewModel(puterAiEngine = fakeAi, fishAudioSttClient = fakeStt)
    vm.openChatWithContact("aria_coach")
    vm.sendVoiceMessage(
      audioFilePath = "/tmp/test_voice.m4a",
      durationMillis = 7_200L,
      waveformBars = listOf(0.3f, 0.7f, 0.5f),
    )
    advanceUntilIdle()

    val messages = vm.uiState.value.activeMessages
    val outgoingVoice = messages.first { it.isOutgoing && it.mediaLabel == "Voice" }
    assertEquals("/tmp/test_voice.m4a", outgoingVoice.audioFilePath)
    assertEquals(7_200L, outgoingVoice.audioDurationMs)
    assertEquals("What workout should I do today?", outgoingVoice.voiceTranscript)

    val aiReply = messages.last()
    assertFalse(aiReply.isOutgoing)
    assertEquals("Great plan! Do 3 sets of 12 push-ups today.", aiReply.text)
    assertTrue(
      capturedTurns.any {
        it.role == "user" && it.content.contains("[Voice message (0:07)]: What workout should I do today?")
      }
    )
  }

  @Test
  fun `ChatsViewModel voice message requesting image triggers Puter image generation`() = runTest {
    var generatedPrompt: String? = null
    val fakeAi =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult = PuterAiResult.Success("ok")

        override suspend fun generateImage(
          prompt: String,
          authToken: String?,
        ): PuterImageResult {
          generatedPrompt = prompt
          return PuterImageResult.Success("https://cdn.puter.site/img/mountain.png")
        }
      }
    val fakeStt =
      object : FishAudioSttClient {
        override suspend fun transcribeAudioFile(
          file: File,
          language: String?,
        ): FishSttResult = FishSttResult.Success(transcript = "generate an image of a snowy mountain")
      }

    val vm = ChatsViewModel(puterAiEngine = fakeAi, fishAudioSttClient = fakeStt)
    vm.openChatWithContact("aria_coach")
    vm.sendVoiceMessage(
      audioFilePath = "/tmp/test_voice_img.m4a",
      durationMillis = 4_000L,
      waveformBars = listOf(0.4f, 0.8f),
      imagePersister = { it },
    )
    advanceUntilIdle()

    assertEquals("a snowy mountain", generatedPrompt)
    val last = vm.uiState.value.activeMessages.last()
    assertEquals("AI Image", last.mediaLabel)
    assertEquals("https://cdn.puter.site/img/mountain.png", last.imageUri)
  }

  @Test
  fun `FishAudioConfig resolveEffectiveApiKey prioritizes user settings key over build config`() {
    assertEquals(
      "sk-user-key-9999",
      FishAudioConfig.resolveEffectiveApiKey(
        userApiKey = " sk-user-key-9999 ",
        buildConfigKey = "MY_FISH_AUDIO_API_KEY",
      ),
    )
    assertEquals(
      "sk-env-fallback-1111",
      FishAudioConfig.resolveEffectiveApiKey(
        userApiKey = "   ",
        buildConfigKey = "sk-env-fallback-1111",
      ),
    )
    assertNull(
      FishAudioConfig.resolveEffectiveApiKey(
        userApiKey = "",
        buildConfigKey = "MY_FISH_AUDIO_API_KEY",
      )
    )
  }

  @Test
  fun `ChatsViewModel setFishAudioApiKey updates state and shows Settings guidance when missing`() = runTest {
    val missingStt =
      object : FishAudioSttClient {
        override suspend fun transcribeAudioFile(
          file: File,
          language: String?,
        ): FishSttResult =
          FishSttResult.MissingApiKey(
            "Fish Audio API key is not configured. Add it in Settings → Fish Audio Voice STT."
          )
      }

    val vm = ChatsViewModel(fishAudioSttClient = missingStt)
    assertFalse(vm.uiState.value.hasFishAudioApiKey)

    vm.setFishAudioApiKey("  sk-user-sample-4321  ")
    assertEquals("sk-user-sample-4321", vm.uiState.value.fishAudioApiKey)
    assertTrue(vm.uiState.value.hasFishAudioApiKey)
    assertEquals("Fish Audio API key saved", vm.uiState.value.snackbarMessage)

    vm.setFishAudioApiKey("")
    assertEquals("", vm.uiState.value.fishAudioApiKey)
    assertEquals("Fish Audio API key cleared", vm.uiState.value.snackbarMessage)

    vm.openChatWithContact("aria_coach")
    vm.sendVoiceMessage(
      audioFilePath = "/tmp/no_key.m4a",
      durationMillis = 3_000L,
      waveformBars = listOf(0.4f, 0.7f),
    )
    advanceUntilIdle()

    val lastMsg = vm.uiState.value.activeMessages.last()
    assertFalse(lastMsg.isOutgoing)
    assertTrue(lastMsg.text.contains("Settings → Fish Audio Voice STT"))
    assertNull(vm.uiState.value.aiThinkingContactId)
  }
}
