package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.AccentPurpleMuted
import com.example.ui.theme.OnAccentPurple
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.delay

private val CodeCardBg = Color(0xFF0D1117)
private val CodeHeaderBg = Color(0xFF161B22)
private val CodeBorderColor = Color(0xFF28323E)
private val CodeLanguageLabelColor = Color(0xFFA3B3C2)
private val CodeLineNumberColor = Color(0xFF546170)

/**
 * Renders rich chat messages mixing styled Markdown prose (**bold**, *italic*, headings,
 * quotes, lists, inline `code`) with ChatGPT-style syntax-colored code block cards.
 */
@Composable
fun RichChatMessageContent(
  rawText: String,
  isOutgoing: Boolean,
  modifier: Modifier = Modifier,
) {
  val blocks = remember(rawText) { splitChatMarkdownBlocks(rawText) }
  val codeBg = Color(0x550D1117)
  val codeFg = AccentPurpleMuted
  val accent = if (isOutgoing) OnAccentPurple else AccentPurpleBright

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    blocks.forEach { block ->
      when (block) {
        is ChatMarkdownBlock.Text -> {
          val styled =
            remember(block.markdown, isOutgoing) {
              parseChatMarkdown(
                rawText = block.markdown,
                codeBackground = codeBg,
                codeTextColor = codeFg,
                accentColor = accent,
                quoteColor = WhatsAppTextSecondary,
              )
            }
          Text(
            text = styled,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.5.sp),
            color = WhatsAppTextPrimary,
          )
        }
        is ChatMarkdownBlock.Code -> {
          ChatGptCodeBlockCard(
            language = block.language,
            code = block.code,
          )
        }
      }
    }
  }
}

/**
 * ChatGPT-style fenced code card featuring a dark terminal header bar with language badge,
 * one-tap Copy code button with confirmation state, optional line numbers, horizontal scroll,
 * and syntax-colored code.
 */
@Composable
fun ChatGptCodeBlockCard(
  language: String,
  code: String,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var isCopied by remember(code) { mutableStateOf(false) }

  LaunchedEffect(isCopied) {
    if (isCopied) {
      delay(2_000L)
      isCopied = false
    }
  }

  val displayLanguage = language.trim().ifEmpty { "code" }
  val highlightedCode =
    remember(code, language) {
      highlightCodeSyntax(code = code, language = language)
    }
  val codeLines = remember(code) { code.split("\n") }
  val showLineNumbers = codeLines.size >= 2
  val lineNumbersText =
    remember(codeLines.size) {
      (1..codeLines.size).joinToString("\n")
    }

  Surface(
    color = CodeCardBg,
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.dp, CodeBorderColor),
    modifier = modifier.fillMaxWidth().testTag("code_block_card"),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Header bar: [ </> language                    📋 Copy code ]
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(CodeHeaderBg)
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = LucideIcons.Code2,
            contentDescription = null,
            tint = AccentPurpleBright,
            modifier = Modifier.size(14.dp),
          )
          Text(
            text = displayLanguage,
            style =
              MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
              ),
            color = CodeLanguageLabelColor,
            modifier = Modifier.testTag("code_block_language"),
          )
        }

        Row(
          modifier =
            Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable {
                copyTextToClipboard(context = context, label = "code", text = code)
                isCopied = true
              }
              .padding(horizontal = 8.dp, vertical = 6.dp)
              .testTag("copy_code_button"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = if (isCopied) LucideIcons.Check else LucideIcons.Copy,
            contentDescription = if (isCopied) "Code copied" else "Copy code",
            tint = if (isCopied) AccentPurpleBright else CodeLanguageLabelColor,
            modifier = Modifier.size(13.5.dp),
          )
          Text(
            text = if (isCopied) "Copied!" else "Copy code",
            style =
              MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
              ),
            color = if (isCopied) AccentPurpleBright else CodeLanguageLabelColor,
          )
        }
      }

      HorizontalDivider(thickness = 1.dp, color = CodeBorderColor)

      // Horizontally scrollable syntax-colored code body
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
      ) {
        if (showLineNumbers) {
          Text(
            text = lineNumbersText,
            style =
              MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 18.5.sp,
              ),
            color = CodeLineNumberColor,
          )
          Spacer(modifier = Modifier.width(10.dp))
          Box(
            modifier =
              Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(CodeBorderColor.copy(alpha = 0.6f)),
          )
          Spacer(modifier = Modifier.width(10.dp))
        }

        SelectionContainer {
          Text(
            text = highlightedCode,
            style =
              MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 18.5.sp,
              ),
            color = CodeSyntaxColors.DefaultText,
          )
        }
      }
    }
  }
}

fun copyTextToClipboard(
  context: Context,
  label: String,
  text: String,
) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
  clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
