package com.example.kokoro.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.system.measureNanoTime

/**
 * Simple performance HUD for tracking synchronous and asynchronous metrics.
 * The stats map can be updated manually via [recordValue] or by wrapping a block
 * with [record] to measure its execution time.
 */
object PerfHud {
    private val stats = mutableStateMapOf<String, Float>()

    /** Measure the execution time of [block] and store the result under [label]. */
    fun <T> record(label: String, block: () -> T): T {
        var result: T? = null
        val ms = measureNanoTime { result = block() } / 1e6f
        stats[label] = ms
        return result!!
    }

    /** Manually update the metric associated with [label]. */
    fun recordValue(label: String, ms: Float) {
        stats[label] = ms
    }

    /** Overlay composable displaying the latest metrics. */
    @Composable
    fun Overlay() {
        Column(Modifier.padding(6.dp)) {
            stats.forEach { (k, v) ->
                Text("$k: ${"%.1f".format(v)} ms", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

