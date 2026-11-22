package com.example

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.ui.components.ChatMarkdownBlock
import com.example.ui.components.CodeSyntaxColors
import com.example.ui.components.highlightCodeSyntax
import com.example.ui.components.parseChatMarkdown
import com.example.ui.components.splitChatMarkdownBlocks
import com.example.ui.components.stripChatMarkdown
import org.junit.Assert.assertEquals
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
      ```kotlin
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

  @Test
  fun `splitChatMarkdownBlocks separates prose and fenced code blocks with language tags`() {
    val message =
      """
      Here is **Python** and **Kotlin**:
      ```python
      def greet(name):
          return f"Hi {name}"
      ```
      Run it with:
      ```bash echo "ok"```
      Done!
      """.trimIndent()

    val blocks = splitChatMarkdownBlocks(message)
    assertEquals(5, blocks.size)

    val text1 = blocks[0] as ChatMarkdownBlock.Text
    assertEquals("Here is **Python** and **Kotlin**:", text1.markdown)

    val pyBlock = blocks[1] as ChatMarkdownBlock.Code
    assertEquals("python", pyBlock.language)
    assertTrue(pyBlock.code.contains("def greet(name):"))
    assertTrue(pyBlock.code.contains("    return f\"Hi {name}\""))

    val text2 = blocks[2] as ChatMarkdownBlock.Text
    assertEquals("Run it with:", text2.markdown)

    val bashBlock = blocks[3] as ChatMarkdownBlock.Code
    assertEquals("bash", bashBlock.language)
    assertEquals("echo \"ok\"", bashBlock.code)

    val text3 = blocks[4] as ChatMarkdownBlock.Text
    assertEquals("Done!", text3.markdown)
  }

  @Test
  fun `splitChatMarkdownBlocks handles unclosed code fence gracefully`() {
    val blocks = splitChatMarkdownBlocks("Snippet:\n```kotlin\nval ready = true")
    assertEquals(2, blocks.size)
    val code = blocks[1] as ChatMarkdownBlock.Code
    assertEquals("kotlin", code.language)
    assertEquals("val ready = true", code.code)
  }

  @Test
  fun `highlightCodeSyntax applies distinct colors to keywords strings comments numbers and functions`() {
    val kotlinSample =
      """
      // Greet user
      fun greet(count: Int) {
        val label = "Hello"
        if (count > 0 && true) {
          println(label)
        }
      }
      """.trimIndent()

    val highlighted = highlightCodeSyntax(kotlinSample, "kotlin")

    assertTrue(highlighted.spanStyles.any { it.item.color == CodeSyntaxColors.Comment })
    assertTrue(highlighted.spanStyles.any { it.item.color == CodeSyntaxColors.Keyword })
    assertTrue(highlighted.spanStyles.any { it.item.color == CodeSyntaxColors.StringLiteral })
    assertTrue(highlighted.spanStyles.any { it.item.color == CodeSyntaxColors.NumberOrConstant })
    assertTrue(highlighted.spanStyles.any { it.item.color == CodeSyntaxColors.Function })
    assertTrue(highlighted.spanStyles.any { it.item.color == CodeSyntaxColors.TypeOrClass })
  }

  @Test
  fun `highlightCodeSyntax colors json keys and python hash comments`() {
    val jsonHighlighted = highlightCodeSyntax("""{"status": "ok", "count": 42}""", "json")
    assertTrue(jsonHighlighted.spanStyles.any { it.item.color == CodeSyntaxColors.PropertyKey })
    assertTrue(jsonHighlighted.spanStyles.any { it.item.color == CodeSyntaxColors.StringLiteral })
    assertTrue(jsonHighlighted.spanStyles.any { it.item.color == CodeSyntaxColors.NumberOrConstant })

    val pyHighlighted = highlightCodeSyntax("# compute total\nres = sum(items)", "python")
    assertTrue(pyHighlighted.spanStyles.any { it.item.color == CodeSyntaxColors.Comment })
    assertTrue(pyHighlighted.spanStyles.any { it.item.color == CodeSyntaxColors.Function })
  }
}
