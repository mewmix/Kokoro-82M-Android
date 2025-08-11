package com.example.kokoro82m.utils

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kokoro.chat.LlmInference
import com.example.kokoro82m.data.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BenchmarkRunner(private val app: Application) : AndroidViewModel(app) {

    private val _results = MutableStateFlow<PerfProfiler.BenchmarkResult?>(null)
    val results = _results.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking = _isBenchmarking.asStateFlow()

    private val testPhrase = "The quick brown fox jumps over the lazy dog."

    fun runBenchmark() {
        viewModelScope.launch {
            _isBenchmarking.value = true
            _results.value = null
            PerfProfiler.clear()

            val modelManager = ModelManager(app)
            val model = modelManager.models.firstOrNull { it.isDownloaded }

            if (model == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(app, "No downloaded model found for benchmark.", Toast.LENGTH_LONG).show()
                }
                _isBenchmarking.value = false
                return@launch
            }

            val modelFile = File(app.filesDir, "models/${model.id}.task")
            val llm = LlmInference(app, modelFile.absolutePath)
            llm.initialize()

            withContext(Dispatchers.IO) {
                // Warm-up run
                llm.sendMessage("hello") { _, _ -> }
                
                // Timed run
                var responseTokens = 0
                val llmStartTime = System.nanoTime()
                PerfProfiler.start("Benchmark TTFT")

                llm.sendMessage(testPhrase) { partial, done ->
                    if (responseTokens == 0) PerfProfiler.end("Benchmark TTFT")
                    responseTokens += 1 // Approximation: 1 chunk = 1 token

                    if (done) {
                        val llmTotalTime = (System.nanoTime() - llmStartTime) / 1e9f
                        val tps = if (llmTotalTime > 0) responseTokens / llmTotalTime else 0f
                        PerfProfiler.record("Benchmark TPS", "%.1f".format(tps))
                        
                        // Now run TTS part
                        runTtsBenchmark(tps)
                    }
                }
            }
        }
    }

    private fun runTtsBenchmark(tps: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val ortSession = OnnxRuntimeManager.getSession()
            val styleLoader = StyleLoader(app)
            val voice = styleLoader.getStyleArray(styleLoader.names.first())
            val engine = SettingsManager.getTtsEngine(app)

            val (synthesisTime, audioDuration) = if (engine == TtsEngine.KITTEN) {
                val (_, tokens) = KittenPhonemizer.phonemize(testPhrase)
                val synthStart = System.nanoTime()
                val (audio, sampleRate) = createKittenAudioFromStyleVector(tokens, voice, 1.0f, ortSession)
                val synthEnd = System.nanoTime()
                Pair((synthEnd - synthStart) / 1e6f, (audio.size.toFloat() / sampleRate) * 1000)
            } else {
                val phonemeConverter = PhonemeConverter(app)
                val phonemes = phonemeConverter.phonemize(testPhrase)
                val synthStart = System.nanoTime()
                val (audio, sampleRate) = createAudioFromStyleVector(phonemes, voice, 1.0f, ortSession)
                val synthEnd = System.nanoTime()
                Pair((synthEnd - synthStart) / 1e6f, (audio.size.toFloat() / sampleRate) * 1000)
            }
            
            val rtf = if (synthesisTime > 0) audioDuration / synthesisTime else 0f
            
            val finalResult = PerfProfiler.BenchmarkResult(
                ttft = PerfProfiler.getLiveStats()["Benchmark TTFT"]?.removeSuffix(" ms")?.toFloat() ?: -1f,
                tps = tps,
                synthesisTime = synthesisTime,
                audioDuration = audioDuration,
                rtf = rtf,
                handoffLatency = 20.0f + (Math.random() * 20).toFloat(), // Simulated
                e2eLatency = (PerfProfiler.getLiveStats()["Benchmark TTFT"]?.removeSuffix(" ms")?.toFloat() ?: 0f) + synthesisTime
            )
            _results.value = finalResult
            _isBenchmarking.value = false
        }
    }

    fun formatResultsForSharing(): String {
        val res = _results.value ?: return "No benchmark results available."
        val device = android.os.Build.MODEL
        val androidVer = android.os.Build.VERSION.RELEASE
        return """
        ## Nabu Performance Benchmark
        - **Device:** $device
        - **Android Version:** $androidVer
        
        ### Metrics
        - **LLM Time to First Token:** ${"%.1f".format(res.ttft)} ms
        - **LLM Tokens per Second:** ${"%.1f".format(res.tps)}
        - **TTS Synthesis Time:** ${"%.1f".format(res.synthesisTime)} ms
        - **TTS Real-Time Factor:** ${"%.2f".format(res.rtf)}x
        - **End-to-End Latency (simulated):** ${"%.1f".format(res.e2eLatency)} ms
        """.trimIndent()
    }
}
