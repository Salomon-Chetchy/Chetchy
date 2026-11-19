package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ChatsScreen
import com.example.ui.ChatsViewModel
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
  fun `chats screen displays exact hero content and navigation tabs`() {
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
    composeTestRule.onNodeWithTag("archived_row").assertIsDisplayed()

    composeTestRule.onNodeWithText("Chetchy").assertIsDisplayed()
    composeTestRule.onNodeWithText("Archived").assertIsDisplayed()
    assertEquals(1, composeTestRule.onAllNodesWithText("Chats").fetchSemanticsNodes().size)
    assertEquals(2, composeTestRule.onAllNodesWithText("Start chatting").fetchSemanticsNodes().size)

    composeTestRule
      .onNodeWithText("Message privately with your contacts, no matter what device they use.")
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("start_chatting_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("invite_friend_button").assertIsDisplayed()

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

    // Open New chat sheet
    composeTestRule.onNodeWithTag("start_chatting_button").performClick()
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
        "body": "Automated APK build for Chats (`versionCode = 1`).",
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

    assertTrue(
      isNewerRelease(
        installedVersionName = "1.0",
        installedVersionCode = 1,
        installedCiBuild = 4,
        release = release,
      )
    )
    assertFalse(
      isNewerRelease(
        installedVersionName = "1.0",
        installedVersionCode = 1,
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

    composeTestRule.onNodeWithTag("app_updates_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("check_updates_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("update_available_banner").assertIsDisplayed()
    composeTestRule.onNodeWithText("Update available: v1.1 (Build 5)").assertIsDisplayed()
    composeTestRule.onNodeWithText("Download needed: 14.8 MB").assertIsDisplayed()
    composeTestRule.onNodeWithText("Download update (14.8 MB)").assertIsDisplayed()
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

    composeTestRule.onNodeWithTag("check_updates_button").performClick()
    composeTestRule.mainClock.advanceTimeBy(400L)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("update_up_to_date_banner").assertIsDisplayed()
    composeTestRule.onNodeWithText("You're up to date").assertIsDisplayed()
  }
}
