package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.zip.Inflater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DocumentMetadata(
  val fileName: String,
  val extension: String,
  val sizeBytes: Long,
  val sizeLabel: String,
  val mimeType: String,
)

data class ExtractedDocumentPayload(
  val fileName: String,
  val extension: String,
  val sizeBytes: Long,
  val sizeLabel: String,
  val mimeType: String,
  val extractedText: String,
  val pageImageDataUrls: List<String> = emptyList(),
  val pageCount: Int = 0,
  val isReadableText: Boolean = true,
)

object PdfTextExtractor {
  private const val MAX_EXTRACTED_CHARS = 24_000
  private const val MIN_READABLE_ALNUM_CHARS = 12

  fun extractTextFromPdfBytes(pdfBytes: ByteArray): String {
    if (pdfBytes.isEmpty()) return ""
    val rawLatin = String(pdfBytes, Charsets.ISO_8859_1)
    val builder = StringBuilder()

    var searchFrom = 0
    while (searchFrom < rawLatin.length && builder.length < MAX_EXTRACTED_CHARS) {
      val streamIdx = rawLatin.indexOf("stream", startIndex = searchFrom)
      if (streamIdx < 0) break

      // Verify "stream" is a standalone token (not part of "endstream" or another word)
      val prevChar = if (streamIdx > 0) rawLatin[streamIdx - 1] else ' '
      val afterStreamIdx = streamIdx + 6
      if (!prevChar.isWhitespace() && prevChar != '>') {
        searchFrom = afterStreamIdx
        continue
      }

      val dataStart =
        when {
          rawLatin.startsWith("\r\n", afterStreamIdx) -> afterStreamIdx + 2
          rawLatin.startsWith("\n", afterStreamIdx) -> afterStreamIdx + 1
          rawLatin.startsWith("\r", afterStreamIdx) -> afterStreamIdx + 1
          else -> {
            searchFrom = afterStreamIdx
            continue
          }
        }

      val endStreamIdx = rawLatin.indexOf("endstream", startIndex = dataStart)
      if (endStreamIdx < 0) break

      var dataEnd = endStreamIdx
      if (dataEnd > dataStart && rawLatin[dataEnd - 1] == '\n') dataEnd--
      if (dataEnd > dataStart && rawLatin[dataEnd - 1] == '\r') dataEnd--

      if (dataEnd > dataStart) {
        val headerLookback = rawLatin.substring((streamIdx - 250).coerceAtLeast(0), streamIdx)
        val rawChunk = pdfBytes.copyOfRange(dataStart, dataEnd)
        val isFlate = headerLookback.contains("/FlateDecode") || looksLikeZlibHeader(rawChunk)

        val decodedStreamText =
          if (isFlate) {
            inflateZlibQuietly(rawChunk)?.let { String(it, Charsets.ISO_8859_1) }
          } else {
            String(rawChunk, Charsets.ISO_8859_1)
          }

        if (!decodedStreamText.isNullOrBlank() && (decodedStreamText.contains("Tj") || decodedStreamText.contains("TJ"))) {
          val pageText = parsePdfContentStreamText(decodedStreamText)
          if (pageText.isNotBlank()) {
            if (builder.isNotEmpty()) builder.append("\n\n")
            builder.append(pageText)
          }
        }
      }

      searchFrom = endStreamIdx + 9
    }

    val cleaned = normalizeExtractedText(builder.toString())
    val alnumCount = cleaned.count { it.isLetterOrDigit() }
    return if (alnumCount >= MIN_READABLE_ALNUM_CHARS) {
      if (cleaned.length > MAX_EXTRACTED_CHARS) {
        cleaned.take(MAX_EXTRACTED_CHARS) + "\n...[truncated]"
      } else {
        cleaned
      }
    } else {
      ""
    }
  }

  private fun looksLikeZlibHeader(bytes: ByteArray): Boolean {
    if (bytes.size < 4) return false
    val b0 = bytes[0].toInt() and 0xFF
    val b1 = bytes[1].toInt() and 0xFF
    return (b0 == 0x78) && ((b0 * 256 + b1) % 31 == 0)
  }

