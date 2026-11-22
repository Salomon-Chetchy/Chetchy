package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.R
import com.example.ai.PuterImageStorage
import com.example.ai.PuterPreferences
import com.example.ui.components.ActiveChatsListContent
import com.example.ui.components.ChetchyBottomNavigationBar
import com.example.ui.components.ChetchyNavigationRail
import com.example.ui.components.ChetchyTopBar
import com.example.ui.components.NewChatSheetContent
import com.example.ui.components.NewContactSheetContent
import com.example.ui.components.PuterAuthTokenDialog
import com.example.ui.components.PuterConnectPromptDialog
import com.example.ui.components.PuterWebBridgeHost
import com.example.ui.components.StartChattingIllustration
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.OnAccentPurple
import com.example.ui.theme.WhatsAppBackground
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppSurfaceElevated
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
  modifier: Modifier = Modifier,
  viewModel: ChatsViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  var showPuterTokenDialogFromPrompt by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    val savedModel = PuterPreferences.getSelectedModel(context)
    val savedToken = PuterPreferences.getAuthToken(context)
    val savedUser = PuterPreferences.getUsername(context)
    viewModel.syncPuterPreferences(savedModel, savedToken, savedUser)
  }

  LaunchedEffect(uiState.puterAuthToken, uiState.puterUsername) {
    if (uiState.puterAuthToken.isNotBlank()) {
      PuterPreferences.saveAuthToken(context, uiState.puterAuthToken)
    }
    if (!uiState.puterUsername.isNullOrBlank()) {
      PuterPreferences.saveUsername(context, uiState.puterUsername)
    }
  }

  val photoPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia(),
      onResult = { uri -> viewModel.onPhotoPicked(uri) },
    )

  LaunchedEffect(uiState.snackbarMessage) {
    val msg = uiState.snackbarMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  PuterWebBridgeHost(controller = viewModel.puterBridgeController)

  val shareInvite = {
    val sendIntent =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.invite_share_subject))
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.invite_share_text))
      }
    val chooser = Intent.createChooser(sendIntent, context.getString(R.string.invite_chooser_title))
    context.startActivity(chooser)
  }

  val activeChatContact = uiState.activeChatContact
  val activeChannel = uiState.activeChannel

  AnimatedContent(
    targetState =
      when {
        activeChatContact != null -> "chat:${activeChatContact.id}"
        activeChannel != null -> "channel:${activeChannel.id}"
        else -> "home"
      },
    transitionSpec = {
      if (targetState != "home") {
        (slideInHorizontally { it / 3 } + fadeIn(tween(220))) togetherWith
          (fadeOut(tween(150)))
      } else {
        fadeIn(tween(200)) togetherWith
          (slideOutHorizontally { it / 3 } + fadeOut(tween(180)))
      }
    },
    label = "chats_to_detail",
    modifier = modifier.fillMaxSize(),
  ) { screenKey ->
    when {
      screenKey.startsWith("chat:") && activeChatContact != null -> {
        ChatConversationScreen(
          contact = activeChatContact,
          messages = uiState.activeMessages,
          inputText = uiState.chatInputText,
          showMediaTooltip = uiState.showMediaTooltip,
          activeCallType = uiState.activeCallType,
          onBack = viewModel::closeChatScreen,
          onInputChange = viewModel::updateChatInput,
          onAppendEmoji = viewModel::appendEmoji,
          onSendMessage = {
            viewModel.sendMessage(
              imageEncoder = { uri -> PuterImageStorage.encodeUriToDataUrl(context, uri) },
              imagePersister = { raw -> PuterImageStorage.persistGeneratedImageIfDataUri(context, raw) },
            )
          },
          onAttachMedia = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onVoiceNote = viewModel::sendVoiceNotePlaceholder,
          onDismissTooltip = viewModel::dismissMediaTooltip,
          onStartCall = viewModel::startCall,
          onEndCall = viewModel::endCall,
          onUpdateGoalProgress = viewModel::updateGoalProgress,
          isAiThinking = uiState.aiThinkingContactId == activeChatContact.id,
          isAiGeneratingImage = uiState.aiGeneratingImageContactId == activeChatContact.id,
          pendingPhotoUri = uiState.pendingPhotoUri,
          pendingPhotoIdentifyWithAi = uiState.pendingPhotoIdentifyWithAi,
          onClearPendingPhoto = viewModel::clearPendingPhoto,
          onTogglePendingPhotoIdentify = viewModel::togglePendingPhotoIdentify,
          onGenerateAiImage = { prompt ->
            viewModel.generateAiImage(
              prompt = prompt,
              imagePersister = { raw -> PuterImageStorage.persistGeneratedImageIfDataUri(context, raw) },
            )
          },
          replyingToMessage = uiState.replyingToMessage,
          onReplyToMessage = viewModel::startReplyingTo,
          onCancelReply = viewModel::cancelReply,
          onClearChat = viewModel::clearActiveChat,
        )
      }
      screenKey.startsWith("channel:") && activeChannel != null -> {
        ChannelFeedScreen(
          channel = activeChannel,
          onPost = { text -> viewModel.postToChannel(activeChannel.id, text) },
          onBack = viewModel::closeChannel,
        )
      }
      else -> {
        ChatsHomeContainer(
          uiState = uiState,
          snackbarHostState = snackbarHostState,
          onSelectTab = viewModel::selectTab,
          onOpenCamera = viewModel::openCameraSheet,
          onOpenNewChat = viewModel::openNewChatSheet,
          onOpenConversation = viewModel::openChatWithContact,
          onShareInvite = shareInvite,
          onChatListSearchChange = viewModel::updateChatListSearchQuery,
          onMyStatusClick = {
            if (uiState.myStatuses.isEmpty()) {
              viewModel.openStatusTypeChooser()
            } else {
              viewModel.openStatusViewer(0)
            }
          },
          onAddPhotoStatusClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onAddTextStatusClick = viewModel::openTextStatusComposer,
          onOpenStatusAt = viewModel::openStatusViewer,
          onCreateChannelClick = viewModel::openCreateChannelSheet,
          onOpenChannelClick = { ch -> viewModel.openChannel(ch.id) },
          installedVersionName = BuildConfig.VERSION_NAME,
          installedBuildNumber = viewModel.installedDisplayBuild,
          onSyncSavedRepo = { saved -> viewModel.setGithubRepoSlug(saved, resetStatus = false) },
          onSaveRepo = { updated -> viewModel.setGithubRepoSlug(updated, resetStatus = true) },
          onCheckUpdates = viewModel::checkForUpdates,
          onSignInWithPuter = { viewModel.puterBridgeController.triggerInteractiveSignIn() },
          onSavePuterToken = viewModel::setPuterAuthToken,
          onSelectPuterModel = viewModel::setPuterModel,
          onDisconnectPuter = viewModel::disconnectPuter,
          onShowSnackbar = viewModel::showSnackbar,
        )
      }
    }
  }

  val viewingIndex = uiState.viewingStatusIndex
  if (viewingIndex != null && uiState.myStatuses.isNotEmpty()) {
    StatusViewerOverlay(
      statuses = uiState.myStatuses,
      currentIndex = viewingIndex,
      onStep = viewModel::stepStatusViewer,
      onDelete = viewModel::deleteStatus,
      onClose = viewModel::closeStatusViewer,
    )
  }

  when (uiState.activeSheet) {
    ActiveSheet.NewChat -> {
      ModalBottomSheet(
        onDismissRequest = viewModel::dismissSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WhatsAppSurface,
        contentColor = WhatsAppTextPrimary,
        dragHandle = null,
      ) {
        Spacer(modifier = Modifier.height(14.dp))
        NewChatSheetContent(
          query = uiState.searchQuery,
          contacts = uiState.filteredContacts,
          onQueryChange = viewModel::updateSearchQuery,
          onNewContactClick = viewModel::openNewContactSheet,
          onContactClick = { contact -> viewModel.openChatWithContact(contact.id) },
          onShareInviteClick = {
            viewModel.dismissSheet()
            shareInvite()
          },
          onClose = viewModel::dismissSheet,
        )
      }
    }
    ActiveSheet.NewContact -> {
      ModalBottomSheet(
        onDismissRequest = viewModel::dismissSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WhatsAppSurface,
        contentColor = WhatsAppTextPrimary,
        dragHandle = null,
      ) {
        Spacer(modifier = Modifier.height(14.dp))
        NewContactSheetContent(
          draft = uiState.newContactDraft,
          onFirstNameChange = viewModel::updateFirstName,
          onLastNameChange = viewModel::updateLastName,
          onCountryChange = viewModel::updateCountry,
          onPhoneChange = viewModel::updatePhone,
          onSyncChange = viewModel::updateSyncToPhone,
          onAiIdentityChange = viewModel::updateAiIdentity,
          onGoalTitleChange = viewModel::updateGoalTitle,
          onCancel = viewModel::cancelNewContact,
          onDone = viewModel::saveNewContactAndOpenChat,
        )
      }
    }
    ActiveSheet.CameraMedia -> {
      ModalBottomSheet(
        onDismissRequest = viewModel::dismissSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WhatsAppSurface,
        contentColor = WhatsAppTextPrimary,
      ) {
        CameraMediaSheetContent(
          hasPickedPhoto = uiState.selectedPhotoUri != null,
          onPickPhoto = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onClose = viewModel::dismissSheet,
        )
      }
    }
    ActiveSheet.StatusTypeChooser -> {
      ModalBottomSheet(
        onDismissRequest = viewModel::dismissSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WhatsAppSurface,
        contentColor = WhatsAppTextPrimary,
        dragHandle = null,
      ) {
        Spacer(modifier = Modifier.height(14.dp))
        StatusTypeChooserContent(
          onWriteTextClick = viewModel::openTextStatusComposer,
          onPickPhotoClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onClose = viewModel::dismissSheet,
        )
      }
    }
    ActiveSheet.TextStatusComposer -> {
      ModalBottomSheet(
        onDismissRequest = viewModel::dismissSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WhatsAppSurface,
        contentColor = WhatsAppTextPrimary,
        dragHandle = null,
      ) {
        Spacer(modifier = Modifier.height(14.dp))
        TextStatusComposerContent(
          onPost = viewModel::postTextStatus,
          onCancel = viewModel::dismissSheet,
        )
      }
    }
    ActiveSheet.CreateChannel -> {
      ModalBottomSheet(
        onDismissRequest = viewModel::dismissSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = WhatsAppSurface,
        contentColor = WhatsAppTextPrimary,
        dragHandle = null,
      ) {
        Spacer(modifier = Modifier.height(14.dp))
        CreateChannelSheetContent(
          onCreate = viewModel::createChannel,
          onCancel = viewModel::dismissSheet,
        )
      }
    }
    null -> Unit
  }

  if (uiState.showPuterConnectPrompt) {
    PuterConnectPromptDialog(
      onDismiss = viewModel::dismissPuterConnectPrompt,
      onSignInWithPuter = { viewModel.puterBridgeController.triggerInteractiveSignIn() },
      onEnterTokenClick = { showPuterTokenDialogFromPrompt = true },
    )
  }

  if (showPuterTokenDialogFromPrompt) {
    PuterAuthTokenDialog(
      currentToken = uiState.puterAuthToken,
      onDismiss = { showPuterTokenDialogFromPrompt = false },
      onSaveToken = { token ->
        PuterPreferences.saveAuthToken(context, token)
        viewModel.setPuterAuthToken(token)
        showPuterTokenDialogFromPrompt = false
      },
    )
  }
}

