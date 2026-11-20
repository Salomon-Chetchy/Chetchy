package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.ai.PuterModels
import com.example.ai.PuterPreferences
import com.example.ui.components.PuterAuthTokenDialog
import com.example.ui.components.PuterModelSelectorDialog
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.AccentPurpleMuted
import com.example.ui.theme.AccentPurpleSoftDark
import com.example.ui.theme.OnAccentPurple
import com.example.ui.theme.WhatsAppOutlineSubtle
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppSurfaceCard
import com.example.ui.theme.WhatsAppSurfaceContainer
import com.example.ui.theme.WhatsAppSurfaceElevated
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary
import com.example.update.UpdateCheckUiState
import com.example.update.UpdatePreferences
import com.example.update.getInstalledSigningSha256

private val ErrorCoral = Color(0xFFF28B82)
private val ErrorSurfaceDark = Color(0xFF2D191B)

@Composable
fun SettingsTabContent(
  installedVersionName: String,
  installedBuildNumber: Int,
  githubRepoSlug: String,
  updateCheckState: UpdateCheckUiState,
  puterModelId: String,
  puterAuthToken: String,
  puterUsername: String?,
  isPuterConnected: Boolean,
  onSyncSavedRepo: (String) -> Unit,
  onSaveRepo: (String) -> Unit,
  onCheckUpdates: () -> Unit,
  onSignInWithPuter: () -> Unit,
  onSavePuterToken: (String) -> Unit,
  onSelectPuterModel: (String) -> Unit,
  onDisconnectPuter: () -> Unit,
  onShowSnackbar: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val installedCertSha256 = remember(context) { getInstalledSigningSha256(context) }
  var showRepoDialog by remember { mutableStateOf(false) }
  var showEncryptionDialog by remember { mutableStateOf(false) }
  var showPuterTokenDialog by remember { mutableStateOf(false) }
  var showPuterModelDialog by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    val saved = UpdatePreferences.getRepoSlug(context)
    if (saved != githubRepoSlug) {
      onSyncSavedRepo(saved)
    }
  }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // App Identity Header Card
    Surface(
      color = WhatsAppSurfaceCard,
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(0.5.dp, WhatsAppOutlineSubtle),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
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
            imageVector = LucideIcons.ChatsFilled,
            contentDescription = null,
            tint = AccentPurpleBright,
            modifier = Modifier.size(28.dp),
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.settings_profile_name),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = WhatsAppTextPrimary,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text =
              stringResource(
                R.string.settings_installed_version,
                installedVersionName,
                installedBuildNumber,
              ),
            style = MaterialTheme.typography.bodyMedium,
            color = WhatsAppTextSecondary,
          )
        }
      }
    }

    // Puter AI Service Card
    PuterAiSettingsCard(
      puterModelId = puterModelId,
      puterAuthToken = puterAuthToken,
      puterUsername = puterUsername,
      isPuterConnected = isPuterConnected,
      onSignInClick = onSignInWithPuter,
      onEditTokenClick = { showPuterTokenDialog = true },
      onEditModelClick = { showPuterModelDialog = true },
      onDisconnectClick = {
        PuterPreferences.clearSession(context)
        onDisconnectPuter()
      },
    )

    // In-App Update Card
    AppUpdatesCard(
      installedVersionName = installedVersionName,
      installedCertSha256 = installedCertSha256,
      githubRepoSlug = githubRepoSlug,
      updateCheckState = updateCheckState,
      onEditRepoClick = { showRepoDialog = true },
      onCheckUpdatesClick = onCheckUpdates,
      onDownloadApkClick = { url, sizeLabel ->
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            onShowSnackbar(context.getString(R.string.settings_download_started_snackbar, sizeLabel))
          }
          .onFailure {
            onShowSnackbar("Could not open browser for download.")
          }
      },
      onOpenReleasePageClick = { pageUrl ->
        runCatching {
          context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)))
        }
      },
    )

    // Additional Settings Card
    Surface(
      color = WhatsAppSurfaceCard,
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(0.5.dp, WhatsAppOutlineSubtle),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        SettingsInfoRow(
          icon = LucideIcons.Shield,
          title = stringResource(R.string.settings_privacy_title),
          subtitle = stringResource(R.string.settings_privacy_subtitle),
          onClick = { showEncryptionDialog = true },
          testTag = "settings_privacy_row",
        )
        HorizontalDivider(
          thickness = 0.5.dp,
          color = WhatsAppOutlineSubtle,
          modifier = Modifier.padding(start = 60.dp),
        )
        SettingsInfoRow(
          icon = LucideIcons.Lock,
          title = stringResource(R.string.settings_appearance_title),
          subtitle = stringResource(R.string.settings_appearance_subtitle),
          onClick = null,
          testTag = "settings_appearance_row",
        )
        HorizontalDivider(
          thickness = 0.5.dp,
          color = WhatsAppOutlineSubtle,
          modifier = Modifier.padding(start = 60.dp),
        )
        SettingsInfoRow(
          icon = LucideIcons.ChatsOutlined,
          title = stringResource(R.string.settings_about_title),
          subtitle =
            buildString {
              append(stringResource(R.string.settings_about_subtitle, BuildConfig.APPLICATION_ID))
              append(" • Code ${BuildConfig.VERSION_CODE}")
              if (!installedCertSha256.isNullOrBlank()) {
                append(" • Key ${installedCertSha256.take(8)}…")
              }
            },
          onClick = null,
          testTag = "settings_about_row",
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
  }

  if (showRepoDialog) {
    EditGitHubRepoDialog(
      currentRepo = githubRepoSlug,
      onDismiss = { showRepoDialog = false },
      onSave = { updated ->
        UpdatePreferences.saveRepoSlug(context, updated)
        onSaveRepo(updated)
        showRepoDialog = false
      },
    )
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
          style = MaterialTheme.typography.titleMedium,
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
          Text(
            text = stringResource(R.string.qr_dialog_close),
            color = AccentPurpleBright,
          )
        }
      },
    )
  }

  if (showPuterTokenDialog) {
    PuterAuthTokenDialog(
      currentToken = puterAuthToken,
      onDismiss = { showPuterTokenDialog = false },
      onSaveToken = { token ->
        PuterPreferences.saveAuthToken(context, token)
        onSavePuterToken(token)
        showPuterTokenDialog = false
      },
    )
  }

  if (showPuterModelDialog) {
    PuterModelSelectorDialog(
      selectedModelId = puterModelId,
      onDismiss = { showPuterModelDialog = false },
      onSelectModel = { modelId ->
        PuterPreferences.saveSelectedModel(context, modelId)
        onSelectPuterModel(modelId)
        showPuterModelDialog = false
      },
    )
  }
}

