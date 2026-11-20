package com.example.ai

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "PuterAiEngine"

data class PuterChatTurn(
  val role: String, // "system", "user", "assistant"
  val content: String,
)

data class PuterModelOption(
  val id: String,
  val displayName: String,
  val description: String,
  val restDriver: String,
)

object PuterModels {
  const val DEFAULT_MODEL_ID = "gpt-4o-mini"

  val availableModels: List<PuterModelOption> =
    listOf(
      PuterModelOption(
        id = "gpt-4o-mini",
        displayName = "GPT-4o Mini",
        description = "Fast, low-cost OpenAI model (Recommended)",
        restDriver = "openai-completion",
      ),
      PuterModelOption(
        id = "gpt-5-nano",
        displayName = "GPT-5 Nano",
        description = "Ultra-lightweight model for minimal consumption",
        restDriver = "openai-completion",
      ),
      PuterModelOption(
        id = "google/gemini-2.5-flash-lite",
        displayName = "Gemini 2.5 Flash Lite",
        description = "Lightweight Google model via Puter",
        restDriver = "openrouter",
      ),
      PuterModelOption(
        id = "deepseek/deepseek-chat",
        displayName = "DeepSeek Chat",
        description = "Efficient reasoning & conversational model",
        restDriver = "deepseek",
      ),
    )

  fun findById(modelId: String): PuterModelOption =
    availableModels.find { it.id == modelId } ?: availableModels.first()
}

object PuterPreferences {
  private const val PREFS_NAME = "chetchy_puter_prefs"
  private const val KEY_AUTH_TOKEN = "puter_auth_token"
  private const val KEY_USERNAME = "puter_username"
  private const val KEY_MODEL_ID = "puter_model_id"

  fun getAuthToken(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_AUTH_TOKEN, "").orEmpty().trim()
  }

  fun saveAuthToken(context: Context, token: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_AUTH_TOKEN, token.trim()).apply()
  }

  fun getUsername(context: Context): String? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_USERNAME, null)?.trim()?.ifEmpty { null }
  }

  fun saveUsername(context: Context, username: String?) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (username.isNullOrBlank()) {
      prefs.edit().remove(KEY_USERNAME).apply()
    } else {
      prefs.edit().putString(KEY_USERNAME, username.trim()).apply()
    }
  }

  fun getSelectedModel(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_MODEL_ID, PuterModels.DEFAULT_MODEL_ID)
      ?.takeIf { it.isNotBlank() }
      ?: PuterModels.DEFAULT_MODEL_ID
  }

  fun saveSelectedModel(context: Context, modelId: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_MODEL_ID, modelId.trim()).apply()
  }

  fun clearSession(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().remove(KEY_AUTH_TOKEN).remove(KEY_USERNAME).apply()
  }
}

sealed interface PuterAiResult {
  data class Success(val replyText: String) : PuterAiResult
  data class AuthRequired(val message: String) : PuterAiResult
  data class Error(val message: String) : PuterAiResult
}

interface PuterAiEngine {
  suspend fun generateReply(
    messages: List<PuterChatTurn>,
    modelId: String,
    authToken: String?,
  ): PuterAiResult
}

/**
 * Coordinates communication between Kotlin coroutines and the Puter.js v2 SDK
 * hosted inside an Android WebView, and falls back to Puter's REST drivers endpoint
 * when an explicit Puter Auth Token is supplied.
 */
class PuterWebBridgeController {
  @Volatile
  var isPageReady: Boolean = false

  @Volatile
  var isJsSignedIn: Boolean = false

  @Volatile
  var jsUsername: String? = null

  private var evaluateJs: ((String) -> Unit)? = null
  private val pendingRequests = ConcurrentHashMap<String, CompletableDeferred<PuterAiResult>>()

  var onAuthStateUpdated: ((signedIn: Boolean, username: String?, token: String?) -> Unit)? = null

  fun attachWebEvaluator(evaluator: (String) -> Unit) {
    evaluateJs = evaluator
  }

  fun detachWebEvaluator() {
    evaluateJs = null
    isPageReady = false
  }

  fun onBridgeReady(signedIn: Boolean, username: String?, token: String?) {
    isPageReady = true
    isJsSignedIn = signedIn
    jsUsername = username?.ifBlank { null }
    onAuthStateUpdated?.invoke(signedIn, jsUsername, token?.ifBlank { null })
  }

