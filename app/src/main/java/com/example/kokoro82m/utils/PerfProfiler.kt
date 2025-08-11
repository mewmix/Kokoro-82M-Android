package com.example.kokoro82m.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.system.measureNanoTime

/**
 * A comprehensive profiler for measuring and analyzing performance metrics.
 * This singleton object replaces the simpler PerfHud.
 */
object PerfProfiler {

    data class BenchmarkResult(
        val ttft: Float,
        val tps: Float,
        val synthesisTime: Float,
        val audioDuration: Float,
        val rtf: Float,
        val handoffLatency: Float,
        val e2eLatency: Float
    )

    private val liveStats = mutableStateMapOf<String, String>()
    private val ongoingTimers = mutableMapOf<String, Long>()

    fun start(label: String) {
        ongoingTimers[label] = System.nanoTime()
    }

    fun end(label: String): Float {
        val startTime = ongoingTimers.remove(label) ?: return -1f
        val durationMs = (System.nanoTime() - startTime) / 1e6f
        liveStats[label] = "%.1f ms".format(durationMs)
        return durationMs
    }

    fun record(label: String, value: String) {
        liveStats[label] = value
    }

    fun <T> track(label: String, block: () -> T): T {
        var result: T? = null
        val ms = measureNanoTime { result = block() } / 1e6f
        liveStats[label] = "%.1f ms".format(ms)
        return result!!
    }

    fun getLiveStats(): Map<String, String> = liveStats

    fun clear() {
        liveStats.clear()
        ongoingTimers.clear()
    }

    @Composable
    fun Overlay() {
        Column(
            Modifier
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(8.dp)
        ) {
            Text(
                "Live Profiler",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            liveStats.toSortedMap().forEach { (k, v) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "$k:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        v,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
