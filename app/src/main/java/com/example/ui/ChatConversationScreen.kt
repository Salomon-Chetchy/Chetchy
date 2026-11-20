package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
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
import com.example.ui.theme.WhatsAppSurfaceElevated
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val QuickEmojis = listOf("👋", "😊", "💜", "👍", "🔥", "🎉", "😂", "🙏")
private val IncomingBubbleSurface = Color(0xFF1B262D)
private val VoiceWaveformHeights =
  listOf(
    0.25f, 0.55f, 0.35f, 0.85f, 0.65f, 0.95f, 0.45f, 0.75f, 0.3f, 0.9f,
    0.6f, 0.4f, 0.8f, 0.5f, 0.25f, 0.7f, 0.45f, 0.3f, 0.65f, 0.35f,
    0.25f, 0.5f, 0.3f, 0.25f, 0.4f, 0.25f,
  )

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
  onUpdateGoalProgress: (String, Int) -> Unit,
  isAiThinking: Boolean = false,
  replyingToMessage: ChatMessage? = null,
  onReplyToMessage: (ChatMessage) -> Unit = {},
  onCancelReply: () -> Unit = {},
  onClearChat: () -> Unit = {},
) {
  BackHandler(onBack = onBack)

  var showEncryptionDialog by remember { mutableStateOf(false) }
  var showContactInfoSheet by remember { mutableStateOf(false) }
  var showEmojiStrip by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val showScrollToBottom by remember {
    derivedStateOf { listState.canScrollForward }
  }

  LaunchedEffect(messages.size, isAiThinking) {
    val targetIndex = messages.size + (if (isAiThinking) 2 else 1)
    if (messages.isNotEmpty() || isAiThinking) {
      listState.animateScrollToItem(targetIndex)
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
          onClearChat = onClearChat,
          onShowEncryption = { showEncryptionDialog = true },
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
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(top = innerPadding.calculateTopPadding()),
    ) {
      WhatsAppDarkDoodleWallpaper(modifier = Modifier.fillMaxSize())

      Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.Top,
          ) {
            item(key = "today_pill") {
              Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Surface(
                  color = WhatsAppSurfaceCard.copy(alpha = 0.88f),
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Text(
                    text = stringResource(R.string.chat_date_today),
                    style =
                      MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                      ),
                    color = WhatsAppTextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                  )
                }

                if (!contact.isSelf) {
                  Surface(
                    color = WhatsAppSurfaceCard.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(10.dp),
                  ) {
                    Text(
                      text = "${contact.displayName} is a contact",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                      color = WhatsAppTextSecondary,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                  }
                }
              }
            }

            item(key = "encryption_notice") {
              Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                contentAlignment = Alignment.Center,
              ) {
                EncryptionNoticeCard(
                  onLearnMoreClick = { showEncryptionDialog = true },
                )
              }
            }

            itemsIndexed(messages, key = { _, msg -> msg.id }) { index, message ->
              val prevMessage = messages.getOrNull(index - 1)
              val isFirstInGroup = prevMessage == null || prevMessage.isOutgoing != message.isOutgoing
              val topGap = if (index == 0) 0.dp else if (isFirstInGroup) 8.dp else 3.dp

              Spacer(modifier = Modifier.height(topGap))
              ChatBubbleRow(
                message = message,
                contact = contact,
                isFirstInGroup = isFirstInGroup,
                onReply = { onReplyToMessage(message) },
              )
            }

            if (isAiThinking) {
              item(key = "ai_thinking_indicator") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                  horizontalArrangement = Arrangement.Start,
                ) {
                  Surface(
                    color = IncomingBubbleSurface,
                    shape =
                      RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 14.dp,
                        bottomStart = 14.dp,
                        bottomEnd = 14.dp,
                      ),
                    modifier = Modifier.testTag("ai_thinking_bubble"),
                  ) {
                    Text(
                      text = stringResource(R.string.ai_typing),
                      style = MaterialTheme.typography.bodyMedium,
                      color = AccentPurpleMuted,
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                  }
                }
              }
            }
          }

          // Floating scroll-to-bottom double-chevron button (like screenshot)
          androidx.compose.animation.AnimatedVisibility(
            visible = showScrollToBottom,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 8.dp),
          ) {
            Surface(
              color = WhatsAppSurfaceElevated.copy(alpha = 0.94f),
              shape = CircleShape,
              shadowElevation = 3.dp,
              modifier =
                Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .clickable {
                    coroutineScope.launch {
                      val lastIdx = (messages.size + 1).coerceAtLeast(0)
                      listState.animateScrollToItem(lastIdx)
                    }
                  }
                  .testTag("scroll_to_bottom_button"),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = LucideIcons.ChevronsDown,
                  contentDescription = "Scroll to bottom",
                  tint = WhatsAppTextSecondary,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }

        // Floating composer area directly over wallpaper (matches screenshot)
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
          AnimatedVisibility(
            visible = showMediaTooltip,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
          ) {
            Box(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
              contentAlignment = Alignment.CenterEnd,
            ) {
              MediaTooltipBubble(
                onDismiss = onDismissTooltip,
              )
            }
          }

          AnimatedVisibility(visible = showEmojiStrip) {
            Surface(
              color = WhatsAppSurfaceCard.copy(alpha = 0.96f),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
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
          }

          if (replyingToMessage != null) {
            ReplyComposerPreview(
              message = replyingToMessage,
              contactName = contact.displayName,
              onCancel = onCancelReply,
            )
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
        if (contact.aiIdentity.isNotBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = AccentPurpleSoftDark,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = stringResource(R.string.ai_persona_badge),
                style = MaterialTheme.typography.labelSmall,
                color = AccentPurpleBright,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = contact.aiIdentity,
                style = MaterialTheme.typography.bodyMedium,
                color = WhatsAppTextPrimary,
              )
            }
          }
        }
        if (!contact.goalTitle.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            color = WhatsAppSurfaceCard,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("contact_info_goal_card"),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = contact.goalTitle,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                  color = WhatsAppTextPrimary,
                  modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${contact.goalProgress}%",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = AccentPurpleBright,
                  modifier = Modifier.testTag("goal_progress_text"),
                )
              }
              LinearProgressIndicator(
                progress = { (contact.goalProgress / 100f).coerceIn(0f, 1f) },
                color = AccentPurpleBright,
                trackColor = AccentPurpleSoftDark,
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
              ) {
                TextButton(
                  onClick = { onUpdateGoalProgress(contact.id, contact.goalProgress + 10) },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.minimumInteractiveComponentSize().testTag("increment_goal_button"),
                ) {
                  Text(
                    text = "+10% Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPurpleBright,
                  )
                }
              }
            }
          }
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
  onClearChat: () -> Unit,
  onShowEncryption: () -> Unit,
) {
  var showOverflowMenu by remember { mutableStateOf(false) }

  Surface(color = WhatsAppBackground, tonalElevation = 0.dp) {
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
            imageVector = LucideIcons.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            tint = WhatsAppTextPrimary,
            modifier = Modifier.size(22.dp),
          )
        }

        Row(
          modifier =
            Modifier
              .weight(1f)
              .minimumInteractiveComponentSize()
              .clip(RoundedCornerShape(10.dp))
              .clickable(onClick = onHeaderClick)
              .padding(vertical = 4.dp, horizontal = 2.dp)
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
              style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.5.sp, fontWeight = FontWeight.Medium),
              color = WhatsAppTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = stringResource(R.string.chat_tap_contact_info),
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
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
            modifier = Modifier.size(21.dp),
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

        Box {
          IconButton(
            onClick = { showOverflowMenu = true },
            modifier = Modifier.testTag("chat_more_options_button"),
          ) {
            Icon(
              imageVector = LucideIcons.MoreVertical,
              contentDescription = stringResource(R.string.cd_more_options),
              tint = WhatsAppTextPrimary,
              modifier = Modifier.size(20.dp),
            )
          }
          DropdownMenu(
            expanded = showOverflowMenu,
            onDismissRequest = { showOverflowMenu = false },
            containerColor = WhatsAppSurfaceElevated,
          ) {
            DropdownMenuItem(
              text = { Text(stringResource(R.string.chat_info_title), color = WhatsAppTextPrimary) },
              onClick = {
                showOverflowMenu = false
                onHeaderClick()
              },
            )
            DropdownMenuItem(
              text = {
                Text(
                  stringResource(R.string.chat_encryption_dialog_title),
                  color = WhatsAppTextPrimary,
                )
              },
              onClick = {
                showOverflowMenu = false
                onShowEncryption()
              },
            )
            DropdownMenuItem(
              text = { Text("Clear chat", color = WhatsAppTextPrimary) },
              onClick = {
                showOverflowMenu = false
                onClearChat()
              },
            )
          }
        }
      }
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
    color = ChatEncryptionBannerBg.copy(alpha = 0.92f),
    shape = RoundedCornerShape(10.dp),
    modifier =
      Modifier
        .widthIn(max = 310.dp)
        .clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onLearnMoreClick)
        .testTag("encryption_notice_card"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.Top,
    ) {
      Icon(
        imageVector = LucideIcons.Lock,
        contentDescription = null,
        tint = AccentPurpleBright,
        modifier = Modifier.padding(top = 2.dp).size(12.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
        color = ChatEncryptionBannerText,
        textAlign = TextAlign.Center,
      )
    }
  }
}