  fun onAuthChanged(signedIn: Boolean, username: String?, token: String?) {
    isJsSignedIn = signedIn
    jsUsername = username?.ifBlank { null }
    onAuthStateUpdated?.invoke(signedIn, jsUsername, token?.ifBlank { null })
  }

  fun onChatResult(requestId: String, text: String) {
    val deferred = pendingRequests.remove(requestId) ?: return
    val cleaned = text.trim()
    if (cleaned.isNotEmpty()) {
      deferred.complete(PuterAiResult.Success(cleaned))
    } else {
      deferred.complete(PuterAiResult.Error("Puter AI returned an empty response."))
    }
  }

  fun onChatError(requestId: String, errorMessage: String) {
    val deferred = pendingRequests.remove(requestId) ?: return
    val lower = errorMessage.lowercase()
    if (
      lower.contains("auth") ||
      lower.contains("sign in") ||
      lower.contains("login") ||
      lower.contains("401") ||
      lower.contains("token") ||
      lower.contains("unauthorized")
    ) {
      deferred.complete(PuterAiResult.AuthRequired(errorMessage))
    } else {
      deferred.complete(PuterAiResult.Error(errorMessage))
    }
  }

  fun triggerInteractiveSignIn() {
    val evaluator = evaluateJs ?: return
    evaluator("window.chetchyStartSignIn && window.chetchyStartSignIn();")
  }

  fun triggerSignOut() {
    isJsSignedIn = false
    jsUsername = null
    evaluateJs?.invoke("window.chetchySignOut && window.chetchySignOut();")
  }

  suspend fun chatViaWebBridge(
    messages: List<PuterChatTurn>,
    modelId: String,
    authToken: String?,
  ): PuterAiResult? {
    val evaluator = evaluateJs ?: return null
    if (!isPageReady) return null

    val requestId = UUID.randomUUID().toString()
    val deferred = CompletableDeferred<PuterAiResult>()
    pendingRequests[requestId] = deferred

    val messagesArray =
      JSONArray().apply {
        messages.forEach { turn ->
          put(
            JSONObject().apply {
              put("role", turn.role)
              put("content", turn.content)
            }
          )
        }
      }

    val payloadJson =
      JSONObject()
        .apply {
          put("requestId", requestId)
          put("model", modelId)
          put("authToken", authToken.orEmpty())
          put("messages", messagesArray)
        }
        .toString()

    val escapedPayload = JSONObject.quote(payloadJson)
    withContext(Dispatchers.Main) {
      evaluator("window.chetchyPuterChat($escapedPayload);")
    }

    val result = withTimeoutOrNull(45_000L) { deferred.await() }
    if (result == null) {
      pendingRequests.remove(requestId)
      return PuterAiResult.Error("Puter AI request timed out.")
    }
    return result
  }
}

class HybridPuterAiEngine(
  private val bridgeController: PuterWebBridgeController? = null,
) : PuterAiEngine {

  override suspend fun generateReply(
    messages: List<PuterChatTurn>,
    modelId: String,
    authToken: String?,
  ): PuterAiResult {
    val cleanToken = authToken?.trim().orEmpty()

    // 1. Prefer live Puter.js WebView bridge when available and ready
    if (bridgeController != null && bridgeController.isPageReady) {
      if (cleanToken.isEmpty() && !bridgeController.isJsSignedIn) {
        return PuterAiResult.AuthRequired(
          "Please sign in to your Puter account or add a Puter Auth Token to chat with AI personas."
        )
      }
      val bridgeResult = bridgeController.chatViaWebBridge(messages, modelId, cleanToken)
      if (bridgeResult != null) {
        return bridgeResult
      }
    }

    // 2. Direct REST call to Puter drivers endpoint using Auth Token
    if (cleanToken.isEmpty()) {
      return PuterAiResult.AuthRequired(
        "Connect your free Puter account in Settings -> Puter AI service (or paste a Puter Auth Token) to chat with AI personas."
      )
    }

    return callPuterDriversRest(messages = messages, modelId = modelId, authToken = cleanToken)
  }
}

