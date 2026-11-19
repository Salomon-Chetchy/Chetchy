package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WhatsAppDarkColorScheme =
  darkColorScheme(
    primary = AccentPurple,
    onPrimary = OnAccentPurple,
    primaryContainer = AccentPurpleSoftDark,
    onPrimaryContainer = WhatsAppTextPrimary,
    secondary = AccentPurpleBright,
    onSecondary = OnAccentPurple,
    secondaryContainer = WhatsAppSurfaceElevated,
    onSecondaryContainer = WhatsAppTextPrimary,
    background = WhatsAppBackground,
    onBackground = WhatsAppTextPrimary,
    surface = WhatsAppSurface,
    onSurface = WhatsAppTextPrimary,
    surfaceVariant = WhatsAppSurfaceContainer,
    onSurfaceVariant = WhatsAppTextSecondary,
    outline = WhatsAppOutlineSubtle,
  )

@Composable
fun WhatsAppDarkTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = WhatsAppDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
