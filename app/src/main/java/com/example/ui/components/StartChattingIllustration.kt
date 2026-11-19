package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.IllustrationBubblePurpleDark
import com.example.ui.theme.IllustrationBubbleSlate
import com.example.ui.theme.IllustrationGlobePurple
import com.example.ui.theme.IllustrationHeartPurple
import com.example.ui.theme.IllustrationInnerLine
import com.example.ui.theme.IllustrationLockWarmDark
import com.example.ui.theme.IllustrationOutline
import com.example.ui.theme.IllustrationPlaneDark
import kotlin.math.min

/**
 * Custom vector illustration matching the WhatsApp "Start chatting" hero artwork
 * (sweeping flight arc, paper plane, top-right speech bubble with heart & double-check,
 * central #7F46B8 globe with network nodes, bottom-left purple voice-note bubble, and padlock),
 * tailored for the WhatsApp Dark Theme.
 */
@Composable
fun StartChattingIllustration(modifier: Modifier = Modifier) {
  val description = stringResource(R.string.cd_hero_illustration)

  Canvas(
    modifier =
      modifier
        .size(width = 228.dp, height = 196.dp)
        .semantics { contentDescription = description }
  ) {
    val virtualWidth = 228f
    val virtualHeight = 196f
    val scale = min(size.width / virtualWidth, size.height / virtualHeight)
    val dx = (size.width - virtualWidth * scale) / 2f
    val dy = (size.height - virtualHeight * scale) / 2f

    withTransform({
      translate(left = dx, top = dy)
      scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
    }) {
      val outlineStroke =
        Stroke(width = 2.3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
      val innerStroke =
        Stroke(width = 2.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)

      // 1. Sweeping trajectory arc behind globe and left bubble
      val arcPath =
        Path().apply {
          moveTo(40f, 134f)
          cubicTo(16f, 94f, 34f, 52f, 92f, 32f)
        }
      drawPath(path = arcPath, color = IllustrationOutline, style = outlineStroke)

      // 2. Paper airplane at top-left-center
      drawPaperPlane(
        fillColor = IllustrationPlaneDark,
        strokeColor = IllustrationOutline,
        stroke = outlineStroke,
      )

      // 3. Top-right speech bubble with double-checkmarks
      drawTopRightSpeechBubble(
        fillColor = IllustrationBubbleSlate,
        strokeColor = IllustrationOutline,
        stroke = outlineStroke,
      )

      // 4. Purple heart overlapping top-left of the speech bubble
      drawHeartAccent(
        fillColor = IllustrationHeartPurple,
        strokeColor = IllustrationOutline,
        stroke = outlineStroke,
      )

      // 5. Central #7F46B8 globe with latitude/longitude curves and 3 nodes
      drawCentralGlobe(
        globeColor = IllustrationGlobePurple,
        innerLineColor = IllustrationInnerLine,
        outlineColor = IllustrationOutline,
        innerStroke = innerStroke,
        outlineStroke = outlineStroke,
      )

      // 6. Bottom-left purple-tinted bubble with Lucide-style microphone
      drawMintVoiceBubble(
        fillColor = IllustrationBubblePurpleDark,
        strokeColor = IllustrationOutline,
        stroke = outlineStroke,
      )

      // 7. Bottom-right warm dark padlock with keyhole
      drawPadlock(
        fillColor = IllustrationLockWarmDark,
        strokeColor = IllustrationOutline,
        stroke = outlineStroke,
      )
    }
  }
}

private fun DrawScope.drawPaperPlane(
  fillColor: Color,
  strokeColor: Color,
  stroke: Stroke,
) {
  val planeBody =
    Path().apply {
      moveTo(74f, 22f)
      lineTo(127f, 11f)
      lineTo(106f, 47f)
      lineTo(92f, 33f)
      close()
    }
  drawPath(path = planeBody, color = fillColor)
  drawPath(path = planeBody, color = strokeColor, style = stroke)

  val foldLine =
    Path().apply {
      moveTo(127f, 11f)
      lineTo(92f, 33f)
    }
  drawPath(path = foldLine, color = strokeColor, style = stroke)
}

private fun DrawScope.drawTopRightSpeechBubble(
  fillColor: Color,
  strokeColor: Color,
  stroke: Stroke,
) {
  val bubble =
    Path().apply {
      // Rounded speech bubble with top-right tail/wing silhouette matching screenshot
      moveTo(132f, 36f)
      lineTo(192f, 36f)
      cubicTo(204f, 36f, 206f, 36f, 198f, 46f)
      lineTo(198f, 64f)
      cubicTo(198f, 82f, 184f, 94f, 166f, 94f)
      lineTo(146f, 94f)
      cubicTo(128f, 94f, 116f, 82f, 116f, 64f)
      lineTo(116f, 54f)
      cubicTo(116f, 42f, 122f, 36f, 132f, 36f)
      close()
    }
  drawPath(path = bubble, color = fillColor)
  drawPath(path = bubble, color = strokeColor, style = stroke)

  // Double checkmark (✓✓) inside lower-right of bubble
  val check1 =
    Path().apply {
      moveTo(164f, 75f)
      lineTo(168f, 79f)
      lineTo(176f, 69f)
    }
  val check2 =
    Path().apply {
      moveTo(171f, 76f)
      lineTo(174f, 79f)
      lineTo(182f, 69f)
    }
  val checkStroke = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
  drawPath(path = check1, color = strokeColor, style = checkStroke)
  drawPath(path = check2, color = strokeColor, style = checkStroke)
}

private fun DrawScope.drawHeartAccent(
  fillColor: Color,
  strokeColor: Color,
  stroke: Stroke,
) {
  rotate(degrees = -12f, pivot = Offset(146f, 33f)) {
    val heart =
      Path().apply {
        moveTo(146f, 48f)
        cubicTo(146f, 48f, 128f, 37f, 128f, 26f)
        cubicTo(128f, 20f, 133f, 16f, 139f, 16f)
        cubicTo(143f, 16f, 145f, 19f, 146f, 21f)
        cubicTo(147f, 19f, 149f, 16f, 153f, 16f)
        cubicTo(159f, 16f, 164f, 20f, 164f, 26f)
        cubicTo(164f, 37f, 146f, 48f, 146f, 48f)
        close()
      }
    drawPath(path = heart, color = fillColor)
    drawPath(path = heart, color = strokeColor, style = stroke)
  }
}

private fun DrawScope.drawCentralGlobe(
  globeColor: Color,
  innerLineColor: Color,
  outlineColor: Color,
  innerStroke: Stroke,
  outlineStroke: Stroke,
) {
  val center = Offset(108f, 102f)
  val radius = 43f

  drawCircle(color = globeColor, radius = radius, center = center)

  val clipCircle =
    Path().apply {
      addOval(
        Rect(
          left = center.x - radius,
          top = center.y - radius,
          right = center.x + radius,
          bottom = center.y + radius,
        )
      )
    }

  clipPath(clipCircle) {
    rotate(degrees = -18f, pivot = center) {
      // Horizontal curved parallels
      val topParallel =
        Path().apply {
          moveTo(center.x - 45f, center.y - 16f)
          quadraticTo(center.x, center.y - 6f, center.x + 45f, center.y - 16f)
        }
      val bottomParallel =
        Path().apply {
          moveTo(center.x - 45f, center.y + 18f)
          quadraticTo(center.x, center.y + 28f, center.x + 45f, center.y + 18f)
        }
      // Vertical curved meridians
      val leftMeridian =
        Path().apply {
          moveTo(center.x - 8f, center.y - 46f)
          quadraticTo(center.x - 28f, center.y, center.x - 8f, center.y + 46f)
        }
      val rightMeridian =
        Path().apply {
          moveTo(center.x + 8f, center.y - 46f)
          quadraticTo(center.x + 28f, center.y, center.x + 8f, center.y + 46f)
        }

      drawPath(topParallel, color = innerLineColor, style = innerStroke)
      drawPath(bottomParallel, color = innerLineColor, style = innerStroke)
      drawPath(leftMeridian, color = innerLineColor, style = innerStroke)
      drawPath(rightMeridian, color = innerLineColor, style = innerStroke)

      // 3 intersection nodes
      val nodes =
        listOf(
          Offset(center.x - 16f, center.y - 11.5f),
          Offset(center.x + 17.5f, center.y - 11.5f),
          Offset(center.x - 14.5f, center.y + 22f),
        )
      nodes.forEach { node ->
        drawCircle(color = globeColor, radius = 4.2f, center = node)
        drawCircle(color = innerLineColor, radius = 4.2f, center = node, style = innerStroke)
      }
    }
  }

  drawCircle(color = outlineColor, radius = radius, center = center, style = outlineStroke)
}

private fun DrawScope.drawMintVoiceBubble(
  fillColor: Color,
  strokeColor: Color,
  stroke: Stroke,
) {
  rotate(degrees = -14f, pivot = Offset(66f, 134f)) {
    val bubble =
      Path().apply {
        moveTo(42f, 108f)
        lineTo(84f, 108f)
        cubicTo(94f, 108f, 100f, 114f, 100f, 124f)
        lineTo(100f, 144f)
        cubicTo(100f, 154f, 92f, 162f, 80f, 162f)
        lineTo(54f, 162f)
        cubicTo(42f, 162f, 34f, 154f, 34f, 142f)
        lineTo(34f, 114f)
        cubicTo(34f, 110f, 37f, 108f, 42f, 108f)
        close()
      }
    drawPath(path = bubble, color = fillColor)
    drawPath(path = bubble, color = strokeColor, style = stroke)

    // Microphone icon inside bubble (Lucide style)
    val micStroke = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawRoundRect(
      color = strokeColor,
      topLeft = Offset(62.5f, 119f),
      size = Size(9f, 16f),
      cornerRadius = CornerRadius(4.5f, 4.5f),
      style = micStroke,
    )
    val cradle =
      Path().apply {
        moveTo(57.5f, 132f)
        cubicTo(57.5f, 140f, 76.5f, 140f, 76.5f, 132f)
        moveTo(67f, 139f)
        lineTo(67f, 147f)
        moveTo(61f, 147f)
        lineTo(73f, 147f)
      }
    drawPath(path = cradle, color = strokeColor, style = micStroke)
  }
}

private fun DrawScope.drawPadlock(
  fillColor: Color,
  strokeColor: Color,
  stroke: Stroke,
) {
  rotate(degrees = 10f, pivot = Offset(154f, 138f)) {
    // Shackle outer & inner arch
    val outerShackle =
      Path().apply {
        moveTo(137f, 126f)
        lineTo(137f, 112f)
        cubicTo(137f, 96f, 171f, 96f, 171f, 112f)
        lineTo(171f, 126f)
      }
    val innerShackle =
      Path().apply {
        moveTo(144f, 126f)
        lineTo(144f, 113f)
        cubicTo(144f, 104f, 164f, 104f, 164f, 113f)
        lineTo(164f, 126f)
      }
    // Fill shackle arch band
    val shackleBand =
      Path().apply {
        moveTo(137f, 126f)
        lineTo(137f, 112f)
        cubicTo(137f, 96f, 171f, 96f, 171f, 112f)
        lineTo(171f, 126f)
        lineTo(164f, 126f)
        lineTo(164f, 113f)
        cubicTo(164f, 104f, 144f, 104f, 144f, 113f)
        lineTo(144f, 126f)
        close()
      }
    drawPath(path = shackleBand, color = fillColor)
    drawPath(path = outerShackle, color = strokeColor, style = stroke)
    drawPath(path = innerShackle, color = strokeColor, style = stroke)

    // Lock body
    drawRoundRect(
      color = fillColor,
      topLeft = Offset(128f, 124f),
      size = Size(52f, 44f),
      cornerRadius = CornerRadius(12f, 12f),
    )
    drawRoundRect(
      color = strokeColor,
      topLeft = Offset(128f, 124f),
      size = Size(52f, 44f),
      cornerRadius = CornerRadius(12f, 12f),
      style = stroke,
    )

    // Keyhole silhouette
    val keyhole =
      Path().apply {
        addOval(Rect(left = 149.5f, top = 136f, right = 158.5f, bottom = 145f))
      }
    val keyholeStem =
      Path().apply {
        moveTo(151.5f, 144.5f)
        lineTo(149.5f, 155f)
        lineTo(158.5f, 155f)
        lineTo(156.5f, 144.5f)
      }
    drawPath(path = keyhole, color = strokeColor, style = stroke)
    drawPath(path = keyholeStem, color = strokeColor, style = stroke)
  }
}
