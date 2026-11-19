package com.example.update

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class UpdateReleaseInfo(
  val versionName: String,
  val versionCode: Int?,
  val buildNumber: Int?,
  val tagName: String,
  val releaseName: String,
  val apkFileName: String,
  val apkSizeBytes: Long,
  val downloadUrl: String,
  val releasePageUrl: String,
  val releaseNotes: String,
) {
  val formattedSizeMb: String
    get() = formatSizeInMb(apkSizeBytes)

  val displayVersionLabel: String
    get() =
      when {
        buildNumber != null -> "v$versionName (Build $buildNumber)"
        else -> "v$versionName"
      }
}

sealed interface UpdateCheckUiState {
  data object Idle : UpdateCheckUiState

  data object Checking : UpdateCheckUiState

  data class UpToDate(
    val installedVersionLabel: String,
    val checkedAtLabel: String,
  ) : UpdateCheckUiState

  data class UpdateAvailable(
    val release: UpdateReleaseInfo,
    val installedVersionLabel: String,
  ) : UpdateCheckUiState

  data class Error(
    val message: String,
    val suggestRepoEdit: Boolean = false,
  ) : UpdateCheckUiState
}

sealed interface UpdateFetchResult {
  data class Found(val release: UpdateReleaseInfo) : UpdateFetchResult

  data class Failure(val message: String, val suggestRepoEdit: Boolean = false) : UpdateFetchResult
}

interface AppUpdateClient {
  suspend fun fetchLatestRelease(repoSlug: String): UpdateFetchResult
}

class GitHubAppUpdateClient : AppUpdateClient {
  override suspend fun fetchLatestRelease(repoSlug: String): UpdateFetchResult =
    withContext(Dispatchers.IO) {
      val normalized = normalizeRepoSlug(repoSlug)
      if (normalized == null) {
        return@withContext UpdateFetchResult.Failure(
          message = "Enter a valid GitHub repository as owner/repo.",
          suggestRepoEdit = true,
        )
      }

      var connection: HttpURLConnection? = null
      try {
        val url = URL("https://api.github.com/repos/$normalized/releases/latest")
        connection =
          (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Chats-Android-Updater")
          }

        val code = connection.responseCode
        val stream =
          if (code in 200..299) {
            connection.inputStream
          } else {
            connection.errorStream
          }

        val body =
          stream?.let { input ->
            BufferedReader(InputStreamReader(input)).use { it.readText() }
          } ?: ""

        if (code == 404) {
          return@withContext UpdateFetchResult.Failure(
            message =
              "No published release found for \"$normalized\". Tap Edit on GitHub repo to verify the repo name or wait for a release build.",
            suggestRepoEdit = true,
          )
        }

        if (code == 403) {
          return@withContext UpdateFetchResult.Failure(
            message = "GitHub rate limit reached. Please wait a minute and try again."
          )
        }

        if (code !in 200..299) {
          return@withContext UpdateFetchResult.Failure(
            message = "Could not check updates right now (HTTP $code)."
          )
        }

        parseReleaseJson(body)
      } catch (e: Exception) {
        UpdateFetchResult.Failure(
          message =
            "Network error while checking updates: ${e.localizedMessage ?: "Check your internet connection"}"
        )
      } finally {
        connection?.disconnect()
      }
    }
}

object UpdatePreferences {
  private const val PREFS_NAME = "chats_update_prefs"
  private const val KEY_GITHUB_REPO = "github_repo_slug"
  const val DEFAULT_REPO_SLUG = "Salomon-Chetchy/Chetchy"

  fun getRepoSlug(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_GITHUB_REPO, DEFAULT_REPO_SLUG)?.takeIf { it.isNotBlank() }
      ?: DEFAULT_REPO_SLUG
  }

  fun saveRepoSlug(context: Context, repoSlug: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_GITHUB_REPO, repoSlug.trim()).apply()
  }
}

fun normalizeRepoSlug(raw: String): String? {
  val trimmed =
    raw.trim()
      .removePrefix("https://github.com/")
      .removePrefix("http://github.com/")
      .removeSuffix("/")
      .removeSuffix(".git")
  val parts = trimmed.split("/").filter { it.isNotBlank() }
  if (parts.size != 2) return null
  return "${parts[0]}/${parts[1]}"
}

