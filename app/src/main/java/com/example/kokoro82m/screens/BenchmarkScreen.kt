package com.example.kokoro82m.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kokoro82m.utils.BenchmarkRunner

@Composable
fun BenchmarkScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val benchmarkRunner: BenchmarkRunner = viewModel { BenchmarkRunner(context.applicationContext) }
    val benchmarkResults by benchmarkRunner.results.collectAsState()
    val isBenchmarking by benchmarkRunner.isBenchmarking.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Benchmark", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Run a standardized test to measure device performance. " +
                            "Please ensure a chat model is downloaded.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isBenchmarking) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Benchmark in progress...")
                    }
                } else {
                    Button(
                        onClick = { benchmarkRunner.runBenchmark() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Benchmark")
                    }
                }

                benchmarkResults?.let { results ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Results", style = MaterialTheme.typography.titleMedium)
                    ResultRow("LLM First Token:", "%.1f ms".format(results.ttft))
                    ResultRow("LLM Speed:", "%.1f tokens/sec".format(results.tps))
                    ResultRow("TTS Synthesis:", "%.1f ms".format(results.synthesisTime))
                    ResultRow("TTS Real-Time Factor:", "%.2f x".format(results.rtf))
                    ResultRow("E2E Latency:", "%.1f ms".format(results.e2eLatency))
                    ResultRow("Handoff Latency:", "%.1f ms".format(results.handoffLatency))
                    
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(benchmarkRunner.formatResultsForSharing()))
                    }) {
                        Text("Copy Results")
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}
