package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.AccentPurpleMuted
import com.example.ui.theme.AccentPurpleSoftDark
import com.example.ui.theme.OnAccentPurple
import com.example.ui.theme.WhatsAppBackground
import com.example.ui.theme.WhatsAppOutlineSubtle
import com.example.ui.theme.WhatsAppSurfaceCard
import com.example.ui.theme.WhatsAppSurfaceContainer
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

private val StatusColorSwatches =
  listOf(
    0xFF7F46B8L,
    0xFF532D7AL,
    0xFF2B193DL,
    0xFF1F2C34L,
    0xFF6B3E6BL,
  )

@Composable
fun UpdatesTabContent(
  myStatuses: List<StatusUpdate>,
  channels: List<ChannelItem>,
  onMyStatusClick: () -> Unit,
  onAddPhotoStatusClick: () -> Unit,
  onAddTextStatusClick: () -> Unit,
  onOpenStatusAt: (Int) -> Unit,
  onCreateChannelClick: () -> Unit,
  onOpenChannelClick: (ChannelItem) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Status Header Row ("Status" + Camera + Pencil actions)
    item(key = "status_header") {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = stringResource(R.string.updates_status_header),
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
          color = WhatsAppTextPrimary,
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .size(36.dp)
                .clip(CircleShape)
                .background(WhatsAppSurfaceContainer)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(bounded = true),
                  onClick = onAddPhotoStatusClick,
                )
                .testTag("updates_camera_button"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = LucideIcons.Camera,
              contentDescription = stringResource(R.string.cd_camera_status),
              tint = WhatsAppTextPrimary,
              modifier = Modifier.size(18.dp),
            )
          }

          Box(
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .size(36.dp)
                .clip(CircleShape)
                .background(WhatsAppSurfaceContainer)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(bounded = true),
                  onClick = onAddTextStatusClick,
                )
                .testTag("updates_pencil_button"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = LucideIcons.Pencil,
              contentDescription = stringResource(R.string.cd_text_status),
              tint = WhatsAppTextPrimary,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    }

    // My Status Card
    item(key = "my_status_card") {
      MyStatusRowCard(
        statuses = myStatuses,
        onClick = onMyStatusClick,
      )
    }

    // Horizontal Status Previews Strip when user has posted status updates
    if (myStatuses.isNotEmpty()) {
      item(key = "status_strip") {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(vertical = 2.dp),
        ) {
          itemsIndexed(myStatuses, key = { _, s -> s.id }) { index, status ->
            StatusMiniCard(
              status = status,
              onClick = { onOpenStatusAt(index) },
              modifier = Modifier.testTag("status_preview_$index"),
            )
          }
        }
      }
    } else {
      item(key = "recent_updates_hint") {
        Text(
          text = stringResource(R.string.updates_no_recent_contacts),
          style = MaterialTheme.typography.bodyMedium,
          color = WhatsAppTextSecondary,
          modifier = Modifier.padding(horizontal = 4.dp),
        )
      }
    }

    item(key = "section_divider") {
      HorizontalDivider(
        thickness = 0.5.dp,
        color = WhatsAppOutlineSubtle,
        modifier = Modifier.padding(vertical = 4.dp),
      )
    }

    // Channels Section Header ("Channels" + "+")
    item(key = "channels_header") {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = stringResource(R.string.updates_channels_header),
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
          color = WhatsAppTextPrimary,
        )

        Box(
          modifier =
            Modifier
              .minimumInteractiveComponentSize()
              .size(36.dp)
              .clip(CircleShape)
              .background(AccentPurpleSoftDark)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onCreateChannelClick,
              )
              .testTag("channels_plus_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = LucideIcons.Plus,
            contentDescription = stringResource(R.string.cd_create_channel),
            tint = AccentPurpleMuted,
            modifier = Modifier.size(19.dp),
          )
        }
      }
    }

    if (channels.isEmpty()) {
      item(key = "channels_empty_card") {
        Surface(
          color = WhatsAppSurfaceCard,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Box(
              modifier =
                Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(AccentPurpleSoftDark),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = LucideIcons.Radio,
                contentDescription = null,
                tint = AccentPurpleBright,
                modifier = Modifier.size(26.dp),
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = stringResource(R.string.updates_channels_subtitle),
              style = MaterialTheme.typography.bodyMedium,
              color = WhatsAppTextSecondary,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = onCreateChannelClick,
              shape = RoundedCornerShape(50),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = AccentPurple,
                  contentColor = OnAccentPurple,
                ),
              modifier = Modifier.height(44.dp).testTag("create_channel_button"),
            ) {
              Text(
                text = stringResource(R.string.updates_create_channel),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
              )
            }
          }
        }
      }
    } else {
      item(key = "channels_list_card") {
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(WhatsAppSurfaceCard),
        ) {
          channels.forEachIndexed { index, channel ->
            ChannelRow(
              channel = channel,
              onClick = { onOpenChannelClick(channel) },
            )
            if (index < channels.lastIndex) {
              HorizontalDivider(
                thickness = 0.5.dp,
                color = WhatsAppOutlineSubtle,
                modifier = Modifier.padding(start = 74.dp),
              )
            }
          }
        }
      }
    }

    // End-to-end encrypted footer
    item(key = "updates_encryption_footer") {
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = LucideIcons.Lock,
          contentDescription = null,
          tint = WhatsAppTextSecondary,
          modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text =
            buildAnnotatedString {
              append(stringResource(R.string.updates_status_encrypted_prefix))
              withStyle(SpanStyle(color = AccentPurpleBright, fontWeight = FontWeight.Medium)) {
                append(stringResource(R.string.updates_status_encrypted_accent))
              }
            },
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
          color = WhatsAppTextSecondary,
        )
      }
    }
  }
}

