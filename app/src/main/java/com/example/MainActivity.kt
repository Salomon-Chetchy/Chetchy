package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.ChatsScreen
import com.example.ui.theme.WhatsAppDarkTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    setContent {
      WhatsAppDarkTheme {
        ChatsScreen()
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B141A)
@Composable
fun ChatsScreenPreview() {
  WhatsAppDarkTheme {
    ChatsScreen()
  }
}