fun formatSizeInMb(bytes: Long): String {
  if (bytes <= 0L) return "Unknown size"
  val mb = bytes / (1024.0 * 1024.0)
  return if (mb < 0.1) {
    String.format(Locale.US, "%.2f MB", mb)
  } else {
    String.format(Locale.US, "%.1f MB", mb)
  }
}

fun parseReleaseJson(jsonText: String): UpdateFetchResult {
  return try {
    val json = JSONObject(jsonText)
    val tagName = json.optString("tag_name", "").trim()
    val releaseName = json.optString("name", tagName).trim().ifEmpty { tagName }
    val releaseBody = json.optString("body", "").trim()
    val releasePageUrl = json.optString("html_url", "").trim()

    val assets = json.optJSONArray("assets")
    var chosenApkName = ""
    var chosenApkUrl = ""
    var chosenApkSize = 0L

    if (assets != null) {
      for (i in 0 until assets.length()) {
        val asset = assets.optJSONObject(i) ?: continue
        val assetName = asset.optString("name", "")
        if (assetName.endsWith(".apk", ignoreCase = true)) {
          val url = asset.optString("browser_download_url", "")
          val size = asset.optLong("size", 0L)
          if (chosenApkUrl.isEmpty() || assetName.equals("Chats-debug.apk", ignoreCase = true)) {
            chosenApkName = assetName
            chosenApkUrl = url
            chosenApkSize = size
          }
        }
      }
    }

    if (chosenApkUrl.isEmpty()) {
      return UpdateFetchResult.Failure(
        message = "Latest release ($tagName) does not contain an APK file yet."
      )
    }

    val (parsedVersionName, parsedBuildNumber) = parseVersionAndBuildFromTag(tagName)
    val parsedVersionCode = parseVersionCodeFromBody(releaseBody)

    UpdateFetchResult.Found(
      UpdateReleaseInfo(
        versionName = parsedVersionName,
        versionCode = parsedVersionCode,
        buildNumber = parsedBuildNumber,
        tagName = tagName,
        releaseName = releaseName,
        apkFileName = chosenApkName,
        apkSizeBytes = chosenApkSize,
        downloadUrl = chosenApkUrl,
        releasePageUrl = releasePageUrl,
        releaseNotes = releaseBody,
      )
    )
  } catch (e: Exception) {
    UpdateFetchResult.Failure("Failed to read release details from GitHub.")
  }
}

internal fun parseVersionAndBuildFromTag(tagName: String): Pair<String, Int?> {
  val clean = tagName.trim().removePrefix("v").removePrefix("V")
  val match = Regex("""^([0-9]+(?:\.[0-9]+)*)(?:-b([0-9]+))?""").find(clean)
  if (match != null) {
    val ver = match.groupValues[1]
    val build = match.groupValues[2].toIntOrNull()
    return ver to build
  }
  return clean.ifEmpty { "1.0" } to null
}

internal fun parseVersionCodeFromBody(body: String): Int? {
  val match = Regex("""versionCode\s*=\s*([0-9]+)""", RegexOption.IGNORE_CASE).find(body)
  return match?.groupValues?.getOrNull(1)?.toIntOrNull()
}

fun isNewerRelease(
  installedVersionName: String,
  installedVersionCode: Int,
  installedCiBuild: Int,
  release: UpdateReleaseInfo,
): Boolean {
  if (release.versionCode != null && release.versionCode > installedVersionCode) {
    return true
  }

  val semverCmp = compareSemanticVersions(release.versionName, installedVersionName)
  if (semverCmp > 0) return true
  if (semverCmp < 0) return false

  val releaseBuild = release.buildNumber
  if (releaseBuild != null && installedCiBuild > 0 && releaseBuild > installedCiBuild) {
    return true
  }

  return false
}

internal fun compareSemanticVersions(candidate: String, current: String): Int {
  val a = candidate.trim().removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
  val b = current.trim().removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
  val maxLen = maxOf(a.size, b.size)
  for (i in 0 until maxLen) {
    val left = a.getOrElse(i) { 0 }
    val right = b.getOrElse(i) { 0 }
    if (left != right) return left.compareTo(right)
  }
  return 0
}
