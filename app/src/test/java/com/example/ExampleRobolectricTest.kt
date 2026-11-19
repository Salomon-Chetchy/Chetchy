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
import org.junit.Assert.assertEquals
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
    composeTestRule.onNodeWithTag("new_chat_plus_button").assertIsDisplayed()

    assertEquals(2, composeTestRule.onAllNodesWithText("Chats").fetchSemanticsNodes().size)
    assertEquals(2, composeTestRule.onAllNodesWithText("Start chatting").fetchSemanticsNodes().size)

    composeTestRule
      .onNodeWithText("Message privately with your contacts, no matter what device they use.")
      .assertIsDisplayed()
    composeTestRule.onNodeWithTag("start_chatting_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("invite_friend_button").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_updates").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_calls").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_communities").assertDoesNotExist()
    composeTestRule.onNodeWithTag("nav_chats").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nav_settings").assertIsDisplayed()
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
}