private fun bubbleTailShape(
  isOutgoing: Boolean,
  hasTail: Boolean,
  density: Density,
): Shape {
  if (!hasTail) {
    return RoundedCornerShape(14.dp)
  }
  return GenericShape { size, _ ->
    val r = with(density) { 14.dp.toPx() }
    val tailW = with(density) { 8.dp.toPx() }
    val tailTip = with(density) { 2.dp.toPx() }
    val tailH = with(density) { 11.dp.toPx() }

    if (isOutgoing) {
      val bodyRight = size.width - tailW
      moveTo(r, 0f)
      lineTo(size.width - tailTip, 0f)
      quadraticTo(size.width, 0f, bodyRight, tailH)
      lineTo(bodyRight, size.height - r)
      arcTo(Rect(bodyRight - 2 * r, size.height - 2 * r, bodyRight, size.height), 0f, 90f, false)
      lineTo(r, size.height)
      arcTo(Rect(0f, size.height - 2 * r, 2 * r, size.height), 90f, 90f, false)
      lineTo(0f, r)
      arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
      close()
    } else {
      val bodyLeft = tailW
      moveTo(tailTip, 0f)
      lineTo(size.width - r, 0f)
      arcTo(Rect(size.width - 2 * r, 0f, size.width, 2 * r), 270f, 90f, false)
      lineTo(size.width, size.height - r)
      arcTo(Rect(size.width - 2 * r, size.height - 2 * r, size.width, size.height), 0f, 90f, false)
      lineTo(bodyLeft + r, size.height)
      arcTo(Rect(bodyLeft, size.height - 2 * r, bodyLeft + 2 * r, size.height), 90f, 90f, false)
      lineTo(bodyLeft, tailH)
      quadraticTo(0f, 0f, tailTip, 0f)
      close()
    }
  }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ChatBubbleRow(
  message: ChatMessage,
  contact: Contact,
  isFirstInGroup: Boolean,
  onReply: () -> Unit,
) {
  val density = LocalDensity.current
  val bubbleShape = remember(message.isOutgoing, isFirstInGroup, density) {
    bubbleTailShape(isOutgoing = message.isOutgoing, hasTail = isFirstInGroup, density = density)
  }
  val tailInsetStart = if (!message.isOutgoing && isFirstInGroup) 8.dp else 0.dp
  val tailInsetEnd = if (message.isOutgoing && isFirstInGroup) 8.dp else 0.dp
  val horizontalPadStart = if (!message.isOutgoing && !isFirstInGroup) 8.dp else 0.dp
  val horizontalPadEnd = if (message.isOutgoing && !isFirstInGroup) 8.dp else 0.dp

  var showBubbleActions by remember { mutableStateOf(false) }

  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(start = horizontalPadStart, end = horizontalPadEnd),
    contentAlignment = if (message.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart,
  ) {
    Box {
      Surface(
        color = if (message.isOutgoing) AccentPurpleBubble else IncomingBubbleSurface,
        shape = bubbleShape,
        shadowElevation = 0.5.dp,
        modifier =
          Modifier
            .widthIn(max = 300.dp)
            .clip(bubbleShape)
            .combinedClickable(
              onClick = { showBubbleActions = true },
              onLongClick = onReply,
            ),
      ) {
        Column(
          modifier =
            Modifier.padding(
              start = 11.dp + tailInsetStart,
              end = 10.dp + tailInsetEnd,
              top = 7.dp,
              bottom = 6.dp,
            ),
        ) {
          // Quoted reply preview inside bubble (matches screenshot)
          if (!message.replyToText.isNullOrBlank()) {
            Surface(
              color = Color.Black.copy(alpha = 0.24f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            ) {
              Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                  modifier =
                    Modifier
                      .width(3.5.dp)
                      .height(44.dp)
                      .background(AccentPurpleBright),
                )
                Column(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                  Text(
                    text = message.replyToSender ?: contact.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AccentPurpleBright,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = message.replyToText,
                    style = MaterialTheme.typography.bodySmall,
                    color = WhatsAppTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }

          if (message.mediaLabel == "Voice") {
            VoiceNoteBubbleContent(
              message = message,
              contact = contact,
            )
          } else if (message.mediaLabel != null) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 2.dp),
            ) {
              Icon(
                imageVector = LucideIcons.Image,
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
            BubbleMetaRow(message = message, modifier = Modifier.align(Alignment.End).padding(top = 2.dp))
          } else if (message.text.length <= 26 && !message.text.contains('\n')) {
            // Short single-line message: inline timestamp + double checkmarks right next to text
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.Bottom,
            ) {
              Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = WhatsAppTextPrimary,
              )
              BubbleMetaRow(
                message = message,
                modifier = Modifier.align(Alignment.Bottom).padding(top = 4.dp),
              )
            }
          } else {
            Text(
              text = message.text,
              style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp),
              color = WhatsAppTextPrimary,
            )
            BubbleMetaRow(
              message = message,
              modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
            )
          }
        }
      }

      DropdownMenu(
        expanded = showBubbleActions,
        onDismissRequest = { showBubbleActions = false },
        containerColor = WhatsAppSurfaceElevated,
      ) {
        DropdownMenuItem(
          leadingIcon = {
            Icon(
              imageVector = LucideIcons.Reply,
              contentDescription = null,
              tint = AccentPurpleBright,
              modifier = Modifier.size(18.dp),
            )
          },
          text = { Text("Reply", color = WhatsAppTextPrimary) },
          onClick = {
            showBubbleActions = false
            onReply()
          },
        )
      }
    }
  }
}

