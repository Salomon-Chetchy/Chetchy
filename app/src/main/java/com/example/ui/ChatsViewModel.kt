package com.example.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.ai.HybridPuterAiEngine
import com.example.ai.PuterAiEngine
import com.example.ai.PuterAiResult
import com.example.ai.PuterChatTurn
import com.example.ai.PuterModels
import com.example.ai.PuterWebBridgeController
import com.example.update.AppUpdateClient
import com.example.update.GitHubAppUpdateClient
import com.example.update.UpdateCheckUiState
import com.example.update.UpdateFetchResult
import com.example.update.UpdatePreferences
import com.example.update.isNewerRelease
import com.example.update.normalizeRepoSlug
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "ChatsViewModel"

enum class BottomNavTab {
  UPDATES,
  CALLS,
  CHATS,
  SETTINGS,
}

data class CountryCodeOption(
  val name: String,
  val dialCode: String,
)

val DefaultCountries =
  listOf(
    CountryCodeOption("United States", "+1"),
    CountryCodeOption("Canada", "+1"),
    CountryCodeOption("United Kingdom", "+44"),
    CountryCodeOption("Cameroon", "+237"),
    CountryCodeOption("France", "+33"),
    CountryCodeOption("Germany", "+49"),
    CountryCodeOption("Nigeria", "+234"),
    CountryCodeOption("India", "+91"),
  )

data class Contact(
  val id: String,
  val firstName: String,
  val lastName: String,
  val countryName: String,
  val dialCode: String,
  val phoneNumber: String,
  val syncToPhone: Boolean = false,
  val isSelf: Boolean = false,
  val aiIdentity: String = "",
  val goalTitle: String? = null,
  val goalProgress: Int = 0, // 0 to 100
) {
  val displayName: String
    get() =
      listOf(firstName.trim(), lastName.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" ")
        .ifEmpty { "$dialCode $phoneNumber".trim() }

  val formattedPhone: String
    get() = if (phoneNumber.isBlank()) "" else "$dialCode $phoneNumber".trim()

  val initials: String
    get() =
      listOf(firstName.trim(), lastName.trim())
        .filter { it.isNotEmpty() }
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
}

data class ChatMessage(
  val id: String,
  val contactId: String,
  val text: String,
  val timestampLabel: String,
  val isOutgoing: Boolean = true,
  val mediaLabel: String? = null,
  val replyToSender: String? = null,
  val replyToText: String? = null,
)

data class StatusUpdate(
  val id: String,
  val text: String = "",
  val photoUri: Uri? = null,
  val backgroundHex: Long = 0xFF7F46B8L,
  val timestampLabel: String,
  val viewsCount: Int = 0,
)

data class ChannelPost(
  val id: String,
  val text: String,
  val timestampLabel: String,
)

data class ChannelItem(
  val id: String,
  val name: String,
  val description: String,
  val posts: List<ChannelPost> = emptyList(),
)

data class NewContactDraft(
  val firstName: String = "",
  val lastName: String = "",
  val country: CountryCodeOption = DefaultCountries.first(),
  val phone: String = "",
  val syncToPhone: Boolean = false,
  val aiIdentity: String = "",
  val goalTitle: String = "",
  val goalProgress: Int = 0,
) {
  val canSave: Boolean
    get() = firstName.isNotBlank() || lastName.isNotBlank() || phone.isNotBlank()
}

sealed interface ActiveSheet {
  data object NewChat : ActiveSheet
  data object NewContact : ActiveSheet
  data object CameraMedia : ActiveSheet
  data object StatusTypeChooser : ActiveSheet
  data object TextStatusComposer : ActiveSheet
  data object CreateChannel : ActiveSheet
}