@Composable
private fun PuterAiSettingsCard(
  puterModelId: String,
  puterAuthToken: String,
  puterUsername: String?,
  isPuterConnected: Boolean,
  onSignInClick: () -> Unit,
  onEditTokenClick: () -> Unit,
  onEditModelClick: () -> Unit,
  onDisconnectClick: () -> Unit,
) {
  val activeModel = PuterModels.findById(puterModelId)
  val connectionStatusText =
    when {
      !puterUsername.isNullOrBlank() ->
        stringResource(R.string.settings_puter_connected_user, puterUsername)
      puterAuthToken.isNotBlank() ->
        stringResource(R.string.settings_puter_connected_token)
      isPuterConnected ->
        stringResource(R.string.settings_puter_connected_token)
      else ->
        stringResource(R.string.settings_puter_disconnected)
    }

  Surface(
    color = WhatsAppSurfaceCard,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(0.5.dp, WhatsAppOutlineSubtle),
    modifier = Modifier.fillMaxWidth().testTag("puter_ai_settings_card"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(AccentPurpleSoftDark),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = LucideIcons.ChatsFilled,
            contentDescription = null,
            tint = AccentPurpleBright,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.settings_puter_title),
            style = MaterialTheme.typography.titleMedium,
            color = WhatsAppTextPrimary,
          )
          Text(
            text = stringResource(R.string.settings_puter_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = WhatsAppTextSecondary,
          )
        }
        Surface(
          color = if (isPuterConnected) AccentPurpleSoftDark else WhatsAppSurfaceElevated,
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = if (isPuterConnected) "Active" else "Setup",
            style = MaterialTheme.typography.labelSmall,
            color = if (isPuterConnected) AccentPurpleBright else WhatsAppTextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = connectionStatusText,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = if (isPuterConnected) AccentPurpleMuted else WhatsAppTextSecondary,
        modifier = Modifier.testTag("puter_connection_status"),
      )

      // Active Puter Model Selector Row
      Surface(
        color = WhatsAppSurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.settings_puter_model_label),
              style = MaterialTheme.typography.labelSmall,
              color = WhatsAppTextSecondary,
            )
            Text(
              text = activeModel.displayName,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = WhatsAppTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("puter_active_model_value"),
            )
          }
          TextButton(
            onClick = onEditModelClick,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("edit_puter_model_button"),
          ) {
            Text(
              text = stringResource(R.string.settings_edit),
              color = AccentPurpleBright,
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = onSignInClick,
          shape = RoundedCornerShape(50),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = AccentPurple,
              contentColor = OnAccentPurple,
            ),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
          modifier =
            Modifier
              .weight(1f)
              .minimumInteractiveComponentSize()
              .height(48.dp)
              .testTag("puter_sign_in_button"),
        ) {
          Text(
            text =
              if (isPuterConnected) {
                stringResource(R.string.settings_puter_switch_account)
              } else {
                stringResource(R.string.settings_puter_sign_in)
              },
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        OutlinedButton(
          onClick = onEditTokenClick,
          shape = RoundedCornerShape(50),
          border = BorderStroke(1.dp, AccentPurple),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleBright),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
          modifier =
            Modifier
              .minimumInteractiveComponentSize()
              .height(48.dp)
              .testTag("puter_token_button"),
        ) {
          Text(
            text = stringResource(R.string.settings_puter_token_button),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }

      if (isPuterConnected) {
        TextButton(
          onClick = onDisconnectClick,
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
          modifier = Modifier.minimumInteractiveComponentSize().testTag("puter_disconnect_button"),
        ) {
          Text(
            text = stringResource(R.string.settings_puter_sign_out),
            style = MaterialTheme.typography.labelMedium,
            color = ErrorCoral,
          )
        }
      }
    }
  }
}

