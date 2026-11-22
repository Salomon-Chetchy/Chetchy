package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles resizing picked gallery photos into compact JPEG data URIs for Puter Vision
 * and persisting generated data-URI images into app storage for smooth Coil playback.
 */
object PuterImageStorage {
  private const val MAX_DIMENSION_PX = 1024
  private const val JPEG_QUALITY = 82

  suspend fun encodeUriToDataUrl(
    context: Context,
    uri: Uri,
  ): String? =
    withContext(Dispatchers.IO) {
      runCatching {
        val resolver = context.contentResolver
        val bounds =
          BitmapFactory.Options().apply {
            inJustDecodeBounds = true
          }
        resolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream, null, bounds)
        }

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION_PX)
        val decodeOpts =
          BitmapFactory.Options().apply {
            inSampleSize = sampleSize
          }
        val bitmap =
          resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
          } ?: return@runCatching null

        val scaled = scaleDownIfNeeded(bitmap, MAX_DIMENSION_PX)
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()

        val encoded = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        "data:image/jpeg;base64,$encoded"
      }.getOrNull()
    }

  suspend fun persistGeneratedImageIfDataUri(
    context: Context,
    imageUrlOrDataUri: String,
  ): String =
    withContext(Dispatchers.IO) {
      val trimmed = imageUrlOrDataUri.trim()
      if (!trimmed.startsWith("data:image/", ignoreCase = true)) {
        return@withContext trimmed
      }
      val commaIdx = trimmed.indexOf(',')
      if (commaIdx < 0 || commaIdx + 1 >= trimmed.length) {
        return@withContext trimmed
      }
      val header = trimmed.substring(0, commaIdx).lowercase()
      val ext =
        when {
          header.contains("jpeg") || header.contains("jpg") -> "jpg"
          header.contains("webp") -> "webp"
          else -> "png"
        }
      val base64Body = trimmed.substring(commaIdx + 1)
      runCatching {
        val bytes = Base64.decode(base64Body, Base64.DEFAULT)
        val dir = File(context.filesDir, "ai_images").apply { mkdirs() }
        val file = File(dir, "gen_${UUID.randomUUID()}.$ext")
        file.writeBytes(bytes)
        Uri.fromFile(file).toString()
      }.getOrDefault(trimmed)
    }

  private fun calculateInSampleSize(
    width: Int,
    height: Int,
    maxDimension: Int,
  ): Int {
    if (width <= 0 || height <= 0) return 1
    var sample = 1
    var w = width
    var h = height
    while (w > maxDimension * 2 || h > maxDimension * 2) {
      sample *= 2
      w /= 2
      h /= 2
    }
    return sample
  }

  private fun scaleDownIfNeeded(
    bitmap: Bitmap,
    maxDimension: Int,
  ): Bitmap {
    val maxSide = maxOf(bitmap.width, bitmap.height)
    if (maxSide <= maxDimension || maxSide <= 0) return bitmap
    val ratio = maxDimension.toFloat() / maxSide.toFloat()
    val targetW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
    val targetH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
  }
}
