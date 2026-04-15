package com.ahmed.morseapp.converter

object MorseConverter {

    // ===================== قاموس الحروف الإنجليزية =====================
    private val englishToMorse: Map<Char, String> = mapOf(
        'A' to ".-",    'B' to "-...",  'C' to "-.-.",  'D' to "-..",
        'E' to ".",     'F' to "..-.",  'G' to "--.",   'H' to "....",
        'I' to "..",    'J' to ".---",  'K' to "-.-",   'L' to ".-..",
        'M' to "--",    'N' to "-.",    'O' to "---",   'P' to ".--.",
        'Q' to "--.-",  'R' to ".-.",   'S' to "...",   'T' to "-",
        'U' to "..-",   'V' to "...-",  'W' to ".--",   'X' to "-..-",
        'Y' to "-.--",  'Z' to "--.."
    )

    // ===================== قاموس الأرقام =====================
    private val numbersToMorse: Map<Char, String> = mapOf(
        '0' to "-----", '1' to ".----", '2' to "..---",
        '3' to "...--", '4' to "....-", '5' to ".....",
        '6' to "-....", '7' to "--...", '8' to "---..",
        '9' to "----."
    )

    // ===================== قاموس الرموز =====================
    private val symbolsToMorse: Map<Char, String> = mapOf(
        '.' to ".-.-.-",  ',' to "--..--",  '?' to "..--..",
        '!' to "-.-.--",  ':' to "---...",  ';' to "-.-.-.",
        '\'' to ".----.", '"' to ".-..-.",  '(' to "-.--.",
        ')' to "-.--.-",  '/' to "-..-.",   '@' to ".--.-.",
        '&' to ".-...",   '=' to "-...-",   '+' to ".-.-.",
        '-' to "-....-",  '_' to "..--.-",  '$' to "...-..-"
    )

    // ===================== قاموس الحروف العربية =====================
    private val arabicToMorse: Map<Char, String> = mapOf(
        'ا' to ".-",      'ب' to "-...",   'ت' to "-",
        'ث' to "-.-.",    'ج' to ".---",   'ح' to "....",
        'خ' to "---",     'د' to "-..",    'ذ' to "--...",
        'ر' to ".-.",     'ز' to "---..",  'س' to "...",
        'ش' to "----",    'ص' to "-..-",   'ض' to "...-",
        'ط' to "..-",     'ظ' to "-.--",   'ع' to ".-.-",
        'غ' to "--.",     'ف' to "..-.",   'ق' to "--.-",
        'ك' to "-.-",     'ل' to ".-..",   'م' to "--",
        'ن' to "-.",      'ه' to ".....",  'و' to ".--",
        'ي' to "..",      'ى' to ".-..-",  'ة' to "-.-..",
        'ء' to ".",       'ئ' to "..---",  'ؤ' to ".--.-",
        'أ' to ".--.",    'إ' to "..-..",  'آ' to ".-.-."
    )

    // حرف "لا" المركب
    private const val LA_MORSE = ".-...-"

    // ===================== الجداول المعكوسة =====================
    private val morseToEnglish: Map<String, Char> =
        englishToMorse.entries.associate { (k, v) -> v to k }

    private val morseToNumbers: Map<String, Char> =
        numbersToMorse.entries.associate { (k, v) -> v to k }

    private val morseToSymbols: Map<String, Char> =
        symbolsToMorse.entries.associate { (k, v) -> v to k }

    private val morseToArabic: Map<String, Char> =
        arabicToMorse.entries.associate { (k, v) -> v to k }

    // جدول الإنجليزي والأرقام والرموز
    private val morseToCharEN: Map<String, String> = buildMap {
        morseToSymbols.forEach { (k, v) -> put(k, v.toString()) }
        morseToNumbers.forEach { (k, v) -> put(k, v.toString()) }
        morseToEnglish.forEach { (k, v) -> put(k, v.toString()) }
    }

