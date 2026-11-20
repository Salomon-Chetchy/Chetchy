package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.Contact
import com.example.ui.CountryCodeOption
import com.example.ui.DefaultCountries
import com.example.ui.NewContactDraft
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
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

private val IndexLetters =
  ('A'..'Z').map { it.toString() } + "#"

@Composable
fun NewChatSheetContent(
  query: String,
  contacts: List<Contact>,
  onQueryChange: (String) -> Unit,
  onNewContactClick: () -> Unit,
  onContactClick: (Contact) -> Unit,
  onShareInviteClick: () -> Unit,
  onClose: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .navigationBarsPadding(),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(start = 16.dp, end = 24.dp, bottom = 28.dp),
    ) {
      // Centered title + right circular X close button (Image 1)
      Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      ) {
        Text(
          text = stringResource(R.string.sheet_new_chat_title),
          style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
          color = WhatsAppTextPrimary,
          modifier = Modifier.align(Alignment.Center),
        )
        Box(
          modifier =
            Modifier
              .align(Alignment.CenterEnd)
              .minimumInteractiveComponentSize()
              .size(34.dp)
              .clip(CircleShape)
              .background(WhatsAppSurfaceContainer)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClose,
              )
              .testTag("new_chat_close_button"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = LucideIcons.Close,
            contentDescription = stringResource(R.string.cd_close),
            tint = WhatsAppTextSecondary,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      // Rounded search input ("Search name or number")
      SearchFieldPill(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.sheet_search_placeholder),
        modifier = Modifier.fillMaxWidth().testTag("new_chat_search_input"),
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Grouped quick actions card
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WhatsAppSurfaceCard),
      ) {
        NewChatActionRow(
          icon = LucideIcons.Users,
          title = stringResource(R.string.sheet_new_group),
          onClick = onNewContactClick,
          testTag = "new_group_row",
        )
        CardHairlineDivider()
        NewChatActionRow(
          icon = LucideIcons.UserPlus,
          title = stringResource(R.string.sheet_new_contact),
          onClick = onNewContactClick,
          testTag = "new_contact_row",
        )
        CardHairlineDivider()
        NewChatActionRow(
          icon = LucideIcons.Megaphone,
          title = stringResource(R.string.sheet_new_broadcast),
          onClick = onNewContactClick,
          testTag = "new_broadcast_row",
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = stringResource(R.string.sheet_contacts_section_header),
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
        color = WhatsAppTextSecondary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
      )

      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WhatsAppSurfaceCard),
      ) {
        contacts.forEachIndexed { index, contact ->
          ContactRowItem(
            contact = contact,
            onClick = { onContactClick(contact) },
          )
          if (index < contacts.lastIndex) {
            CardHairlineDivider(startIndent = 64.dp)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Share invite link card
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WhatsAppSurfaceCard),
      ) {
        NewChatActionRow(
          icon = LucideIcons.Share,
          title = stringResource(R.string.sheet_share_invite_link),
          onClick = onShareInviteClick,
          testTag = "share_invite_link_row",
        )
      }
    }

    // Right-side A-Z # index rail in #7F46B8
    Column(
      modifier =
        Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 4.dp, top = 44.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
      IndexLetters.forEach { letter ->
        Text(
          text = letter,
          fontSize = 10.sp,
          lineHeight = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = AccentPurpleBright,
        )
      }
    }
  }
}

@Composable
private fun SearchFieldPill(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .height(44.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(WhatsAppSurfaceContainer)
        .padding(horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = LucideIcons.Search,
      contentDescription = null,
      tint = WhatsAppTextSecondary,
      modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
      if (value.isEmpty()) {
        Text(
          text = placeholder,
          style = MaterialTheme.typography.bodyLarge,
          color = WhatsAppTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = WhatsAppTextPrimary),
        cursorBrush = SolidColor(AccentPurple),
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun NewChatActionRow(
  icon: ImageVector,
  title: String,
  subtitle: String? = null,
  onClick: () -> Unit,
  testTag: String,
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
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = AccentPurpleBright,
      modifier = Modifier.size(22.dp),
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = WhatsAppTextPrimary,
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
          color = WhatsAppTextSecondary,
        )
      }
    }
  }
}

