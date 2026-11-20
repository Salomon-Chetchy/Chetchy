package com.example.ui.components

import android.annotation.SuppressLint
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ai.PuterModels
import com.example.ai.PuterWebBridgeController
import com.example.ui.icons.LucideIcons
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleBright
import com.example.ui.theme.AccentPurpleSoftDark
import com.example.ui.theme.OnAccentPurple
import com.example.ui.theme.WhatsAppOutlineSubtle
import com.example.ui.theme.WhatsAppSurface
import com.example.ui.theme.WhatsAppSurfaceCard
import com.example.ui.theme.WhatsAppTextPrimary
import com.example.ui.theme.WhatsAppTextSecondary

private class PuterJsAndroidInterface(
  private val controller: PuterWebBridgeController,
  private val onPopupDismissRequested: () -> Unit,
) {
  @JavascriptInterface
  fun onBridgeReady(signedIn: Boolean, username: String?, token: String?) {
    controller.onBridgeReady(signedIn = signedIn, username = username, token = token)
  }

  @JavascriptInterface
  fun onAuthChanged(signedIn: Boolean, username: String?, token: String?) {
    controller.onAuthChanged(signedIn = signedIn, username = username, token = token)
    if (signedIn) {
      onPopupDismissRequested()
    }
  }

  @JavascriptInterface
  fun onChatResult(requestId: String, replyText: String) {
    controller.onChatResult(requestId, replyText)
  }

  @JavascriptInterface
  fun onChatError(requestId: String, errorMessage: String) {
    controller.onChatError(requestId, errorMessage)
  }
}

private val PuterBridgeHtml =
  """
  <!DOCTYPE html>
  <html>
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <script src="https://js.puter.com/v2/"></script>
  </head>
  <body style="background:#0B141A;color:#E9EDEF;font-family:sans-serif;margin:0;padding:16px;">
    <script>
      (function () {
        function readAuthToken() {
          try {
            if (window.puter && puter.authToken) return String(puter.authToken);
            if (window.localStorage) {
              return localStorage.getItem("puter.auth.token") || localStorage.getItem("auth_token") || "";
            }
          } catch (e) {}
          return "";
        }

        async function syncAuthState(notifyReady) {
          try {
            if (!window.puter) return;
            const signedIn = !!(puter.auth && puter.auth.isSignedIn && puter.auth.isSignedIn());
            let username = "";
            if (signedIn && puter.auth.getUser) {
              try {
                const user = await puter.auth.getUser();
                username = (user && user.username) ? String(user.username) : "";
              } catch (e) {}
            }
            const token = readAuthToken();
            if (notifyReady) {
              AndroidPuterBridge.onBridgeReady(signedIn, username, token);
            } else {
              AndroidPuterBridge.onAuthChanged(signedIn, username, token);
            }
          } catch (e) {
            if (notifyReady) AndroidPuterBridge.onBridgeReady(false, "", "");
          }
        }

        window.chetchyStartSignIn = async function () {
          try {
            if (!window.puter || !puter.auth || !puter.auth.signIn) return;
            await puter.auth.signIn();
            await syncAuthState(false);
          } catch (e) {
            AndroidPuterBridge.onAuthChanged(false, "", "");
          }
        };

        window.chetchySignOut = async function () {
          try {
            if (window.puter && puter.auth && puter.auth.signOut) {
              await puter.auth.signOut();
            }
            if (window.localStorage) {
              localStorage.removeItem("puter.auth.token");
            }
          } catch (e) {}
          AndroidPuterBridge.onAuthChanged(false, "", "");
        };

        window.chetchyPuterChat = async function (payloadJson) {
          let requestId = "";
          try {
            const payload = JSON.parse(payloadJson);
            requestId = payload.requestId || "";
            if (!window.puter || !puter.ai || !puter.ai.chat) {
              AndroidPuterBridge.onChatError(requestId, "Puter.js SDK is still loading.");
              return;
            }
            if (payload.authToken && payload.authToken.length > 0 && puter.setAuthToken) {
              puter.setAuthToken(payload.authToken);
            }
            const resp = await puter.ai.chat(payload.messages, { model: payload.model || "gpt-4o-mini" });
            let text = "";
            if (typeof resp === "string") {
              text = resp;
            } else if (resp && resp.message && typeof resp.message.content === "string") {
              text = resp.message.content;
            } else if (resp && resp.message && Array.isArray(resp.message.content)) {
              text = resp.message.content.map(function (item) { return item && item.text ? item.text : ""; }).join("");
            } else if (resp && resp.text) {
              text = String(resp.text);
            } else if (resp) {
              text = JSON.stringify(resp);
            }
            AndroidPuterBridge.onChatResult(requestId, text);
          } catch (err) {
            const msg = (err && (err.message || (err.error && err.error.message) || JSON.stringify(err))) || "Puter AI error";
            AndroidPuterBridge.onChatError(requestId, String(msg));
          }
        };

        window.addEventListener("load", function () {
          setTimeout(function () { syncAuthState(true); }, 350);
        });
      })();
    </script>
  </body>
  </html>
  """.trimIndent()