@Composable
private fun VoiceNoteBubbleContent(
  message: ChatMessage,
  contact: Contact,
) {
  var isPlaying by remember { mutableStateOf(false) }
  var progress by remember { mutableFloatStateOf(0.08f) }

  LaunchedEffect(isPlaying) {
    while (isPlaying) {
      delay(80L)
      progress += 0.03f
      if (progress >= 1f) {
        progress = 0.08f
        isPlaying = false
      }
    }
  }

  Row(
    modifier = Modifier.widthIn(min = 225.dp, max = 265.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(
      onClick = { isPlaying = !isPlaying },
      modifier = Modifier.size(36.dp),
    ) {
      Icon(
        imageVector = if (isPlaying) LucideIcons.Pause else LucideIcons.Play,
        contentDescription = if (isPlaying) "Pause voice note" else "Play voice note",
        tint = WhatsAppTextSecondary,
        modifier = Modifier.size(18.dp),
      )
    }

    Spacer(modifier = Modifier.width(4.dp))

    Column(modifier = Modifier.weight(1f)) {
      Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
        val barCount = VoiceWaveformHeights.size
        val step = size.width / barCount
        val barWidth = (step * 0.48f).coerceAtLeast(2.dp.toPx())
        val activeIndex = (progress * barCount).toInt()

        for (i in 0 until barCount) {
          val x = i * step + step * 0.25f
          val barH = (size.height * VoiceWaveformHeights[i]).coerceAtLeast(3.dp.toPx())
          val y = (size.height - barH) / 2f
          drawRoundRect(
            color =
              if (i <= activeIndex) {
                AccentPurpleBright
              } else {
                WhatsAppTextSecondary.copy(alpha = 0.55f)
              },
            topLeft = Offset(x, y),
            size = Size(barWidth, barH),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
          )
        }

        val thumbX = (size.width * progress).coerceIn(5.dp.toPx(), size.width - 5.dp.toPx())
        drawCircle(
          color = AccentPurpleBright,
          radius = 5.dp.toPx(),
          center = Offset(thumbX, size.height / 2f),
        )
      }

      Spacer(modifier = Modifier.height(2.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "0:06",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
          color = WhatsAppTextSecondary,
        )
        BubbleMetaRow(message = message)
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Box(modifier = Modifier.size(42.dp)) {
      Box(
        modifier =
          Modifier
            .size(40.dp)
            .align(Alignment.Center)
            .clip(CircleShape)
            .background(AccentPurpleSoftDark),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = if (message.isOutgoing) "ME" else contact.initials.ifEmpty { "AI" },
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = AccentPurpleMuted,
        )
      }
      Icon(
        imageVector = LucideIcons.Mic,
        contentDescription = null,
        tint = AccentPurpleBright,
        modifier =
          Modifier
            .size(16.dp)
            .align(Alignment.BottomStart)
            .clip(CircleShape)
            .background(IncomingBubbleSurface)
            .padding(2.dp),
      )
    }
  }
}

@Composable
private fun BubbleMetaRow(
  message: ChatMessage,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = message.timestampLabel,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
      color =
        if (message.isOutgoing) {
          AccentPurpleMuted.copy(alpha = 0.85f)
        } else {
          WhatsAppTextSecondary
        },
    )
    if (message.isOutgoing) {
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        imageVector = LucideIcons.CheckCheck,
        contentDescription = null,
        tint = AccentPurpleBright,
        modifier = Modifier.size(14.dp),
      )
    }
  }
}

