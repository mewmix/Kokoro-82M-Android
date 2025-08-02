package com.example.kokoro82m.utils

object SymbolDictionary {
    private val map = mapOf(
        '-' to "dash"
    )

    private val units = arrayOf(
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine"
    )

    private val teens = arrayOf(
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteen",
        "sixteen",
        "seventeen",
        "eighteen",
        "nineteen"
    )

    private val tens = arrayOf(
        "",
        "",
        "twenty",
        "thirty",
        "forty",
        "fifty",
        "sixty",
        "seventy",
        "eighty",
        "ninety"
    )

    private fun numberBelowHundredToWords(num: Int): String = when {
        num < 10 -> units[num]
        num < 20 -> teens[num - 10]
        else -> {
            val ten = tens[num / 10]
            val unit = num % 10
            if (unit == 0) ten else "$ten ${units[unit]}"
        }
    }

    private fun numberBelowThousandToWords(num: Int): String {
        val hundred = num / 100
        val remainder = num % 100
        val builder = StringBuilder()
        if (hundred > 0) {
            builder.append(units[hundred]).append(" hundred")
            if (remainder > 0) builder.append(" ")
        }
        if (remainder > 0) {
            builder.append(numberBelowHundredToWords(remainder))
        }
        return builder.toString()
    }

    private fun numberToWords(number: String): String {
        val value = number.toLongOrNull() ?: return number
        if (value == 0L) return units[0]
        val thousands = (value / 1000).toInt()
        val remainder = (value % 1000).toInt()
        val builder = StringBuilder()
        if (thousands > 0) {
            builder.append(numberBelowThousandToWords(thousands)).append(" thousand")
            if (remainder > 0) builder.append(" ")
        }
        if (remainder > 0) {
            builder.append(numberBelowThousandToWords(remainder))
        }
        return builder.toString()
    }

    fun replaceSymbols(text: String): String {
        val builder = StringBuilder()
        var index = 0
        while (index < text.length) {
            val ch = text[index]
            when {
                ch.isDigit() -> {
                    val start = index
                    while (index < text.length && text[index].isDigit()) index++
                    val number = text.substring(start, index)
                    builder.append(' ')
                    builder.append(numberToWords(number))
                    builder.append(' ')
                    continue
                }
                map.containsKey(ch) -> {
                    builder.append(' ')
                    builder.append(map[ch])
                    builder.append(' ')
                }
                ch.code in 32..126 -> builder.append(ch)
                else -> {}
            }
            index++
        }
        return builder.toString()
    }
}

