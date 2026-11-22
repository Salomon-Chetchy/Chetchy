package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import com.example.audio.VoicePlaybackController
import com.example.audio.VoicePlaybackState
import com.example.audio.VoiceRecorderController
import com.example.audio.formatVoiceDuration
import com.example.ui.components.ChatBubbleDocumentCard
import com.example.ui.components.ChatBubbleImage
import com.example.ui.components.ChatMarkdownBlock
import com.example.ui.components.DocumentContentViewerDialog
import com.example.ui.components.FullScreenImageViewerDialog
import com.example.ui.components.PendingDocumentAttachmentBar
import com.example.ui.components.PendingPhotoAttachmentBar
import com.example.ui.components.RichChatMessageContent
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.parseChatMarkdown
import com.example.ui.components.splitChatMarkdownBlocks
import com.example.ui.components.stripChatMarkdown
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
  onSendVoiceNote: (filePath: String, durationMillis: Long, waveformBars: List<Float>) -> Unit = { _, _, _ -> },
  onShowVoiceNotice: (String) -> Unit = {},
  onDismissTooltip: () -> Unit,
  onStartCall: (String) -> Unit,
  onEndCall: () -> Unit,
  onUpdateGoalProgress: (String, Int) -> Unit,
  isAiThinking: Boolean = false,
  isAiGeneratingImage: Boolean = false,
  pendingPhotoUri: Uri? = null,
  onClearPendingPhoto: () -> Unit = {},
  pendingDocument: PendingDocumentAttachment? = null,
  onAttachDocument: () -> Unit = {},
  onClearPendingDocument: () -> Unit = {},
  replyingToMessage: ChatMessage? = null,
  onReplyToMessage: (ChatMessage) -> Unit = {},
  onCancelReply: () -> Unit = {},
  onClearChat: () -> Unit = {},
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  BackHandler(onBack = onBack)

  val context = LocalContext.current
  var showEncryptionDialog by remember { mutableStateOf(false) }
  var showContactInfoSheet by remember { mutableStateOf(false) }
  var showEmojiStrip by remember { mutableStateOf(false) }
  var previewImageMessage by remember { mutableStateOf<ChatMessage?>(null) }
  var previewDocumentMessage by remember { mutableStateOf<ChatMessage?>(null) }
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  val recorderController = remember { VoiceRecorderController() }
  val playbackController = remember(coroutineScope) { VoicePlaybackController(coroutineScope) }
  val playbackState by playbackController.state.collectAsStateWithLifecycle()

  var isRecordingVoice by remember { mutableStateOf(false) }
  var recordingElapsedMs by remember { mutableLongStateOf(0L) }
  val liveRecordingWaveform = remember { mutableStateListOf<Float>() }
  var startRecordingAfterPermission by remember { mutableStateOf(false) }

  fun beginVoiceRecording() {
    playbackController.stop()
    if (recorderController.startRecording(context)) {
      liveRecordingWaveform.clear()
      recordingElapsedMs = 0L
      isRecordingVoice = true
    } else {
      onShowVoiceNotice(context.getString(R.string.voice_too_short))
    }
  }

  val micPermissionLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
      if (granted) {
        if (startRecordingAfterPermission) {
          startRecordingAfterPermission = false
          beginVoiceRecording()
        }
      } else {
        startRecordingAfterPermission = false
        onShowVoiceNotice(context.getString(R.string.voice_permission_denied))
      }
    }

  LaunchedEffect(isRecordingVoice) {
    while (isRecordingVoice) {
      recordingElapsedMs = recorderController.elapsedMillis()
      val amp = recorderController.sampleNormalizedAmplitude()
      liveRecordingWaveform.add(amp)
      if (liveRecordingWaveform.size > 32) {
        liveRecordingWaveform.removeAt(0)
      }
      delay(80L)
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      recorderController.cancelRecording()
      playbackController.stop()
    }
  }

  val showScrollToBottom by remember {
    derivedStateOf { listState.canScrollForward }
  }

  LaunchedEffect(messages.size, isAiThinking, isAiGeneratingImage) {
    val extraItems = if (isAiGeneratingImage) 1 else 0
    val targetIndex = messages.size + 1 + extraItems
    if (messages.isNotEmpty() || isAiThinking || isAiGeneratingImage) {
      listState.animateScrollToItem(targetIndex)
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = WhatsAppBackground,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                playbackState = playbackState,
                onToggleVoicePlay = {
                  playbackController.togglePlayback(
                    messageId = message.id,
                    audioPath = message.audioFilePath,
                    fallbackDurationMs = message.audioDurationMs,
                  )
                },
                onReply = { onReplyToMessage(message) },
                onImageClick = { previewImageMessage = message },
                onDocumentClick = { previewDocumentMessage = message },
              )
            }

            if (isAiGeneratingImage) {
              item(key = "ai_generating_image_indicator") {
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
                    modifier = Modifier.testTag("ai_generating_image_bubble"),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      Icon(
                        imageVector = LucideIcons.Sparkles,
                        contentDescription = null,
                        tint = AccentPurpleBright,
                        modifier = Modifier.size(16.dp),
                      )
                      Text(
                        text = stringResource(R.string.ai_generating_image),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentPurpleMuted,
                      )
                    }
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

          if (pendingPhotoUri != null) {
            PendingPhotoAttachmentBar(
              photoUri = pendingPhotoUri,
              onClear = onClearPendingPhoto,
            )
          }

          if (pendingDocument != null) {
            PendingDocumentAttachmentBar(
              document = pendingDocument,
              onClear = onClearPendingDocument,
            )
          }

          if (replyingToMessage != null) {
            ReplyComposerPreview(
              message = replyingToMessage,
              contactName = contact.displayName,
              onCancel = onCancelReply,
            )
          }

          AnimatedVisibility(
            visible = isAiThinking,
            enter =
              fadeIn(tween(180)) +
                expandVertically(tween(200), expandFrom = Alignment.Bottom) +
                slideInVertically(tween(220)) { it / 2 },
            exit =
              fadeOut(tween(140)) +
                shrinkVertically(tween(160), shrinkTowards = Alignment.Bottom) +
                slideOutVertically(tween(160)) { it / 2 },
          ) {
            TypingIndicatorBar(contact = contact)
          }

          AnimatedContent(
            targetState = isRecordingVoice,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(120)) },
            label = "composer_voice_switch",
          ) { recording ->
            if (recording) {
              VoiceRecordingBottomBar(
                elapsedMillis = recordingElapsedMs,
                liveWaveform = liveRecordingWaveform.toList(),
                onCancel = {
                  recorderController.cancelRecording()
                  isRecordingVoice = false
                },
                onSend = {
                  val recorded = recorderController.stopAndSave()
                  isRecordingVoice = false
                  if (recorded != null) {
                    onSendVoiceNote(
                      recorded.filePath,
                      recorded.durationMillis,
                      recorded.waveformBars,
                    )
                  } else {
                    onShowVoiceNotice(context.getString(R.string.voice_too_short))
                  }
                },
              )
            } else {
              ChatBottomInputBar(
                inputText = inputText,
                hasAttachment = pendingPhotoUri != null || pendingDocument != null,
                onInputChange = onInputChange,
                onAttachPhoto = {
                  onDismissTooltip()
                  onAttachMedia()
                },
                onAttachDocument = {
                  onDismissTooltip()
                  onAttachDocument()
                },
                onEmojiToggle = { showEmojiStrip = !showEmojiStrip },
                onCameraClick = {
                  onDismissTooltip()
                  onAttachMedia()
                },
                onMicClick = {
                  onDismissTooltip()
                  val hasPermission =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                      PackageManager.PERMISSION_GRANTED
                  if (hasPermission) {
                    beginVoiceRecording()
                  } else {
                    startRecordingAfterPermission = true
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                  }
                },
                onSendClick = onSendMessage,
              )
            }
          }
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

  previewImageMessage?.let { previewMsg ->
    val uri = previewMsg.imageUri
    if (!uri.isNullOrBlank()) {
      FullScreenImageViewerDialog(
        imageUri = uri,
        caption = stripChatMarkdown(previewMsg.text),
        isAiGenerated = previewMsg.mediaLabel == "AI Image",
        onDismiss = { previewImageMessage = null },
      )
    }
  }

  previewDocumentMessage?.let { docMsg ->
    val docName = docMsg.documentName
    if (!docName.isNullOrBlank()) {
      val metaParts =
        buildList {
          docMsg.documentExtension?.takeIf { it.isNotBlank() }?.let { add(it) }
          docMsg.documentSizeLabel?.takeIf { it.isNotBlank() }?.let { add(it) }
          if (docMsg.documentPageCount > 0) {
            add(if (docMsg.documentPageCount == 1) "1 page" else "${docMsg.documentPageCount} pages")
          }
        }
      DocumentContentViewerDialog(
        fileName = docName,
        metaLine = metaParts.joinToString(" • "),
        extractedText = docMsg.documentExtractedText.orEmpty(),
        onDismiss = { previewDocumentMessage = null },
      )
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
  playbackState: VoicePlaybackState,
  onToggleVoicePlay: () -> Unit,
  onReply: () -> Unit,
  onImageClick: (ChatMessage) -> Unit = {},
  onDocumentClick: (ChatMessage) -> Unit = {},
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val bubbleShape = remember(message.isOutgoing, isFirstInGroup, density) {
    bubbleTailShape(isOutgoing = message.isOutgoing, hasTail = isFirstInGroup, density = density)
  }
  val hasCodeBlock = remember(message.text) {
    splitChatMarkdownBlocks(message.text).any { it is ChatMarkdownBlock.Code }
  }
  val imageUri = message.imageUri?.takeIf { it.isNotBlank() }
  val docName = message.documentName?.takeIf { it.isNotBlank() }
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
            .widthIn(max = if (hasCodeBlock) 340.dp else if (imageUri != null || docName != null) 310.dp else 300.dp)
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
                    text = stripChatMarkdown(message.replyToText),
                    style = MaterialTheme.typography.bodySmall,
                    color = WhatsAppTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }

          if (imageUri != null) {
            ChatBubbleImage(
              imageUri = imageUri,
              contentDescription = stripChatMarkdown(message.text).ifBlank { contact.displayName },
              isAiGenerated = message.mediaLabel == "AI Image",
              onClick = { onImageClick(message) },
            )
            if (message.text.isNotBlank() && message.text != "Photo") {
              val formattedCaption =
                remember(message.text, message.isOutgoing) {
                  parseChatMarkdown(
                    rawText = message.text,
                    codeBackground = Color(0x550D1117),
                    codeTextColor = AccentPurpleMuted,
                    accentColor = if (message.isOutgoing) OnAccentPurple else AccentPurpleBright,
                    quoteColor = WhatsAppTextSecondary,
                  )
                }
              Text(
                text = formattedCaption,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp, lineHeight = 19.5.sp),
                color = WhatsAppTextPrimary,
                modifier = Modifier.padding(top = 6.dp),
              )
            }
            BubbleMetaRow(message = message, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
          } else if (docName != null) {
            ChatBubbleDocumentCard(
              fileName = docName,
              extension = message.documentExtension ?: "FILE",
              sizeLabel = message.documentSizeLabel,
              pageCount = message.documentPageCount,
              onClick = { onDocumentClick(message) },
            )
            if (message.text.isNotBlank()) {
              val formattedCaption =
                remember(message.text, message.isOutgoing) {
                  parseChatMarkdown(
                    rawText = message.text,
                    codeBackground = Color(0x550D1117),
                    codeTextColor = AccentPurpleMuted,
                    accentColor = if (message.isOutgoing) OnAccentPurple else AccentPurpleBright,
                    quoteColor = WhatsAppTextSecondary,
                  )
                }
              Text(
                text = formattedCaption,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp, lineHeight = 19.5.sp),
                color = WhatsAppTextPrimary,
                modifier = Modifier.padding(top = 6.dp),
              )
            }
            BubbleMetaRow(message = message, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
          } else if (message.mediaLabel == "Voice") {
            VoiceNoteBubbleContent(
              message = message,
              contact = contact,
              playbackState = playbackState,
              onTogglePlay = onToggleVoicePlay,
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
          } else if (!hasCodeBlock) {
            val formattedText =
              remember(message.text, message.isOutgoing) {
                parseChatMarkdown(
                  rawText = message.text,
                  codeBackground = Color(0x550D1117),
                  codeTextColor = AccentPurpleMuted,
                  accentColor = if (message.isOutgoing) OnAccentPurple else AccentPurpleBright,
                  quoteColor = WhatsAppTextSecondary,
                )
              }
            if (formattedText.text.length <= 26 && !formattedText.text.contains('\n')) {
              // Short single-line message: inline timestamp + double checkmarks right next to text
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.Bottom,
              ) {
                Text(
                  text = formattedText,
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
                text = formattedText,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.5.sp),
                color = WhatsAppTextPrimary,
              )
              BubbleMetaRow(
                message = message,
                modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
              )
            }
          } else {
            RichChatMessageContent(
              rawText = message.text,
              isOutgoing = message.isOutgoing,
              modifier = Modifier.fillMaxWidth(),
            )
            BubbleMetaRow(
              message = message,
              modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
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
        DropdownMenuItem(
          leadingIcon = {
            Icon(
              imageVector = LucideIcons.Copy,
              contentDescription = null,
              tint = AccentPurpleBright,
              modifier = Modifier.size(18.dp),
            )
          },
          text = { Text("Copy message", color = WhatsAppTextPrimary) },
          onClick = {
            showBubbleActions = false
            copyTextToClipboard(context = context, label = "message", text = message.text)
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
  playbackState: VoicePlaybackState,
  onTogglePlay: () -> Unit,
) {
  val isLoaded = playbackState.playingMessageId == message.id
  val isPlaying = isLoaded && playbackState.isPlaying
  val progress = if (isLoaded) playbackState.progress else 0f
  val bars = remember(message.audioWaveform) {
    if (message.audioWaveform.isNotEmpty()) message.audioWaveform else VoiceWaveformHeights
  }
  val totalMs =
    when {
      isLoaded && playbackState.durationMs > 0L -> playbackState.durationMs
      message.audioDurationMs > 0L -> message.audioDurationMs
      else -> 6000L
    }
  val durationText =
    if (isLoaded && playbackState.positionMs > 0L) {
      formatVoiceDuration(playbackState.positionMs)
    } else {
      formatVoiceDuration(totalMs)
    }

  Row(
    modifier = Modifier.widthIn(min = 225.dp, max = 265.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(
      onClick = onTogglePlay,
      modifier = Modifier.size(36.dp).testTag("voice_play_button"),
    ) {
      Icon(
        imageVector = if (isPlaying) LucideIcons.Pause else LucideIcons.Play,
        contentDescription =
          if (isPlaying) stringResource(R.string.voice_pause_cd) else stringResource(R.string.voice_play_cd),
        tint = WhatsAppTextSecondary,
        modifier = Modifier.size(18.dp),
      )
    }

    Spacer(modifier = Modifier.width(4.dp))

    Column(modifier = Modifier.weight(1f)) {
      Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
        val barCount = bars.size.coerceAtLeast(1)
        val step = size.width / barCount
        val barWidth = (step * 0.48f).coerceAtLeast(2.dp.toPx())
        val activeIndex = (progress * barCount).toInt()

        for (i in 0 until barCount) {
          val x = i * step + step * 0.25f
          val barH = (size.height * bars[i]).coerceAtLeast(3.dp.toPx())
          val y = (size.height - barH) / 2f
          drawRoundRect(
            color =
              if (i < activeIndex) {
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
          text = durationText,
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
          text = stripChatMarkdown(message.text),
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
private fun TypingIndicatorBar(
  contact: Contact,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "typing_indicator_transition")
  val dotPhases =
    listOf(0, 150, 300).map { delayMs ->
      infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMs),
          ),
        label = "typing_dot_$delayMs",
      )
    }
  val density = LocalDensity.current
  val maxBouncePx = with(density) { 4.dp.toPx() }
  val initials = contact.initials.ifEmpty { contact.displayName.take(1).uppercase() }

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 2.dp)
        .testTag("typing_indicator"),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
      color = IncomingBubbleSurface.copy(alpha = 0.96f),
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(0.5.dp, WhatsAppOutlineSubtle),
      shadowElevation = 2.dp,
    ) {
      Row(
        modifier = Modifier.padding(start = 8.dp, end = 13.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (initials.isNotEmpty()) {
          Box(
            modifier =
              Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(AccentPurpleSoftDark),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = initials,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
              color = AccentPurpleMuted,
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          dotPhases.forEach { phaseState ->
            val t = phaseState.value
            Box(
              modifier =
                Modifier
                  .size(6.dp)
                  .graphicsLayer {
                    translationY = (0.5f - t) * maxBouncePx
                    val scale = 0.8f + 0.35f * t
                    scaleX = scale
                    scaleY = scale
                    alpha = 0.35f + 0.65f * t
                  }
                  .clip(CircleShape)
                  .background(AccentPurpleBright),
            )
          }
        }

        Text(
          text = stringResource(R.string.ai_typing),
          style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
          color = WhatsAppTextSecondary,
        )
      }
    }
  }
}

@Composable
private fun VoiceRecordingBottomBar(
  elapsedMillis: Long,
  liveWaveform: List<Float>,
  onCancel: () -> Unit,
  onSend: () -> Unit,
) {
  val pulseTransition = rememberInfiniteTransition(label = "voice_rec_pulse")
  val dotAlpha by pulseTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0.25f,
    animationSpec = infiniteRepeatable(animation = tween(550), repeatMode = RepeatMode.Reverse),
    label = "voice_rec_dot_alpha",
  )

  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 7.dp)
        .testTag("voice_recording_bar"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(7.dp),
  ) {
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
        onClick = onCancel,
        modifier = Modifier.size(36.dp).testTag("voice_cancel_button"),
      ) {
        Icon(
          imageVector = LucideIcons.Trash2,
          contentDescription = stringResource(R.string.voice_cancel_cd),
          tint = Color(0xFFEF5350),
          modifier = Modifier.size(20.dp),
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      Box(
        modifier =
          Modifier
            .size(9.dp)
            .graphicsLayer { alpha = dotAlpha }
            .clip(CircleShape)
            .background(Color(0xFFEF5350)),
      )

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = formatVoiceDuration(elapsedMillis),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
        color = WhatsAppTextPrimary,
        modifier = Modifier.testTag("voice_recording_timer"),
      )

      Spacer(modifier = Modifier.width(10.dp))

      Canvas(modifier = Modifier.weight(1f).height(24.dp)) {
        val barCount = 24
        val step = size.width / barCount
        val barWidth = (step * 0.48f).coerceAtLeast(2.dp.toPx())
        val offset = (barCount - liveWaveform.size).coerceAtLeast(0)
        for (i in 0 until barCount) {
          val amp =
            if (i >= offset && (i - offset) < liveWaveform.size) {
              liveWaveform[i - offset]
            } else {
              0.2f
            }
          val barH = (size.height * amp).coerceAtLeast(3.dp.toPx())
          val x = i * step + step * 0.25f
          val y = (size.height - barH) / 2f
          drawRoundRect(
            color = if (i >= offset) AccentPurpleBright else WhatsAppTextSecondary.copy(alpha = 0.35f),
            topLeft = Offset(x, y),
            size = Size(barWidth, barH),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
          )
        }
      }
    }

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
            onClick = onSend,
          )
          .testTag("voice_send_button"),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = LucideIcons.SendHorizontal,
        contentDescription = stringResource(R.string.voice_send_cd),
        tint = OnAccentPurple,
        modifier = Modifier.size(21.dp),
      )
    }
  }
}