data class ChatsUiState(
  val selectedTab: BottomNavTab = BottomNavTab.CHATS,
  val activeSheet: ActiveSheet? = null,
  val searchQuery: String = "",
  val newContactDraft: NewContactDraft = NewContactDraft(),
  val contacts: List<Contact> =
    listOf(
      Contact(
        id = "self",
        firstName = "You",
        lastName = "",
        countryName = "United States",
        dialCode = "+1",
        phoneNumber = "",
        isSelf = true,
      ),
      Contact(
        id = "aria_coach",
        firstName = "Aria",
        lastName = "Coach",
        countryName = "United States",
        dialCode = "+1",
        phoneNumber = "5550144",
        syncToPhone = true,
        aiIdentity = "You are Aria, an energetic, warm, and highly motivating fitness coach and wellness mentor. Give encouraging advice, action steps, and keep the user accountable to their goal.",
        goalTitle = "Build consistent morning workout habits",
        goalProgress = 25,
      ),
      Contact(
        id = "sage_mentor",
        firstName = "Sage",
        lastName = "Mentor",
        countryName = "United Kingdom",
        dialCode = "+44",
        phoneNumber = "77009000",
        syncToPhone = true,
        aiIdentity = "You are Sage, a wise and empathetic life mentor and productivity expert. Provide structured thinking, thoughtful guidance, and help the user grow.",
        goalTitle = "Read 12 books and write weekly reflections",
        goalProgress = 40,
      ),
    ),
  val messagesByContact: Map<String, List<ChatMessage>> =
    mapOf(
      "aria_coach" to
        listOf(
          ChatMessage(
            id = "aria_init",
            contactId = "aria_coach",
            text = "Hey! I'm Aria, your fitness coach. Ready to crush your workout goal today?",
            timestampLabel = "9:00 AM",
            isOutgoing = false,
          )
        ),
      "sage_mentor" to
        listOf(
          ChatMessage(
            id = "sage_init",
            contactId = "sage_mentor",
            text = "Greetings. I am Sage. Let's make steady progress on your personal growth goal.",
            timestampLabel = "9:05 AM",
            isOutgoing = false,
          )
        ),
    ),
  val activeChatContactId: String? = null,
  val chatInputText: String = "",
  val showMediaTooltip: Boolean = true,
  val activeCallType: String? = null,
  val selectedPhotoUri: Uri? = null,
  val myStatuses: List<StatusUpdate> = emptyList(),
  val viewingStatusIndex: Int? = null,
  val channels: List<ChannelItem> = emptyList(),
  val activeChannelId: String? = null,
  val chatListSearchQuery: String = "",
  val archivedContactIds: Set<String> = emptySet(),
  val showArchivedChats: Boolean = false,
  val githubRepoSlug: String = UpdatePreferences.DEFAULT_REPO_SLUG,
  val updateCheckState: UpdateCheckUiState = UpdateCheckUiState.Idle,
  val puterModelId: String = PuterModels.DEFAULT_MODEL_ID,
  val puterAuthToken: String = "",
  val puterUsername: String? = null,
  val isPuterWebSignedIn: Boolean = false,
  val aiThinkingContactId: String? = null,
  val showPuterConnectPrompt: Boolean = false,
  val replyingToMessage: ChatMessage? = null,
  val snackbarMessage: String? = null,
) {
  val isPuterConnected: Boolean
    get() = puterAuthToken.isNotBlank() || isPuterWebSignedIn
  val activeChatContact: Contact?
    get() = contacts.find { it.id == activeChatContactId }

  val activeMessages: List<ChatMessage>
    get() = activeChatContactId?.let { messagesByContact[it] }.orEmpty()

  val activeChannel: ChannelItem?
    get() = channels.find { it.id == activeChannelId }

  val filteredContacts: List<Contact>
    get() {
      val q = searchQuery.trim().lowercase()
      if (q.isEmpty()) return contacts
      return contacts.filter {
        it.displayName.lowercase().contains(q) ||
          it.formattedPhone.lowercase().contains(q)
      }
    }

  val conversationsWithMessages: List<Pair<Contact, ChatMessage>>
    get() =
      contacts.mapNotNull { contact ->
        val last = messagesByContact[contact.id]?.lastOrNull()
        if (last != null) contact to last else null
      }

  val archivedConversationsCount: Int
    get() = conversationsWithMessages.count { (contact, _) -> contact.id in archivedContactIds }

  val visibleConversations: List<Pair<Contact, ChatMessage>>
    get() {
      val q = chatListSearchQuery.trim().lowercase()
      return conversationsWithMessages.filter { (contact, lastMessage) ->
        val matchesArchive =
          if (showArchivedChats) {
            contact.id in archivedContactIds
          } else {
            contact.id !in archivedContactIds
          }
        val matchesQuery =
          q.isEmpty() ||
            contact.displayName.lowercase().contains(q) ||
            contact.formattedPhone.lowercase().contains(q) ||
            lastMessage.text.lowercase().contains(q)
        matchesArchive && matchesQuery
      }
    }
}