    // جدول العربي مع الأرقام والرموز
    private val morseToCharAR: Map<String, String> = buildMap {
        morseToSymbols.forEach { (k, v) -> put(k, v.toString()) }
        morseToNumbers.forEach { (k, v) -> put(k, v.toString()) }
        put(LA_MORSE, "لا")
        morseToArabic.forEach { (k, v) -> put(k, v.toString()) }
    }

    // ===================== كشف اللغة =====================
    enum class Language { ARABIC, ENGLISH, AUTO }

    fun detectLanguage(text: String): Language {
        val arabicCount = text.count { it in arabicToMorse }
        val englishCount = text.count { it.uppercaseChar() in englishToMorse }
        return when {
            arabicCount > 0 && arabicCount >= englishCount -> Language.ARABIC
            englishCount > 0 -> Language.ENGLISH
            else -> Language.AUTO
        }
    }

    // ===================== تحويل نص إلى مورس =====================
    fun textToMorse(text: String, language: Language = Language.AUTO): String {
        if (text.isBlank()) return ""

        val sb = StringBuilder()

        fun appendCode(code: String) {
            if (sb.isNotEmpty() && !sb.endsWith(" ")) sb.append(" ")
            sb.append(code)
        }

        var i = 0
        while (i < text.length) {
            val ch = text[i]

            if (ch == ' ') {
                val s = sb.trimEnd().toString()
                if (!s.endsWith("/")) {
                    sb.clear()
                    sb.append(s)
                    sb.append(" / ")
                }
                i++
                continue
            }

            // حرف لا المركب
            if (ch == '\u0644' && i + 1 < text.length && text[i + 1] == '\u0627') {
                appendCode(LA_MORSE)
                i += 2
                continue
            }

            // عربي
            arabicToMorse[ch]?.let { appendCode(it); i++; return@let } ?: run {
                // إنجليزي
                englishToMorse[ch.uppercaseChar()]?.let { appendCode(it); i++; return@run }
                // أرقام
                    ?: numbersToMorse[ch]?.let { appendCode(it); i++; return@run }
                // رموز
                    ?: symbolsToMorse[ch]?.let { appendCode(it); i++; return@run }
                // غير معروف
                    ?: run { appendCode("?"); i++ }
            }
        }

        return sb.toString().trim()
    }

    // ===================== تحويل مورس إلى نص =====================
    fun morseToText(morse: String, preferArabic: Boolean = false): String {
        if (morse.isBlank()) return ""

        val table = if (preferArabic) morseToCharAR else morseToCharEN
        val sb = StringBuilder()

        val normalised = morse.trim().replace(Regex("\\s*/\\s*"), " WORDSEP ")
        val words = normalised.split(" WORDSEP ")

        for ((wi, word) in words.withIndex()) {
            if (wi > 0) sb.append(" ")
            val tokens = word.trim().split(Regex("\\s+"))
            for (token in tokens) {
                if (token.isBlank()) continue
                sb.append(table[token] ?: "?")
            }
        }

        return sb.toString()
    }

    // ===================== التحقق من صحة المورس =====================
    fun isValidMorse(text: String): Boolean {
        return text.isNotBlank() &&
               text.all { it == '.' || it == '-' || it == ' ' || it == '/' }
    }

    // ===================== بيانات الجدول المرجعي =====================
    val arabicReference: List<Pair<String, String>>
        get() = arabicToMorse.map { (k, v) -> k.toString() to v } +
                listOf("لا" to LA_MORSE)

    val englishReference: List<Pair<String, String>>
        get() = englishToMorse.map { (k, v) -> k.toString() to v }

    val numbersReference: List<Pair<String, String>>
        get() = numbersToMorse.map { (k, v) -> k.toString() to v }

    val symbolsReference: List<Pair<String, String>>
        get() = symbolsToMorse.map { (k, v) -> k.toString() to v }
}