suspend fun callPuterDriversRest(
  messages: List<PuterChatTurn>,
  modelId: String,
  authToken: String,
): PuterAiResult =
  withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
      val modelOption = PuterModels.findById(modelId)
      val payload =
        JSONObject().apply {
          put("interface", "puter-chat-completion")
          put("driver", modelOption.restDriver)
          put("test_mode", false)
          put("method", "complete")
          put(
            "args",
            JSONObject().apply {
              put("model", modelId)
              put("stream", false)
              put(
                "messages",
                JSONArray().apply {
                  messages.forEach { turn ->
                    put(
                      JSONObject().apply {
                        put("role", turn.role)
                        put("content", turn.content)
                      }
                    )
                  }
                },
              )
            },
          )
        }

      val url = URL("https://api.puter.com/drivers/call")
      connection =
        (url.openConnection() as HttpURLConnection).apply {
          requestMethod = "POST"
          connectTimeout = 15_000
          readTimeout = 45_000
          doOutput = true
          setRequestProperty("Content-Type", "application/json")
          setRequestProperty("Accept", "application/json")
          setRequestProperty("Authorization", "Bearer $authToken")
          setRequestProperty("Origin", "https://puter.com")
          setRequestProperty("User-Agent", "Chetchy-Android")
        }

      OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
        writer.write(payload.toString())
        writer.flush()
      }

      val code = connection.responseCode
      val stream = if (code in 200..299) connection.inputStream else connection.errorStream
      val body = stream?.let { BufferedReader(InputStreamReader(it)).use { r -> r.readText() } }.orEmpty()

      if (code == 401 || code == 403) {
        return@withContext PuterAiResult.AuthRequired(
          "Puter session expired or unauthorized (HTTP $code). Please reconnect your Puter account."
        )
      }

      if (code !in 200..299) {
        return@withContext PuterAiResult.Error(
          extractPuterErrorMessage(body) ?: "Puter AI returned HTTP $code."
        )
      }

      parsePuterDriverResponse(body)
    } catch (e: Exception) {
      Log.e(TAG, "Puter REST request failed", e)
      PuterAiResult.Error(e.localizedMessage ?: "Failed to connect to Puter AI.")
    } finally {
      connection?.disconnect()
    }
  }

fun parsePuterDriverResponse(rawJson: String): PuterAiResult {
  return try {
    val root = JSONObject(rawJson)
    if (root.has("success") && !root.optBoolean("success", true)) {
      val errMsg = extractPuterErrorMessage(rawJson) ?: "Puter AI request failed."
      val lower = errMsg.lowercase()
      return if (lower.contains("auth") || lower.contains("token") || lower.contains("unauthorized")) {
        PuterAiResult.AuthRequired(errMsg)
      } else {
        PuterAiResult.Error(errMsg)
      }
    }

    val resultObj = root.optJSONObject("result")
    val directText =
      extractContentText(resultObj)
        ?: extractContentText(root)
        ?: root.optString("result").takeIf { it.isNotBlank() && it != "null" && !it.startsWith("{") }

    if (!directText.isNullOrBlank()) {
      PuterAiResult.Success(directText.trim())
    } else {
      PuterAiResult.Error("Puter AI returned an empty response.")
    }
  } catch (e: Exception) {
    Log.e(TAG, "Failed to parse Puter response", e)
    PuterAiResult.Error("Could not parse Puter AI response.")
  }
}

private fun extractContentText(obj: JSONObject?): String? {
  if (obj == null) return null

  val message = obj.optJSONObject("message")
  if (message != null) {
    val contentStr = message.optString("content")
    if (contentStr.isNotBlank() && contentStr != "null") return contentStr

    val contentArr = message.optJSONArray("content")
    if (contentArr != null && contentArr.length() > 0) {
      val parts =
        (0 until contentArr.length()).mapNotNull { idx ->
          contentArr.optJSONObject(idx)?.optString("text")?.takeIf { it.isNotBlank() }
        }
      if (parts.isNotEmpty()) return parts.joinToString("")
    }
  }

  val choices = obj.optJSONArray("choices")
  if (choices != null && choices.length() > 0) {
    val first = choices.optJSONObject(0)
    val fromChoice = extractContentText(first)
    if (!fromChoice.isNullOrBlank()) return fromChoice
    val deltaText = first?.optString("text")?.takeIf { it.isNotBlank() && it != "null" }
    if (!deltaText.isNullOrBlank()) return deltaText
  }

  val textField = obj.optString("text")
  if (textField.isNotBlank() && textField != "null") return textField

  return null
}

private fun extractPuterErrorMessage(rawJson: String): String? {
  if (rawJson.isBlank()) return null
  return runCatching {
    val root = JSONObject(rawJson)
    val errObj = root.optJSONObject("error")
    errObj?.optString("message")?.takeIf { it.isNotBlank() }
      ?: root.optString("error").takeIf { it.isNotBlank() && it != "null" && !it.startsWith("{") }
      ?: root.optString("message").takeIf { it.isNotBlank() && it != "null" }
  }.getOrNull()
}