class ChatsViewModel(
  private val updateClient: AppUpdateClient = GitHubAppUpdateClient(),
  private val installedVersionName: String = BuildConfig.VERSION_NAME,
  private val installedVersionCode: Int = BuildConfig.VERSION_CODE,
  private val installedCiBuild: Int = BuildConfig.CI_BUILD_NUMBER,
  val puterBridgeController: PuterWebBridgeController = PuterWebBridgeController(),
  private val puterAiEngine: PuterAiEngine = HybridPuterAiEngine(puterBridgeController),
) : ViewModel() {
  private val _uiState = MutableStateFlow(ChatsUiState())
  val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

  private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

  val installedDisplayBuild: Int
    get() = if (installedCiBuild > 0) installedCiBuild else installedVersionCode

  val installedVersionLabel: String
    get() = "v$installedVersionName (Build $installedDisplayBuild)"

  init {
    puterBridgeController.onAuthStateUpdated = { signedIn, username, token ->
      updatePuterAuthState(signedIn = signedIn, username = username, token = token)
    }
  }

  fun syncPuterPreferences(modelId: String, authToken: String, username: String?) {
    _uiState.update { state ->
      state.copy(
        puterModelId = modelId.ifBlank { PuterModels.DEFAULT_MODEL_ID },
        puterAuthToken = authToken.trim(),
        puterUsername = username?.takeIf { it.isNotBlank() } ?: state.puterUsername,
      )
    }
  }

  fun updatePuterAuthState(signedIn: Boolean, username: String?, token: String?) {
    _uiState.update { state ->
      val updatedToken = token?.trim()?.takeIf { it.isNotEmpty() } ?: state.puterAuthToken
      state.copy(
        isPuterWebSignedIn = signedIn,
        puterUsername = username?.takeIf { it.isNotBlank() } ?: if (signedIn) state.puterUsername else null,
        puterAuthToken = updatedToken,
        showPuterConnectPrompt = if (signedIn) false else state.showPuterConnectPrompt,
      )
    }
  }

  fun setPuterAuthToken(token: String) {
    val clean = token.trim()
    _uiState.update {
      it.copy(
        puterAuthToken = clean,
        showPuterConnectPrompt = false,
        snackbarMessage =
          if (clean.isNotEmpty()) {
            "Puter Auth Token saved"
          } else {
            "Puter Auth Token cleared"
          },
      )
    }
  }

  fun setPuterModel(modelId: String) {
    val option = PuterModels.findById(modelId)
    _uiState.update {
      it.copy(
        puterModelId = option.id,
        snackbarMessage = "Puter AI model set to ${option.displayName}",
      )
    }
  }

  fun disconnectPuter() {
    puterBridgeController.triggerSignOut()
    _uiState.update {
      it.copy(
        puterAuthToken = "",
        puterUsername = null,
        isPuterWebSignedIn = false,
        snackbarMessage = "Disconnected from Puter AI",
      )
    }
  }

  fun openPuterConnectPrompt() {
    _uiState.update { it.copy(showPuterConnectPrompt = true) }
  }

  fun dismissPuterConnectPrompt() {
    _uiState.update { it.copy(showPuterConnectPrompt = false) }
  }

  fun selectTab(tab: BottomNavTab) {
    _uiState.update { it.copy(selectedTab = tab) }
  }

  fun openNewChatSheet() {
    _uiState.update { it.copy(activeSheet = ActiveSheet.NewChat, searchQuery = "") }
  }

  fun openNewContactSheet() {
    _uiState.update {
      it.copy(
        activeSheet = ActiveSheet.NewContact,
        newContactDraft = NewContactDraft(),
      )
    }
  }

  fun openCameraSheet() {
    _uiState.update { it.copy(activeSheet = ActiveSheet.CameraMedia) }
  }

  fun openStatusTypeChooser() {
    _uiState.update { it.copy(activeSheet = ActiveSheet.StatusTypeChooser) }
  }

  fun openTextStatusComposer() {
    _uiState.update { it.copy(activeSheet = ActiveSheet.TextStatusComposer) }
  }

  fun openCreateChannelSheet() {
    _uiState.update { it.copy(activeSheet = ActiveSheet.CreateChannel) }
  }

  fun dismissSheet() {
    _uiState.update { it.copy(activeSheet = null) }
  }

  fun cancelNewContact() {
    _uiState.update {
      it.copy(
        activeSheet = ActiveSheet.NewChat,
        newContactDraft = NewContactDraft(),
      )
    }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun updateChatListSearchQuery(query: String) {
    _uiState.update { it.copy(chatListSearchQuery = query) }
  }

  fun toggleArchivedFilter(show: Boolean? = null) {
    _uiState.update { state ->
      val next = show ?: !state.showArchivedChats
      state.copy(showArchivedChats = next)
    }
  }

  fun toggleArchiveChat(contactId: String) {
    _uiState.update { state ->
      val wasArchived = contactId in state.archivedContactIds
      val updated =
        if (wasArchived) {
          state.archivedContactIds - contactId
        } else {
          state.archivedContactIds + contactId
        }
      state.copy(
        archivedContactIds = updated,
        snackbarMessage = if (wasArchived) "Chat unarchived" else "Chat archived",
      )
    }
  }

  fun updateFirstName(value: String) {
    _uiState.update { it.copy(newContactDraft = it.newContactDraft.copy(firstName = value)) }
  }

  fun updateLastName(value: String) {
    _uiState.update { it.copy(newContactDraft = it.newContactDraft.copy(lastName = value)) }
  }

  fun updateCountry(country: CountryCodeOption) {
    _uiState.update { it.copy(newContactDraft = it.newContactDraft.copy(country = country)) }
  }

  fun updatePhone(value: String) {
    val filtered = value.filter { it.isDigit() || it == ' ' || it == '-' }
    _uiState.update { it.copy(newContactDraft = it.newContactDraft.copy(phone = filtered)) }
  }

  fun updateSyncToPhone(sync: Boolean) {
    _uiState.update { it.copy(newContactDraft = it.newContactDraft.copy(syncToPhone = sync)) }
  }

  fun updateAiIdentity(value: String) {
    _uiState.update { it.copy(newContactDraft = it.newContactDraft.copy(aiIdentity = value)) }
  }

  fun updateGoalTitle(value: String) {
    _uiState.update { it.copy(newContactDraft = it.newContactDraft.copy(goalTitle = value)) }
  }

  fun updateGoalProgress(contactId: String, newProgress: Int) {
    val clamped = newProgress.coerceIn(0, 100)
    _uiState.update { state ->
      val updatedContacts = state.contacts.map { c ->
        if (c.id == contactId) c.copy(goalProgress = clamped) else c
      }
      state.copy(contacts = updatedContacts, snackbarMessage = "Goal progress updated to $clamped%")
    }
  }

  fun saveNewContactAndOpenChat() {
    val draft = _uiState.value.newContactDraft
    if (!draft.canSave) return

    val contact =
      Contact(
        id = UUID.randomUUID().toString(),
        firstName = draft.firstName.trim(),
        lastName = draft.lastName.trim(),
        countryName = draft.country.name,
        dialCode = draft.country.dialCode,
        phoneNumber = draft.phone.trim(),
        syncToPhone = draft.syncToPhone,
        isSelf = false,
        aiIdentity = draft.aiIdentity.trim(),
        goalTitle = draft.goalTitle.trim().ifEmpty { null },
        goalProgress = draft.goalProgress.coerceIn(0, 100),
      )

    _uiState.update { state ->
      val updatedContacts =
        (state.contacts + contact).sortedWith(
          compareBy<Contact> { !it.isSelf }.thenBy { it.displayName.lowercase() }
        )
      state.copy(
        contacts = updatedContacts,
        activeSheet = null,
        newContactDraft = NewContactDraft(),
        activeChatContactId = contact.id,
        chatInputText = "",
        showMediaTooltip = true,
        activeCallType = null,
      )
    }
  }

  fun openChatWithContact(contactId: String) {
    _uiState.update {
      it.copy(
        activeSheet = null,
        activeChatContactId = contactId,
        chatInputText = "",
        activeCallType = null,
      )
    }
  }

  fun closeChatScreen() {
    _uiState.update {
      it.copy(
        activeChatContactId = null,
        chatInputText = "",
        activeCallType = null,
        replyingToMessage = null,
      )
    }
  }

  fun startReplyingTo(message: ChatMessage) {
    _uiState.update { it.copy(replyingToMessage = message) }
  }

  fun cancelReply() {
    _uiState.update { it.copy(replyingToMessage = null) }
  }

  fun clearActiveChat() {
    val contactId = _uiState.value.activeChatContactId ?: return
    _uiState.update { current ->
      current.copy(
        messagesByContact = current.messagesByContact - contactId,
        replyingToMessage = null,
        snackbarMessage = "Chat cleared",
      )
    }
  }

  fun updateChatInput(text: String) {
    _uiState.update { it.copy(chatInputText = text) }
  }

  fun appendEmoji(emoji: String) {
    _uiState.update { it.copy(chatInputText = it.chatInputText + emoji) }
  }

  fun sendMessage() {
    val state = _uiState.value
    val contactId = state.activeChatContactId ?: return
    val text = state.chatInputText.trim()
    if (text.isEmpty()) return

    val contact = state.contacts.find { it.id == contactId } ?: return
    val replyingTo = state.replyingToMessage

    val userMsg =
      ChatMessage(
        id = UUID.randomUUID().toString(),
        contactId = contactId,
        text = text,
        timestampLabel = timeFormatter.format(Date()),
        isOutgoing = true,
        replyToSender =
          replyingTo?.let { if (it.isOutgoing) "You" else contact.displayName },
        replyToText = replyingTo?.text,
      )

    _uiState.update { current ->
      val existing = current.messagesByContact[contactId].orEmpty()
      current.copy(
        messagesByContact = current.messagesByContact + (contactId to (existing + userMsg)),
        chatInputText = "",
        replyingToMessage = null,
      )
    }

    // If contact has an AI identity, generate Puter AI response
    if (contact.aiIdentity.isNotBlank()) {
      viewModelScope.launch {
        _uiState.update { it.copy(aiThinkingContactId = contactId) }
        try {
          val goalContext =
            if (!contact.goalTitle.isNullOrBlank()) {
              "\nThe user's active goal with you is: \"${contact.goalTitle}\" (Current progress: ${contact.goalProgress}%). Provide encouraging mentorship, practical next steps, and check in on their goal."
            } else {
              ""
            }

          val systemInstruction =
            """
            You are ${contact.displayName}. ${contact.aiIdentity}$goalContext
            Keep your replies concise, warm, and natural like a real chat message.
            When helpful for clarity or emphasis, you may format text with **bold**, *italic*, ~~strikethrough~~, `inline code`, code blocks, > quotes, or bullet/numbered lists.
            """.trimIndent()

          val historyTurns =
            _uiState.value.messagesByContact[contactId].orEmpty().takeLast(12).map { msg ->
              PuterChatTurn(
                role = if (msg.isOutgoing) "user" else "assistant",
                content = msg.text,
              )
            }

          val turns = listOf(PuterChatTurn(role = "system", content = systemInstruction)) + historyTurns
          val currentSnapshot = _uiState.value

          val result =
            withTimeout(60_000L) {
              puterAiEngine.generateReply(
                messages = turns,
                modelId = currentSnapshot.puterModelId,
                authToken = currentSnapshot.puterAuthToken,
              )
            }

          when (result) {
            is PuterAiResult.Success -> {
              val aiMsg =
                ChatMessage(
                  id = UUID.randomUUID().toString(),
                  contactId = contactId,
                  text = result.replyText,
                  timestampLabel = timeFormatter.format(Date()),
                  isOutgoing = false,
                )
              _uiState.update { current ->
                val existing = current.messagesByContact[contactId].orEmpty()
                current.copy(
                  messagesByContact = current.messagesByContact + (contactId to (existing + aiMsg)),
                  aiThinkingContactId =
                    if (current.aiThinkingContactId == contactId) null else current.aiThinkingContactId,
                )
              }
            }
            is PuterAiResult.AuthRequired -> {
              val authMsg =
                ChatMessage(
                  id = UUID.randomUUID().toString(),
                  contactId = contactId,
                  text = "Puter AI sign-in needed: ${result.message}",
                  timestampLabel = timeFormatter.format(Date()),
                  isOutgoing = false,
                )
              _uiState.update { current ->
                val existing = current.messagesByContact[contactId].orEmpty()
                current.copy(
                  messagesByContact = current.messagesByContact + (contactId to (existing + authMsg)),
                  aiThinkingContactId =
                    if (current.aiThinkingContactId == contactId) null else current.aiThinkingContactId,
                  showPuterConnectPrompt = true,
                )
              }
            }
            is PuterAiResult.Error -> {
              val errorMsg =
                ChatMessage(
                  id = UUID.randomUUID().toString(),
                  contactId = contactId,
                  text = "Puter AI error: ${result.message}",
                  timestampLabel = timeFormatter.format(Date()),
                  isOutgoing = false,
                )
              _uiState.update { current ->
                val existing = current.messagesByContact[contactId].orEmpty()
                current.copy(
                  messagesByContact = current.messagesByContact + (contactId to (existing + errorMsg)),
                  aiThinkingContactId =
                    if (current.aiThinkingContactId == contactId) null else current.aiThinkingContactId,
                )
              }
            }
          }
        } catch (e: TimeoutCancellationException) {
          Log.e(TAG, "Puter AI generation timed out", e)
          _uiState.update { current ->
            current.copy(
              aiThinkingContactId =
                if (current.aiThinkingContactId == contactId) null else current.aiThinkingContactId,
              snackbarMessage = "Puter AI timed out. Please try again.",
            )
          }
        } catch (e: Exception) {
          Log.e(TAG, "Puter AI generation failed", e)
          val errorMsg =
            ChatMessage(
              id = UUID.randomUUID().toString(),
              contactId = contactId,
              text = "Could not reach Puter AI right now (${e.localizedMessage ?: "network error"}).",
              timestampLabel = timeFormatter.format(Date()),
              isOutgoing = false,
            )
          _uiState.update { current ->
            val existing = current.messagesByContact[contactId].orEmpty()
            current.copy(
              messagesByContact = current.messagesByContact + (contactId to (existing + errorMsg)),
              aiThinkingContactId =
                if (current.aiThinkingContactId == contactId) null else current.aiThinkingContactId,
            )
          }
        }
      }
    }
  }

  fun sendVoiceNotePlaceholder() {
    val contactId = _uiState.value.activeChatContactId ?: return
    val msg =
      ChatMessage(
        id = UUID.randomUUID().toString(),
        contactId = contactId,
        text = "Voice message (0:06)",
        timestampLabel = timeFormatter.format(Date()),
        isOutgoing = true,
        mediaLabel = "Voice",
      )
    _uiState.update { current ->
      val existing = current.messagesByContact[contactId].orEmpty()
      current.copy(
        messagesByContact = current.messagesByContact + (contactId to (existing + msg)),
      )
    }
  }

  fun dismissMediaTooltip() {
    _uiState.update { it.copy(showMediaTooltip = false) }
  }

  fun startCall(type: String) {
    _uiState.update { it.copy(activeCallType = type) }
  }

  fun endCall() {
    _uiState.update { it.copy(activeCallType = null) }
  }

  fun postTextStatus(text: String, backgroundHex: Long) {
    val clean = text.trim()
    if (clean.isEmpty()) return
    val status =
      StatusUpdate(
        id = UUID.randomUUID().toString(),
        text = clean,
        backgroundHex = backgroundHex,
        timestampLabel = timeFormatter.format(Date()),
      )
    _uiState.update {
      it.copy(
        myStatuses = it.myStatuses + status,
        activeSheet = null,
        snackbarMessage = "Status update posted",
      )
    }
  }

  fun openStatusViewer(index: Int = 0) {
    if (_uiState.value.myStatuses.isEmpty()) return
    val bounded = index.coerceIn(0, _uiState.value.myStatuses.lastIndex)
    _uiState.update { it.copy(viewingStatusIndex = bounded) }
  }

  fun stepStatusViewer(delta: Int) {
    val statuses = _uiState.value.myStatuses
    val current = _uiState.value.viewingStatusIndex ?: return
    val next = current + delta
    if (next in statuses.indices) {
      _uiState.update { it.copy(viewingStatusIndex = next) }
    } else {
      _uiState.update { it.copy(viewingStatusIndex = null) }
    }
  }

  fun closeStatusViewer() {
    _uiState.update { it.copy(viewingStatusIndex = null) }
  }

  fun deleteStatus(statusId: String) {
    _uiState.update { state ->
      val remaining = state.myStatuses.filterNot { it.id == statusId }
      val nextIndex =
        when {
          remaining.isEmpty() -> null
          state.viewingStatusIndex != null ->
            state.viewingStatusIndex.coerceAtMost(remaining.lastIndex)
          else -> null
        }
      state.copy(
        myStatuses = remaining,
        viewingStatusIndex = nextIndex,
        snackbarMessage = "Status update deleted",
      )
    }
  }

  fun createChannel(name: String, description: String) {
    val cleanName = name.trim()
    if (cleanName.isEmpty()) return
    val channel =
      ChannelItem(
        id = UUID.randomUUID().toString(),
        name = cleanName,
        description = description.trim(),
      )
    _uiState.update {
      it.copy(
        channels = it.channels + channel,
        activeSheet = null,
        activeChannelId = channel.id,
      )
    }
  }

  fun openChannel(channelId: String) {
    _uiState.update { it.copy(activeChannelId = channelId) }
  }

  fun closeChannel() {
    _uiState.update { it.copy(activeChannelId = null) }
  }

  fun postToChannel(channelId: String, text: String) {
    val clean = text.trim()
    if (clean.isEmpty()) return
    val post =
      ChannelPost(
        id = UUID.randomUUID().toString(),
        text = clean,
        timestampLabel = timeFormatter.format(Date()),
      )
    _uiState.update { state ->
      state.copy(
        channels =
          state.channels.map { ch ->
            if (ch.id == channelId) ch.copy(posts = ch.posts + post) else ch
          }
      )
    }
  }

  fun onPhotoPicked(uri: Uri?) {
    if (uri == null) return
    val state = _uiState.value
    val contactId = state.activeChatContactId
    if (contactId != null) {
      val msg =
        ChatMessage(
          id = UUID.randomUUID().toString(),
          contactId = contactId,
          text = "Photo attachment",
          timestampLabel = timeFormatter.format(Date()),
          isOutgoing = true,
          mediaLabel = "Photo",
        )
      _uiState.update { current ->
        val existing = current.messagesByContact[contactId].orEmpty()
        current.copy(
          selectedPhotoUri = uri,
          activeSheet = null,
          showMediaTooltip = false,
          messagesByContact = current.messagesByContact + (contactId to (existing + msg)),
        )
      }
    } else if (state.selectedTab == BottomNavTab.UPDATES) {
      val status =
        StatusUpdate(
          id = UUID.randomUUID().toString(),
          photoUri = uri,
          timestampLabel = timeFormatter.format(Date()),
        )
      _uiState.update {
        it.copy(
          selectedPhotoUri = uri,
          myStatuses = it.myStatuses + status,
          activeSheet = null,
          snackbarMessage = "Photo status added",
        )
      }
    } else {
      _uiState.update {
        it.copy(
          selectedPhotoUri = uri,
          activeSheet = null,
          snackbarMessage = "Photo selected and ready to share privately",
        )
      }
    }
  }

  fun setGithubRepoSlug(rawRepo: String, resetStatus: Boolean = false) {
    val clean = normalizeRepoSlug(rawRepo) ?: rawRepo.trim()
    if (clean.isEmpty()) return
    _uiState.update {
      it.copy(
        githubRepoSlug = clean,
        updateCheckState = if (resetStatus) UpdateCheckUiState.Idle else it.updateCheckState,
      )
    }
  }

  fun checkForUpdates() {
    val repoSlug = _uiState.value.githubRepoSlug
    if (_uiState.value.updateCheckState is UpdateCheckUiState.Checking) return

    _uiState.update { it.copy(updateCheckState = UpdateCheckUiState.Checking) }

    viewModelScope.launch {
      when (val result = updateClient.fetchLatestRelease(repoSlug)) {
        is UpdateFetchResult.Found -> {
          val release = result.release
          val newer =
            isNewerRelease(
              installedVersionName = installedVersionName,
              installedVersionCode = installedVersionCode,
              installedCiBuild = installedCiBuild,
              release = release,
            )
          val newState =
            if (newer) {
              UpdateCheckUiState.UpdateAvailable(
                release = release,
                installedVersionLabel = installedVersionLabel,
              )
            } else {
              UpdateCheckUiState.UpToDate(
                installedVersionLabel = installedVersionLabel,
                checkedAtLabel = timeFormatter.format(Date()),
              )
            }
          _uiState.update { it.copy(updateCheckState = newState) }
        }
        is UpdateFetchResult.Failure -> {
          _uiState.update {
            it.copy(
              updateCheckState =
                UpdateCheckUiState.Error(
                  message = result.message,
                  suggestRepoEdit = result.suggestRepoEdit,
                )
            )
          }
        }
      }
    }
  }

  fun showSnackbar(message: String) {
    _uiState.update { it.copy(snackbarMessage = message) }
  }

  fun clearSnackbar() {
    _uiState.update { it.copy(snackbarMessage = null) }
  }
}