@Composable
private fun MyStatusRowCard(
  statuses: List<StatusUpdate>,
  onClick: () -> Unit,
) {
  val latest = statuses.lastOrNull()
  Surface(
    color = WhatsAppSurfaceCard,
    shape = RoundedCornerShape(16.dp),
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(bounded = true),
          onClick = onClick,
        )
        .testTag("my_status_row"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
        if (statuses.isNotEmpty()) {
          SegmentedStatusRing(
            count = statuses.size,
            color = AccentPurple,
            modifier = Modifier.fillMaxSize(),
          )
        }

        Box(
          modifier =
            Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(
                if (latest != null && latest.photoUri == null) {
                  Color(latest.backgroundHex)
                } else {
                  AccentPurpleSoftDark
                }
              ),
          contentAlignment = Alignment.Center,
        ) {
          when {
            latest?.photoUri != null -> {
              AsyncImage(
                model = latest.photoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
              )
            }
            latest != null && latest.text.isNotBlank() -> {
              Text(
                text = latest.text,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = OnAccentPurple,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(4.dp),
              )
            }
            else -> {
              Icon(
                imageVector = LucideIcons.User,
                contentDescription = null,
                tint = AccentPurpleMuted,
                modifier = Modifier.size(24.dp),
              )
            }
          }
        }

        if (statuses.isEmpty()) {
          Box(
            modifier =
              Modifier
                .align(Alignment.BottomEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(WhatsAppBackground)
                .padding(2.dp)
                .clip(CircleShape)
                .background(AccentPurple),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = LucideIcons.Plus,
              contentDescription = null,
              tint = OnAccentPurple,
              modifier = Modifier.size(13.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.updates_my_status),
          style = MaterialTheme.typography.titleMedium,
          color = WhatsAppTextPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text =
            when {
              statuses.isEmpty() -> stringResource(R.string.updates_add_status_subtitle)
              statuses.size == 1 ->
                stringResource(R.string.updates_status_count_single, latest?.timestampLabel.orEmpty())
              else ->
                stringResource(
                  R.string.updates_status_count_multi,
                  statuses.size,
                  latest?.timestampLabel.orEmpty(),
                )
            },
          style = MaterialTheme.typography.bodyMedium,
          color = WhatsAppTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun SegmentedStatusRing(
  count: Int,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val strokeWidth = 2.5.dp.toPx()
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    if (count <= 1) {
      drawCircle(color = color, style = stroke)
    } else {
      val gap = 10f
      val sweep = (360f / count) - gap
      for (i in 0 until count) {
        drawArc(
          color = color,
          startAngle = -90f + i * (360f / count) + gap / 2f,
          sweepAngle = sweep.coerceAtLeast(8f),
          useCenter = false,
          style = stroke,
        )
      }
    }
  }
}

@Composable
private fun StatusMiniCard(
  status: StatusUpdate,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .size(width = 96.dp, height = 136.dp)
        .clip(RoundedCornerShape(14.dp))
        .border(1.5.dp, AccentPurple, RoundedCornerShape(14.dp))
        .background(Color(status.backgroundHex))
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    if (status.photoUri != null) {
      AsyncImage(
        model = status.photoUri,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      Text(
        text = status.text,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
        color = OnAccentPurple,
        textAlign = TextAlign.Center,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(10.dp),
      )
    }
    Text(
      text = status.timestampLabel,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      color = OnAccentPurple.copy(alpha = 0.85f),
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .background(Color.Black.copy(alpha = 0.35f))
          .padding(horizontal = 6.dp, vertical = 4.dp),
      textAlign = TextAlign.Center,
      maxLines = 1,
    )
  }
}

@Composable
private fun ChannelRow(
  channel: ChannelItem,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .minimumInteractiveComponentSize()
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(bounded = true),
          onClick = onClick,
        )
        .padding(horizontal = 14.dp, vertical = 12.dp)
        .testTag("channel_row_${channel.id}"),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(AccentPurpleSoftDark),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = LucideIcons.Radio,
        contentDescription = null,
        tint = AccentPurpleBright,
        modifier = Modifier.size(22.dp),
      )
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = channel.name,
        style = MaterialTheme.typography.titleMedium,
        color = WhatsAppTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      val subtitle =
        channel.posts.lastOrNull()?.text
          ?: channel.description.ifEmpty { stringResource(R.string.updates_channel_followers, 1) }
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = WhatsAppTextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    Icon(
      imageVector = LucideIcons.ChevronRight,
      contentDescription = null,
      tint = WhatsAppTextSecondary,
      modifier = Modifier.size(18.dp),
    )
  }
}

@Composable
fun StatusTypeChooserContent(
  onWriteTextClick: () -> Unit,
  onPickPhotoClick: () -> Unit,
  onClose: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.status_chooser_title),
        style = MaterialTheme.typography.titleLarge,
        color = WhatsAppTextPrimary,
      )
      TextButton(onClick = onClose) {
        Text(stringResource(R.string.action_cancel), color = AccentPurpleBright)
      }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Button(
      onClick = onWriteTextClick,
      shape = RoundedCornerShape(50),
      colors = ButtonDefaults.buttonColors(containerColor = AccentPurple, contentColor = OnAccentPurple),
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("choose_text_status_button"),
    ) {
      Icon(LucideIcons.Pencil, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(8.dp))
      Text(stringResource(R.string.status_chooser_text), style = MaterialTheme.typography.labelLarge)
    }
    Spacer(modifier = Modifier.height(10.dp))
    Button(
      onClick = onPickPhotoClick,
      shape = RoundedCornerShape(50),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = WhatsAppSurfaceContainer,
          contentColor = WhatsAppTextPrimary,
        ),
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("choose_photo_status_button"),
    ) {
      Icon(LucideIcons.Image, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(8.dp))
      Text(stringResource(R.string.status_chooser_photo), style = MaterialTheme.typography.labelLarge)
    }
  }
}