@Composable
private fun ChatBottomInputBar(
  inputText: String,
  hasAttachment: Boolean = false,
  onInputChange: (String) -> Unit,
  onAttachPhoto: () -> Unit,
  onAttachDocument: () -> Unit,
  onEmojiToggle: () -> Unit,
  onCameraClick: () -> Unit,
  onMicClick: () -> Unit,
  onSendClick: () -> Unit,
) {
  var showAttachMenu by remember { mutableStateOf(false) }

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

      Box {
        IconButton(
          onClick = { showAttachMenu = true },
          modifier = Modifier.size(36.dp).testTag("chat_attach_plus_button"),
        ) {
          Icon(
            imageVector = LucideIcons.Paperclip,
            contentDescription = stringResource(R.string.cd_attach_media),
            tint = WhatsAppTextSecondary,
            modifier = Modifier.size(20.dp),
          )
        }
        DropdownMenu(
          expanded = showAttachMenu,
          onDismissRequest = { showAttachMenu = false },
          containerColor = WhatsAppSurfaceElevated,
        ) {
          DropdownMenuItem(
            leadingIcon = {
              Icon(
                imageVector = LucideIcons.FileText,
                contentDescription = null,
                tint = AccentPurpleBright,
                modifier = Modifier.size(18.dp),
              )
            },
            text = {
              Text(
                text = stringResource(R.string.attach_document_label),
                color = WhatsAppTextPrimary,
              )
            },
            onClick = {
              showAttachMenu = false
              onAttachDocument()
            },
            modifier = Modifier.testTag("attach_document_option"),
          )
          DropdownMenuItem(
            leadingIcon = {
              Icon(
                imageVector = LucideIcons.Image,
                contentDescription = null,
                tint = AccentPurpleBright,
                modifier = Modifier.size(18.dp),
              )
            },
            text = {
              Text(
                text = stringResource(R.string.attach_photo_label),
                color = WhatsAppTextPrimary,
              )
            },
            onClick = {
              showAttachMenu = false
              onAttachPhoto()
            },
            modifier = Modifier.testTag("attach_photo_option"),
          )
        }
      }

      if (inputText.isBlank() && !hasAttachment) {
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
    if (inputText.isBlank() && !hasAttachment) {
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
