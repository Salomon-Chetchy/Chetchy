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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.AccentPurpleMuted
import com.example.ui.theme.AccentPurpleSoftDark
import com.example.ui.theme.OnAccentPurple
import com.example.ui.theme.WhatsAppOutlineSubtle
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppSurfaceCard
import com.example.ui.theme.WhatsAppSurfaceElevated
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

private val PromptIdeas =
  listOf(
    "Futuristic neon city",
    "Minimalist purple app icon",
    "Watercolor mountain sunset",
    "Cute 3D clay robot",
  )

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
fun PendingPhotoAttachmentBar(
  photoUri: Uri,
  identifyWithAi: Boolean,
  onToggleIdentify: () -> Unit,
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
          text =
            if (identifyWithAi) {
              stringResource(R.string.photo_identify_ready_title)
            } else {
              stringResource(R.string.photo_attach_ready_title)
            },
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = WhatsAppTextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text =
            if (identifyWithAi) {
              stringResource(R.string.photo_identify_ready_hint)
            } else {
              stringResource(R.string.photo_attach_ready_hint)
            },
          style = MaterialTheme.typography.bodySmall,
          color = WhatsAppTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Surface(
        color = if (identifyWithAi) AccentPurple else WhatsAppSurfaceElevated,
        shape = RoundedCornerShape(50),
        modifier =
          Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onToggleIdentify)
            .testTag("toggle_photo_identify_chip"),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = LucideIcons.Sparkles,
            contentDescription = null,
            tint = if (identifyWithAi) OnAccentPurple else WhatsAppTextSecondary,
            modifier = Modifier.size(13.dp),
          )
          Text(
            text = stringResource(R.string.photo_identify_chip),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (identifyWithAi) OnAccentPurple else WhatsAppTextSecondary,
          )
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMediaAndAiToolsSheet(
  onIdentifyPhotoClick: () -> Unit,
  onGenerateImageClick: () -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = WhatsAppSurface,
    contentColor = WhatsAppTextPrimary,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 26.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = stringResource(R.string.chat_tools_sheet_title),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = WhatsAppTextPrimary,
      )

      Surface(
        color = WhatsAppSurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WhatsAppOutlineSubtle),
        modifier =
          Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onIdentifyPhotoClick)
            .testTag("chat_identify_photo_option"),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(AccentPurpleSoftDark),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = LucideIcons.Image,
              contentDescription = null,
              tint = AccentPurpleBright,
              modifier = Modifier.size(21.dp),
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.chat_tool_identify_photo_title),
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              color = WhatsAppTextPrimary,
            )
            Text(
              text = stringResource(R.string.chat_tool_identify_photo_desc),
              style = MaterialTheme.typography.bodySmall,
              color = WhatsAppTextSecondary,
            )
          }
        }
      }

      Surface(
        color = WhatsAppSurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WhatsAppOutlineSubtle),
        modifier =
          Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onGenerateImageClick)
            .testTag("chat_generate_image_option"),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(AccentPurpleSoftDark),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = LucideIcons.Sparkles,
              contentDescription = null,
              tint = AccentPurpleBright,
              modifier = Modifier.size(21.dp),
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.chat_tool_generate_image_title),
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              color = WhatsAppTextPrimary,
            )
            Text(
              text = stringResource(R.string.chat_tool_generate_image_desc),
              style = MaterialTheme.typography.bodySmall,
              color = WhatsAppTextSecondary,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenerateAiImageDialog(
  onDismiss: () -> Unit,
  onGenerate: (String) -> Unit,
) {
  var promptText by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WhatsAppSurface,
    titleContentColor = WhatsAppTextPrimary,
    textContentColor = WhatsAppTextSecondary,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = LucideIcons.Sparkles,
          contentDescription = null,
          tint = AccentPurpleBright,
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = stringResource(R.string.generate_image_dialog_title),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = stringResource(R.string.generate_image_dialog_subtitle),
          style = MaterialTheme.typography.bodySmall,
          color = WhatsAppTextSecondary,
        )
        OutlinedTextField(
          value = promptText,
          onValueChange = { promptText = it },
          placeholder = {
            Text(
              text = stringResource(R.string.generate_image_prompt_hint),
              color = WhatsAppTextSecondary,
            )
          },
          minLines = 2,
          maxLines = 4,
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentPurple,
              unfocusedBorderColor = WhatsAppOutlineSubtle,
              focusedTextColor = WhatsAppTextPrimary,
              unfocusedTextColor = WhatsAppTextPrimary,
              cursorColor = AccentPurpleBright,
            ),
          modifier = Modifier.fillMaxWidth().testTag("generate_image_prompt_input"),
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          PromptIdeas.forEach { idea ->
            FilterChip(
              selected = promptText == idea,
              onClick = { promptText = idea },
              label = {
                Text(
                  text = idea,
                  style = MaterialTheme.typography.labelSmall,
                )
              },
              colors =
                FilterChipDefaults.filterChipColors(
                  containerColor = WhatsAppSurfaceCard,
                  labelColor = AccentPurpleMuted,
                  selectedContainerColor = AccentPurpleSoftDark,
                  selectedLabelColor = AccentPurpleBright,
                ),
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onGenerate(promptText) },
        enabled = promptText.trim().isNotEmpty(),
        shape = RoundedCornerShape(50),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = AccentPurple,
            contentColor = OnAccentPurple,
          ),
        modifier = Modifier.testTag("confirm_generate_image_button"),
      ) {
        Text(stringResource(R.string.generate_image_action))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(
          text = stringResource(R.string.action_cancel),
          color = WhatsAppTextSecondary,
        )
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
