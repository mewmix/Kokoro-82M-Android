package com.example.kokoro82m.utils

import android.content.Context
import android.net.Uri
import java.text.BreakIterator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

object DocumentReader {
    data class Result(val chunks: Flow<String>, val title: String)

    fun asFlow(
        ctx: Context,
        uri: Uri,
        chunkSize: Int = 1600,
        bySentence: Boolean = false // Renamed from byLine to bySentence for clarity
    ): Result {
        val (seq, meta) = TextExtractor.extract(ctx, uri, if (bySentence) Int.MAX_VALUE else chunkSize)
        val fl = flow {
            for (block in seq) {
                if (bySentence) {
                    block.toSentences().forEach { emit(it) }
                } else {
                    emit(block)
                }
            }
        }.flowOn(Dispatchers.IO)
        return Result(chunks = fl, title = meta.displayName)
    }

    private fun String.toSentences(): List<String> {
        val boundary = BreakIterator.getSentenceInstance()
        boundary.setText(this)
        val sentences = mutableListOf<String>()
        var start = boundary.first()
        var end = boundary.next()
        while (end != BreakIterator.DONE) {
            val sentence = substring(start, end).trim()
            if (sentence.isNotEmpty()) {
                sentences.add(sentence)
            }
            start = end
            end = boundary.next()
        }
        return sentences
    }
}