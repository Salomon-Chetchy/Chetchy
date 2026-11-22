package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.PendingDocumentAttachment
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.AccentPurpleMuted
import com.example.ui.theme.AccentPurpleSoftDark
import com.example.ui.theme.WhatsAppOutlineSubtle
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppSurfaceCard
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

@Composable
fun ChatBubbleImage(
  imageUri: String,
  contentDescription: String,
  isAiGenerated: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val decodedDataBitmap =
    remember(imageUri) {
      if (imageUri.startsWith("data:image/", ignoreCase = true)) {
        runCatching {
          val commaIdx = imageUri.indexOf(',')
          if (commaIdx > 0) {
            val bytes = Base64.decode(imageUri.substring(commaIdx + 1), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
          } else {
            null
          }
        }.getOrNull()
      } else {
        null
      }
    }

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 150.dp, max = 250.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0xFF0D1117))
        .clickable(onClick = onClick)
        .testTag(if (isAiGenerated) "ai_generated_image_preview" else "chat_photo_preview"),
  ) {
    if (decodedDataBitmap != null) {
      Image(
        bitmap = decodedDataBitmap,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 250.dp),
      )
    } else {
      AsyncImage(
        model = imageUri,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 250.dp),
      )
    }

    if (isAiGenerated) {
      Surface(
        color = Color.Black.copy(alpha = 0.68f),
        shape = RoundedCornerShape(50),
        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = LucideIcons.Sparkles,
            contentDescription = null,
            tint = AccentPurpleBright,
            modifier = Modifier.size(12.dp),
          )
          Text(
            text = stringResource(R.string.ai_generated_badge),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold),
            color = WhatsAppTextPrimary,
          )
        }
      }
    }
  }
}

@Composable
fun ChatBubbleDocumentCard(
  fileName: String,
  extension: String,
  sizeLabel: String?,
  pageCount: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val metaParts =
    buildList {
      if (extension.isNotBlank()) add(extension)
      if (!sizeLabel.isNullOrBlank()) add(sizeLabel)
      if (pageCount > 0) add(if (pageCount == 1) "1 page" else "$pageCount pages")
    }
  Surface(
    color = Color.Black.copy(alpha = 0.24f),
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onClick)
        .testTag("chat_document_card"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(AccentPurpleSoftDark),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = LucideIcons.FileText,
          contentDescription = null,
          tint = AccentPurpleBright,
          modifier = Modifier.size(20.dp),
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = fileName,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = WhatsAppTextPrimary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        if (metaParts.isNotEmpty()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = metaParts.joinToString(" • "),
            style = MaterialTheme.typography.labelSmall,
            color = WhatsAppTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
fun PendingPhotoAttachmentBar(
  photoUri: Uri,
  onClear: () -> Unit,
) {
  Surface(
    color = WhatsAppSurfaceCard.copy(alpha = 0.96f),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(0.5.dp, WhatsAppOutlineSubtle),
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 3.dp)
        .testTag("pending_photo_bar"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AsyncImage(
        model = photoUri,
        contentDescription = stringResource(R.string.cd_pending_photo),
        contentScale = ContentScale.Crop,
        modifier =
          Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0D1117)),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.photo_attach_ready_title),
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = WhatsAppTextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = stringResource(R.string.photo_attach_ready_hint),
          style = MaterialTheme.typography.bodySmall,
          color = WhatsAppTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      IconButton(
        onClick = onClear,
        modifier = Modifier.size(34.dp).testTag("clear_pending_photo_button"),
      ) {
        Icon(
          imageVector = LucideIcons.Close,
          contentDescription = stringResource(R.string.cd_close),
          tint = WhatsAppTextSecondary,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
fun PendingDocumentAttachmentBar(
  document: PendingDocumentAttachment,
  onClear: () -> Unit,
) {
  val subtitle =
    when {
      document.isLoading -> stringResource(R.string.document_reading_hint)
      document.sizeLabel.isNotBlank() ->
        "${document.extension} • ${document.sizeLabel} • ${stringResource(R.string.photo_attach_ready_hint)}"
      else -> "${document.extension} • ${stringResource(R.string.photo_attach_ready_hint)}"
    }

  Surface(
    color = WhatsAppSurfaceCard.copy(alpha = 0.96f),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(0.5.dp, WhatsAppOutlineSubtle),
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 3.dp)
        .testTag("pending_document_bar"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AccentPurpleSoftDark),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = LucideIcons.FileText,
          contentDescription = null,
          tint = AccentPurpleBright,
          modifier = Modifier.size(22.dp),
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = document.name,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = WhatsAppTextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = AccentPurpleMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      IconButton(
        onClick = onClear,
        modifier = Modifier.size(34.dp).testTag("clear_pending_document_button"),
      ) {
        Icon(
          imageVector = LucideIcons.Close,
          contentDescription = stringResource(R.string.cd_close),
          tint = WhatsAppTextSecondary,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
fun DocumentContentViewerDialog(
  fileName: String,
  metaLine: String,
  extractedText: String,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WhatsAppSurface,
    titleContentColor = WhatsAppTextPrimary,
    textContentColor = WhatsAppTextSecondary,
    title = {
      Column {
        Text(
          text = fileName,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        if (metaLine.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = metaLine,
            style = MaterialTheme.typography.labelSmall,
            color = AccentPurpleMuted,
          )
        }
      }
    },
    text = {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 360.dp)
            .verticalScroll(rememberScrollState()),
      ) {
        SelectionContainer {
          Text(
            text = extractedText.ifBlank { stringResource(R.string.document_preview_vision_fallback) },
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
            color = WhatsAppTextPrimary,
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.qr_dialog_close), color = AccentPurpleBright)
      }
    },
  )
}

@Composable
fun FullScreenImageViewerDialog(
  imageUri: String,
  caption: String,
  isAiGenerated: Boolean,
  onDismiss: () -> Unit,
) {
  val decodedDataBitmap =
    remember(imageUri) {
      if (imageUri.startsWith("data:image/", ignoreCase = true)) {
        runCatching {
          val commaIdx = imageUri.indexOf(',')
          if (commaIdx > 0) {
            val bytes = Base64.decode(imageUri.substring(commaIdx + 1), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
          } else {
            null
          }
        }.getOrNull()
      } else {
        null
      }
    }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      color = Color(0xFF060A0D),
      modifier = Modifier.fillMaxSize(),
    ) {
      Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text =
              if (isAiGenerated) {
                stringResource(R.string.ai_generated_badge)
              } else {
                stringResource(R.string.photo_viewer_title)
              },
            style = MaterialTheme.typography.titleMedium,
            color = WhatsAppTextPrimary,
          )
          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = LucideIcons.Close,
              contentDescription = stringResource(R.string.cd_close),
              tint = WhatsAppTextPrimary,
            )
          }
        }

        Box(
          modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
          contentAlignment = Alignment.Center,
        ) {
          if (decodedDataBitmap != null) {
            Image(
              bitmap = decodedDataBitmap,
              contentDescription = caption,
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            AsyncImage(
              model = imageUri,
              contentDescription = caption,
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxSize(),
            )
          }
        }

        if (caption.isNotBlank()) {
          Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium,
            color = WhatsAppTextSecondary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
          )
        }
      }
    }
  }
}
