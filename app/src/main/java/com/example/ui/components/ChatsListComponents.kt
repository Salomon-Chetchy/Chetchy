package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.BottomNavTab
import com.example.ui.ChatMessage
import com.example.ui.Contact
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

@Composable
fun ChetchyTopBar(
  title: String,
  isChatsTab: Boolean,
  onCameraClick: () -> Unit,
  onNewChatClick: () -> Unit,
  onToggleArchivedClick: () -> Unit,
  onOpenSettingsClick: () -> Unit,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(WhatsAppBackground)
        .statusBarsPadding()
        .padding(start = 18.dp, end = 6.dp, top = 8.dp, bottom = 6.dp)
        .height(44.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style =
        MaterialTheme.typography.titleLarge.copy(
          fontSize = if (isChatsTab) 24.sp else 22.sp,
          fontWeight = FontWeight.Bold,
        ),
      color = WhatsAppTextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )

    if (isChatsTab) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        IconButton(
          onClick = onCameraClick,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("camera_button"),
        ) {
          Icon(
            imageVector = LucideIcons.Camera,
            contentDescription = stringResource(R.string.cd_camera_action),
            tint = WhatsAppTextPrimary,
            modifier = Modifier.size(22.dp),
          )
        }

        Box {
          IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.minimumInteractiveComponentSize().testTag("more_options_button"),
          ) {
            Icon(
              imageVector = LucideIcons.MoreVertical,
              contentDescription = stringResource(R.string.cd_more_options),
              tint = WhatsAppTextPrimary,
              modifier = Modifier.size(21.dp),
            )
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            containerColor = WhatsAppSurfaceElevated,
          ) {
            DropdownMenuItem(
              text = {
                Text(
                  text = stringResource(R.string.chats_menu_new_chat),
                  color = WhatsAppTextPrimary,
                )
              },
              onClick = {
                menuExpanded = false
                onNewChatClick()
              },
            )
            DropdownMenuItem(
              text = {
                Text(
                  text = stringResource(R.string.chats_menu_archived),
                  color = WhatsAppTextPrimary,
                )
              },
              onClick = {
                menuExpanded = false
                onToggleArchivedClick()
              },
            )
            DropdownMenuItem(
              text = {
                Text(
                  text = stringResource(R.string.chats_menu_settings),
                  color = WhatsAppTextPrimary,
                )
              },
              onClick = {
                menuExpanded = false
                onOpenSettingsClick()
              },
            )
          }
        }
      }
    }
  }
}

