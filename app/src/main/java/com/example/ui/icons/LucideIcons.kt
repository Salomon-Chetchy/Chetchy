package com.example.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-authored Lucide Icons (24x24 viewport, 2dp stroke, Round cap & join)
 * matching the official Lucide icon geometry.
 */
object LucideIcons {

  val Camera: ImageVector by lazy {
    lucideIcon("Camera") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(14.5f, 4f)
        horizontalLineTo(9.5f)
        lineTo(7f, 7f)
        horizontalLineTo(4f)
        arcTo(2f, 2f, 0f, false, false, 2f, 9f)
        verticalLineTo(18f)
        arcTo(2f, 2f, 0f, false, false, 4f, 20f)
        horizontalLineTo(20f)
        arcTo(2f, 2f, 0f, false, false, 22f, 18f)
        verticalLineTo(9f)
        arcTo(2f, 2f, 0f, false, false, 20f, 7f)
        horizontalLineTo(17f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(15f, 13f)
        arcTo(3f, 3f, 0f, true, true, 9f, 13f)
        arcTo(3f, 3f, 0f, true, true, 15f, 13f)
        close()
      }
    }
  }

  val Plus: ImageVector by lazy {
    lucideIcon("Plus") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(5f, 12f)
        horizontalLineTo(19f)
        moveTo(12f, 5f)
        verticalLineTo(19f)
      }
    }
  }

  // Lucide: message-circle-dashed (used for Updates)
  val MessageCircleDashed: ImageVector by lazy {
    lucideIcon("MessageCircleDashed") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(13.5f, 3.1f)
        curveTo(13f, 3.1f, 12.5f, 3f, 12f, 3f)
        curveTo(11.5f, 3f, 11f, 3.1f, 10.5f, 3.1f)

        moveTo(19.3f, 6.8f)
        arcTo(10.45f, 10.45f, 0f, false, false, 17.2f, 4.7f)

        moveTo(20.9f, 13.5f)
        curveTo(21f, 13f, 21f, 12.5f, 21f, 12f)
        curveTo(21f, 11.5f, 20.9f, 11f, 20.9f, 10.5f)

        moveTo(17.2f, 19.3f)
        arcTo(10.45f, 10.45f, 0f, false, false, 19.3f, 17.2f)

        moveTo(10.5f, 20.9f)
        curveTo(11f, 21f, 11.5f, 21f, 12f, 21f)
        curveTo(12.5f, 21f, 13f, 20.9f, 13.5f, 20.9f)

        moveTo(3.5f, 17.5f)
        lineTo(2f, 22f)
        lineTo(6.5f, 20.5f)

        moveTo(3.1f, 10.5f)
        curveTo(3.1f, 11f, 3f, 11.5f, 3f, 12f)
        curveTo(3f, 12.5f, 3.1f, 13f, 3.1f, 13.5f)

        moveTo(6.8f, 4.7f)
        arcTo(10.45f, 10.45f, 0f, false, false, 4.7f, 6.8f)
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.75f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(15.5f, 12f)
        arcTo(3.5f, 3.5f, 0f, true, true, 8.5f, 12f)
        arcTo(3.5f, 3.5f, 0f, true, true, 15.5f, 12f)
        close()
      }
    }
  }

  // Lucide: phone
  val Phone: ImageVector by lazy {
    lucideIcon("Phone") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(22f, 16.92f)
        verticalLineTo(19.92f)
        arcTo(2f, 2f, 0f, false, true, 19.82f, 21.92f)
        arcTo(19.79f, 19.79f, 0f, false, true, 11.19f, 18.85f)
        arcTo(19.5f, 19.5f, 0f, false, true, 5.19f, 12.85f)
        arcTo(19.79f, 19.79f, 0f, false, true, 2.12f, 4.18f)
        arcTo(2f, 2f, 0f, false, true, 4.11f, 2f)
        horizontalLineTo(7.11f)
        arcTo(2f, 2f, 0f, false, true, 9.11f, 3.72f)
        curveTo(9.23f, 4.68f, 9.47f, 5.62f, 9.81f, 6.53f)
        arcTo(2f, 2f, 0f, false, true, 9.36f, 8.64f)
        lineTo(8.09f, 9.91f)
        arcTo(16f, 16f, 0f, false, false, 14.09f, 15.91f)
        lineTo(15.36f, 14.64f)
        arcTo(2f, 2f, 0f, false, true, 17.47f, 14.19f)
        curveTo(18.38f, 14.53f, 19.32f, 14.77f, 20.28f, 14.89f)
        arcTo(2f, 2f, 0f, false, true, 22f, 16.92f)
        close()
      }
    }
  }

  // Lucide: users
  val Users: ImageVector by lazy {
    lucideIcon("Users") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 21f)
        verticalLineTo(19f)
        arcTo(4f, 4f, 0f, false, false, 12f, 15f)
        horizontalLineTo(6f)
        arcTo(4f, 4f, 0f, false, false, 2f, 19f)
        verticalLineTo(21f)
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(13f, 7f)
        arcTo(4f, 4f, 0f, true, true, 5f, 7f)
        arcTo(4f, 4f, 0f, true, true, 13f, 7f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(22f, 21f)
        verticalLineTo(19f)
        arcTo(4f, 4f, 0f, false, false, 19f, 15.13f)
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 3.13f)
        arcTo(4f, 4f, 0f, false, true, 16f, 10.88f)
      }
    }
  }

  // Lucide: messages-square
  val ChatsOutlined: ImageVector by lazy {
    lucideIcon("ChatsOutlined") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(14f, 9f)
        arcTo(2f, 2f, 0f, false, true, 12f, 11f)
        horizontalLineTo(6f)
        lineTo(2f, 15f)
        verticalLineTo(4f)
        curveTo(2f, 2.9f, 2.9f, 2f, 4f, 2f)
        horizontalLineTo(12f)
        arcTo(2f, 2f, 0f, false, true, 14f, 4f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(18f, 9f)
        horizontalLineTo(20f)
        arcTo(2f, 2f, 0f, false, true, 22f, 11f)
        verticalLineTo(22f)
        lineTo(18f, 18f)
        horizontalLineTo(10f)
        arcTo(2f, 2f, 0f, false, true, 8f, 16f)
        verticalLineTo(15f)
      }
    }
  }

  val ChatsFilled: ImageVector by lazy {
    lucideIcon("ChatsFilled") {
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.4f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 10.5f)
        curveTo(16f, 14.37f, 12.64f, 17.5f, 8.5f, 17.5f)
        curveTo(7.45f, 17.5f, 6.45f, 17.28f, 5.55f, 16.9f)
        lineTo(2.5f, 18.2f)
        lineTo(3.4f, 15.2f)
        curveTo(2.51f, 13.92f, 2f, 12.28f, 2f, 10.5f)
        curveTo(2f, 6.63f, 5.36f, 3.5f, 9.5f, 3.5f)
        curveTo(13.64f, 3.5f, 16f, 6.63f, 16f, 10.5f)
        close()
      }
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.4f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(18.2f, 9.4f)
        curveTo(20.4f, 10.4f, 21.8f, 12.4f, 21.8f, 14.7f)
        curveTo(21.8f, 16.1f, 21.3f, 17.3f, 20.5f, 18.3f)
        lineTo(21.4f, 20.9f)
        lineTo(18.7f, 19.8f)
        curveTo(17.7f, 20.2f, 16.6f, 20.4f, 15.5f, 20.4f)
        curveTo(12.6f, 20.4f, 10.3f, 18.7f, 9.7f, 16.5f)
      }
    }
  }

  // Lucide: settings
  val Settings: ImageVector by lazy {
    lucideIcon("Settings") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12.22f, 2f)
        horizontalLineTo(11.78f)
        arcTo(2f, 2f, 0f, false, false, 9.78f, 4f)
        verticalLineTo(4.18f)
        arcTo(2f, 2f, 0f, false, true, 8.78f, 5.91f)
        lineTo(8.35f, 6.16f)
        arcTo(2f, 2f, 0f, false, true, 6.35f, 6.16f)
        lineTo(6.2f, 6.08f)
        arcTo(2f, 2f, 0f, false, false, 3.47f, 6.81f)
        lineTo(3.25f, 7.19f)
        arcTo(2f, 2f, 0f, false, false, 3.98f, 9.92f)
        lineTo(4.13f, 10.02f)
        arcTo(2f, 2f, 0f, false, true, 5.13f, 11.74f)
        verticalLineTo(12.25f)
        arcTo(2f, 2f, 0f, false, true, 4.13f, 13.99f)
        lineTo(3.98f, 14.08f)
        arcTo(2f, 2f, 0f, false, false, 3.25f, 16.81f)
        lineTo(3.47f, 17.19f)
        arcTo(2f, 2f, 0f, false, false, 6.2f, 17.92f)
        lineTo(6.35f, 17.84f)
        arcTo(2f, 2f, 0f, false, true, 8.35f, 17.84f)
        lineTo(8.78f, 18.09f)
        arcTo(2f, 2f, 0f, false, true, 9.78f, 19.82f)
        verticalLineTo(20f)
        arcTo(2f, 2f, 0f, false, false, 11.78f, 22f)
        horizontalLineTo(12.22f)
        arcTo(2f, 2f, 0f, false, false, 14.22f, 20f)
        verticalLineTo(19.82f)
        arcTo(2f, 2f, 0f, false, true, 15.22f, 18.09f)
        lineTo(15.65f, 17.84f)
        arcTo(2f, 2f, 0f, false, true, 17.65f, 17.84f)
        lineTo(17.8f, 17.92f)
        arcTo(2f, 2f, 0f, false, false, 20.53f, 17.19f)
        lineTo(20.75f, 16.8f)
        arcTo(2f, 2f, 0f, false, false, 20.02f, 14.07f)
        lineTo(19.87f, 13.99f)
        arcTo(2f, 2f, 0f, false, true, 18.87f, 12.25f)
        verticalLineTo(11.75f)
        arcTo(2f, 2f, 0f, false, true, 19.87f, 10.01f)
        lineTo(20.02f, 9.92f)
        arcTo(2f, 2f, 0f, false, false, 20.75f, 7.19f)
        lineTo(20.53f, 6.81f)
        arcTo(2f, 2f, 0f, false, false, 17.8f, 6.08f)
        lineTo(17.65f, 6.16f)
        arcTo(2f, 2f, 0f, false, true, 15.65f, 6.16f)
        lineTo(15.22f, 5.91f)
        arcTo(2f, 2f, 0f, false, true, 14.22f, 4.18f)
        verticalLineTo(4f)
        arcTo(2f, 2f, 0f, false, false, 12.22f, 2f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(15f, 12f)
        arcTo(3f, 3f, 0f, true, true, 9f, 12f)
        arcTo(3f, 3f, 0f, true, true, 15f, 12f)
        close()
      }
    }
  }

  // Lucide: search
  val Search: ImageVector by lazy {
    lucideIcon("Search") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(19f, 11f)
        arcTo(8f, 8f, 0f, true, true, 3f, 11f)
        arcTo(8f, 8f, 0f, true, true, 19f, 11f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(21f, 21f)
        lineTo(16.65f, 16.65f)
      }
    }
  }

  // Lucide: user-plus
  val UserPlus: ImageVector by lazy {
    lucideIcon("UserPlus") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 21f)
        verticalLineTo(19f)
        arcTo(4f, 4f, 0f, false, false, 12f, 15f)
        horizontalLineTo(6f)
        arcTo(4f, 4f, 0f, false, false, 2f, 19f)
        verticalLineTo(21f)
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(13f, 7f)
        arcTo(4f, 4f, 0f, true, true, 5f, 7f)
        arcTo(4f, 4f, 0f, true, true, 13f, 7f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(19f, 8f)
        verticalLineTo(14f)
        moveTo(22f, 11f)
        horizontalLineTo(16f)
      }
    }
  }

  // Lucide: user
  val User: ImageVector by lazy {
    lucideIcon("User") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(19f, 21f)
        verticalLineTo(19f)
        arcTo(4f, 4f, 0f, false, false, 15f, 15f)
        horizontalLineTo(9f)
        arcTo(4f, 4f, 0f, false, false, 5f, 19f)
        verticalLineTo(21f)
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 7f)
        arcTo(4f, 4f, 0f, true, true, 8f, 7f)
        arcTo(4f, 4f, 0f, true, true, 16f, 7f)
        close()
      }
    }
  }

  // Lucide: globe
  val Globe: ImageVector by lazy {
    lucideIcon("Globe") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(22f, 12f)
        arcTo(10f, 10f, 0f, true, true, 2f, 12f)
        arcTo(10f, 10f, 0f, true, true, 22f, 12f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 2f)
        arcTo(14.5f, 14.5f, 0f, false, false, 12f, 22f)
        arcTo(14.5f, 14.5f, 0f, false, false, 12f, 2f)
        moveTo(2f, 12f)
        horizontalLineTo(22f)
      }
    }
  }

  // Lucide: megaphone
  val Megaphone: ImageVector by lazy {
    lucideIcon("Megaphone") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3f, 11f)
        lineTo(21f, 5f)
        verticalLineTo(19f)
        lineTo(3f, 13f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(11.6f, 16.8f)
        arcTo(3f, 3f, 0f, true, true, 5.8f, 14.8f)
      }
    }
  }

  // Lucide: share (box with upward arrow matching iOS/WhatsApp invite row)
  val Share: ImageVector by lazy {
    lucideIcon("Share") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(4f, 12f)
        verticalLineTo(20f)
        arcTo(2f, 2f, 0f, false, false, 6f, 22f)
        horizontalLineTo(18f)
        arcTo(2f, 2f, 0f, false, false, 20f, 20f)
        verticalLineTo(12f)
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16f, 6f)
        lineTo(12f, 2f)
        lineTo(8f, 6f)
        moveTo(12f, 2f)
        verticalLineTo(15f)
      }
    }
  }

  // Lucide: chevron-left
  val ChevronLeft: ImageVector by lazy {
    lucideIcon("ChevronLeft") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(15f, 18f)
        lineTo(9f, 12f)
        lineTo(15f, 6f)
      }
    }
  }

  // Lucide: chevron-right
  val ChevronRight: ImageVector by lazy {
    lucideIcon("ChevronRight") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(9f, 18f)
        lineTo(15f, 12f)
        lineTo(9f, 6f)
      }
    }
  }

  // Lucide: qr-code
  val QrCode: ImageVector by lazy {
    lucideIcon("QrCode") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        // Top-left square
        moveTo(3f, 3f)
        horizontalLineTo(8f)
        verticalLineTo(8f)
        horizontalLineTo(3f)
        close()
        // Top-right square
        moveTo(16f, 3f)
        horizontalLineTo(21f)
        verticalLineTo(8f)
        horizontalLineTo(16f)
        close()
        // Bottom-left square
        moveTo(3f, 16f)
        horizontalLineTo(8f)
        verticalLineTo(21f)
        horizontalLineTo(3f)
        close()
        // Matrix accents
        moveTo(21f, 16f)
        horizontalLineTo(18f)
        verticalLineTo(19f)
        moveTo(21f, 21f)
        horizontalLineTo(21.01f)
        moveTo(12f, 7f)
        verticalLineTo(3f)
        moveTo(12f, 12f)
        horizontalLineTo(12.01f)
        moveTo(12f, 16f)
        verticalLineTo(21f)
        moveTo(16f, 12f)
        horizontalLineTo(21f)
        moveTo(3f, 12f)
        horizontalLineTo(8f)
      }
    }
  }

  // Lucide: video
  val Video: ImageVector by lazy {
    lucideIcon("Video") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(22f, 8f)
        lineTo(16f, 12f)
        lineTo(22f, 16f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(4f, 6f)
        horizontalLineTo(14f)
        arcTo(2f, 2f, 0f, false, true, 16f, 8f)
        verticalLineTo(16f)
        arcTo(2f, 2f, 0f, false, true, 14f, 18f)
        horizontalLineTo(4f)
        arcTo(2f, 2f, 0f, false, true, 2f, 16f)
        verticalLineTo(8f)
        arcTo(2f, 2f, 0f, false, true, 4f, 6f)
        close()
      }
    }
  }

  // Lucide: lock
  val Lock: ImageVector by lazy {
    lucideIcon("Lock") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(5f, 11f)
        horizontalLineTo(19f)
        arcTo(2f, 2f, 0f, false, true, 21f, 13f)
        verticalLineTo(20f)
        arcTo(2f, 2f, 0f, false, true, 19f, 22f)
        horizontalLineTo(5f)
        arcTo(2f, 2f, 0f, false, true, 3f, 20f)
        verticalLineTo(13f)
        arcTo(2f, 2f, 0f, false, true, 5f, 11f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(7f, 11f)
        verticalLineTo(7f)
        arcTo(5f, 5f, 0f, false, true, 17f, 7f)
        verticalLineTo(11f)
      }
    }
  }

  // Lucide: smile
  val Smile: ImageVector by lazy {
    lucideIcon("Smile") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(22f, 12f)
        arcTo(10f, 10f, 0f, true, true, 2f, 12f)
        arcTo(10f, 10f, 0f, true, true, 22f, 12f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(8f, 14f)
        reflectiveCurveTo(9.5f, 16f, 12f, 16f)
        reflectiveCurveTo(16f, 14f, 16f, 14f)
        moveTo(9f, 9f)
        horizontalLineTo(9.01f)
        moveTo(15f, 9f)
        horizontalLineTo(15.01f)
      }
    }
  }

  // Lucide: mic
  val Mic: ImageVector by lazy {
    lucideIcon("Mic") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 2f)
        arcTo(3f, 3f, 0f, false, false, 9f, 5f)
        verticalLineTo(12f)
        arcTo(3f, 3f, 0f, false, false, 15f, 12f)
        verticalLineTo(5f)
        arcTo(3f, 3f, 0f, false, false, 12f, 2f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(19f, 10f)
        verticalLineTo(12f)
        arcTo(7f, 7f, 0f, false, true, 5f, 12f)
        verticalLineTo(10f)
        moveTo(12f, 19f)
        verticalLineTo(22f)
      }
    }
  }

  // Lucide: send-horizontal
  val SendHorizontal: ImageVector by lazy {
    lucideIcon("SendHorizontal") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3.71f, 3.04f)
        arcTo(0.5f, 0.5f, 0f, false, false, 3.05f, 3.69f)
        lineTo(5.97f, 11.65f)
        arcTo(1f, 1f, 0f, false, true, 5.97f, 12.35f)
        lineTo(3.05f, 20.31f)
        arcTo(0.5f, 0.5f, 0f, false, false, 3.71f, 20.96f)
        lineTo(21.6f, 12.45f)
        arcTo(0.5f, 0.5f, 0f, false, false, 21.6f, 11.55f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(6f, 12f)
        horizontalLineTo(22f)
      }
    }
  }

  // Lucide: check-check
  val CheckCheck: ImageVector by lazy {
    lucideIcon("CheckCheck") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(18f, 6f)
        lineTo(7f, 17f)
        lineTo(2f, 12f)
        moveTo(22f, 10f)
        lineTo(14.5f, 17.5f)
        lineTo(13f, 16f)
      }
    }
  }

  // Lucide: image
  val Image: ImageVector by lazy {
    lucideIcon("Image") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(5f, 3f)
        horizontalLineTo(19f)
        arcTo(2f, 2f, 0f, false, true, 21f, 5f)
        verticalLineTo(19f)
        arcTo(2f, 2f, 0f, false, true, 19f, 21f)
        horizontalLineTo(5f)
        arcTo(2f, 2f, 0f, false, true, 3f, 19f)
        verticalLineTo(5f)
        arcTo(2f, 2f, 0f, false, true, 5f, 3f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(11f, 9f)
        arcTo(2f, 2f, 0f, true, true, 7f, 9f)
        arcTo(2f, 2f, 0f, true, true, 11f, 9f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(21f, 15f)
        lineTo(17.91f, 11.91f)
        arcTo(2f, 2f, 0f, false, false, 15.09f, 11.91f)
        lineTo(6f, 21f)
      }
    }
  }

  // Lucide: x
  val Close: ImageVector by lazy {
    lucideIcon("Close") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(18f, 6f)
        lineTo(6f, 18f)
        moveTo(6f, 6f)
        lineTo(18f, 18f)
      }
    }
  }

  // Lucide: pencil
  val Pencil: ImageVector by lazy {
    lucideIcon("Pencil") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(21.174f, 6.812f)
        arcTo(1f, 1f, 0f, false, false, 17.188f, 2.826f)
        lineTo(3.842f, 16.174f)
        arcTo(2f, 2f, 0f, false, false, 3.336f, 17.028f)
        lineTo(2.015f, 21.382f)
        arcTo(0.5f, 0.5f, 0f, false, false, 2.633f, 21.999f)
        lineTo(6.987f, 20.678f)
        arcTo(2f, 2f, 0f, false, false, 7.841f, 20.172f)
        close()
        moveTo(15f, 5f)
        lineTo(19f, 9f)
      }
    }
  }

  // Lucide: eye
  val Eye: ImageVector by lazy {
    lucideIcon("Eye") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(2.062f, 12.348f)
        arcTo(1f, 1f, 0f, false, true, 2.062f, 11.652f)
        curveTo(4.022f, 7.648f, 7.752f, 5f, 12f, 5f)
        curveTo(16.248f, 5f, 19.978f, 7.648f, 21.938f, 11.652f)
        arcTo(1f, 1f, 0f, false, true, 21.938f, 12.348f)
        curveTo(19.978f, 16.352f, 16.248f, 19f, 12f, 19f)
        curveTo(7.752f, 19f, 4.022f, 16.352f, 2.062f, 12.348f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(15f, 12f)
        arcTo(3f, 3f, 0f, true, true, 9f, 12f)
        arcTo(3f, 3f, 0f, true, true, 15f, 12f)
        close()
      }
    }
  }

  // Lucide: trash-2
  val Trash2: ImageVector by lazy {
    lucideIcon("Trash2") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3f, 6f)
        horizontalLineTo(21f)
        moveTo(19f, 6f)
        verticalLineTo(20f)
        curveTo(19f, 21f, 18f, 22f, 17f, 22f)
        horizontalLineTo(7f)
        curveTo(6f, 22f, 5f, 21f, 5f, 20f)
        verticalLineTo(6f)
        moveTo(8f, 6f)
        verticalLineTo(4f)
        curveTo(8f, 3f, 9f, 2f, 10f, 2f)
        horizontalLineTo(14f)
        curveTo(15f, 2f, 16f, 3f, 16f, 4f)
        verticalLineTo(6f)
        moveTo(10f, 11f)
        verticalLineTo(17f)
        moveTo(14f, 11f)
        verticalLineTo(17f)
      }
    }
  }

  // Lucide: radio (Channels icon)
  val Radio: ImageVector by lazy {
    lucideIcon("Radio") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(4.9f, 19.1f)
        curveTo(1f, 15.2f, 1f, 8.8f, 4.9f, 4.9f)
        moveTo(7.8f, 16.2f)
        curveTo(5.5f, 13.9f, 5.5f, 10.1f, 7.8f, 7.8f)
        moveTo(14f, 12f)
        arcTo(2f, 2f, 0f, true, true, 10f, 12f)
        arcTo(2f, 2f, 0f, true, true, 14f, 12f)
        close()
        moveTo(16.2f, 7.8f)
        curveTo(18.5f, 10.1f, 18.5f, 13.9f, 16.2f, 16.2f)
        moveTo(19.1f, 4.9f)
        curveTo(23f, 8.8f, 23f, 15.2f, 19.1f, 19.1f)
      }
    }
  }

  // Lucide: check
  val Check: ImageVector by lazy {
    lucideIcon("Check") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(20f, 6f)
        lineTo(9f, 17f)
        lineTo(4f, 12f)
      }
    }
  }

  // Lucide: download
  val Download: ImageVector by lazy {
    lucideIcon("Download") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(21f, 15f)
        verticalLineTo(19f)
        arcTo(2f, 2f, 0f, false, true, 19f, 21f)
        horizontalLineTo(5f)
        arcTo(2f, 2f, 0f, false, true, 3f, 19f)
        verticalLineTo(15f)
        moveTo(7f, 10f)
        lineTo(12f, 15f)
        lineTo(17f, 10f)
        moveTo(12f, 15f)
        verticalLineTo(3f)
      }
    }
  }

  // Lucide: refresh-cw
  val RefreshCw: ImageVector by lazy {
    lucideIcon("RefreshCw") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3f, 12f)
        arcTo(9f, 9f, 0f, false, true, 18.36f, 5.64f)
        lineTo(21f, 8f)
        moveTo(21f, 3f)
        verticalLineTo(8f)
        horizontalLineTo(16f)
        moveTo(21f, 12f)
        arcTo(9f, 9f, 0f, false, true, 5.64f, 18.36f)
        lineTo(3f, 16f)
        moveTo(8f, 16f)
        horizontalLineTo(3f)
        verticalLineTo(21f)
      }
    }
  }

  // Lucide: circle-check
  val CircleCheck: ImageVector by lazy {
    lucideIcon("CircleCheck") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(22f, 12f)
        arcTo(10f, 10f, 0f, true, true, 2f, 12f)
        arcTo(10f, 10f, 0f, true, true, 22f, 12f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(9f, 12f)
        lineTo(11f, 14f)
        lineTo(15f, 10f)
      }
    }
  }

  // Lucide: external-link
  val ExternalLink: ImageVector by lazy {
    lucideIcon("ExternalLink") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(15f, 3f)
        horizontalLineTo(21f)
        verticalLineTo(9f)
        moveTo(10f, 14f)
        lineTo(21f, 3f)
        moveTo(18f, 13f)
        verticalLineTo(19f)
        arcTo(2f, 2f, 0f, false, true, 16f, 21f)
        horizontalLineTo(5f)
        arcTo(2f, 2f, 0f, false, true, 3f, 19f)
        verticalLineTo(8f)
        arcTo(2f, 2f, 0f, false, true, 5f, 6f)
        horizontalLineTo(11f)
      }
    }
  }

  // Lucide: shield
  val Shield: ImageVector by lazy {
    lucideIcon("Shield") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(20f, 13f)
        curveTo(20f, 18f, 16.5f, 20.5f, 12.34f, 21.95f)
        arcTo(1f, 1f, 0f, false, true, 11.66f, 21.95f)
        curveTo(7.5f, 20.5f, 4f, 18f, 4f, 13f)
        verticalLineTo(6f)
        arcTo(1f, 1f, 0f, false, true, 5f, 5f)
        curveTo(7f, 5f, 9.5f, 3.8f, 11.24f, 2.28f)
        arcTo(1.17f, 1.17f, 0f, false, true, 12.76f, 2.28f)
        curveTo(14.51f, 3.81f, 17f, 5f, 19f, 5f)
        arcTo(1f, 1f, 0f, false, true, 20f, 6f)
        close()
      }
    }
  }

  // Lucide: more-vertical
  val MoreVertical: ImageVector by lazy {
    lucideIcon("MoreVertical") {
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.6f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(13f, 5f)
        arcTo(1f, 1f, 0f, true, true, 11f, 5f)
        arcTo(1f, 1f, 0f, true, true, 13f, 5f)
        close()
        moveTo(13f, 12f)
        arcTo(1f, 1f, 0f, true, true, 11f, 12f)
        arcTo(1f, 1f, 0f, true, true, 13f, 12f)
        close()
        moveTo(13f, 19f)
        arcTo(1f, 1f, 0f, true, true, 11f, 19f)
        arcTo(1f, 1f, 0f, true, true, 13f, 19f)
        close()
      }
    }
  }

  // Lucide: archive
  val Archive: ImageVector by lazy {
    lucideIcon("Archive") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(3f, 4f)
        horizontalLineTo(21f)
        arcTo(1f, 1f, 0f, false, true, 22f, 5f)
        verticalLineTo(7f)
        arcTo(1f, 1f, 0f, false, true, 21f, 8f)
        horizontalLineTo(3f)
        arcTo(1f, 1f, 0f, false, true, 2f, 7f)
        verticalLineTo(5f)
        arcTo(1f, 1f, 0f, false, true, 3f, 4f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(4f, 8f)
        verticalLineTo(19f)
        arcTo(2f, 2f, 0f, false, false, 6f, 21f)
        horizontalLineTo(18f)
        arcTo(2f, 2f, 0f, false, false, 20f, 19f)
        verticalLineTo(8f)
        moveTo(10f, 12f)
        horizontalLineTo(14f)
      }
    }
  }

  // Lucide: message-square-plus (floating New chat FAB icon)
  val MessageSquarePlus: ImageVector by lazy {
    lucideIcon("MessageSquarePlus") {
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(21f, 15f)
        arcTo(2f, 2f, 0f, false, true, 19f, 17f)
        horizontalLineTo(7f)
        lineTo(3f, 21f)
        verticalLineTo(5f)
        arcTo(2f, 2f, 0f, false, true, 5f, 3f)
        horizontalLineTo(19f)
        arcTo(2f, 2f, 0f, false, true, 21f, 5f)
        close()
      }
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 7f)
        verticalLineTo(13f)
        moveTo(9f, 10f)
        horizontalLineTo(15f)
      }
    }
  }

  private inline fun lucideIcon(
    name: String,
    block: ImageVector.Builder.() -> Unit,
  ): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
      )
      .apply(block)
      .build()
}
