package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.test.core.app.ApplicationProvider
import android.net.Uri
import com.example.ai.PuterAiEngine
import com.example.ai.PuterAiResult
import com.example.ai.PuterChatTurn
import com.example.ai.PuterImageResult
import com.example.ai.parsePuterDriverResponse
import com.example.ai.parsePuterImageResponse
import com.example.ui.ChatsScreen
import com.example.ui.ChatsViewModel
import com.example.ui.components.parseChatMarkdown
import com.example.ui.theme.WhatsAppDarkTheme
import com.example.update.AppUpdateClient
import com.example.update.UpdateFetchResult
import com.example.update.UpdateReleaseInfo
import com.example.update.formatSizeInMb
import com.example.update.isNewerRelease
import com.example.update.parseReleaseJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Chats", appName)
  }

  @Test
  fun `chats screen displays AI persona contacts and navigation tabs`() {
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = ChatsViewModel())
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("camera_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("more_options_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("new_chat_plus_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chats_search_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("archived_row").assertDoesNotExist()

    composeTestRule.onNodeWithText("Chetchy").assertIsDisplayed()
    composeTestRule.onNodeWithText("Archived").assertDoesNotExist()
    composeTestRule.onNodeWithText("Aria Coach").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sage Mentor").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_chats").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_calls").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_updates").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_settings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_communities").assertDoesNotExist()
  }

  @Test
  fun `new chat to new contact creation opens chat interface and sends message`() {
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = ChatsViewModel())
      }
    }
    composeTestRule.waitForIdle()

    // Open New chat sheet via FAB
    composeTestRule.onNodeWithTag("new_chat_plus_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(500L)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("new_chat_search_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("new_contact_row").assertIsDisplayed()
    composeTestRule.onNodeWithTag("share_invite_link_row").assertIsDisplayed()

    // Open New contact sheet
    composeTestRule.onNodeWithTag("new_contact_row").performClick()
    composeTestRule.mainClock.advanceTimeBy(500L)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("first_name_input").performTextInput("Kate")
    composeTestRule.onNodeWithTag("last_name_input").performTextInput("Ray")
    composeTestRule.onNodeWithTag("phone_number_input").performTextInput("5550199")
    composeTestRule.waitForIdle()

    // Save contact and open Chat interface
    composeTestRule.onNodeWithTag("new_contact_done_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(600L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Kate Ray").assertIsDisplayed()
    composeTestRule.onNodeWithText("tap here for contact info").assertIsDisplayed()
    composeTestRule.onNodeWithText("Today").assertIsDisplayed()
    composeTestRule.onNodeWithTag("media_tooltip_banner").assertIsDisplayed()

    // Type and send a message
    composeTestRule.onNodeWithTag("chat_message_input").performTextInput("Hey Kate!")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("chat_send_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Hey Kate!").assertIsDisplayed()
  }

  @Test
  fun `updates tab allows posting text status and creating a channel`() {
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = ChatsViewModel())
      }
    }
    composeTestRule.waitForIdle()

    // Navigate to Updates tab
    composeTestRule.onNodeWithTag("nav_updates").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Status").assertIsDisplayed()
    composeTestRule.onNodeWithText("My status").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tap to add status update").assertIsDisplayed()
    composeTestRule.onNodeWithText("Channels").assertIsDisplayed()

    // Post a text status update
    composeTestRule.onNodeWithTag("updates_pencil_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("text_status_input").performTextInput("Hello Updates!")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("post_text_status_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("status_preview_0").assertIsDisplayed()

    // Create a channel
    composeTestRule.onNodeWithTag("create_channel_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("channel_name_input").performTextInput("Design Notes")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_create_channel_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(500L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Design Notes").assertIsDisplayed()

    // Navigate back and verify Calls tab displays Coming soon
    composeTestRule.onNodeWithTag("channel_back_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nav_calls").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Coming soon").assertIsDisplayed()
  }

  @Test
  fun `release json parsing computes exact MB size and compares CI build numbers`() {
    val json =
      """
      {
        "tag_name": "v1.0-b5",
        "name": "Chats v1.0 (Build 5)",
        "html_url": "https://github.com/Salomon-Chetchy/Chats/releases/tag/v1.0-b5",
        "body": "Automated APK build for Chats (`versionCode = 7`).\n- **Signing SHA-256:** `d64b2673c8af271f6aa1050c8d6a865e179dfe2c8c0fcef9f505039bb05b09fb`",
        "assets": [
          {
            "name": "Chats-debug.apk",
            "size": 15518925,
            "browser_download_url": "https://github.com/Salomon-Chetchy/Chats/releases/download/v1.0-b5/Chats-debug.apk"
          }
        ]
      }
      """.trimIndent()

    val parsed = parseReleaseJson(json)
    assertTrue(parsed is UpdateFetchResult.Found)
    val release = (parsed as UpdateFetchResult.Found).release
    assertEquals("14.8 MB", release.formattedSizeMb)
    assertEquals("14.8 MB", formatSizeInMb(15_518_925L))
    assertEquals(5, release.buildNumber)
    assertEquals(7, release.versionCode)
    assertEquals(
      "d64b2673c8af271f6aa1050c8d6a865e179dfe2c8c0fcef9f505039bb05b09fb",
      release.signingSha256,
    )

    assertTrue(
      isNewerRelease(
        installedVersionName = "1.0",
        installedVersionCode = 1,
        installedCiBuild = 0,
        release = release,
      )
    )
    assertTrue(
      isNewerRelease(
        installedVersionName = "1.0",
        installedVersionCode = 7,
        installedCiBuild = 4,
        release = release,
      )
    )
    assertFalse(
      isNewerRelease(
        installedVersionName = "1.0",
        installedVersionCode = 7,
        installedCiBuild = 5,
        release = release,
      )
    )
  }

  @Test
  fun `settings tab checks for update and displays available version with MB download size`() {
    val fakeClient =
      object : AppUpdateClient {
        override suspend fun fetchLatestRelease(repoSlug: String): UpdateFetchResult {
          return UpdateFetchResult.Found(
            UpdateReleaseInfo(
              versionName = "1.1",
              versionCode = 2,
              buildNumber = 5,
              tagName = "v1.1-b5",
              releaseName = "Chats v1.1 (Build 5)",
              apkFileName = "Chats-debug.apk",
              apkSizeBytes = 15_518_925L,
              downloadUrl = "https://example.com/Chats-debug.apk",
              releasePageUrl = "https://example.com/release",
              releaseNotes = "Added in-app update checker.",
            )
          )
        }
      }

    val vm =
      ChatsViewModel(
        updateClient = fakeClient,
        installedVersionName = "1.0",
        installedVersionCode = 1,
        installedCiBuild = 4,
      )

    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = vm)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_settings").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("app_updates_card").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("check_updates_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("update_available_banner").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Update available: v1.1 (Build 5)").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Download needed: 14.8 MB").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("Download update (14.8 MB)").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `settings tab shows up to date when latest release matches installed build`() {
    val fakeClient =
      object : AppUpdateClient {
        override suspend fun fetchLatestRelease(repoSlug: String): UpdateFetchResult {
          return UpdateFetchResult.Found(
            UpdateReleaseInfo(
              versionName = "1.0",
              versionCode = 1,
              buildNumber = 4,
              tagName = "v1.0-b4",
              releaseName = "Chats v1.0 (Build 4)",
              apkFileName = "Chats-debug.apk",
              apkSizeBytes = 14_000_000L,
              downloadUrl = "https://example.com/Chats-debug.apk",
              releasePageUrl = "https://example.com/release",
              releaseNotes = "",
            )
          )
        }
      }

    val vm =
      ChatsViewModel(
        updateClient = fakeClient,
        installedVersionName = "1.0",
        installedVersionCode = 1,
        installedCiBuild = 4,
      )

    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = vm)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_settings").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("check_updates_button").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("update_up_to_date_banner").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText("You're up to date").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun `puter driver json parser handles success choices and auth failure`() {
    val successJson =
      """
      {
        "success": true,
        "result": {
          "index": 0,
          "message": {
            "role": "assistant",
            "content": "Let's crush your 20 minute morning run!"
          }
        }
      }
      """.trimIndent()
    val parsedSuccess = parsePuterDriverResponse(successJson)
    assertTrue(parsedSuccess is PuterAiResult.Success)
    assertEquals(
      "Let's crush your 20 minute morning run!",
      (parsedSuccess as PuterAiResult.Success).replyText,
    )

    val authJson =
      """
      {
        "success": false,
        "error": {
          "message": "Authentication token invalid"
        }
      }
      """.trimIndent()
    val parsedAuth = parsePuterDriverResponse(authJson)
    assertTrue(parsedAuth is PuterAiResult.AuthRequired)
  }

  @Test
  fun `parsePuterImageResponse handles url b64_json and auth errors`() {
    val urlJson =
      """
      {
        "success": true,
        "result": {
          "url": "https://cdn.puter.site/generated/neon_city.png"
        }
      }
      """.trimIndent()
    val urlResult = parsePuterImageResponse(urlJson)
    assertTrue(urlResult is PuterImageResult.Success)
    assertEquals(
      "https://cdn.puter.site/generated/neon_city.png",
      (urlResult as PuterImageResult.Success).imageUrlOrDataUri,
    )

    val b64Json =
      """
      {
        "success": true,
        "result": {
          "b64_json": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJ"
        }
      }
      """.trimIndent()
    val b64Result = parsePuterImageResponse(b64Json)
    assertTrue(b64Result is PuterImageResult.Success)
    assertEquals(
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJ",
      (b64Result as PuterImageResult.Success).imageUrlOrDataUri,
    )

    val authJson =
      """
      {
        "success": false,
        "error": {
          "message": "Authentication required to run txt2img"
        }
      }
      """.trimIndent()
    val authResult = parsePuterImageResponse(authJson)
    assertTrue(authResult is PuterImageResult.AuthRequired)
  }

  @Test
  fun `ai persona chat sends prompt through PuterAiEngine and renders reply`() {
    var capturedModel: String? = null
    var capturedTurns: List<PuterChatTurn> = emptyList()
    val fakePuterEngine =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult {
          capturedModel = modelId
          capturedTurns = messages
          return PuterAiResult.Success("Awesome! Let's log your 15-minute stretch right now.")
        }
      }

    val vm = ChatsViewModel(puterAiEngine = fakePuterEngine)
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = vm)
      }
    }
    composeTestRule.waitForIdle()

    // Open Aria Coach conversation
    composeTestRule.onNodeWithText("Aria Coach").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ai_goal_banner").assertDoesNotExist()
    composeTestRule.onNodeWithTag("chat_message_input").performTextInput("Ready for my morning workout!")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("chat_send_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Awesome! Let's log your 15-minute stretch right now.").assertIsDisplayed()
    assertEquals("gpt-4o-mini", capturedModel)
    assertTrue(capturedTurns.any { it.role == "system" && it.content.contains("Aria Coach") })
  }

  @Test
  fun `settings tab displays Puter AI service card and allows model switching`() {
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = ChatsViewModel())
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_settings").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("puter_ai_settings_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("puter_active_model_value").assertIsDisplayed()
    composeTestRule.onNodeWithTag("edit_puter_model_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("puter_model_option_gpt-5-nano").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("GPT-5 Nano").assertIsDisplayed()
  }

  @Test
  fun `chat screen supports quoted reply preview and renders reply inside sent bubble`() {
    val fakePuterEngine =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult = PuterAiResult.Success("Got your reply!")
      }

    val vm = ChatsViewModel(puterAiEngine = fakePuterEngine)
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = vm)
      }
    }
    composeTestRule.waitForIdle()

    // Open Aria Coach conversation
    composeTestRule.onNodeWithText("Aria Coach").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    // Tap initial incoming message bubble and choose Reply
    composeTestRule
      .onNodeWithText("Hey! I'm Aria, your fitness coach. Ready to crush your workout goal today?")
      .performClick()
    composeTestRule.mainClock.advanceTimeBy(200L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Reply").performClick()
    composeTestRule.mainClock.advanceTimeBy(200L)
    composeTestRule.waitForIdle()

    // Send reply message
    composeTestRule.onNodeWithTag("chat_message_input").performTextInput("Yes, let's start!")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("chat_send_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Yes, let's start!").assertIsDisplayed()
    assertTrue(
      composeTestRule
        .onAllNodesWithText("Hey! I'm Aria, your fitness coach. Ready to crush your workout goal today?")
        .fetchSemanticsNodes()
        .size >= 2
    )
  }

  @Test
  fun `chat screen mic button sends voice note bubble with waveform and duration`() {
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = ChatsViewModel())
      }
    }
    composeTestRule.waitForIdle()

    // Open Aria Coach conversation
    composeTestRule.onNodeWithText("Aria Coach").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    // Tap floating mic button to send a voice note
    composeTestRule.onNodeWithTag("chat_mic_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(300L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("0:06").assertIsDisplayed()
  }

  @Test
  fun `parseChatMarkdown styles bold italic strikethrough code lists quotes and keeps snake_case`() {
    val input =
      """
      # Focus Plan
      **Bold** *Italic* ***Both*** ~~Old~~ `cmd()` [Docs](https://example.com) snake_case
      - First item
      > Stay steady
      """.trimIndent()

    val annotated = parseChatMarkdown(input)
    val plain = annotated.text

    assertTrue(plain.contains("Focus Plan"))
    assertTrue(plain.contains("Bold Italic Both Old"))
    assertTrue(plain.contains("cmd()"))
    assertTrue(plain.contains("Docs"))
    assertTrue(plain.contains("snake_case"))
    assertTrue(plain.contains("• First item"))
    assertTrue(plain.contains("│ Stay steady"))
    assertFalse(plain.contains("**"))
    assertFalse(plain.contains("~~"))
    assertFalse(plain.contains("https://example.com"))

    assertTrue(annotated.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    assertTrue(annotated.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
    assertTrue(annotated.spanStyles.any { it.item.textDecoration == TextDecoration.LineThrough })
    assertTrue(annotated.spanStyles.any { it.item.fontFamily == FontFamily.Monospace })
    assertTrue(annotated.spanStyles.any { it.item.textDecoration == TextDecoration.Underline })
  }

  @Test
  fun `ai reply with bold and bullet markdown renders formatted text without raw markers`() {
    val fakePuterEngine =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult =
          PuterAiResult.Success("**Great job!** Try *3 sets* today:\n- Warm up")
      }

    val vm = ChatsViewModel(puterAiEngine = fakePuterEngine)
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = vm)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Aria Coach").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("chat_message_input").performTextInput("What should I do?")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("chat_send_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Great job! Try 3 sets today:\n• Warm up").assertIsDisplayed()
  }

  @Test
  fun `chat screen attach tools sheet generates AI image from prompt dialog`() {
    var capturedPrompt: String? = null
    val tinyPngDataUri =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    val fakePuterEngine =
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
          return PuterImageResult.Success(tinyPngDataUri)
        }
      }

    val vm = ChatsViewModel(puterAiEngine = fakePuterEngine)
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = vm)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Aria Coach").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    // Open AI & Media tools bottom sheet
    composeTestRule.onNodeWithTag("chat_attach_plus_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("chat_generate_image_option").assertIsDisplayed().performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("generate_image_prompt_input").performTextInput("Neon runner at dawn")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_generate_image_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(500L)
    composeTestRule.waitForIdle()

    assertEquals("Neon runner at dawn", capturedPrompt)
    composeTestRule.onNodeWithTag("ai_generated_image_preview").assertIsDisplayed()
    composeTestRule.onNodeWithText("AI Generated").assertIsDisplayed()
  }

  @Test
  fun `pending photo stages with AI Identify chip and passes imageDataUrl on send`() {
    var capturedImageDataUrl: String? = null
    var capturedPrompt: String? = null
    val fakePuterEngine =
      object : PuterAiEngine {
        override suspend fun generateReply(
          messages: List<PuterChatTurn>,
          modelId: String,
          authToken: String?,
        ): PuterAiResult {
          val lastUser = messages.lastOrNull { it.role == "user" }
          capturedImageDataUrl = lastUser?.imageDataUrl
          capturedPrompt = lastUser?.content
          return PuterAiResult.Success("That looks like a **running shoe**!")
        }
      }

    val vm = ChatsViewModel(puterAiEngine = fakePuterEngine)
    composeTestRule.setContent {
      WhatsAppDarkTheme {
        ChatsScreen(viewModel = vm)
      }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Aria Coach").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    // Simulate user picking a photo while inside the active chat
    vm.onPhotoPicked(Uri.parse("content://media/external/images/media/42"))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("pending_photo_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_photo_identify_chip").assertIsDisplayed()

    composeTestRule.onNodeWithTag("chat_message_input").performTextInput("What gear is this?")
    composeTestRule.waitForIdle()

    // Trigger send directly with test encoder to verify end-to-end multimodal payload
    vm.sendMessage(imageEncoder = { "data:image/jpeg;base64,/9j/test" })
    composeTestRule.mainClock.advanceTimeBy(500L)
    composeTestRule.waitForIdle()

    assertEquals("data:image/jpeg;base64,/9j/test", capturedImageDataUrl)
    assertEquals("What gear is this?", capturedPrompt)
    composeTestRule.onNodeWithTag("pending_photo_bar").assertDoesNotExist()
    composeTestRule.onNodeWithText("That looks like a running shoe!").assertIsDisplayed()
  }
}
