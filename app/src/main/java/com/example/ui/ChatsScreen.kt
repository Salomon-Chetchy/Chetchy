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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.NewChatSheetContent
import com.example.ui.components.NewContactSheetContent
import com.example.ui.components.StartChattingIllustration
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.AccentPurpleMuted
import com.example.ui.theme.AccentPurpleSoftDark
import com.example.ui.theme.OnAccentPurple
import com.example.ui.theme.WhatsAppBackground
import com.example.ui.theme.WhatsAppOutlineSubtle
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppSurfaceContainer
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
          onSendMessage = viewModel::sendMessage,
          onAttachMedia = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onVoiceNote = viewModel::sendVoiceNotePlaceholder,
          onDismissTooltip = viewModel::dismissMediaTooltip,
          onStartCall = viewModel::startCall,
          onEndCall = viewModel::endCall,
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
          installedVersionName = com.example.BuildConfig.VERSION_NAME,
          installedBuildNumber = viewModel.installedDisplayBuild,
          onSyncSavedRepo = { saved -> viewModel.setGithubRepoSlug(saved, resetStatus = false) },
          onSaveRepo = { updated -> viewModel.setGithubRepoSlug(updated, resetStatus = true) },
          onCheckUpdates = viewModel::checkForUpdates,
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
  onShowSnackbar: (String) -> Unit,
) {
  BackHandler(enabled = uiState.selectedTab != BottomNavTab.CHATS) {
    onSelectTab(BottomNavTab.CHATS)
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize().background(WhatsAppBackground)) {
    val isExpandedWidth = maxWidth >= 840.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (isExpandedWidth) {
        WhatsAppNavigationRail(
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
          WhatsAppChatsTopBar(
            title = tabTitle(uiState.selectedTab),
            showChatActions = uiState.selectedTab == BottomNavTab.CHATS,
            onCameraClick = onOpenCamera,
            onNewChatClick = onOpenNewChat,
          )
        },
        bottomBar = {
          if (!isExpandedWidth) {
            WhatsAppBottomNavigationBar(
              selectedTab = uiState.selectedTab,
              onSelectTab = onSelectTab,
            )
          }
        },
      ) { innerPadding ->
        Box(
          modifier = Modifier.fillMaxSize().padding(innerPadding),
          contentAlignment = Alignment.Center,
        ) {
          Crossfade(
            targetState = uiState.selectedTab,
            animationSpec = tween(durationMillis = 180),
            label = "tab_content_crossfade",
          ) { tab ->
            when (tab) {
              BottomNavTab.CHATS -> {
                val activeConversations = uiState.conversationsWithMessages
                if (activeConversations.isEmpty()) {
                  ChatsEmptyHeroContent(
                    onStartChattingClick = onOpenNewChat,
                    onInviteFriendClick = onShareInvite,
                  )
                } else {
                  ActiveChatsListContent(
                    conversations = activeConversations,
                    onConversationClick = { contact -> onOpenConversation(contact.id) },
                  )
                }
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
                  onSyncSavedRepo = onSyncSavedRepo,
                  onSaveRepo = onSaveRepo,
                  onCheckUpdates = onCheckUpdates,
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
private fun ActiveChatsListContent(
  conversations: List<Pair<Contact, ChatMessage>>,
  onConversationClick: (Contact) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
    contentPadding = PaddingValues(vertical = 8.dp),
  ) {
    items(conversations, key = { it.first.id }) { (contact, lastMessage) ->
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(bounded = true),
              onClick = { onConversationClick(contact) },
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("conversation_row_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .size(50.dp)
              .clip(CircleShape)
              .background(AccentPurpleSoftDark),
          contentAlignment = Alignment.Center,
        ) {
          if (contact.initials.isNotEmpty() && !contact.isSelf) {
            Text(
              text = contact.initials,
              style = MaterialTheme.typography.titleMedium,
              color = AccentPurpleMuted,
            )
          } else {
            Icon(
              imageVector = LucideIcons.User,
              contentDescription = null,
              tint = AccentPurpleMuted,
              modifier = Modifier.size(24.dp),
            )
          }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = contact.displayName,
              style = MaterialTheme.typography.titleMedium,
              color = WhatsAppTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
            )
            Text(
              text = lastMessage.timestampLabel,
              style = MaterialTheme.typography.labelSmall,
              color = AccentPurpleBright,
            )
          }
          Spacer(modifier = Modifier.height(3.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = LucideIcons.CheckCheck,
              contentDescription = null,
              tint = AccentPurpleBright,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = lastMessage.text,
              style = MaterialTheme.typography.bodyMedium,
              color = WhatsAppTextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
      HorizontalDivider(
        thickness = 0.5.dp,
        color = WhatsAppOutlineSubtle,
        modifier = Modifier.padding(start = 80.dp),
      )
    }
  }
}

@Composable
private fun tabTitle(tab: BottomNavTab): String =
  when (tab) {
    BottomNavTab.CHATS -> stringResource(R.string.title_chats)
    BottomNavTab.UPDATES -> stringResource(R.string.nav_updates)
    BottomNavTab.CALLS -> stringResource(R.string.nav_calls)
    BottomNavTab.SETTINGS -> stringResource(R.string.nav_settings)
  }

@Composable
private fun WhatsAppChatsTopBar(
  title: String,
  showChatActions: Boolean,
  onCameraClick: () -> Unit,
  onNewChatClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(WhatsAppBackground)
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp)
        .height(48.dp)
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = WhatsAppTextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.align(Alignment.Center),
    )

    if (showChatActions) {
      Row(
        modifier = Modifier.align(Alignment.CenterEnd),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .minimumInteractiveComponentSize()
              .size(38.dp)
              .clip(CircleShape)
              .background(WhatsAppSurfaceContainer)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onCameraClick,
              )
              .testTag("camera_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = LucideIcons.Camera,
            contentDescription = stringResource(R.string.cd_camera_action),
            tint = WhatsAppTextPrimary,
            modifier = Modifier.size(19.dp),
          )
        }

        Box(
          modifier =
            Modifier
              .minimumInteractiveComponentSize()
              .size(38.dp)
              .clip(CircleShape)
              .background(AccentPurple)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onNewChatClick,
              )
              .testTag("new_chat_plus_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = LucideIcons.Plus,
            contentDescription = stringResource(R.string.cd_new_chat_action),
            tint = OnAccentPurple,
            modifier = Modifier.size(21.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun ChatsEmptyHeroContent(
  onStartChattingClick: () -> Unit,
  onInviteFriendClick: () -> Unit,
) {
  BoxWithConstraints(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    val compactHeight = maxHeight < 520.dp
    val illustrationWidth = if (compactHeight) 182.dp else 228.dp
    val illustrationHeight = if (compactHeight) 156.dp else 196.dp

    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .widthIn(max = 520.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 32.dp, vertical = if (compactHeight) 12.dp else 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      StartChattingIllustration(
        modifier = Modifier.size(width = illustrationWidth, height = illustrationHeight)
      )

      Spacer(modifier = Modifier.height(if (compactHeight) 12.dp else 20.dp))

      Text(
        text = stringResource(R.string.hero_headline),
        style = MaterialTheme.typography.headlineMedium,
        color = WhatsAppTextPrimary,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))

      Text(
        text = stringResource(R.string.hero_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = WhatsAppTextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.widthIn(max = 310.dp),
      )

      Spacer(modifier = Modifier.height(if (compactHeight) 18.dp else 28.dp))

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

      Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))

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
        .fillMaxWidth()
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

private data class NavTabSpec(
  val tab: BottomNavTab,
  val labelRes: Int,
  val icon: ImageVector,
  val activeIcon: ImageVector = icon,
  val testTag: String,
)

private val BottomNavSpecs =
  listOf(
    NavTabSpec(
      tab = BottomNavTab.UPDATES,
      labelRes = R.string.nav_updates,
      icon = LucideIcons.MessageCircleDashed,
      testTag = "nav_updates",
    ),
    NavTabSpec(
      tab = BottomNavTab.CALLS,
      labelRes = R.string.nav_calls,
      icon = LucideIcons.Phone,
      testTag = "nav_calls",
    ),
    NavTabSpec(
      tab = BottomNavTab.CHATS,
      labelRes = R.string.nav_chats,
      icon = LucideIcons.ChatsOutlined,
      activeIcon = LucideIcons.ChatsFilled,
      testTag = "nav_chats",
    ),
    NavTabSpec(
      tab = BottomNavTab.SETTINGS,
      labelRes = R.string.nav_settings,
      icon = LucideIcons.Settings,
      testTag = "nav_settings",
    ),
  )

@Composable
private fun WhatsAppBottomNavigationBar(
  selectedTab: BottomNavTab,
  onSelectTab: (BottomNavTab) -> Unit,
) {
  Surface(
    color = WhatsAppSurface,
    tonalElevation = 0.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
      HorizontalDivider(thickness = 0.5.dp, color = WhatsAppOutlineSubtle)
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        BottomNavSpecs.forEach { spec ->
          val isSelected = selectedTab == spec.tab
          val label = stringResource(spec.labelRes)
          val tint = if (isSelected) AccentPurpleBright else WhatsAppTextSecondary

          Column(
            modifier =
              Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(14.dp))
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(bounded = true),
                  onClick = { onSelectTab(spec.tab) },
                )
                .padding(vertical = 6.dp)
                .testTag(spec.testTag),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = if (isSelected) spec.activeIcon else spec.icon,
              contentDescription = label,
              tint = tint,
              modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = label,
              style =
                MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                ),
              color = tint,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun WhatsAppNavigationRail(
  selectedTab: BottomNavTab,
  onSelectTab: (BottomNavTab) -> Unit,
) {
  NavigationRail(
    containerColor = WhatsAppSurface,
    contentColor = WhatsAppTextPrimary,
    modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
  ) {
    Spacer(modifier = Modifier.height(16.dp))
    BottomNavSpecs.forEach { spec ->
      val isSelected = selectedTab == spec.tab
      val label = stringResource(spec.labelRes)
      NavigationRailItem(
        selected = isSelected,
        onClick = { onSelectTab(spec.tab) },
        icon = {
          Icon(
            imageVector = if (isSelected) spec.activeIcon else spec.icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
          )
        },
        label = {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
          )
        },
        colors =
          NavigationRailItemDefaults.colors(
            selectedIconColor = OnAccentPurple,
            selectedTextColor = WhatsAppTextPrimary,
            indicatorColor = AccentPurple,
            unselectedIconColor = WhatsAppTextSecondary,
            unselectedTextColor = WhatsAppTextSecondary,
          ),
        modifier = Modifier.testTag(spec.testTag),
      )
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
