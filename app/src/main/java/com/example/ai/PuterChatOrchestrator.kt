package com.example.ai

import com.example.audio.formatVoiceDuration

data class ChatHistoryTurnInput(
  val id: String,
  val text: String,
  val isOutgoing: Boolean,
  val mediaLabel: String? = null,
  val imageDataUrl: String? = null,
  val documentName: String? = null,
  val documentSizeLabel: String? = null,
  val documentExtractedText: String? = null,
  val documentPageImageDataUrls: List<String> = emptyList(),
  val voiceTranscript: String? = null,
  val audioDurationMs: Long = 0L,
)

sealed interface ParsedAiReply {
  data class TextOnly(val text: String) : ParsedAiReply
  data class AskImageValidation(val text: String, val offeredPrompt: String) : ParsedAiReply
  data class GenerateImage(val prompt: String, val companionText: String) : ParsedAiReply
}

object PuterChatOrchestrator {
  const val USER_IMAGE_MEMORY_LIMIT = 10
  private const val MAX_RECENT_IMAGES_IN_PAYLOAD = 4

  private val slashImagineRegex =
    Regex("^/imagine\\s+(.+)$", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

  private val explicitGenerateImageRegex =
    Regex(
      "^(?:please\\s+|can\\s+you\\s+|could\\s+you\\s+|would\\s+you\\s+|now\\s+)?(?:generate|create|make|render|produce|paint|draw|sketch)\\s+(?:me\\s+)?(?:(?:an?|some)\\s+)?(?:ai\\s+)?(?:image|picture|photo|artwork|illustration|drawing|sketch)\\s+(?:of|about|showing|for|with)\\s+(.+)$",
      RegexOption.IGNORE_CASE,
    )

  private val explicitDrawMeRegex =
    Regex(
      "^(?:please\\s+|can\\s+you\\s+|could\\s+you\\s+)?(?:draw|paint|sketch)\\s+me\\s+(?!how\\s+to\\b)(.+)$",
      RegexOption.IGNORE_CASE,
    )

  private val showMeImageRegex =
    Regex(
      "^(?:please\\s+|can\\s+you\\s+|could\\s+you\\s+)?show\\s+(?:me\\s+)?(?:an?\\s+)(?:image|picture|photo|drawing|illustration)\\s+(?:of|about|showing)\\s+(.+)$",
      RegexOption.IGNORE_CASE,
    )

  private val generateTagRegex =
    Regex("\\[GENERATE_IMAGE\\s*:\\s*(.+?)\\]", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

  private val askGenerateTagRegex =
    Regex("\\[ASK_GENERATE_IMAGE\\s*:\\s*(.+?)\\]", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

  private val affirmativeReplies =
    setOf(
      "yes",
      "y",
      "yeah",
      "yep",
      "yup",
      "sure",
      "ok",
      "okay",
      "k",
      "please",
      "yes please",
      "yeah please",
      "sure please",
      "go ahead",
      "do it",
      "yes do it",
      "generate",
      "generate it",
      "yes generate it",
      "create it",
      "show me",
      "yes show me",
      "confirm",
      "confirmed",
      "validate",
      "oui",
      "vas y",
      "vas-y",
      "daccord",
      "d'accord",
    )

  fun buildSystemInstruction(
    displayName: String,
    aiIdentity: String,
    goalTitle: String?,
    goalProgress: Int,
  ): String {
    val goalContext =
      if (!goalTitle.isNullOrBlank()) {
        "\nThe user's active goal with you is: \"$goalTitle\" (Current progress: $goalProgress%). Provide encouraging mentorship, practical next steps, and check in on their goal."
      } else {
        ""
      }

    return """
      You are $displayName. $aiIdentity$goalContext
      General capabilities: Even within your persona, you can always answer everyday questions, understand voice messages, analyze uploaded images and documents (.txt, .pdf, .csv, .md, .json), and generate images naturally inside the chat.

      Conversational Image, Voice & Document Rules:
      1. Direct image generation: If the user explicitly asks to generate, create, draw, render, or show an image/picture/photo — OR confirms/validates a previous offer you made to generate an image — include the exact tag [GENERATE_IMAGE: <detailed visual prompt in English>] on its own line.
      2. Ask to validate first for visual curiosity: When the user asks how something looks without explicitly requesting image generation (e.g., "can I see how a volcano looks like?", "how does an aurora look?", "what does a red panda look like?"), briefly describe what it looks like and ask the user to validate/confirm if they would like you to generate an image of it, and add [ASK_GENERATE_IMAGE: <detailed visual prompt in English>] at the end of your reply. Wait for user confirmation before outputting [GENERATE_IMAGE: ...].
      3. Uploaded photos & documents memory: You remember photos and documents (.txt, .pdf, etc.) uploaded by the user for up to $USER_IMAGE_MEMORY_LIMIT user messages. Always refer accurately to details from uploaded photos or document contents across follow-up turns. If the user uploads a photo or document without text instructions, briefly describe/summarize what you see or read and warmly ask what they want you to do with it.
      4. Voice messages: User voice notes arrive as speech-to-text transcripts labeled [Voice message ...]. Respond naturally and directly to what the user said as if listening to them in conversation.

      Keep replies concise, warm, and natural like a real chat message.
      When helpful for clarity or emphasis, format text with **bold**, *italic*, ~~strikethrough~~, `inline code`, > quotes, or bullet/numbered lists.
      Whenever sharing multi-line code or commands, wrap them in fenced Markdown code blocks with the language name (for example ```python or ```kotlin).
    """.trimIndent()
  }

  fun detectDirectImageGenerationPrompt(rawText: String): String? {
    val trimmed = rawText.trim()
    if (trimmed.isEmpty()) return null

    slashImagineRegex.matchEntire(trimmed)?.let { match ->
      return cleanExtractedPrompt(match.groupValues[1])
    }
    explicitGenerateImageRegex.matchEntire(trimmed)?.let { match ->
      return cleanExtractedPrompt(match.groupValues[1])
    }
    explicitDrawMeRegex.matchEntire(trimmed)?.let { match ->
      return cleanExtractedPrompt(match.groupValues[1])
    }
    showMeImageRegex.matchEntire(trimmed)?.let { match ->
      return cleanExtractedPrompt(match.groupValues[1])
    }

    return null
  }

  private fun cleanExtractedPrompt(raw: String): String? {
    val cleaned =
      raw.trim()
        .removeSuffix("?")
        .removeSuffix(".")
        .removeSuffix("!")
        .replace(Regex("\\s+please$", RegexOption.IGNORE_CASE), "")
        .trim()
    return cleaned.takeIf { it.isNotEmpty() }
  }

  fun isAffirmativeValidation(rawText: String): Boolean {
    val normalized =
      rawText.trim()
        .lowercase()
        .replace(Regex("[.!?,]+$"), "")
        .replace(Regex("\\s+"), " ")
    return normalized in affirmativeReplies
  }

  fun parseAiReplyDirectives(rawReply: String): ParsedAiReply {
    val trimmed = rawReply.trim()
    val genMatch = generateTagRegex.find(trimmed)
    if (genMatch != null) {
      val prompt = genMatch.groupValues[1].trim()
      val companion =
        trimmed
          .replace(generateTagRegex, "")
          .replace(askGenerateTagRegex, "")
          .replace(Regex("\n{3,}"), "\n\n")
          .trim()
      if (prompt.isNotEmpty()) {
        return ParsedAiReply.GenerateImage(prompt = prompt, companionText = companion)
      }
    }

    val askMatch = askGenerateTagRegex.find(trimmed)
    if (askMatch != null) {
      val offered = askMatch.groupValues[1].trim()
      val visible =
        trimmed
          .replace(askGenerateTagRegex, "")
          .replace(Regex("\n{3,}"), "\n\n")
          .trim()
      if (offered.isNotEmpty()) {
        return ParsedAiReply.AskImageValidation(
          text = visible.ifEmpty { "Would you like me to generate an image of $offered?" },
          offeredPrompt = offered,
        )
      }
    }

    return ParsedAiReply.TextOnly(trimmed)
  }

  /**
   * Builds the turn list with at least [USER_IMAGE_MEMORY_LIMIT] user messages of history,
   * keeping encoded photos and extracted document contents attached for turns within the
   * [USER_IMAGE_MEMORY_LIMIT] user-message memory window.
   */
  fun buildHistoryTurns(
    systemInstruction: String,
    messages: List<ChatHistoryTurnInput>,
    currentUserMessageId: String? = null,
    userImageMemoryLimit: Int = USER_IMAGE_MEMORY_LIMIT,
  ): List<PuterChatTurn> {
    if (messages.isEmpty()) {
      return listOf(PuterChatTurn(role = "system", content = systemInstruction))
    }

    val userIndices = messages.mapIndexedNotNull { index, msg -> if (msg.isOutgoing) index else null }
    val cutoffStartIndex =
      if (userIndices.size > userImageMemoryLimit + 2) {
        userIndices[userIndices.size - (userImageMemoryLimit + 2)]
      } else {
        0
      }

    val windowed = messages.subList(cutoffStartIndex, messages.size)

    val userMsgsAfter = IntArray(windowed.size)
    var seenUsersAfter = 0
    for (i in windowed.indices.reversed()) {
      userMsgsAfter[i] = seenUsersAfter
      if (windowed[i].isOutgoing) {
        seenUsersAfter++
      }
    }

    val allowedImageIndices = mutableSetOf<Int>()
    var attachedImageCount = 0
    for (i in windowed.indices.reversed()) {
      val msg = windowed[i]
      val imagesInTurn =
        (if (!msg.imageDataUrl.isNullOrBlank()) 1 else 0) + msg.documentPageImageDataUrls.size
      if (imagesInTurn > 0 &&
        userMsgsAfter[i] <= userImageMemoryLimit &&
        attachedImageCount + imagesInTurn <= MAX_RECENT_IMAGES_IN_PAYLOAD
      ) {
        allowedImageIndices.add(i)
        attachedImageCount += imagesInTurn
      }
    }

    val historyTurns =
      windowed.mapIndexed { idx, msg ->
        val keepMedia = idx in allowedImageIndices
        val keepDocumentText = userMsgsAfter[idx] <= userImageMemoryLimit
        val dataUrl = if (keepMedia) msg.imageDataUrl else null
        val pageImages = if (keepMedia) msg.documentPageImageDataUrls else emptyList()

        val turnText =
          when {
            msg.documentName != null -> {
              val header =
                listOfNotNull(msg.documentName, msg.documentSizeLabel?.takeIf { it.isNotBlank() })
                  .joinToString(" • ")
              val userPromptPart =
                when {
                  msg.text.isNotBlank() -> msg.text
                  msg.id == currentUserMessageId ->
                    "[The user uploaded document \"$header\" without text instructions. Briefly summarize what this document contains and ask what they would like you to do with it.]"
                  else -> "[User attached document: $header]"
                }
              val docBody = msg.documentExtractedText?.trim().orEmpty()
              when {
                keepDocumentText && docBody.isNotEmpty() ->
                  "$userPromptPart\n\n--- Document: $header ---\n$docBody"
                keepDocumentText && pageImages.isNotEmpty() ->
                  "$userPromptPart\n\n[Attached PDF pages for visual reading: $header]"
                else -> userPromptPart
              }
            }
            msg.mediaLabel == "Voice" -> {
              val durationLabel =
                if (msg.audioDurationMs > 0L) formatVoiceDuration(msg.audioDurationMs) else ""
              val cleanVoiceText = msg.voiceTranscript?.trim().orEmpty()
              when {
                cleanVoiceText.isNotEmpty() && durationLabel.isNotEmpty() ->
                  "[Voice message ($durationLabel)]: $cleanVoiceText"
                cleanVoiceText.isNotEmpty() ->
                  "[Voice message]: $cleanVoiceText"
                msg.text.isNotBlank() -> msg.text
                else -> "[User sent a voice message]"
              }
            }
            msg.text.isNotBlank() -> msg.text
            msg.id == currentUserMessageId && dataUrl != null ->
              "[The user uploaded this image without text instructions. Briefly describe what you see in the image (e.g. 'I see ... in this image') and ask what they would like you to do with it or how you can help.]"
            msg.mediaLabel == "Photo" -> "[User shared a photo]"
            msg.mediaLabel == "AI Image" -> "[AI generated image]"
            else -> ""
          }

        PuterChatTurn(
          role = if (msg.isOutgoing) "user" else "assistant",
          content = turnText,
          imageDataUrl = dataUrl,
          extraImageDataUrls = pageImages,
        )
      }.filter { it.content.isNotBlank() || !it.imageDataUrl.isNullOrBlank() || it.extraImageDataUrls.isNotEmpty() }

    return listOf(PuterChatTurn(role = "system", content = systemInstruction)) + historyTurns
  }
}