@Composable
private fun AppUpdatesCard(
  installedVersionName: String,
  installedCertSha256: String?,
  githubRepoSlug: String,
  updateCheckState: UpdateCheckUiState,
  onEditRepoClick: () -> Unit,
  onCheckUpdatesClick: () -> Unit,
  onDownloadApkClick: (url: String, sizeLabel: String) -> Unit,
  onOpenReleasePageClick: (url: String) -> Unit,
) {
  Surface(
    color = WhatsAppSurfaceCard,
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(0.5.dp, WhatsAppOutlineSubtle),
    modifier = Modifier.fillMaxWidth().testTag("app_updates_card"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(AccentPurpleSoftDark),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = LucideIcons.Download,
            contentDescription = null,
            tint = AccentPurpleBright,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.settings_updates_title),
            style = MaterialTheme.typography.titleMedium,
            color = WhatsAppTextPrimary,
          )
          Text(
            text = stringResource(R.string.settings_updates_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = WhatsAppTextSecondary,
          )
        }
        Surface(
          color = WhatsAppSurfaceElevated,
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = "v$installedVersionName",
            style = MaterialTheme.typography.labelSmall,
            color = AccentPurpleMuted,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      // GitHub Repo source row
      Surface(
        color = WhatsAppSurfaceContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.settings_github_repo_label),
              style = MaterialTheme.typography.labelSmall,
              color = WhatsAppTextSecondary,
            )
            Text(
              text = githubRepoSlug,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = WhatsAppTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("github_repo_value"),
            )
          }
          TextButton(
            onClick = onEditRepoClick,
            modifier = Modifier.minimumInteractiveComponentSize().testTag("edit_repo_button"),
          ) {
            Text(
              text = stringResource(R.string.settings_edit),
              color = AccentPurpleBright,
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }

      // Update check status box
      AnimatedContent(
        targetState = updateCheckState,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
        label = "update_status_transition",
      ) { state ->
        when (state) {
          UpdateCheckUiState.Idle -> Unit
          UpdateCheckUiState.Checking -> {
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(WhatsAppSurfaceContainer)
                  .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.4.dp,
                color = AccentPurpleBright,
              )
              Text(
                text = stringResource(R.string.settings_checking_updates),
                style = MaterialTheme.typography.bodyMedium,
                color = WhatsAppTextPrimary,
              )
            }
          }
          is UpdateCheckUiState.UpToDate -> {
            Surface(
              color = AccentPurpleSoftDark,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth().testTag("update_up_to_date_banner"),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Icon(
                  imageVector = LucideIcons.CircleCheck,
                  contentDescription = null,
                  tint = AccentPurpleBright,
                  modifier = Modifier.size(22.dp),
                )
                Column {
                  Text(
                    text = stringResource(R.string.settings_up_to_date),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    color = WhatsAppTextPrimary,
                  )
                  Text(
                    text =
                      stringResource(
                        R.string.settings_up_to_date_detail,
                        state.installedVersionLabel,
                        state.checkedAtLabel,
                      ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentPurpleMuted,
                  )
                }
              }
            }
          }
          is UpdateCheckUiState.UpdateAvailable -> {
            val release = state.release
            val sizeLabel = release.formattedSizeMb
            Surface(
              color = AccentPurpleSoftDark,
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, AccentPurple),
              modifier = Modifier.fillMaxWidth().testTag("update_available_banner"),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text =
                      stringResource(
                        R.string.settings_update_available,
                        release.displayVersionLabel,
                      ),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    color = WhatsAppTextPrimary,
                    modifier = Modifier.weight(1f),
                  )
                  Surface(
                    color = AccentPurple,
                    shape = RoundedCornerShape(50),
                  ) {
                    Text(
                      text = sizeLabel,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = OnAccentPurple,
                      modifier =
                        Modifier
                          .padding(horizontal = 10.dp, vertical = 4.dp)
                          .testTag("update_size_badge"),
                    )
                  }
                }

                Text(
                  text = stringResource(R.string.settings_update_size_needed, sizeLabel),
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = AccentPurpleMuted,
                  modifier = Modifier.testTag("update_size_needed_text"),
                )

                val releaseSha = release.signingSha256
                if (!releaseSha.isNullOrBlank() && !installedCertSha256.isNullOrBlank()) {
                  val certMatches = releaseSha.equals(installedCertSha256, ignoreCase = true)
                  Text(
                    text =
                      if (certMatches) {
                        "Signing key verified (compatible in-place update)"
                      } else {
                        "Signing key differs (${releaseSha.take(8)} vs ${installedCertSha256.take(8)}) — uninstall old app once before installing"
                      },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (certMatches) AccentPurpleBright else ErrorCoral,
                    modifier = Modifier.testTag("update_signature_check_text"),
                  )
                }

                if (release.releaseNotes.isNotBlank()) {
                  Text(
                    text = release.releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhatsAppTextSecondary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                  )
                }

                Button(
                  onClick = { onDownloadApkClick(release.downloadUrl, sizeLabel) },
                  shape = RoundedCornerShape(50),
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = AccentPurple,
                      contentColor = OnAccentPurple,
                    ),
                  contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .minimumInteractiveComponentSize()
                      .height(48.dp)
                      .testTag("download_update_button"),
                ) {
                  Icon(
                    imageVector = LucideIcons.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = stringResource(R.string.settings_download_update, sizeLabel),
                    style = MaterialTheme.typography.labelLarge,
                  )
                }

                Text(
                  text = stringResource(R.string.settings_install_tip),
                  style = MaterialTheme.typography.labelSmall,
                  color = WhatsAppTextSecondary,
                )

                if (release.releasePageUrl.isNotBlank()) {
                  TextButton(
                    onClick = { onOpenReleasePageClick(release.releasePageUrl) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(
                      imageVector = LucideIcons.ExternalLink,
                      contentDescription = null,
                      tint = AccentPurpleBright,
                      modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = stringResource(R.string.settings_view_release_notes),
                      style = MaterialTheme.typography.labelMedium,
                      color = AccentPurpleBright,
                    )
                  }
                }
              }
            }
          }
          is UpdateCheckUiState.Error -> {
            Surface(
              color = ErrorSurfaceDark,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(0.8.dp, ErrorCoral.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth().testTag("update_error_banner"),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = state.message,
                  style = MaterialTheme.typography.bodyMedium,
                  color = ErrorCoral,
                )
                if (state.suggestRepoEdit) {
                  TextButton(
                    onClick = onEditRepoClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Text(
                      text = stringResource(R.string.settings_edit) + " " + stringResource(R.string.settings_github_repo_label),
                      color = AccentPurpleBright,
                      style = MaterialTheme.typography.labelLarge,
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Check for updates CTA button
      val isChecking = updateCheckState is UpdateCheckUiState.Checking
      val buttonLabel =
        if (updateCheckState is UpdateCheckUiState.Idle) {
          stringResource(R.string.settings_check_updates)
        } else {
          stringResource(R.string.settings_check_again)
        }

      OutlinedButton(
        onClick = onCheckUpdatesClick,
        enabled = !isChecking,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, AccentPurple),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurpleBright),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        modifier =
          Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .height(48.dp)
            .testTag("check_updates_button"),
      ) {
        Icon(
          imageVector = LucideIcons.RefreshCw,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = buttonLabel, style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}

@Composable
private fun SettingsInfoRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: (() -> Unit)?,
  testTag: String,
) {
  val baseModifier =
    Modifier
      .fillMaxWidth()
      .minimumInteractiveComponentSize()
      .let { mod ->
        if (onClick != null) {
          mod.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = onClick,
          )
        } else {
          mod
        }
      }
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag(testTag)

  Row(
    modifier = baseModifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(WhatsAppSurfaceElevated),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AccentPurpleBright,
        modifier = Modifier.size(18.dp),
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
        color = WhatsAppTextPrimary,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = WhatsAppTextSecondary,
      )
    }
    if (onClick != null) {
      Icon(
        imageVector = LucideIcons.ChevronRight,
        contentDescription = null,
        tint = WhatsAppTextSecondary,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
private fun EditGitHubRepoDialog(
  currentRepo: String,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit,
) {
  var value by remember(currentRepo) { mutableStateOf(currentRepo) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WhatsAppSurface,
    titleContentColor = WhatsAppTextPrimary,
    textContentColor = WhatsAppTextSecondary,
    title = {
      Text(
        text = stringResource(R.string.settings_repo_dialog_title),
        style = MaterialTheme.typography.titleMedium,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = stringResource(R.string.settings_repo_dialog_hint),
          style = MaterialTheme.typography.bodyMedium,
          color = WhatsAppTextSecondary,
        )
        OutlinedTextField(
          value = value,
          onValueChange = { value = it },
          singleLine = true,
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentPurple,
              unfocusedBorderColor = WhatsAppOutlineSubtle,
              focusedTextColor = WhatsAppTextPrimary,
              unfocusedTextColor = WhatsAppTextPrimary,
              cursorColor = AccentPurpleBright,
            ),
          modifier = Modifier.fillMaxWidth().testTag("edit_repo_input"),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onSave(value) },
        enabled = value.contains("/") && value.trim().length >= 3,
        modifier = Modifier.testTag("save_repo_button"),
      ) {
        Text(
          text = stringResource(R.string.settings_repo_save),
          color = AccentPurpleBright,
        )
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
