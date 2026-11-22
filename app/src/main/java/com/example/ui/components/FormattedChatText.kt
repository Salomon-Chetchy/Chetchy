package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Parses Markdown and chat formatting into a Compose [AnnotatedString] so AI personas
 * and users can write in **bold**, *italic*, ***bold italic***, ~~strikethrough~~,
 * `inline code`, fenced ```code blocks```, > quotes, headings, links, and lists.
 */
fun parseChatMarkdown(
  rawText: String,
  codeBackground: Color = Color(0x40000000),
  codeTextColor: Color = Color(0xFFD6B9FF),
  accentColor: Color = Color(0xFFB687EB),
  quoteColor: Color = Color(0xFFB3BFC7),
): AnnotatedString {
  if (rawText.isEmpty()) return AnnotatedString("")

  val normalized = rawText.replace("\r\n", "\n")
  val lines = normalized.split("\n")

  return buildAnnotatedString {
    var i = 0
    var needNewline = false

    fun appendLineBreakIfNeeded() {
      if (needNewline) {
        append("\n")
      }
      needNewline = true
    }

    while (i < lines.size) {
      val line = lines[i]
      val trimmedStart = line.trimStart()

      // Fenced code block ```...```
      if (trimmedStart.startsWith("```")) {
        appendLineBreakIfNeeded()
        val codeLines = mutableListOf<String>()
        i++
        while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
          codeLines += lines[i]
          i++
        }
        val codeBody = codeLines.joinToString("\n")
        if (codeBody.isNotEmpty()) {
          withStyle(
            SpanStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 13.5.sp,
              background = codeBackground,
              color = codeTextColor,
            )
          ) {
            append(codeBody)
          }
        }
        i++
        continue
      }

      appendLineBreakIfNeeded()

      // Headings (#, ##, ###)
      val headingMatch = Regex("""^(#{1,3})\s+(.+)$""").find(trimmedStart)
      if (headingMatch != null) {
        val level = headingMatch.groupValues[1].length
        val content = headingMatch.groupValues[2]
        val sizeSp = if (level == 1) 17.sp else if (level == 2) 16.sp else 15.5.sp
        withStyle(
          SpanStyle(
            fontWeight = FontWeight.Bold,
            fontSize = sizeSp,
            color = accentColor,
          )
        ) {
          appendInlineFormatted(
            text = content,
            codeBackground = codeBackground,
            codeTextColor = codeTextColor,
            accentColor = accentColor,
          )
        }
        i++
        continue
      }

      // Blockquote (> quote)
      val quoteMatch = Regex("""^>\s?(.*)$""").find(trimmedStart)
      if (quoteMatch != null) {
        withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
          append("│ ")
        }
        withStyle(SpanStyle(color = quoteColor, fontStyle = FontStyle.Italic)) {
          appendInlineFormatted(
            text = quoteMatch.groupValues[1],
            codeBackground = codeBackground,
            codeTextColor = codeTextColor,
            accentColor = accentColor,
          )
        }
        i++
        continue
      }

      // Unordered bullet lists (- item, * item, • item)
      val bulletMatch = Regex("""^(\s*)[-*•]\s+(.+)$""").find(line)
      if (bulletMatch != null) {
        val indent = if (bulletMatch.groupValues[1].length >= 2) "  " else ""
        append(indent)
        withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
          append("• ")
        }
        appendInlineFormatted(
          text = bulletMatch.groupValues[2],
          codeBackground = codeBackground,
          codeTextColor = codeTextColor,
          accentColor = accentColor,
        )
        i++
        continue
      }

      // Ordered list items (1. item)
      val orderedMatch = Regex("""^(\s*)(\d+)\.\s+(.+)$""").find(line)
      if (orderedMatch != null) {
        val indent = if (orderedMatch.groupValues[1].length >= 2) "  " else ""
        append(indent)
        withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
          append("${orderedMatch.groupValues[2]}. ")
        }
        appendInlineFormatted(
          text = orderedMatch.groupValues[3],
          codeBackground = codeBackground,
          codeTextColor = codeTextColor,
          accentColor = accentColor,
        )
        i++
        continue
      }

      // Regular paragraph line
      appendInlineFormatted(
        text = line,
        codeBackground = codeBackground,
        codeTextColor = codeTextColor,
        accentColor = accentColor,
      )
      i++
    }
  }
}

