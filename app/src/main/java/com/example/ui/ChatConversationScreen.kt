package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.R
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.AccentPurpleBubble
import com.example.ui.theme.AccentPurpleMuted
import com.example.ui.theme.AccentPurpleSoftDark
import com.example.ui.theme.ChatEncryptionBannerBg
import com.example.ui.theme.ChatEncryptionBannerText
import com.example.ui.theme.ChatWallpaperDoodle
import com.example.ui.theme.OnAccentPurple
import com.example.ui.theme.WhatsAppBackground
import com.example.ui.theme.WhatsAppOutlineSubtle
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppSurfaceCard
import com.example.ui.theme.WhatsAppSurfaceContainer
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

private val QuickEmojis = listOf("👋", "😊", "💜", "👍", "🔥", "🎉", "😂", "🙏")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
  contact: Contact,
  messages: List<ChatMessage>,
  inputText: String,
  showMediaTooltip: Boolean,
  activeCallType: String?,
  onBack: () -> Unit,
  onInputChange: (String) -> Unit,
  onAppendEmoji: (String) -> Unit,
  onSendMessage: () -> Unit,
  onAttachMedia: () -> Unit,
  onVoiceNote: () -> Unit,
  onDismissTooltip: () -> Unit,
  onStartCall: (String) -> Unit,
  onEndCall: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var showEncryptionDialog by remember { mutableStateOf(false) }
  var showContactInfoSheet by remember { mutableStateOf(false) }
  var showEmojiStrip by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size + 1)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = WhatsAppBackground,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      Column {
        ChatTopAppBar(
          contact = contact,
          onBack = onBack,
          onHeaderClick = { showContactInfoSheet = true },
          onVideoCallClick = { onStartCall("video") },
          onVoiceCallClick = { onStartCall("voice") },
        )
        AnimatedVisibility(visible = activeCallType != null) {
          if (activeCallType != null) {
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .background(AccentPurple)
                  .padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text =
                  stringResource(
                    R.string.chat_calling_banner,
                    activeCallType,
                    contact.displayName,
                  ),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = OnAccentPurple,
                modifier = Modifier.weight(1f),
              )
              TextButton(onClick = onEndCall) {
                Text(
                  text = stringResource(R.string.chat_end_call),
                  color = OnAccentPurple,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }
    },
    bottomBar = {
      Column(
        modifier = Modifier.fillMaxWidth(),
      ) {
        AnimatedVisibility(
          visible = showMediaTooltip,
          enter = fadeIn() + slideInVertically { it / 2 },
          exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
          MediaTooltipBubble(
            onDismiss = onDismissTooltip,
            modifier = Modifier.padding(start = 10.dp, end = 16.dp, bottom = 6.dp),
          )
        }

        AnimatedVisibility(visible = showEmojiStrip) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .background(WhatsAppSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
          ) {
            QuickEmojis.forEach { emoji ->
              Text(
                text = emoji,
                fontSize = 22.sp,
                modifier =
                  Modifier
                    .clip(CircleShape)
                    .clickable { onAppendEmoji(emoji) }
                    .padding(6.dp),
              )
            }
          }
        }

        ChatBottomInputBar(
          inputText = inputText,
          onInputChange = onInputChange,
          onPlusClick = {
            onDismissTooltip()
            onAttachMedia()
          },
          onEmojiToggle = { showEmojiStrip = !showEmojiStrip },
          onCameraClick = {
            onDismissTooltip()
            onAttachMedia()
          },
          onMicClick = onVoiceNote,
          onSendClick = onSendMessage,
        )
      }
    },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
    ) {
      WhatsAppDarkDoodleWallpaper(modifier = Modifier.fillMaxSize())

      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        item(key = "today_pill") {
          Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
              color = WhatsAppSurfaceCard,
              shape = RoundedCornerShape(10.dp),
            ) {
              Text(
                text = stringResource(R.string.chat_date_today),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                color = WhatsAppTextSecondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
              )
            }
          }
        }

        item(key = "encryption_notice") {
          Box(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 6.dp),
            contentAlignment = Alignment.Center,
          ) {
            EncryptionNoticeCard(
              onLearnMoreClick = { showEncryptionDialog = true },
            )
          }
        }

        items(messages, key = { it.id }) { message ->
          ChatBubbleRow(message = message)
        }
      }
    }
  }

  if (showEncryptionDialog) {
    AlertDialog(
      onDismissRequest = { showEncryptionDialog = false },
      containerColor = WhatsAppSurface,
      titleContentColor = WhatsAppTextPrimary,
      textContentColor = WhatsAppTextSecondary,
      title = {
        Text(
          text = stringResource(R.string.chat_encryption_dialog_title),
          style = MaterialTheme.typography.titleLarge,
        )
      },
      text = {
        Text(
          text = stringResource(R.string.chat_encryption_dialog_body),
          style = MaterialTheme.typography.bodyMedium,
        )
      },
      confirmButton = {
        TextButton(onClick = { showEncryptionDialog = false }) {
          Text(stringResource(R.string.qr_dialog_close), color = AccentPurpleBright)
        }
      },
    )
  }

  if (showContactInfoSheet) {
    ModalBottomSheet(
      onDismissRequest = { showContactInfoSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      containerColor = WhatsAppSurface,
      contentColor = WhatsAppTextPrimary,
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier =
            Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(AccentPurpleSoftDark),
          contentAlignment = Alignment.Center,
        ) {
          if (contact.initials.isNotEmpty() && !contact.isSelf) {
            Text(
              text = contact.initials,
              style = MaterialTheme.typography.headlineMedium,
              color = AccentPurpleMuted,
            )
          } else {
            Icon(
              imageVector = LucideIcons.User,
              contentDescription = null,
              tint = AccentPurpleMuted,
              modifier = Modifier.size(34.dp),
            )
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = contact.displayName,
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
          color = WhatsAppTextPrimary,
        )
        if (contact.formattedPhone.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = contact.formattedPhone,
            style = MaterialTheme.typography.bodyLarge,
            color = WhatsAppTextSecondary,
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          color = WhatsAppSurfaceCard,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = LucideIcons.Lock,
              contentDescription = null,
              tint = AccentPurpleBright,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text =
                if (contact.syncToPhone) {
                  stringResource(R.string.chat_info_synced)
                } else {
                  stringResource(R.string.chat_info_local)
                },
              style = MaterialTheme.typography.bodyMedium,
              color = WhatsAppTextPrimary,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChatTopAppBar(
  contact: Contact,
  onBack: () -> Unit,
  onHeaderClick: () -> Unit,
  onVideoCallClick: () -> Unit,
  onVoiceCallClick: () -> Unit,
) {
  Surface(color = WhatsAppSurface, tonalElevation = 0.dp) {
    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("chat_back_button"),
        ) {
          Icon(
            imageVector = LucideIcons.ChevronLeft,
            contentDescription = stringResource(R.string.cd_back),
            tint = WhatsAppTextPrimary,
            modifier = Modifier.size(26.dp),
          )
        }

        Row(
          modifier =
            Modifier
              .weight(1f)
              .minimumInteractiveComponentSize()
              .clip(RoundedCornerShape(10.dp))
              .clickable(onClick = onHeaderClick)
              .padding(vertical = 4.dp, horizontal = 4.dp)
              .testTag("chat_header_info"),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier =
              Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(AccentPurpleSoftDark),
            contentAlignment = Alignment.Center,
          ) {
            if (contact.initials.isNotEmpty() && !contact.isSelf) {
              Text(
                text = contact.initials,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                color = AccentPurpleMuted,
              )
            } else {
              Icon(
                imageVector = LucideIcons.User,
                contentDescription = null,
                tint = AccentPurpleMuted,
                modifier = Modifier.size(20.dp),
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = contact.displayName,
              style = MaterialTheme.typography.titleMedium,
              color = WhatsAppTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = stringResource(R.string.chat_tap_contact_info),
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
              color = WhatsAppTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        IconButton(
          onClick = onVideoCallClick,
          modifier = Modifier.testTag("chat_video_call_button"),
        ) {
          Icon(
            imageVector = LucideIcons.Video,
            contentDescription = stringResource(R.string.cd_video_call),
            tint = WhatsAppTextPrimary,
            modifier = Modifier.size(22.dp),
          )
        }

        IconButton(
          onClick = onVoiceCallClick,
          modifier = Modifier.testTag("chat_voice_call_button"),
        ) {
          Icon(
            imageVector = LucideIcons.Phone,
            contentDescription = stringResource(R.string.cd_voice_call),
            tint = WhatsAppTextPrimary,
            modifier = Modifier.size(20.dp),
          )
        }
      }
      HorizontalDivider(thickness = 0.5.dp, color = WhatsAppOutlineSubtle)
    }
  }
}

@Composable
private fun EncryptionNoticeCard(onLearnMoreClick: () -> Unit) {
  val noticeBody = stringResource(R.string.chat_encryption_notice)
  val learnMore = stringResource(R.string.chat_learn_more)
  val annotated =
    buildAnnotatedString {
      append(noticeBody)
      append(" ")
      withStyle(SpanStyle(color = AccentPurpleBright, fontWeight = FontWeight.SemiBold)) {
        append(learnMore)
      }
    }

  Surface(
    color = ChatEncryptionBannerBg,
    shape = RoundedCornerShape(12.dp),
    modifier =
      Modifier
        .widthIn(max = 310.dp)
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onLearnMoreClick)
        .testTag("encryption_notice_card"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.Top,
    ) {
      Icon(
        imageVector = LucideIcons.Lock,
        contentDescription = null,
        tint = AccentPurpleBright,
        modifier = Modifier.padding(top = 2.dp).size(13.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
        color = ChatEncryptionBannerText,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun ChatBubbleRow(message: ChatMessage) {
  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = if (message.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart,
  ) {
    Surface(
      color = if (message.isOutgoing) AccentPurpleBubble else WhatsAppSurfaceCard,
      shape =
        RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (message.isOutgoing) 16.dp else 4.dp,
          bottomEnd = if (message.isOutgoing) 4.dp else 16.dp,
        ),
      modifier = Modifier.widthIn(max = 290.dp),
    ) {
      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        if (message.mediaLabel != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp),
          ) {
            Icon(
              imageVector =
                if (message.mediaLabel == "Voice") LucideIcons.Mic else LucideIcons.Image,
              contentDescription = null,
              tint = AccentPurpleMuted,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = message.text,
              style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
              color = WhatsAppTextPrimary,
            )
          }
        } else {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = WhatsAppTextPrimary,
          )
        }

        Row(
          modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = message.timestampLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = AccentPurpleMuted.copy(alpha = 0.85f),
          )
          if (message.isOutgoing) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = LucideIcons.CheckCheck,
              contentDescription = null,
              tint = AccentPurpleMuted,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MediaTooltipBubble(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Row(
      modifier =
        Modifier
          .clip(RoundedCornerShape(50))
          .background(AccentPurple)
          .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
          .testTag("media_tooltip_banner"),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.chat_tooltip_media),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 13.5.sp),
        color = OnAccentPurple,
      )
      Spacer(modifier = Modifier.width(10.dp))
      Box(
        modifier =
          Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(OnAccentPurple)
            .clickable(onClick = onDismiss)
            .testTag("dismiss_tooltip_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = LucideIcons.Close,
          contentDescription = stringResource(R.string.cd_dismiss_tooltip),
          tint = AccentPurple,
          modifier = Modifier.size(13.dp),
        )
      }
    }

    // Speech pointer pointing down to the + button
    Canvas(modifier = Modifier.padding(start = 12.dp).size(width = 14.dp, height = 7.dp)) {
      val pointer =
        Path().apply {
          moveTo(0f, 0f)
          lineTo(size.width, 0f)
          lineTo(size.width / 2f, size.height)
          close()
        }
      drawPath(path = pointer, color = AccentPurple)
    }
  }
}

@Composable
private fun ChatBottomInputBar(
  inputText: String,
  onInputChange: (String) -> Unit,
  onPlusClick: () -> Unit,
  onEmojiToggle: () -> Unit,
  onCameraClick: () -> Unit,
  onMicClick: () -> Unit,
  onSendClick: () -> Unit,
) {
  Surface(color = WhatsAppSurface, tonalElevation = 0.dp) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
    ) {
      HorizontalDivider(thickness = 0.5.dp, color = WhatsAppOutlineSubtle)
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onPlusClick,
          modifier = Modifier.testTag("chat_attach_plus_button"),
        ) {
          Icon(
            imageVector = LucideIcons.Plus,
            contentDescription = stringResource(R.string.cd_attach_media),
            tint = WhatsAppTextPrimary,
            modifier = Modifier.size(24.dp),
          )
        }

        // Rounded pill input with trailing emoji/sticker Lucide icon
        Row(
          modifier =
            Modifier
              .weight(1f)
              .height(40.dp)
              .clip(RoundedCornerShape(22.dp))
              .background(WhatsAppSurfaceContainer)
              .padding(start = 14.dp, end = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (inputText.isEmpty()) {
              Text(
                text = stringResource(R.string.chat_input_placeholder),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = WhatsAppTextSecondary,
              )
            }
            BasicTextField(
              value = inputText,
              onValueChange = onInputChange,
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, color = WhatsAppTextPrimary),
              cursorBrush = SolidColor(AccentPurple),
              modifier = Modifier.fillMaxWidth().testTag("chat_message_input"),
            )
          }

          Icon(
            imageVector = LucideIcons.Smile,
            contentDescription = stringResource(R.string.cd_emoji_stickers),
            tint = WhatsAppTextSecondary,
            modifier =
              Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onEmojiToggle)
                .padding(4.dp)
                .testTag("chat_emoji_button"),
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        if (inputText.isBlank()) {
          IconButton(
            onClick = onCameraClick,
            modifier = Modifier.testTag("chat_camera_button"),
          ) {
            Icon(
              imageVector = LucideIcons.Camera,
              contentDescription = stringResource(R.string.cd_camera_action),
              tint = WhatsAppTextPrimary,
              modifier = Modifier.size(22.dp),
            )
          }
          IconButton(
            onClick = onMicClick,
            modifier = Modifier.testTag("chat_mic_button"),
          ) {
            Icon(
              imageVector = LucideIcons.Mic,
              contentDescription = stringResource(R.string.cd_voice_note),
              tint = WhatsAppTextPrimary,
              modifier = Modifier.size(22.dp),
            )
          }
        } else {
          Box(
            modifier =
              Modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentPurple)
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(bounded = true),
                  onClick = onSendClick,
                )
                .testTag("chat_send_button"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = LucideIcons.SendHorizontal,
              contentDescription = stringResource(R.string.cd_send_message),
              tint = OnAccentPurple,
              modifier = Modifier.size(19.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun WhatsAppDarkDoodleWallpaper(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val stepX = 76.dp.toPx()
    val stepY = 76.dp.toPx()
    val stroke = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round)
    val cols = (size.width / stepX).toInt() + 2
    val rows = (size.height / stepY).toInt() + 2

    for (r in 0 until rows) {
      for (c in 0 until cols) {
        val x = c * stepX + (if (r % 2 == 1) stepX * 0.45f else 18f)
        val y = r * stepY + 20f
        when ((r + c) % 4) {
          0 -> {
            // Subtle mini chat bubble outline
            drawCircle(
              color = ChatWallpaperDoodle,
              radius = 9.dp.toPx(),
              center = Offset(x, y),
              style = stroke,
            )
          }
          1 -> {
            // Subtle small star / sparkle cross
            val s = 7.dp.toPx()
            drawLine(ChatWallpaperDoodle, Offset(x - s, y), Offset(x + s, y), strokeWidth = stroke.width)
            drawLine(ChatWallpaperDoodle, Offset(x, y - s), Offset(x, y + s), strokeWidth = stroke.width)
          }
          2 -> {
            // Subtle small crescent / wave
            val path =
              Path().apply {
                moveTo(x - 8.dp.toPx(), y)
                quadraticTo(x, y - 9.dp.toPx(), x + 8.dp.toPx(), y)
              }
            drawPath(path, ChatWallpaperDoodle, style = stroke)
          }
          else -> {
            // Subtle diamond outline
            val s = 7.dp.toPx()
            val diamond =
              Path().apply {
                moveTo(x, y - s)
                lineTo(x + s, y)
                lineTo(x, y + s)
                lineTo(x - s, y)
                close()
              }
            drawPath(diamond, ChatWallpaperDoodle, style = stroke)
          }
        }
      }
    }
  }
}