/**
 * Hosts the invisible Puter.js v2 WebView bridge and displays a modal WebView popup
 * whenever Puter authentication spawns a window (puter.auth.signIn()).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PuterWebBridgeHost(
  controller: PuterWebBridgeController,
) {
  if (LocalInspectionMode.current) return

  var popupWebView by remember { mutableStateOf<WebView?>(null) }

  DisposableEffect(controller) {
    onDispose {
      controller.detachWebEvaluator()
      popupWebView?.destroy()
      popupWebView = null
    }
  }

  Box(modifier = Modifier.size(1.dp)) {
    AndroidView(
      factory = { context ->
        runCatching {
          WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(1, 1)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            addJavascriptInterface(
              PuterJsAndroidInterface(
                controller = controller,
                onPopupDismissRequested = {
                  post {
                    popupWebView?.destroy()
                    popupWebView = null
                  }
                },
              ),
              "AndroidPuterBridge",
            )

            webViewClient = object : WebViewClient() {}
            webChromeClient = object : WebChromeClient() {
              override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?,
              ): Boolean {
                val parent = view ?: return false
                val child =
                  WebView(parent.context).apply {
                    layoutParams =
                      ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                      )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.setSupportMultipleWindows(true)
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    webViewClient = object : WebViewClient() {
                      override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                      ): Boolean = false
                    }
                    webChromeClient = object : WebChromeClient() {
                      override fun onCloseWindow(window: WebView?) {
                        popupWebView?.destroy()
                        popupWebView = null
                      }
                    }
                  }
                popupWebView = child
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                transport.webView = child
                resultMsg.sendToTarget()
                return true
              }

              override fun onCloseWindow(window: WebView?) {
                popupWebView?.destroy()
                popupWebView = null
              }
            }

            controller.attachWebEvaluator { js ->
              post { evaluateJavascript(js, null) }
            }

            loadDataWithBaseURL(
              "https://app.puter.site/",
              PuterBridgeHtml,
              "text/html",
              "UTF-8",
              null,
            )
          }
        }.getOrElse {
          WebView(context)
        }
      },
      modifier = Modifier.size(1.dp),
    )
  }

  val activePopup = popupWebView
  if (activePopup != null) {
    Dialog(
      onDismissRequest = {
        activePopup.destroy()
        popupWebView = null
      },
      properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
      Surface(
        color = WhatsAppSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = stringResource(R.string.puter_auth_dialog_title),
              style = MaterialTheme.typography.titleMedium,
              color = WhatsAppTextPrimary,
            )
            IconButton(
              onClick = {
                activePopup.destroy()
                popupWebView = null
              },
            ) {
              Icon(
                imageVector = LucideIcons.Close,
                contentDescription = stringResource(R.string.cd_close),
                tint = WhatsAppTextPrimary,
              )
            }
          }
          AndroidView(
            factory = { activePopup },
            modifier = Modifier.weight(1f).fillMaxWidth(),
          )
        }
      }
    }
  }
}

@Composable
fun PuterConnectPromptDialog(
  onDismiss: () -> Unit,
  onSignInWithPuter: () -> Unit,
  onEnterTokenClick: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WhatsAppSurface,
    titleContentColor = WhatsAppTextPrimary,
    textContentColor = WhatsAppTextSecondary,
    title = {
      Text(
        text = stringResource(R.string.puter_connect_prompt_title),
        style = MaterialTheme.typography.titleMedium,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = stringResource(R.string.puter_connect_prompt_body),
          style = MaterialTheme.typography.bodyMedium,
        )
        Button(
          onClick = {
            onDismiss()
            onSignInWithPuter()
          },
          shape = RoundedCornerShape(50),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = AccentPurple,
              contentColor = OnAccentPurple,
            ),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("puter_prompt_sign_in_button"),
        ) {
          Text(
            text = stringResource(R.string.settings_puter_sign_in),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onDismiss()
          onEnterTokenClick()
        },
      ) {
        Text(
          text = stringResource(R.string.settings_puter_token_button),
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

@Composable
fun PuterAuthTokenDialog(
  currentToken: String,
  onDismiss: () -> Unit,
  onSaveToken: (String) -> Unit,
) {
  var tokenValue by remember(currentToken) { mutableStateOf(currentToken) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WhatsAppSurface,
    titleContentColor = WhatsAppTextPrimary,
    textContentColor = WhatsAppTextSecondary,
    title = {
      Text(
        text = stringResource(R.string.puter_token_dialog_title),
        style = MaterialTheme.typography.titleMedium,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = stringResource(R.string.puter_token_dialog_hint),
          style = MaterialTheme.typography.bodyMedium,
          color = WhatsAppTextSecondary,
        )
        OutlinedTextField(
          value = tokenValue,
          onValueChange = { tokenValue = it },
          placeholder = {
            Text(
              text = stringResource(R.string.puter_token_placeholder),
              color = WhatsAppTextSecondary,
            )
          },
          singleLine = true,
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AccentPurple,
              unfocusedBorderColor = WhatsAppOutlineSubtle,
              focusedTextColor = WhatsAppTextPrimary,
              unfocusedTextColor = WhatsAppTextPrimary,
              cursorColor = AccentPurpleBright,
            ),
          modifier = Modifier.fillMaxWidth().testTag("puter_token_input"),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onSaveToken(tokenValue) },
        modifier = Modifier.testTag("save_puter_token_button"),
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

@Composable
fun PuterModelSelectorDialog(
  selectedModelId: String,
  onDismiss: () -> Unit,
  onSelectModel: (String) -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = WhatsAppSurface,
    titleContentColor = WhatsAppTextPrimary,
    textContentColor = WhatsAppTextSecondary,
    title = {
      Text(
        text = stringResource(R.string.puter_model_dialog_title),
        style = MaterialTheme.typography.titleMedium,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PuterModels.availableModels.forEach { model ->
          val isSelected = model.id == selectedModelId
          Surface(
            color = if (isSelected) AccentPurpleSoftDark else WhatsAppSurfaceCard,
            shape = RoundedCornerShape(12.dp),
            border =
              BorderStroke(
                width = 1.dp,
                color = if (isSelected) AccentPurple else WhatsAppOutlineSubtle,
              ),
            modifier =
              Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clickable { onSelectModel(model.id) }
                .testTag("puter_model_option_${model.id}"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { onSelectModel(model.id) },
                colors =
                  RadioButtonDefaults.colors(
                    selectedColor = AccentPurpleBright,
                    unselectedColor = WhatsAppTextSecondary,
                  ),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = model.displayName,
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                  color = WhatsAppTextPrimary,
                )
                Text(
                  text = model.description,
                  style = MaterialTheme.typography.bodySmall,
                  color = WhatsAppTextSecondary,
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(
          text = stringResource(R.string.qr_dialog_close),
          color = AccentPurpleBright,
        )
      }
    },
  )
}