  private fun inflateZlibQuietly(compressed: ByteArray): ByteArray? {
    val inflater = Inflater()
    return try {
      inflater.setInput(compressed)
      val out = ByteArrayOutputStream()
      val buffer = ByteArray(4096)
      var total = 0
      while (!inflater.finished() && total < 2_000_000) {
        val count = inflater.inflate(buffer)
        if (count == 0) {
          if (inflater.needsInput() || inflater.needsDictionary()) break
        } else {
          out.write(buffer, 0, count)
          total += count
        }
      }
      out.toByteArray()
    } catch (_: Exception) {
      null
    } finally {
      inflater.end()
    }
  }

  private fun parsePdfContentStreamText(stream: String): String {
    val out = StringBuilder()
    var i = 0
    val len = stream.length
    var inTextBlock = false
    val pendingArrayParts = StringBuilder()
    var inArray = false

    fun appendLineBreakIfNeeded() {
      if (out.isNotEmpty() && !out.endsWith("\n")) {
        out.append('\n')
      }
    }

    fun appendSpaceIfNeeded() {
      if (out.isNotEmpty() && !out.last().isWhitespace()) {
        out.append(' ')
      }
    }

    while (i < len) {
      val ch = stream[i]
      when {
        ch == '%' -> {
          // Skip comment until newline
          while (i < len && stream[i] != '\n' && stream[i] != '\r') i++
        }
        ch == '(' -> {
          val (decoded, nextIdx) = readPdfLiteralString(stream, i)
          if (inTextBlock) {
            if (inArray) {
              pendingArrayParts.append(decoded)
            } else {
              // Check operator right after ')'
              var j = nextIdx
              while (j < len && stream[j].isWhitespace()) j++
              if (j < len && (stream.startsWith("Tj", j) || stream[j] == '\'' || stream[j] == '"')) {
                if (stream[j] == '\'' || stream[j] == '"') appendLineBreakIfNeeded()
                out.append(decoded)
              }
            }
          }
          i = nextIdx
          continue
        }
        ch == '<' && i + 1 < len && stream[i + 1] != '<' -> {
          val closeIdx = stream.indexOf('>', startIndex = i + 1)
          if (closeIdx > i) {
            val hex = stream.substring(i + 1, closeIdx)
            val decoded = decodePdfHexString(hex)
            if (inTextBlock) {
              if (inArray) {
                pendingArrayParts.append(decoded)
              } else {
                var j = closeIdx + 1
                while (j < len && stream[j].isWhitespace()) j++
                if (j < len && (stream.startsWith("Tj", j) || stream[j] == '\'' || stream[j] == '"')) {
                  out.append(decoded)
                }
              }
            }
            i = closeIdx + 1
            continue
          }
        }
        ch == '[' && inTextBlock -> {
          inArray = true
          pendingArrayParts.clear()
        }
        ch == ']' && inTextBlock && inArray -> {
          inArray = false
          var j = i + 1
          while (j < len && stream[j].isWhitespace()) j++
          if (j < len && stream.startsWith("TJ", j)) {
            out.append(pendingArrayParts)
          }
          pendingArrayParts.clear()
        }
        inTextBlock && inArray && (ch == '-' || ch.isDigit()) -> {
          // Negative kerning < -180 typically represents a word space in TJ arrays
          val startNum = i
          if (stream[i] == '-') i++
          while (i < len && (stream[i].isDigit() || stream[i] == '.')) i++
          val num = stream.substring(startNum, i).toDoubleOrNull()
          if (num != null && num <= -180.0 && pendingArrayParts.isNotEmpty() && !pendingArrayParts.last().isWhitespace()) {
            pendingArrayParts.append(' ')
          }
          continue
        }
        ch.isLetter() || ch == '*' -> {
          val startTok = i
          while (i < len && (stream[i].isLetterOrDigit() || stream[i] == '*')) i++
          val tok = stream.substring(startTok, i)
          when (tok) {
            "BT" -> inTextBlock = true
            "ET" -> {
              inTextBlock = false
              appendLineBreakIfNeeded()
            }
            "Td", "TD", "T*" -> if (inTextBlock) appendLineBreakIfNeeded()
            "Tm" -> if (inTextBlock) appendSpaceIfNeeded()
          }
          continue
        }
      }
      i++
    }
    return out.toString()
  }