@Composable
fun ChatsSearchPillBar(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = WhatsAppSurfaceContainer,
    shape = RoundedCornerShape(50),
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .height(48.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = LucideIcons.Search,
        contentDescription = null,
        tint = WhatsAppTextSecondary,
        modifier = Modifier.size(19.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.CenterStart,
      ) {
        if (query.isEmpty()) {
          Text(
            text = stringResource(R.string.chats_search_placeholder),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.5.sp),
            color = WhatsAppTextSecondary,
          )
        }
        BasicTextField(
          value = query,
          onValueChange = onQueryChange,
          singleLine = true,
          cursorBrush = SolidColor(AccentPurpleBright),
          textStyle =
            MaterialTheme.typography.bodyLarge.copy(
              fontSize = 15.5.sp,
              color = WhatsAppTextPrimary,
            ),
          modifier = Modifier.fillMaxWidth().testTag("chats_search_input"),
        )
      }
      if (query.isNotEmpty()) {
        IconButton(
          onClick = { onQueryChange("") },
          modifier = Modifier.minimumInteractiveComponentSize().size(32.dp),
        ) {
          Icon(
            imageVector = LucideIcons.Close,
            contentDescription = stringResource(R.string.cd_clear_search),
            tint = WhatsAppTextSecondary,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
fun ArchivedChatsRow(
  archivedCount: Int,
  isViewingArchived: Boolean,
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
        .padding(horizontal = 16.dp, vertical = 11.dp)
        .testTag("archived_row"),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.width(50.dp),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = LucideIcons.Archive,
        contentDescription = null,
        tint = if (isViewingArchived) AccentPurpleBright else WhatsAppTextSecondary,
        modifier = Modifier.size(20.dp),
      )
    }
    Spacer(modifier = Modifier.width(14.dp))
    Text(
      text = stringResource(R.string.chats_archived_label),
      style =
        MaterialTheme.typography.titleMedium.copy(
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
        ),
      color = if (isViewingArchived) AccentPurpleBright else WhatsAppTextSecondary,
      modifier = Modifier.weight(1f),
    )
    if (archivedCount > 0) {
      Text(
        text = archivedCount.toString(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = AccentPurpleBright,
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveChatsListContent(
  conversations: List<Pair<Contact, ChatMessage>>,
  hasMyStatuses: Boolean,
  isViewingArchived: Boolean,
  archivedCount: Int,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onToggleArchived: () -> Unit,
  onConversationClick: (Contact) -> Unit,
  onToggleArchiveChat: (Contact) -> Unit,
  onStartChattingClick: () -> Unit,
  onInviteFriendClick: () -> Unit,
  emptyHeroContent: @Composable () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
    contentPadding = PaddingValues(bottom = 96.dp),
  ) {
    item(key = "search_pill") {
      ChatsSearchPillBar(
        query = searchQuery,
        onQueryChange = onSearchQueryChange,
      )
    }

    item(key = "archived_row") {
      ArchivedChatsRow(
        archivedCount = archivedCount,
        isViewingArchived = isViewingArchived,
        onClick = onToggleArchived,
      )
    }

    if (isViewingArchived) {
      item(key = "archived_back_banner") {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.chats_menu_archived),
            style = MaterialTheme.typography.labelMedium,
            color = AccentPurpleMuted,
          )
          TextButton(
            onClick = onToggleArchived,
            modifier = Modifier.testTag("back_to_all_chats_button"),
          ) {
            Text(
              text = stringResource(R.string.chats_back_to_all),
              color = AccentPurpleBright,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
      }
    }

    if (conversations.isEmpty()) {
      item(key = "empty_state") {
        when {
          isViewingArchived -> {
            Text(
              text = stringResource(R.string.chats_archived_empty),
              style = MaterialTheme.typography.bodyMedium,
              color = WhatsAppTextSecondary,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
            )
          }
          searchQuery.isNotBlank() -> {
            Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 36.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                text = "No chats matching \"$searchQuery\"",
                style = MaterialTheme.typography.bodyLarge,
                color = WhatsAppTextSecondary,
                textAlign = TextAlign.Center,
              )
              Spacer(modifier = Modifier.height(12.dp))
              TextButton(onClick = onStartChattingClick) {
                Text(
                  text = stringResource(R.string.sheet_new_chat_title),
                  color = AccentPurpleBright,
                )
              }
            }
          }
          else -> {
            emptyHeroContent()
          }
        }
      }
    } else {
      items(conversations, key = { it.first.id }) { (contact, lastMessage) ->
        var showRowMenu by remember { mutableStateOf(false) }
        val showPurpleRing = contact.isSelf && hasMyStatuses

        Box(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .combinedClickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = ripple(bounded = true),
                  onClick = { onConversationClick(contact) },
                  onLongClick = { showRowMenu = true },
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("conversation_row_${contact.id}"),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier =
                Modifier
                  .size(52.dp)
                  .let { mod ->
                    if (showPurpleRing) {
                      mod.border(BorderStroke(2.dp, AccentPurple), CircleShape).padding(3.dp)
                    } else {
                      mod
                    }
                  }
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
                  style =
                    MaterialTheme.typography.titleMedium.copy(
                      fontSize = 16.5.sp,
                      fontWeight = FontWeight.SemiBold,
                    ),
                  color = WhatsAppTextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = lastMessage.timestampLabel,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                  color = WhatsAppTextSecondary,
                )
              }
              Spacer(modifier = Modifier.height(3.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = LucideIcons.CheckCheck,
                  contentDescription = null,
                  tint = AccentPurpleBright,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                if (lastMessage.mediaLabel == "Voice") {
                  Icon(
                    imageVector = LucideIcons.Mic,
                    contentDescription = null,
                    tint = WhatsAppTextSecondary,
                    modifier = Modifier.size(14.dp),
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                  text = stripChatMarkdown(lastMessage.text).replace('\n', ' '),
                  style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                  color = WhatsAppTextSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }

          DropdownMenu(
            expanded = showRowMenu,
            onDismissRequest = { showRowMenu = false },
            containerColor = WhatsAppSurfaceElevated,
          ) {
            DropdownMenuItem(
              text = {
                Text(
                  text =
                    if (isViewingArchived) {
                      stringResource(R.string.chats_unarchive_action)
                    } else {
                      stringResource(R.string.chats_archive_action)
                    },
                  color = WhatsAppTextPrimary,
                )
              },
              onClick = {
                showRowMenu = false
                onToggleArchiveChat(contact)
              },
            )
          }
        }
      }
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

private val ChetchyNavSpecs =
  listOf(
    NavTabSpec(
      tab = BottomNavTab.CHATS,
      labelRes = R.string.nav_chats,
      icon = LucideIcons.ChatsOutlined,
      activeIcon = LucideIcons.ChatsFilled,
      testTag = "nav_chats",
    ),
    NavTabSpec(
      tab = BottomNavTab.CALLS,
      labelRes = R.string.nav_calls,
      icon = LucideIcons.Phone,
      testTag = "nav_calls",
    ),
    NavTabSpec(
      tab = BottomNavTab.UPDATES,
      labelRes = R.string.nav_updates,
      icon = LucideIcons.MessageCircleDashed,
      testTag = "nav_updates",
    ),
    NavTabSpec(
      tab = BottomNavTab.SETTINGS,
      labelRes = R.string.nav_settings,
      icon = LucideIcons.Settings,
      testTag = "nav_settings",
    ),
  )

@Composable
fun ChetchyBottomNavigationBar(
  selectedTab: BottomNavTab,
  showUpdatesBadge: Boolean,
  onSelectTab: (BottomNavTab) -> Unit,
) {
  Surface(
    color = WhatsAppBackground,
    tonalElevation = 0.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 8.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ChetchyNavSpecs.forEach { spec ->
        val isSelected = selectedTab == spec.tab
        val label = stringResource(spec.labelRes)
        val iconTint = if (isSelected) AccentPurpleBright else WhatsAppTextPrimary
        val labelTint = if (isSelected) WhatsAppTextPrimary else WhatsAppTextSecondary

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
              .padding(vertical = 4.dp)
              .testTag(spec.testTag),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Box(
            modifier =
              Modifier
                .width(60.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(50))
                .background(
                  if (isSelected) AccentPurpleSoftDark else WhatsAppBackground
                ),
            contentAlignment = Alignment.Center,
          ) {
            Box {
              Icon(
                imageVector = if (isSelected) spec.activeIcon else spec.icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
              )
              if (spec.tab == BottomNavTab.UPDATES && showUpdatesBadge) {
                Box(
                  modifier =
                    Modifier
                      .align(Alignment.TopEnd)
                      .offset(x = 3.dp, y = (-2).dp)
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(AccentPurpleBright)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = label,
            style =
              MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              ),
            color = labelTint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
fun ChetchyNavigationRail(
  selectedTab: BottomNavTab,
  onSelectTab: (BottomNavTab) -> Unit,
) {
  NavigationRail(
    containerColor = WhatsAppSurface,
    contentColor = WhatsAppTextPrimary,
    modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
  ) {
    Spacer(modifier = Modifier.height(16.dp))
    ChetchyNavSpecs.forEach { spec ->
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
