package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * High-contrast dark syntax color palette (ChatGPT / One Dark Pro inspired)
 * tuned for readability inside dark code block cards.
 */
object CodeSyntaxColors {
  val DefaultText = Color(0xFFE6EDF3)
  val Keyword = Color(0xFFC792EA)
  val StringLiteral = Color(0xFF98C379)
  val Comment = Color(0xFF7D8590)
  val NumberOrConstant = Color(0xFFD19A66)
  val Function = Color(0xFF61AFEF)
  val TypeOrClass = Color(0xFFE5C07B)
  val Operator = Color(0xFF56B6C2)
  val Annotation = Color(0xFF56B6C2)
  val PropertyKey = Color(0xFF79C0FF)
  val Punctuation = Color(0xFF9CA8B8)
}

private val CommonKeywords =
  setOf(
    "abstract", "as", "async", "await", "break", "by", "case", "catch", "class",
    "companion", "const", "continue", "data", "def", "default", "defer", "del",
    "delete", "do", "elif", "else", "enum", "except", "export", "extends", "final",
    "finally", "fn", "for", "from", "fun", "function", "global", "go", "if",
    "impl", "implements", "import", "in", "init", "inline", "inner", "interface",
    "internal", "is", "lambda", "lateinit", "let", "loop", "match", "mut", "new",
    "nonlocal", "not", "object", "open", "operator", "or", "out", "override",
    "package", "pass", "private", "protected", "pub", "public", "raise", "reified",
    "return", "sealed", "select", "self", "static", "struct", "super", "suspend",
    "switch", "this", "throw", "throws", "trait", "try", "type", "typealias",
    "typeof", "use", "val", "var", "vararg", "void", "when", "where", "while",
    "with", "yield", "and", "SELECT", "FROM", "WHERE", "INSERT", "INTO",
    "VALUES", "UPDATE", "SET", "DELETE", "CREATE", "TABLE", "DROP", "ALTER",
    "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON", "GROUP", "BY", "ORDER",
    "ASC", "DESC", "LIMIT", "AS", "AND", "OR", "NOT", "NULL", "PRIMARY", "KEY",
  )

private val ConstantLiterals =
  setOf(
    "true", "false", "null", "nil", "none", "True", "False", "None",
    "undefined", "NaN", "Infinity", "it",
  )

private val BuiltinTypeNames =
  setOf(
    "int", "long", "float", "double", "bool", "boolean", "char", "byte", "short",
    "str", "string", "number", "any", "never", "unknown", "list", "dict", "tuple",
    "set", "map", "vec", "usize", "isize", "i32", "i64", "u32", "u64", "f32", "f64",
    "Unit", "Nothing", "Any", "String", "Int", "Long", "Float", "Double", "Boolean",
    "List", "Map", "Set", "Pair", "Result", "StateFlow", "Flow", "Modifier",
  )

private val HashCommentLanguages =
  setOf(
    "py", "python", "bash", "sh", "zsh", "shell", "yaml", "yml", "rb", "ruby",
    "perl", "r", "toml", "dockerfile", "docker", "makefile", "make", "ini", "conf",
    "env", "ps1", "powershell",
  )

private val DashCommentLanguages =
  setOf("sql", "psql", "mysql", "sqlite", "postgres", "postgresql", "lua", "haskell", "hs")

private val MarkupLanguages =
  setOf("html", "xml", "svg", "xhtml", "jsx", "tsx", "vue")

/**
 * Tokenizes source code into syntax-colored spans for dark ChatGPT-style code blocks.
 */