@Composable
fun TextStatusComposerContent(
  onPost: (String, Long) -> Unit,
  onCancel: () -> Unit,
) {
  var statusText by remember { mutableStateOf("") }
  var selectedBg by remember { mutableLongStateOf(StatusColorSwatches.first()) }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TextButton(onClick = onCancel) {
        Text(stringResource(R.string.action_cancel), color = AccentPurpleBright)
      }
      Text(
        text = stringResource(R.string.status_composer_title),
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
        color = WhatsAppTextPrimary,
      )
      TextButton(
        onClick = { onPost(statusText, selectedBg) },
        enabled = statusText.isNotBlank(),
        modifier = Modifier.testTag("post_text_status_button"),
      ) {
        Text(
          text = stringResource(R.string.status_composer_post),
          color =
            if (statusText.isNotBlank()) AccentPurpleBright else WhatsAppTextSecondary.copy(alpha = 0.5f),
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(180.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(Color(selectedBg))
          .padding(20.dp),
      contentAlignment = Alignment.Center,
    ) {
      if (statusText.isEmpty()) {
        Text(
          text = stringResource(R.string.status_composer_hint),
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
          color = OnAccentPurple.copy(alpha = 0.65f),
          textAlign = TextAlign.Center,
        )
      }
      BasicTextField(
        value = statusText,
        onValueChange = { statusText = it },
        textStyle =
          MaterialTheme.typography.titleLarge.copy(
            fontSize = 22.sp,
            color = OnAccentPurple,
            textAlign = TextAlign.Center,
          ),
        cursorBrush = SolidColor(OnAccentPurple),
        modifier = Modifier.fillMaxWidth().testTag("text_status_input"),
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      StatusColorSwatches.forEach { swatch ->
        val isSelected = selectedBg == swatch
        Box(
          modifier =
            Modifier
              .padding(horizontal = 6.dp)
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(swatch))
              .then(
                if (isSelected) Modifier.border(2.dp, OnAccentPurple, CircleShape) else Modifier
              )
              .clickable { selectedBg = swatch },
          contentAlignment = Alignment.Center,
        ) {
          if (isSelected) {
            Icon(
              imageVector = LucideIcons.Check,
              contentDescription = null,
              tint = OnAccentPurple,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
fun CreateChannelSheetContent(
  onCreate: (String, String) -> Unit,
  onCancel: () -> Unit,
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TextButton(onClick = onCancel) {
        Text(stringResource(R.string.action_cancel), color = AccentPurpleBright)
      }
      Text(
        text = stringResource(R.string.channel_sheet_title),
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
        color = WhatsAppTextPrimary,
      )
      Spacer(modifier = Modifier.width(56.dp))
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      singleLine = true,
      placeholder = { Text(stringResource(R.string.channel_name_hint), color = WhatsAppTextSecondary) },
      shape = RoundedCornerShape(14.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = WhatsAppSurfaceContainer,
          unfocusedContainerColor = WhatsAppSurfaceContainer,
          focusedBorderColor = AccentPurple,
          unfocusedBorderColor = Color.Transparent,
          focusedTextColor = WhatsAppTextPrimary,
          unfocusedTextColor = WhatsAppTextPrimary,
        ),
      modifier = Modifier.fillMaxWidth().testTag("channel_name_input"),
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = description,
      onValueChange = { description = it },
      minLines = 3,
      maxLines = 4,
      placeholder = { Text(stringResource(R.string.channel_desc_hint), color = WhatsAppTextSecondary) },
      shape = RoundedCornerShape(14.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = WhatsAppSurfaceContainer,
          unfocusedContainerColor = WhatsAppSurfaceContainer,
          focusedBorderColor = AccentPurple,
          unfocusedBorderColor = Color.Transparent,
          focusedTextColor = WhatsAppTextPrimary,
          unfocusedTextColor = WhatsAppTextPrimary,
        ),
      modifier = Modifier.fillMaxWidth().testTag("channel_desc_input"),
    )

    Spacer(modifier = Modifier.height(18.dp))

    Button(
      onClick = { onCreate(name, description) },
      enabled = name.isNotBlank(),
      shape = RoundedCornerShape(50),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = AccentPurple,
          contentColor = OnAccentPurple,
        ),
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_create_channel_button"),
    ) {
      Text(stringResource(R.string.updates_create_channel), style = MaterialTheme.typography.labelLarge)
    }
  }
}