@Composable
private fun ChatsHomeContainer(
  uiState: ChatsUiState,
  snackbarHostState: SnackbarHostState,
  onSelectTab: (BottomNavTab) -> Unit,
  onOpenCamera: () -> Unit,
  onOpenNewChat: () -> Unit,
  onOpenConversation: (String) -> Unit,
  onShareInvite: () -> Unit,
  onChatListSearchChange: (String) -> Unit,
  onMyStatusClick: () -> Unit,
  onAddPhotoStatusClick: () -> Unit,
  onAddTextStatusClick: () -> Unit,
  onOpenStatusAt: (Int) -> Unit,
  onCreateChannelClick: () -> Unit,
  onOpenChannelClick: (ChannelItem) -> Unit,
  installedVersionName: String,
  installedBuildNumber: Int,
  onSyncSavedRepo: (String) -> Unit,
  onSaveRepo: (String) -> Unit,
  onCheckUpdates: () -> Unit,
  onSignInWithPuter: () -> Unit,
  onSavePuterToken: (String) -> Unit,
  onSelectPuterModel: (String) -> Unit,
  onDisconnectPuter: () -> Unit,
  onShowSnackbar: (String) -> Unit,
) {
  BackHandler(enabled = uiState.selectedTab != BottomNavTab.CHATS) {
    onSelectTab(BottomNavTab.CHATS)
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize().background(WhatsAppBackground)) {
    val isExpandedWidth = maxWidth >= 840.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (isExpandedWidth) {
        ChetchyNavigationRail(
          selectedTab = uiState.selectedTab,
          onSelectTab = onSelectTab,
        )
      }

      Scaffold(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        containerColor = WhatsAppBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
          ChetchyTopBar(
            title = tabTitle(uiState.selectedTab),
            isChatsTab = uiState.selectedTab == BottomNavTab.CHATS,
            onCameraClick = onOpenCamera,
            onNewChatClick = onOpenNewChat,
            onOpenSettingsClick = { onSelectTab(BottomNavTab.SETTINGS) },
          )
        },
        floatingActionButton = {
          if (uiState.selectedTab == BottomNavTab.CHATS) {
            FloatingActionButton(
              onClick = onOpenNewChat,
              shape = RoundedCornerShape(16.dp),
              containerColor = AccentPurple,
              contentColor = OnAccentPurple,
              elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
              modifier = Modifier.size(56.dp).testTag("new_chat_plus_button"),
            ) {
              Icon(
                imageVector = LucideIcons.MessageSquarePlus,
                contentDescription = stringResource(R.string.cd_new_chat_action),
                modifier = Modifier.size(24.dp),
              )
            }
          }
        },
        bottomBar = {
          if (!isExpandedWidth) {
            ChetchyBottomNavigationBar(
              selectedTab = uiState.selectedTab,
              showUpdatesBadge = uiState.myStatuses.isNotEmpty() || uiState.channels.isNotEmpty(),
              onSelectTab = onSelectTab,
            )
          }
        },
      ) { innerPadding ->
        Box(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentAlignment = Alignment.TopCenter,
        ) {
          Crossfade(
            targetState = uiState.selectedTab,
            animationSpec = tween(durationMillis = 180),
            label = "tab_content_crossfade",
          ) { tab ->
            when (tab) {
              BottomNavTab.CHATS -> {
                ActiveChatsListContent(
                  conversations = uiState.visibleConversations,
                  hasMyStatuses = uiState.myStatuses.isNotEmpty(),
                  searchQuery = uiState.chatListSearchQuery,
                  onSearchQueryChange = onChatListSearchChange,
                  onConversationClick = { contact -> onOpenConversation(contact.id) },
                  onStartChattingClick = onOpenNewChat,
                  onInviteFriendClick = onShareInvite,
                  emptyHeroContent = {
                    ChatsEmptyHeroContent(
                      onStartChattingClick = onOpenNewChat,
                      onInviteFriendClick = onShareInvite,
                    )
                  },
                )
              }
              BottomNavTab.UPDATES ->
                UpdatesTabContent(
                  myStatuses = uiState.myStatuses,
                  channels = uiState.channels,
                  onMyStatusClick = onMyStatusClick,
                  onAddPhotoStatusClick = onAddPhotoStatusClick,
                  onAddTextStatusClick = onAddTextStatusClick,
                  onOpenStatusAt = onOpenStatusAt,
                  onCreateChannelClick = onCreateChannelClick,
                  onOpenChannelClick = onOpenChannelClick,
                )
              BottomNavTab.CALLS ->
                SecondaryTabEmptyContent(
                  icon = LucideIcons.Phone,
                  title = stringResource(R.string.empty_calls_title),
                  subtitle = stringResource(R.string.empty_calls_subtitle),
                  actionLabel = stringResource(R.string.nav_chats),
                  onActionClick = { onSelectTab(BottomNavTab.CHATS) },
                )
              BottomNavTab.SETTINGS ->
                SettingsTabContent(
                  installedVersionName = installedVersionName,
                  installedBuildNumber = installedBuildNumber,
                  githubRepoSlug = uiState.githubRepoSlug,
                  updateCheckState = uiState.updateCheckState,
                  puterModelId = uiState.puterModelId,
                  puterAuthToken = uiState.puterAuthToken,
                  puterUsername = uiState.puterUsername,
                  isPuterConnected = uiState.isPuterConnected,
                  onSyncSavedRepo = onSyncSavedRepo,
                  onSaveRepo = onSaveRepo,
                  onCheckUpdates = onCheckUpdates,
                  onSignInWithPuter = onSignInWithPuter,
                  onSavePuterToken = onSavePuterToken,
                  onSelectPuterModel = onSelectPuterModel,
                  onDisconnectPuter = onDisconnectPuter,
                  onShowSnackbar = onShowSnackbar,
                )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun tabTitle(tab: BottomNavTab): String =
  when (tab) {
    BottomNavTab.CHATS -> stringResource(R.string.title_chetchy)
    BottomNavTab.UPDATES -> stringResource(R.string.nav_updates)
    BottomNavTab.CALLS -> stringResource(R.string.nav_calls)
    BottomNavTab.SETTINGS -> stringResource(R.string.nav_settings)
  }

@Composable
private fun ChatsEmptyHeroContent(
  onStartChattingClick: () -> Unit,
  onInviteFriendClick: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp)
        .padding(horizontal = 32.dp, vertical = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    StartChattingIllustration(
      modifier = Modifier.size(width = 188.dp, height = 160.dp)
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = stringResource(R.string.hero_headline),
      style = MaterialTheme.typography.headlineMedium,
      color = WhatsAppTextPrimary,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = stringResource(R.string.hero_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = WhatsAppTextSecondary,
      textAlign = TextAlign.Center,
      modifier = Modifier.widthIn(max = 310.dp),
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onStartChattingClick,
      shape = RoundedCornerShape(50),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = AccentPurple,
          contentColor = OnAccentPurple,
        ),
      contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
      modifier =
        Modifier
          .minimumInteractiveComponentSize()
          .height(48.dp)
          .testTag("start_chatting_button"),
    ) {
      Text(
        text = stringResource(R.string.cta_start_chatting),
        style = MaterialTheme.typography.labelLarge,
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    TextButton(
      onClick = onInviteFriendClick,
      shape = RoundedCornerShape(50),
      colors = ButtonDefaults.textButtonColors(contentColor = AccentPurpleBright),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
      modifier =
        Modifier
          .minimumInteractiveComponentSize()
          .testTag("invite_friend_button"),
    ) {
      Text(
        text = stringResource(R.string.cta_invite_friend),
        style = MaterialTheme.typography.labelLarge,
      )
    }
  }
}

@Composable
private fun SecondaryTabEmptyContent(
  icon: ImageVector,
  title: String,
  subtitle: String,
  actionLabel: String,
  onActionClick: () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .widthIn(max = 460.dp)
        .padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier =
        Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(WhatsAppSurfaceElevated),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AccentPurpleBright,
        modifier = Modifier.size(32.dp),
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
      color = WhatsAppTextPrimary,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyLarge,
      color = WhatsAppTextSecondary,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onActionClick,
      shape = RoundedCornerShape(50),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = AccentPurple,
          contentColor = OnAccentPurple,
        ),
      contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
      modifier = Modifier.minimumInteractiveComponentSize().height(48.dp),
    ) {
      Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun CameraMediaSheetContent(
  hasPickedPhoto: Boolean,
  onPickPhoto: () -> Unit,
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
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stringResource(R.string.sheet_camera_title),
        style = MaterialTheme.typography.titleLarge,
        color = WhatsAppTextPrimary,
      )
      IconButton(onClick = onClose) {
        Icon(
          imageVector = LucideIcons.Close,
          contentDescription = stringResource(R.string.cd_camera_action),
          tint = WhatsAppTextSecondary,
        )
      }
    }

    Text(
      text =
        if (hasPickedPhoto) {
          stringResource(R.string.sheet_selected_photo)
        } else {
          stringResource(R.string.sheet_camera_subtitle)
        },
      style = MaterialTheme.typography.bodyMedium,
      color = WhatsAppTextSecondary,
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onPickPhoto,
      shape = RoundedCornerShape(50),
      colors =
        ButtonDefaults.buttonColors(
          containerColor = AccentPurple,
          contentColor = OnAccentPurple,
        ),
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pick_photo_button"),
    ) {
      Icon(
        imageVector = LucideIcons.Image,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(R.string.sheet_pick_photo),
        style = MaterialTheme.typography.labelLarge,
      )
    }
  }
}
