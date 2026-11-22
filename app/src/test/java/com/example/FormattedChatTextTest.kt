package com.example

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.components.parseChatMarkdown
import com.example.ui.components.stripChatMarkdown
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattedChatTextTest {
  @Test
  fun `parseChatMarkdown formats bold italic strikethrough code blocks lists quotes and links`() {
    val input =
      """
      # Workout Plan
      **Bold** *Italic* ***Both*** ~~Skipped~~ `run()` [Guide](https://example.com) snake_case_var
      - Warm up
      1. Sprint
      > Keep going
      ```
      val x = 1
      ```
      """.trimIndent()

    val annotated = parseChatMarkdown(input)
    val plain = annotated.text

    assertTrue(plain.contains("Workout Plan"))
    assertTrue(plain.contains("Bold Italic Both Skipped"))
    assertTrue(plain.contains("run()"))
    assertTrue(plain.contains("Guide"))
    assertTrue(plain.contains("snake_case_var"))
    assertTrue(plain.contains("• Warm up"))
    assertTrue(plain.contains("1. Sprint"))
    assertTrue(plain.contains("│ Keep going"))
    assertTrue(plain.contains("val x = 1"))
    assertFalse(plain.contains("**"))
    assertFalse(plain.contains("~~"))
    assertFalse(plain.contains("```"))
    assertFalse(plain.contains("https://example.com"))

    assertTrue(annotated.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    assertTrue(annotated.spanStyles.any { it.item.fontStyle == FontStyle.Italic })
    assertTrue(annotated.spanStyles.any { it.item.textDecoration == TextDecoration.LineThrough })
    assertTrue(annotated.spanStyles.any { it.item.fontFamily == FontFamily.Monospace })
    assertTrue(annotated.spanStyles.any { it.item.textDecoration == TextDecoration.Underline })
  }

  @Test
  fun `stripChatMarkdown preserves unmatched math asterisks cleanly`() {
    val plain = stripChatMarkdown("Compute 3 * 4 = **12** and __done__")
    assertTrue(plain == "Compute 3 * 4 = 12 and done")
  }
}
