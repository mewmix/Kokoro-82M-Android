package com.example.kokoro82m.utils

object SymbolDictionary {
    private val map = mapOf(
        '0' to "zero",
        '1' to "one",
        '2' to "two",
        '3' to "three",
        '4' to "four",
        '5' to "five",
        '6' to "six",
        '7' to "seven",
        '8' to "eight",
        '9' to "nine",
        '-' to "dash"
    )

    fun replaceSymbols(text: String): String {
        val builder = StringBuilder()
        for (ch in text) {
            val replacement = map[ch]
            if (replacement != null) {
                builder.append(' ')
                builder.append(replacement)
                builder.append(' ')
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }
}