fun highlightCodeSyntax(
  code: String,
  language: String = "",
): AnnotatedString {
  if (code.isEmpty()) return AnnotatedString("")
  val lang = language.trim().lowercase()
  val len = code.length

  return buildAnnotatedString {
    var i = 0

    while (i < len) {
      val ch = code[i]

      // HTML / XML multi-line comments <!-- ... -->
      if (code.startsWith("<!--", i)) {
        val end = code.indexOf("-->", startIndex = i + 4)
        val stop = if (end >= 0) end + 3 else len
        withStyle(SpanStyle(color = CodeSyntaxColors.Comment, fontStyle = FontStyle.Italic)) {
          append(code.substring(i, stop))
        }
        i = stop
        continue
      }

      // C/Kotlin/JS block comment /* ... */
      if (code.startsWith("/*", i)) {
        val end = code.indexOf("*/", startIndex = i + 2)
        val stop = if (end >= 0) end + 2 else len
        withStyle(SpanStyle(color = CodeSyntaxColors.Comment, fontStyle = FontStyle.Italic)) {
          append(code.substring(i, stop))
        }
        i = stop
        continue
      }

      // Single-line // comment
      if (code.startsWith("//", i)) {
        val end = code.indexOf('\n', startIndex = i + 2)
        val stop = if (end >= 0) end else len
        withStyle(SpanStyle(color = CodeSyntaxColors.Comment, fontStyle = FontStyle.Italic)) {
          append(code.substring(i, stop))
        }
        i = stop
        continue
      }

      // SQL / Lua -- comment
      if (code.startsWith("--", i) && (lang in DashCommentLanguages || lang.isEmpty())) {
        val next = code.getOrNull(i + 2)
        if (next == null || next == ' ' || next == '\t' || next == '-') {
          val end = code.indexOf('\n', startIndex = i + 2)
          val stop = if (end >= 0) end else len
          withStyle(SpanStyle(color = CodeSyntaxColors.Comment, fontStyle = FontStyle.Italic)) {
            append(code.substring(i, stop))
          }
          i = stop
          continue
        }
      }

      // Python / Shell / YAML # comment (or C preprocessor #include)
      if (ch == '#') {
        val isPreprocessor =
          lang in setOf("c", "cpp", "c++", "h", "hpp", "cs", "csharp") &&
            i + 1 < len && code[i + 1].isLetter()
        if (isPreprocessor) {
          var end = i + 1
          while (end < len && (code[end].isLetterOrDigit() || code[end] == '_')) end++
          withStyle(SpanStyle(color = CodeSyntaxColors.Keyword, fontWeight = FontWeight.Medium)) {
            append(code.substring(i, end))
          }
          i = end
          continue
        } else if (lang in HashCommentLanguages || lang.isEmpty()) {
          val end = code.indexOf('\n', startIndex = i + 1)
          val stop = if (end >= 0) end else len
          withStyle(SpanStyle(color = CodeSyntaxColors.Comment, fontStyle = FontStyle.Italic)) {
            append(code.substring(i, stop))
          }
          i = stop
          continue
        }
      }

      // Triple-quoted strings (Python / Kotlin)
      if (code.startsWith("\"\"\"", i) || code.startsWith("'''", i)) {
        val delim = code.substring(i, i + 3)
        val end = code.indexOf(delim, startIndex = i + 3)
        val stop = if (end >= 0) end + 3 else len
        withStyle(SpanStyle(color = CodeSyntaxColors.StringLiteral)) {
          append(code.substring(i, stop))
        }
        i = stop
        continue
      }

      // Quoted strings: "...", '...', `...`
      if (ch == '"' || ch == '\'' || ch == '`') {
        val stop = findQuotedEnd(code, i, ch)
        val token = code.substring(i, stop)
        // Check if this quoted string is a JSON / JS object key ("key":)
        var lookahead = stop
        while (lookahead < len && (code[lookahead] == ' ' || code[lookahead] == '\t')) {
          lookahead++
        }
        val isJsonKey = ch == '"' && lookahead < len && code[lookahead] == ':'
        val color =
          if (isJsonKey) CodeSyntaxColors.PropertyKey else CodeSyntaxColors.StringLiteral
        withStyle(SpanStyle(color = color)) {
          append(token)
        }
        i = stop
        continue
      }

      // Annotations / decorators (@Composable, @Override, @app.get)
      if (ch == '@' && i + 1 < len && (code[i + 1].isLetter() || code[i + 1] == '_')) {
        var end = i + 1
        while (end < len && (code[end].isLetterOrDigit() || code[end] == '_')) {
          end++
        }
        withStyle(SpanStyle(color = CodeSyntaxColors.Annotation)) {
          append(code.substring(i, end))
        }
        i = end
        continue
      }

      // Markup tags (<div, </div, <Text)
      if (ch == '<' && (lang in MarkupLanguages || lang.isEmpty())) {
        val isClosing = i + 1 < len && code[i + 1] == '/'
        val tagStart = if (isClosing) i + 2 else i + 1
        if (tagStart < len && code[tagStart].isLetter()) {
          var end = tagStart
          while (end < len && (code[end].isLetterOrDigit() || code[end] in charArrayOf('-', '_', ':'))) {
            end++
          }
          withStyle(SpanStyle(color = CodeSyntaxColors.Punctuation)) {
            append(code.substring(i, tagStart))
          }
          val tagName = code.substring(tagStart, end)
          val tagColor =
            if (tagName.first().isUpperCase()) CodeSyntaxColors.TypeOrClass else CodeSyntaxColors.Keyword
          withStyle(SpanStyle(color = tagColor, fontWeight = FontWeight.Medium)) {
            append(tagName)
          }
          i = end
          continue
        }
      }

      // Numbers (decimal, hex 0x..., binary 0b..., floats, underscores)
      val isNumberStart =
        ch.isDigit() ||
          (ch == '.' && i + 1 < len && code[i + 1].isDigit() && (i == 0 || !code[i - 1].isLetterOrDigit()))
      if (isNumberStart && (i == 0 || !isIdentContinue(code[i - 1]))) {
        var end = i + 1
        while (end < len) {
          val c = code[end]
          if (c.isLetterOrDigit() || c == '_' || c == '.') {
            end++
          } else {
            break
          }
        }
        withStyle(SpanStyle(color = CodeSyntaxColors.NumberOrConstant)) {
          append(code.substring(i, end))
        }
        i = end
        continue
      }

      // Identifiers, keywords, functions, types, YAML/CSS keys
      if (ch.isLetter() || ch == '_') {
        var end = i + 1
        while (end < len && isIdentContinue(code[end])) {
          end++
        }
        val ident = code.substring(i, end)

        var lookahead = end
        while (lookahead < len && (code[lookahead] == ' ' || code[lookahead] == '\t')) {
          lookahead++
        }
        val followedByParen = lookahead < len && code[lookahead] == '('
        val followedByColon =
          lookahead < len &&
            code[lookahead] == ':' &&
            (lookahead + 1 >= len || code[lookahead + 1] != ':')

        val style =
          when {
            ident in CommonKeywords ->
              SpanStyle(color = CodeSyntaxColors.Keyword, fontWeight = FontWeight.Medium)
            ident in ConstantLiterals ->
              SpanStyle(color = CodeSyntaxColors.NumberOrConstant, fontWeight = FontWeight.Medium)
            ident in BuiltinTypeNames ->
              SpanStyle(color = CodeSyntaxColors.TypeOrClass)
            followedByParen ->
              SpanStyle(color = CodeSyntaxColors.Function)
            ident.first().isUpperCase() && ident.any { it.isLowerCase() } ->
              SpanStyle(color = CodeSyntaxColors.TypeOrClass)
            (lang in setOf("yaml", "yml", "css", "scss") || lang.isEmpty()) && followedByColon ->
              SpanStyle(color = CodeSyntaxColors.PropertyKey)
            else ->
              SpanStyle(color = CodeSyntaxColors.DefaultText)
          }

        withStyle(style) {
          append(ident)
        }
        i = end
        continue
      }

      // Multi-char operators
      if (
        code.startsWith("->", i) ||
        code.startsWith("=>", i) ||
        code.startsWith("::", i) ||
        code.startsWith("===", i) ||
        code.startsWith("!==", i) ||
        code.startsWith("==", i) ||
        code.startsWith("!=", i) ||
        code.startsWith("<=", i) ||
        code.startsWith(">=", i) ||
        code.startsWith("&&", i) ||
        code.startsWith("||", i)
      ) {
        val opLen = if (code.startsWith("===", i) || code.startsWith("!==", i)) 3 else 2
        withStyle(SpanStyle(color = CodeSyntaxColors.Operator)) {
          append(code.substring(i, i + opLen))
        }
        i += opLen
        continue
      }

      // Single-char operators
      if (ch in charArrayOf('=', '+', '-', '*', '/', '%', '<', '>', '!', '&', '|', '^', '~', '?')) {
        withStyle(SpanStyle(color = CodeSyntaxColors.Operator)) {
          append(ch)
        }
        i++
        continue
      }

      // Brackets & punctuation
      if (ch in charArrayOf('(', ')', '{', '}', '[', ']', ',', ';', ':', '.')) {
        withStyle(SpanStyle(color = CodeSyntaxColors.Punctuation)) {
          append(ch)
        }
        i++
        continue
      }

      // Whitespace or any other character
      append(ch)
      i++
    }
  }
}

private fun isIdentContinue(ch: Char): Boolean = ch.isLetterOrDigit() || ch == '_'

private fun findQuotedEnd(text: String, startIndex: Int, quoteChar: Char): Int {
  var pos = startIndex + 1
  val len = text.length
  while (pos < len) {
    val c = text[pos]
    if (c == '\\') {
      pos += 2
      continue
    }
    if (c == quoteChar) {
      return pos + 1
    }
    if (c == '\n' && quoteChar != '`') {
      return pos
    }
    pos++
  }
  return len
}