  private fun readPdfLiteralString(src: String, openParenIdx: Int): Pair<String, Int> {
    val sb = StringBuilder()
    var i = openParenIdx + 1
    var depth = 1
    val len = src.length
    while (i < len && depth > 0) {
      val c = src[i]
      if (c == '\\' && i + 1 < len) {
        val next = src[i + 1]
        when (next) {
          'n' -> { sb.append('\n'); i += 2 }
          'r' -> { sb.append('\r'); i += 2 }
          't' -> { sb.append('\t'); i += 2 }
          'b' -> { sb.append('\b'); i += 2 }
          'f' -> { sb.append('\u000C'); i += 2 }
          '(', ')', '\\' -> { sb.append(next); i += 2 }
          '\r' -> { i += if (i + 2 < len && src[i + 2] == '\n') 3 else 2 }
          '\n' -> i += 2
          in '0'..'7' -> {
            var octEnd = i + 2
            while (octEnd < len && octEnd < i + 4 && src[octEnd] in '0'..'7') octEnd++
            val code = src.substring(i + 1, octEnd).toIntOrNull(8) ?: 32
            if (code in 32..126 || code >= 160) sb.append(code.toChar()) else if (code == 10 || code == 13) sb.append(' ')
            i = octEnd
          }
          else -> { sb.append(next); i += 2 }
        }
      } else if (c == '(') {
        depth++
        sb.append('(')
        i++
      } else if (c == ')') {
        depth--
        if (depth > 0) sb.append(')')
        i++
      } else {
        sb.append(c)
        i++
      }
    }
    return sb.toString() to i
  }

  private fun decodePdfHexString(rawHex: String): String {
    val clean = rawHex.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    if (clean.length < 2) return ""
    val even = if (clean.length % 2 == 1) "${clean}0" else clean
    val bytes = ByteArray(even.length / 2)
    for (i in bytes.indices) {
      bytes[i] = even.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    return if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
      String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
    } else {
      String(bytes, Charsets.ISO_8859_1).filter { !it.isISOControl() || it == '\n' || it == '\t' }
    }
  }

  private fun normalizeExtractedText(raw: String): String =
    raw
      .replace("\r\n", "\n")
      .replace("\r", "\n")
      .lines()
      .map { it.trimEnd() }
      .joinToString("\n")
      .replace(Regex("\n{3,}"), "\n\n")
      .trim()
}

object PuterDocumentStorage {
  private const val MAX_FILE_BYTES = 12 * 1024 * 1024
  private const val MAX_TEXT_CHARS = 24_000
  private const val MAX_PDF_VISION_PAGES = 3
  private const val PDF_PAGE_MAX_DIMENSION_PX = 1024

  fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "Unknown size"
    if (sizeBytes < 1024L) return "$sizeBytes B"
    val kb = sizeBytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.US, "%.0f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.US, "%.1f MB", mb)
  }

  suspend fun queryMetadata(context: Context, uri: Uri): DocumentMetadata =
    withContext(Dispatchers.IO) {
      val resolver = context.contentResolver
      var displayName = "Document"
      var sizeBytes = 0L
      runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
          ?.use { cursor ->
            if (cursor.moveToFirst()) {
              val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
              val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
              if (nameIdx >= 0 && !cursor.isNull(nameIdx)) {
                displayName = cursor.getString(nameIdx)?.takeIf { it.isNotBlank() } ?: displayName
              }
              if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                sizeBytes = cursor.getLong(sizeIdx).coerceAtLeast(0L)
              }
            }
          }
      }
      val mimeType = resolver.getType(uri).orEmpty()
      val extFromName = displayName.substringAfterLast('.', "").uppercase(Locale.US)
      val ext =
        when {
          extFromName.isNotBlank() && extFromName != displayName.uppercase(Locale.US) -> extFromName
          mimeType.contains("pdf", ignoreCase = true) -> "PDF"
          mimeType.contains("csv", ignoreCase = true) -> "CSV"
          mimeType.contains("json", ignoreCase = true) -> "JSON"
          mimeType.contains("markdown", ignoreCase = true) -> "MD"
          mimeType.startsWith("text/", ignoreCase = true) -> "TXT"
          else -> "FILE"
        }
      DocumentMetadata(
        fileName = displayName,
        extension = ext,
        sizeBytes = sizeBytes,
        sizeLabel = formatFileSize(sizeBytes),
        mimeType = mimeType,
      )
    }

  suspend fun readDocumentFromUri(context: Context, uri: Uri): ExtractedDocumentPayload? =
    withContext(Dispatchers.IO) {
      val meta = queryMetadata(context, uri)
      val resolver = context.contentResolver
      val rawBytes =
        runCatching {
          resolver.openInputStream(uri)?.use { input ->
            val out = ByteArrayOutputStream()
            val buf = ByteArray(8192)
            var total = 0
            while (total < MAX_FILE_BYTES) {
              val read = input.read(buf)
              if (read <= 0) break
              out.write(buf, 0, read)
              total += read
            }
            out.toByteArray()
          }
        }.getOrNull() ?: return@withContext null

      val resolvedSize = if (meta.sizeBytes > 0L) meta.sizeBytes else rawBytes.size.toLong()
      val sizeLabel = formatFileSize(resolvedSize)
      val isPdf = meta.extension == "PDF" || meta.mimeType.contains("pdf", ignoreCase = true) ||
        (rawBytes.size >= 4 && String(rawBytes, 0, 4, Charsets.ISO_8859_1) == "%PDF")

      if (isPdf) {
        val extracted = PdfTextExtractor.extractTextFromPdfBytes(rawBytes)
        val (pageCount, renderedPages) =
          if (extracted.isBlank()) {
            renderPdfPagesAsDataUrls(context, uri)
          } else {
            countPdfPagesQuietly(context, uri) to emptyList()
          }

        ExtractedDocumentPayload(
          fileName = meta.fileName,
          extension = "PDF",
          sizeBytes = resolvedSize,
          sizeLabel = sizeLabel,
          mimeType = meta.mimeType.ifBlank { "application/pdf" },
          extractedText = extracted,
          pageImageDataUrls = renderedPages,
          pageCount = pageCount,
          isReadableText = extracted.isNotBlank() || renderedPages.isNotEmpty(),
        )
      } else {
        val zeroByteCount = rawBytes.take(512).count { it == 0.toByte() }
        if (zeroByteCount > 8) {
          return@withContext ExtractedDocumentPayload(
            fileName = meta.fileName,
            extension = meta.extension,
            sizeBytes = resolvedSize,
            sizeLabel = sizeLabel,
            mimeType = meta.mimeType,
            extractedText = "",
            isReadableText = false,
          )
        }
        val fullText =
          String(rawBytes, Charsets.UTF_8)
            .replace("\uFEFF", "")
            .replace("\r\n", "\n")
            .trim()
        val capped =
          if (fullText.length > MAX_TEXT_CHARS) {
            fullText.take(MAX_TEXT_CHARS) + "\n...[truncated]"
          } else {
            fullText
          }
        ExtractedDocumentPayload(
          fileName = meta.fileName,
          extension = meta.extension,
          sizeBytes = resolvedSize,
          sizeLabel = sizeLabel,
          mimeType = meta.mimeType.ifBlank { "text/plain" },
          extractedText = capped,
          isReadableText = capped.isNotBlank(),
        )
      }
    }

  private fun countPdfPagesQuietly(context: Context, uri: Uri): Int =
    runCatching {
      context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
        PdfRenderer(pfd).use { renderer -> renderer.pageCount }
      } ?: 0
    }.getOrDefault(0)

  private fun renderPdfPagesAsDataUrls(context: Context, uri: Uri): Pair<Int, List<String>> =
    runCatching {
      val pages = mutableListOf<String>()
      var pageCount = 0
      context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          pageCount = renderer.pageCount
          val limit = pageCount.coerceAtMost(MAX_PDF_VISION_PAGES)
          for (idx in 0 until limit) {
            renderer.openPage(idx).use { page ->
              val longest = maxOf(page.width, page.height).coerceAtLeast(1)
              val scale = PDF_PAGE_MAX_DIMENSION_PX.toFloat() / longest.toFloat()
              val targetW = (page.width * scale).toInt().coerceIn(200, PDF_PAGE_MAX_DIMENSION_PX)
              val targetH = (page.height * scale).toInt().coerceIn(200, PDF_PAGE_MAX_DIMENSION_PX)
              val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
              try {
                Canvas(bitmap).drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                pages.add("data:image/jpeg;base64,$b64")
              } finally {
                bitmap.recycle()
              }
            }
          }
        }
      }
      pageCount to pages
    }.getOrDefault(0 to emptyList())
}
