package com.example.kokoro82m.utils

import java.util.regex.Pattern

/**
 * A utility class that buffers text and invokes a callback for each complete sentence.
 */
class SentenceSplitter(private val onSentenceReady: (String) -> Unit) {
    private val buffer = StringBuilder()
    // This regex looks for sentence-ending punctuation that is followed by a space or the end of the text.
    private val sentenceEndPattern: Pattern = Pattern.compile("(?<=[.!?])(?=\\s|$)")

    /**
     * Processes an incoming chunk of text, identifies complete sentences, and invokes the callback.
     * Any partial sentence at the end is kept in the buffer.
     */
    fun process(textChunk: String) {
        buffer.append(textChunk)
        val potentialSentences = sentenceEndPattern.split(buffer.toString())

        if (potentialSentences.size > 1) {
            // All parts except the last are considered complete sentences.
            for (i in 0 until potentialSentences.size - 1) {
                val sentence = potentialSentences[i].trim()
                if (sentence.isNotEmpty()) {
                    onSentenceReady(sentence)
                }
            }
            // The last, incomplete part becomes the new buffer content.
            buffer.clear().append(potentialSentences.last())
        }
    }

    /**
     * Call this when the stream is finished to process any remaining text in the buffer.
     */
    fun flush() {
        val remaining = buffer.toString().trim()
        if (remaining.isNotEmpty()) {
            onSentenceReady(remaining)
        }
        buffer.clear()
    }
}