@Composable
private fun ContactRowItem(
  contact: Contact,
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
        .padding(horizontal = 14.dp, vertical = 11.dp)
        .testTag("contact_row_${contact.id}"),
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
      if (contact.isSelf || contact.initials.isEmpty()) {
        Icon(
          imageVector = LucideIcons.User,
          contentDescription = null,
          tint = AccentPurpleMuted,
          modifier = Modifier.size(20.dp),
        )
      } else {
        Text(
          text = contact.initials,
          style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
          color = AccentPurpleMuted,
        )
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text =
          if (contact.isSelf) {
            stringResource(R.string.sheet_message_yourself_name)
          } else {
            contact.displayName
          },
        style = MaterialTheme.typography.titleMedium,
        color = WhatsAppTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      val subtitle =
        if (contact.isSelf) {
          stringResource(R.string.sheet_message_yourself_subtitle)
        } else {
          contact.formattedPhone.ifEmpty { contact.countryName }
        }
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
        color = WhatsAppTextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun NewContactSheetContent(
  draft: NewContactDraft,
  onFirstNameChange: (String) -> Unit,
  onLastNameChange: (String) -> Unit,
  onCountryChange: (CountryCodeOption) -> Unit,
  onPhoneChange: (String) -> Unit,
  onSyncChange: (Boolean) -> Unit,
  onAiIdentityChange: (String) -> Unit,
  onGoalTitleChange: (String) -> Unit,
  onCancel: () -> Unit,
  onDone: () -> Unit,
) {
  var showCountryPicker by remember { mutableStateOf(false) }
  var showQrDialog by remember { mutableStateOf(false) }

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp)
        .padding(bottom = 28.dp),
  ) {
    // Top Header: Cancel | New contact | Done (Image 2)
    Box(
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
    ) {
      TextButton(
        onClick = onCancel,
        modifier = Modifier.align(Alignment.CenterStart).testTag("new_contact_cancel_button"),
      ) {
        Text(
          text = stringResource(R.string.action_cancel),
          style = MaterialTheme.typography.bodyLarge,
          color = AccentPurpleBright,
        )
      }
      Text(
        text = stringResource(R.string.new_contact_title),
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp),
        color = WhatsAppTextPrimary,
        modifier = Modifier.align(Alignment.Center),
      )
      TextButton(
        onClick = onDone,
        enabled = draft.canSave,
        modifier = Modifier.align(Alignment.CenterEnd).testTag("new_contact_done_button"),
      ) {
        Text(
          text = stringResource(R.string.action_done),
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
          color = if (draft.canSave) AccentPurpleBright else WhatsAppTextSecondary.copy(alpha = 0.5f),
        )
      }
    }

    // Group 1: First name / Last name
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(WhatsAppSurfaceCard),
    ) {
      PlainCardInputRow(
        value = draft.firstName,
        onValueChange = onFirstNameChange,
        placeholder = stringResource(R.string.hint_first_name),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = Modifier.testTag("first_name_input"),
      )
      CardHairlineDivider()
      PlainCardInputRow(
        value = draft.lastName,
        onValueChange = onLastNameChange,
        placeholder = stringResource(R.string.hint_last_name),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = Modifier.testTag("last_name_input"),
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Group 2: Phone -> Country > | Mobile -> +1 Phone
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(WhatsAppSurfaceCard),
    ) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = ripple(bounded = true),
              onClick = { showCountryPicker = true },
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("country_selector_row"),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.label_phone),
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
          color = WhatsAppTextPrimary,
          modifier = Modifier.width(96.dp),
        )
        Text(
          text = draft.country.name,
          style = MaterialTheme.typography.bodyLarge,
          color = WhatsAppTextPrimary,
          modifier = Modifier.weight(1f),
        )
        Icon(
          imageVector = LucideIcons.ChevronRight,
          contentDescription = null,
          tint = WhatsAppTextSecondary,
          modifier = Modifier.size(18.dp),
        )
      }

      CardHairlineDivider()

      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.label_mobile),
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
          color = WhatsAppTextPrimary,
          modifier = Modifier.width(96.dp),
        )
        Text(
          text = draft.country.dialCode,
          style = MaterialTheme.typography.bodyLarge,
          color = WhatsAppTextPrimary,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
          if (draft.phone.isEmpty()) {
            Text(
              text = stringResource(R.string.hint_phone),
              style = MaterialTheme.typography.bodyLarge,
              color = WhatsAppTextSecondary,
            )
          }
          BasicTextField(
            value = draft.phone,
            onValueChange = onPhoneChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = WhatsAppTextPrimary),
            cursorBrush = SolidColor(AccentPurple),
            modifier = Modifier.fillMaxWidth().testTag("phone_number_input"),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Group 4: AI Persona & Goal (Optional)
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(WhatsAppSurfaceCard),
    ) {
      PlainCardInputRow(
        value = draft.aiIdentity,
        onValueChange = onAiIdentityChange,
        placeholder = stringResource(R.string.hint_ai_identity),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.testTag("ai_identity_input"),
      )
      CardHairlineDivider()
      PlainCardInputRow(
        value = draft.goalTitle,
        onValueChange = onGoalTitleChange,
        placeholder = stringResource(R.string.hint_goal_title),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.testTag("goal_title_input"),
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Group 3: Sync contact to phone Switch
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(WhatsAppSurfaceCard)
          .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stringResource(R.string.label_sync_contact),
        style = MaterialTheme.typography.bodyLarge,
        color = WhatsAppTextPrimary,
      )
      Switch(
        checked = draft.syncToPhone,
        onCheckedChange = onSyncChange,
        colors =
          SwitchDefaults.colors(
            checkedThumbColor = OnAccentPurple,
            checkedTrackColor = AccentPurple,
            uncheckedThumbColor = WhatsAppTextPrimary,
            uncheckedTrackColor = WhatsAppSurfaceContainer,
            uncheckedBorderColor = WhatsAppOutlineSubtle,
          ),
        modifier = Modifier.testTag("sync_contact_switch"),
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Add via QR code action in #7F46B8
    Row(
      modifier =
        Modifier
          .align(Alignment.CenterHorizontally)
          .minimumInteractiveComponentSize()
          .clip(RoundedCornerShape(50))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = { showQrDialog = true },
          )
          .padding(horizontal = 18.dp, vertical = 10.dp)
          .testTag("add_via_qr_button"),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = LucideIcons.QrCode,
        contentDescription = null,
        tint = AccentPurpleBright,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(R.string.action_add_qr_code),
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = AccentPurpleBright,
      )
    }
  }

  if (showCountryPicker) {
    AlertDialog(
      onDismissRequest = { showCountryPicker = false },
      containerColor = WhatsAppSurface,
      titleContentColor = WhatsAppTextPrimary,
      textContentColor = WhatsAppTextPrimary,
      title = {
        Text(
          text = stringResource(R.string.country_picker_title),
          style = MaterialTheme.typography.titleLarge,
        )
      },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          DefaultCountries.forEach { option ->
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .minimumInteractiveComponentSize()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    onCountryChange(option)
                    showCountryPicker = false
                  }
                  .padding(horizontal = 8.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(text = option.name, color = WhatsAppTextPrimary)
              Text(
                text = option.dialCode,
                color = if (option == draft.country) AccentPurpleBright else WhatsAppTextSecondary,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showCountryPicker = false }) {
          Text(stringResource(R.string.action_cancel), color = AccentPurpleBright)
        }
      },
    )
  }

  if (showQrDialog) {
    AlertDialog(
      onDismissRequest = { showQrDialog = false },
      containerColor = WhatsAppSurface,
      titleContentColor = WhatsAppTextPrimary,
      textContentColor = WhatsAppTextSecondary,
      title = {
        Text(
          text = stringResource(R.string.qr_dialog_title),
          style = MaterialTheme.typography.titleLarge,
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier =
              Modifier
                .size(164.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
          ) {
            QrPatternCanvas(color = AccentPurple)
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = stringResource(R.string.qr_dialog_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = WhatsAppTextSecondary,
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showQrDialog = false }) {
          Text(stringResource(R.string.qr_dialog_close), color = AccentPurpleBright)
        }
      },
    )
  }
}