@Composable
private fun ReplyComposerPreview(
  message: ChatMessage,
  contactName: String,
  onCancel: () -> Unit,
) {
  Surface(
    color = WhatsAppSurfaceCard.copy(alpha = 0.96f),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(0.5.dp, WhatsAppOutlineSubtle),
    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .width(3.5.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(AccentPurpleBright),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (message.isOutgoing) "You" else contactName,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = AccentPurpleBright,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = message.text,
          style = MaterialTheme.typography.bodySmall,
          color = WhatsAppTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      IconButton(
        onClick = onCancel,
        modifier = Modifier.size(32.dp),
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
private fun MediaTooltipBubble(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.End,
  ) {
    Row(
      modifier =
        Modifier
          .clip(RoundedCornerShape(50))
          .background(AccentPurple)
          .padding(start = 14.dp, end = 8.dp, top = 7.dp, bottom = 7.dp)
          .testTag("media_tooltip_banner"),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.chat_tooltip_media),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
        color = OnAccentPurple,
      )
      Spacer(modifier = Modifier.width(8.dp))
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

    Canvas(modifier = Modifier.padding(end = 68.dp).size(width = 12.dp, height = 6.dp)) {
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
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(7.dp),
  ) {
    // Floating pill composer matching screenshot: [ 😊  Message   📎  📷 ]
    Row(
      modifier =
        Modifier
          .weight(1f)
          .height(48.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(IncomingBubbleSurface)
          .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onEmojiToggle,
        modifier = Modifier.size(36.dp).testTag("chat_emoji_button"),
      ) {
        Icon(
          imageVector = LucideIcons.Smile,
          contentDescription = stringResource(R.string.cd_emoji_stickers),
          tint = WhatsAppTextSecondary,
          modifier = Modifier.size(22.dp),
        )
      }

      Box(
        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        if (inputText.isEmpty()) {
          Text(
            text = stringResource(R.string.chat_input_placeholder),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = WhatsAppTextSecondary,
          )
        }
        BasicTextField(
          value = inputText,
          onValueChange = onInputChange,
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = WhatsAppTextPrimary),
          cursorBrush = SolidColor(AccentPurpleBright),
          modifier = Modifier.fillMaxWidth().testTag("chat_message_input"),
        )
      }

      IconButton(
        onClick = onPlusClick,
        modifier = Modifier.size(36.dp).testTag("chat_attach_plus_button"),
      ) {
        Icon(
          imageVector = LucideIcons.Paperclip,
          contentDescription = stringResource(R.string.cd_attach_media),
          tint = WhatsAppTextSecondary,
          modifier = Modifier.size(20.dp),
        )
      }

      if (inputText.isBlank()) {
        IconButton(
          onClick = onCameraClick,
          modifier = Modifier.size(36.dp).testTag("chat_camera_button"),
        ) {
          Icon(
            imageVector = LucideIcons.Camera,
            contentDescription = stringResource(R.string.cd_camera_action),
            tint = WhatsAppTextSecondary,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }

    // Floating circular Mic / Send button matching screenshot
    if (inputText.isBlank()) {
      Box(
        modifier =
          Modifier
            .minimumInteractiveComponentSize()
            .size(48.dp)
            .clip(CircleShape)
            .background(AccentPurple)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(bounded = true),
              onClick = onMicClick,
            )
            .testTag("chat_mic_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = LucideIcons.Mic,
          contentDescription = stringResource(R.string.cd_voice_note),
          tint = OnAccentPurple,
          modifier = Modifier.size(22.dp),
        )
      }
    } else {
      Box(
        modifier =
          Modifier
            .minimumInteractiveComponentSize()
            .size(48.dp)
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
          modifier = Modifier.size(21.dp),
        )
      }
    }
  }
}

@Composable
private fun WhatsAppDarkDoodleWallpaper(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier.background(WhatsAppBackground)) {
    val spacingX = 64.dp.toPx()
    val spacingY = 64.dp.toPx()
    val cols = (size.width / spacingX).toInt() + 2
    val rows = (size.height / spacingY).toInt() + 2
    val stroke = Stroke(width = 1.25.dp.toPx(), cap = StrokeCap.Round)

    for (r in 0..rows) {
      for (c in 0..cols) {
        val offsetX = if (r % 2 == 1) spacingX * 0.5f else 0f
        val x = c * spacingX + offsetX
        val y = r * spacingY
        when ((r + c) % 4) {
          0 -> {
            drawCircle(
              color = ChatWallpaperDoodle,
              radius = 9.dp.toPx(),
              center = Offset(x, y),
              style = stroke,
            )
          }
          1 -> {
            val s = 7.dp.toPx()
            drawLine(ChatWallpaperDoodle, Offset(x - s, y), Offset(x + s, y), strokeWidth = stroke.width)
            drawLine(ChatWallpaperDoodle, Offset(x, y - s), Offset(x, y + s), strokeWidth = stroke.width)
          }
          2 -> {
            val path =
              Path().apply {
                moveTo(x - 8.dp.toPx(), y)
                quadraticTo(x, y - 9.dp.toPx(), x + 8.dp.toPx(), y)
              }
            drawPath(path, ChatWallpaperDoodle, style = stroke)
          }
          else -> {
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
