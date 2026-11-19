package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppSurfaceCard
import com.example.ui.theme.WhatsAppSurfaceContainer
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

@Composable
fun StatusViewerOverlay(
  statuses: List<StatusUpdate>,
  currentIndex: Int,
  onStep: (Int) -> Unit,
  onDelete: (String) -> Unit,
  onClose: () -> Unit,
) {
  BackHandler(onBack = onClose)
  val status = statuses.getOrNull(currentIndex) ?: return

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(if (status.photoUri != null) Color.Black else Color(status.backgroundHex))
        .testTag("status_viewer_overlay"),
  ) {
    // Left / Right tap zones to step between statuses
    Row(modifier = Modifier.fillMaxSize()) {
      Box(modifier = Modifier.weight(1f).fillMaxSize().clickable { onStep(-1) })
      Box(modifier = Modifier.weight(1f).fillMaxSize().clickable { onStep(1) })
    }

    if (status.photoUri != null) {
      AsyncImage(
        model = status.photoUri,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize().align(Alignment.Center),
      )
    } else {
      Text(
        text = status.text,
        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
        color = OnAccentPurple,
        textAlign = TextAlign.Center,
        modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
      )
    }

    // Top progress segments + user bar
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .statusBarsPadding()
          .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        statuses.forEachIndexed { idx, _ ->
          Box(
            modifier =
              Modifier
                .weight(1f)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                  if (idx <= currentIndex) OnAccentPurple else OnAccentPurple.copy(alpha = 0.35f)
                )
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onClose) {
          Icon(
            imageVector = LucideIcons.ChevronLeft,
            contentDescription = stringResource(R.string.cd_back),
            tint = OnAccentPurple,
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.updates_my_status),
            style = MaterialTheme.typography.titleMedium,
            color = OnAccentPurple,
          )
          Text(
            text = status.timestampLabel,
            style = MaterialTheme.typography.labelSmall,
            color = OnAccentPurple.copy(alpha = 0.8f),
          )
        }
        IconButton(
          onClick = { onDelete(status.id) },
          modifier = Modifier.testTag("delete_status_button"),
        ) {
          Icon(
            imageVector = LucideIcons.Trash2,
            contentDescription = stringResource(R.string.cd_delete_status),
            tint = OnAccentPurple,
          )
        }
      }
    }

    // Bottom views indicator
    Row(
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = LucideIcons.Eye,
        contentDescription = null,
        tint = OnAccentPurple,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "${status.viewsCount}",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = OnAccentPurple,
      )
    }
  }
}

@Composable
fun ChannelFeedScreen(
  channel: ChannelItem,
  onPost: (String) -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)
  var draft by remember { mutableStateOf("") }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = WhatsAppBackground,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      Surface(color = WhatsAppSurface) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
          Row(
            modifier = Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("channel_back_button")) {
              Icon(
                imageVector = LucideIcons.ChevronLeft,
                contentDescription = stringResource(R.string.cd_back),
                tint = WhatsAppTextPrimary,
              )
            }
            Box(
              modifier =
                Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(AccentPurpleSoftDark),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = LucideIcons.Radio,
                contentDescription = null,
                tint = AccentPurpleBright,
                modifier = Modifier.size(20.dp),
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = channel.name,
                style = MaterialTheme.typography.titleMedium,
                color = WhatsAppTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = stringResource(R.string.updates_channel_followers, 1),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = WhatsAppTextSecondary,
              )
            }
          }
          HorizontalDivider(thickness = 0.5.dp, color = WhatsAppOutlineSubtle)
        }
      }
    },
    bottomBar = {
      Surface(color = WhatsAppSurface) {
        Column(
          modifier =
            Modifier
              .fillMaxWidth()
              .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
        ) {
          HorizontalDivider(thickness = 0.5.dp, color = WhatsAppOutlineSubtle)
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              modifier =
                Modifier
                  .weight(1f)
                  .height(42.dp)
                  .clip(RoundedCornerShape(22.dp))
                  .background(WhatsAppSurfaceContainer)
                  .padding(horizontal = 16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (draft.isEmpty()) {
                  Text(
                    text = stringResource(R.string.channel_post_hint),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    color = WhatsAppTextSecondary,
                  )
                }
                BasicTextField(
                  value = draft,
                  onValueChange = { draft = it },
                  singleLine = true,
                  textStyle =
                    MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, color = WhatsAppTextPrimary),
                  cursorBrush = SolidColor(AccentPurple),
                  modifier = Modifier.fillMaxWidth().testTag("channel_post_input"),
                )
              }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier =
                Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(
                    if (draft.isNotBlank()) AccentPurple else WhatsAppSurfaceContainer
                  )
                  .clickable(enabled = draft.isNotBlank()) {
                    onPost(draft)
                    draft = ""
                  }
                  .testTag("channel_send_button"),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = LucideIcons.SendHorizontal,
                contentDescription = stringResource(R.string.cd_send_message),
                tint = if (draft.isNotBlank()) OnAccentPurple else WhatsAppTextSecondary,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      if (channel.description.isNotBlank()) {
        item(key = "channel_desc") {
          Surface(
            color = WhatsAppSurfaceCard,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = channel.description,
              style = MaterialTheme.typography.bodyMedium,
              color = WhatsAppTextSecondary,
              modifier = Modifier.padding(14.dp),
            )
          }
        }
      }

      if (channel.posts.isEmpty()) {
        item(key = "channel_empty") {
          Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = stringResource(R.string.channel_empty_posts),
              style = MaterialTheme.typography.bodyLarge,
              color = WhatsAppTextSecondary,
              textAlign = TextAlign.Center,
            )
          }
        }
      } else {
        items(channel.posts, key = { it.id }) { post ->
          Surface(
            color = WhatsAppSurfaceCard,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = post.text,
                style = MaterialTheme.typography.bodyLarge,
                color = WhatsAppTextPrimary,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = post.timestampLabel,
                style = MaterialTheme.typography.labelSmall,
                color = AccentPurpleMuted,
                modifier = Modifier.align(Alignment.End),
              )
            }
          }
        }
      }
    }
  }
}