private fun AnnotatedString.Builder.appendInlineFormatted(
  text: String,
  codeBackground: Color,
  codeTextColor: Color,
  accentColor: Color,
) {
  var index = 0
  val len = text.length

  while (index < len) {
    // Escaped formatting character (e.g. \*, \_)
    if (text[index] == '\\' && index + 1 < len && text[index + 1] in charArrayOf('*', '_', '~', '`', '[', ']')) {
      append(text[index + 1])
      index += 2
      continue
    }

    // Inline code: `code`
    if (text[index] == '`') {
      val close = text.indexOf('`', startIndex = index + 1)
      if (close > index + 1) {
        val codeContent = text.substring(index + 1, close)
        withStyle(
          SpanStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.5.sp,
            background = codeBackground,
            color = codeTextColor,
          )
        ) {
          append(" $codeContent ")
        }
        index = close + 1
        continue
      }
    }

    // Markdown link: [label](url)
    if (text[index] == '[') {
      val closeBracket = text.indexOf(']', startIndex = index + 1)
      if (closeBracket > index + 1 && closeBracket + 1 < len && text[closeBracket + 1] == '(') {
        val closeParen = text.indexOf(')', startIndex = closeBracket + 2)
        if (closeParen > closeBracket + 2) {
          val label = text.substring(index + 1, closeBracket)
          withStyle(
            SpanStyle(
              color = accentColor,
              textDecoration = TextDecoration.Underline,
              fontWeight = FontWeight.Medium,
            )
          ) {
            append(label)
          }
          index = closeParen + 1
          continue
        }
      }
    }

    // Bold + Italic: ***text*** or ___text___
    if (index + 2 < len && (text.startsWith("***", index) || text.startsWith("___", index))) {
      val delim = text.substring(index, index + 3)
      val close = findClosingDelimiter(text, delim, startIndex = index + 3)
      if (close > index + 3) {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
          appendInlineFormatted(
            text = text.substring(index + 3, close),
            codeBackground = codeBackground,
            codeTextColor = codeTextColor,
            accentColor = accentColor,
          )
        }
        index = close + 3
        continue
      }
    }

    // Bold: **text** or __text__
    if (index + 1 < len && (text.startsWith("**", index) || text.startsWith("__", index))) {
      val delim = text.substring(index, index + 2)
      val close = findClosingDelimiter(text, delim, startIndex = index + 2)
      if (close > index + 2) {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
          appendInlineFormatted(
            text = text.substring(index + 2, close),
            codeBackground = codeBackground,
            codeTextColor = codeTextColor,
            accentColor = accentColor,
          )
        }
        index = close + 2
        continue
      }
    }

    // Strikethrough: ~~text~~
    if (index + 1 < len && text.startsWith("~~", index)) {
      val close = findClosingDelimiter(text, "~~", startIndex = index + 2)
      if (close > index + 2) {
        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
          appendInlineFormatted(
            text = text.substring(index + 2, close),
            codeBackground = codeBackground,
            codeTextColor = codeTextColor,
            accentColor = accentColor,
          )
        }
        index = close + 2
        continue
      }
    }

    // Italic: *text* or _text_
    if (text[index] == '*' || text[index] == '_') {
      val ch = text[index]
      val validStart =
        if (ch == '_') {
          index == 0 || !text[index - 1].isLetterOrDigit()
        } else {
          true
        }
      if (validStart && index + 1 < len && !text[index + 1].isWhitespace()) {
        val close = findSingleItalicClose(text, ch, startIndex = index + 1)
        if (close > index + 1) {
          withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            appendInlineFormatted(
              text = text.substring(index + 1, close),
              codeBackground = codeBackground,
              codeTextColor = codeTextColor,
              accentColor = accentColor,
            )
          }
          index = close + 1
          continue
        }
      }
    }

    append(text[index])
    index++
  }
}

private fun findClosingDelimiter(text: String, delim: String, startIndex: Int): Int {
  var pos = startIndex
  while (pos < text.length) {
    val found = text.indexOf(delim, startIndex = pos)
    if (found < 0) return -1
    if (found > startIndex && !text[found - 1].isWhitespace()) {
      return found
    }
    pos = found + 1
  }
  return -1
}

private fun findSingleItalicClose(text: String, delim: Char, startIndex: Int): Int {
  var pos = startIndex
  while (pos < text.length) {
    val found = text.indexOf(delim, startIndex = pos)
    if (found < 0) return -1
    val notDouble = !(found + 1 < text.length && text[found + 1] == delim)
    val notLeadingSpace = found > startIndex && !text[found - 1].isWhitespace()
    val validEnd =
      if (delim == '_') {
        found + 1 >= text.length || !text[found + 1].isLetterOrDigit()
      } else {
        true
      }
    if (notDouble && notLeadingSpace && validEnd) {
      return found
    }
    pos = found + 1
  }
  return -1
}

fun stripChatMarkdown(rawText: String): String = parseChatMarkdown(rawText).text