@Composable
private fun PlainCardInputRow(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  keyboardOptions: KeyboardOptions,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .minimumInteractiveComponentSize()
        .padding(horizontal = 16.dp, vertical = 14.dp),
    contentAlignment = Alignment.CenterStart,
  ) {
    if (value.isEmpty()) {
      Text(
        text = placeholder,
        style = MaterialTheme.typography.bodyLarge,
        color = WhatsAppTextSecondary,
      )
    }
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      singleLine = true,
      keyboardOptions = keyboardOptions,
      textStyle = MaterialTheme.typography.bodyLarge.copy(color = WhatsAppTextPrimary),
      cursorBrush = SolidColor(AccentPurple),
      modifier = modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun CardHairlineDivider(startIndent: androidx.compose.ui.unit.Dp = 16.dp) {
  HorizontalDivider(
    thickness = 0.5.dp,
    color = WhatsAppOutlineSubtle,
    modifier = Modifier.padding(start = startIndent),
  )
}

@Composable
private fun QrPatternCanvas(color: Color) {
  Canvas(modifier = Modifier.size(132.dp)) {
    val grid = 9
    val cell = size.width / grid
    val pattern =
      listOf(
        "111010111",
        "101001101",
        "111010111",
        "000111000",
        "101101101",
        "010110010",
        "111010101",
        "101001010",
        "111011101",
      )
    pattern.forEachIndexed { r, row ->
      row.forEachIndexed { c, ch ->
        if (ch == '1') {
          drawRect(
            color = color,
            topLeft = Offset(c * cell + 1f, r * cell + 1f),
            size = Size(cell - 2f, cell - 2f),
          )
        }
      }
    }
  }
}
