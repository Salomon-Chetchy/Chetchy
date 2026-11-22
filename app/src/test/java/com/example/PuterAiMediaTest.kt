package com.example

import com.example.ai.PuterAiEngine
import com.example.ai.PuterAiResult
import com.example.ai.PuterChatTurn
import com.example.ai.PuterImageResult
import com.example.ai.PuterModels
import com.example.ui.ChatsViewModel
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
  fun `ChatsViewModel generateAiImage appends prompt and AI Image response message`() = runTest {
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
          return PuterImageResult.Success("https://cdn.puter.site/img/cyber.png")
        }
      }

    val vm = ChatsViewModel(puterAiEngine = fakeEngine)
    vm.openChatWithContact("aria_coach")
    vm.generateAiImage(
      prompt = "Cyberpunk skyline at night",
      imagePersister = { it },
    )
    advanceUntilIdle()

    assertEquals("Cyberpunk skyline at night", capturedPrompt)
    val messages = vm.uiState.value.activeMessages
    assertTrue(messages.any { it.isOutgoing && it.text.contains("Cyberpunk skyline at night") })
    val generatedMsg = messages.last()
    assertFalse(generatedMsg.isOutgoing)
    assertEquals("AI Image", generatedMsg.mediaLabel)
    assertEquals("https://cdn.puter.site/img/cyber.png", generatedMsg.imageUri)
    assertEquals(null, vm.uiState.value.aiGeneratingImageContactId)
  }

  @Test
  fun `ChatsViewModel slash imagine command triggers AI image generation`() = runTest {
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
          return PuterImageResult.Success("https://cdn.puter.site/img/sunset.png")
        }
      }

    val vm = ChatsViewModel(puterAiEngine = fakeEngine)
    vm.openChatWithContact("aria_coach")
    vm.updateChatInput("/imagine Pastel mountain lake")
    vm.sendMessage(imagePersister = { it })
    advanceUntilIdle()

    assertEquals("Pastel mountain lake", capturedPrompt)
    val generatedMsg = vm.uiState.value.activeMessages.last()
    assertEquals("AI Image", generatedMsg.mediaLabel)
    assertEquals("https://cdn.puter.site/img/sunset.png", generatedMsg.imageUri)
  }

  @Test
  fun `ChatsViewModel togglePendingPhotoIdentify flips flag`() {
    val vm = ChatsViewModel()
    assertTrue(vm.uiState.value.pendingPhotoIdentifyWithAi)
    vm.togglePendingPhotoIdentify()
    assertFalse(vm.uiState.value.pendingPhotoIdentifyWithAi)
    vm.togglePendingPhotoIdentify()
    assertTrue(vm.uiState.value.pendingPhotoIdentifyWithAi)
  }
}
