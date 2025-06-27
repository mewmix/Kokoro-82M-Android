import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.StyleLoader
import com.example.kokoro82m.utils.InterpolationMode
import com.example.kokoro82m.utils.createAudioFromStyleVector
import com.example.kokoro82m.utils.mixStyles
import com.example.kokoro82m.utils.playAudio
import com.example.kokoro82m.utils.saveAudio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun MixerScreen(
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
    styleLoader: StyleLoader,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    var selectedStyles by remember { mutableStateOf(listOf("af_sarah", "am_adam", "af_bella")) }
    var weights by remember { mutableStateOf(mapOf("af_sarah" to 0.5f, "am_adam" to 0.5f, "af_bella" to 0.25f)) }
    var interpolationMode by remember { mutableStateOf(InterpolationMode.LINEAR) }

    LaunchedEffect(Unit) {
        loadStyleConfig(context)?.let { (styles, w, mode) ->
            selectedStyles = styles
            weights = w
            interpolationMode = mode
        }
    }

    var text by remember { mutableStateOf("This is her warm heart, her warmest kokoro, unwavering love and comfort.") }
    var speed by remember { mutableFloatStateOf(1.0f) }

    var isProcessing by remember { mutableStateOf(false) }
    var shouldSaveFile by remember { mutableStateOf(false) }

    val styleNames = styleLoader.names

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = text,
            minLines = 3,
            maxLines = 12,
            onValueChange = { text = it },
            label = { Text("Text to speak") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            )
        )

        Text("Speed: $speed", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = 0.5f..2.0f,
            steps = 5,
            modifier = Modifier.fillMaxWidth()
        )

        StyleSelector(
            styleNames = styleNames,
            selectedStyles = selectedStyles,
            onAddStyle = { style ->
                selectedStyles = selectedStyles.toMutableList().apply { add(style) }
                weights = weights.toMutableMap().apply { put(style, 1f) }
            },
            onRemoveStyle = { style ->
                selectedStyles = selectedStyles.toMutableList().apply { remove(style) }
                weights = weights.toMutableMap().apply { remove(style) }
            }
        )


        WeightSliders(
            selectedStyles = selectedStyles,
            weights = weights,
            onWeightChanged = { style, value ->
                weights = weights.toMutableMap().apply { put(style, value) }
            }
        )


        InterpolationModeSelector(
            currentMode = interpolationMode,
            onModeSelected = { interpolationMode = it }
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                selectedStyles = listOf("af_sarah", "am_adam", "af_bella")
                weights = mapOf("af_sarah" to 0.5f, "am_adam" to 0.5f, "af_bella" to 0.25f)
            }) { Text("Reset") }

            Button(onClick = { saveStyleConfig(context, selectedStyles, weights, interpolationMode) }) {
                Text("Save Style")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    shouldSaveFile = false
                    isProcessing = true
                    scope.launch {
                        val mixedVector = mixStyles(
                            styleLoader = styleLoader,
                            styles = selectedStyles,
                            weights = weights,
                            mode = interpolationMode
                        )
                        generateAudio(
                            text = text,
                            style = mixedVector,
                            speed = speed,
                            shouldSaveFile = shouldSaveFile,
                            session = session,
                            phonemeConverter = phonemeConverter,
                            scope = scope,
                            context = context
                        ) {
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "Mixing..." else "Play")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    shouldSaveFile = true
                    isProcessing = true
                    scope.launch {
                        val mixedVector = mixStyles(
                            styleLoader = styleLoader,
                            styles = selectedStyles,
                            weights = weights,
                            mode = interpolationMode
                        )
                        generateAudio(
                            text = text,
                            style = mixedVector,
                            speed = speed,
                            shouldSaveFile = shouldSaveFile,
                            session = session,
                            phonemeConverter = phonemeConverter,
                            scope = scope,
                            context = context
                        ) {
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "Mixing..." else "Play & Save")
            }
        }
    }
}

fun generateAudio(
    text: String,
    style: Array<FloatArray>,
    speed: Float,
    shouldSaveFile: Boolean,
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
    scope: CoroutineScope,
    context: Context,
    onComplete: () -> Unit
) {
    scope.launch(Dispatchers.IO) {
        try {
            val phonemes = phonemeConverter.phonemize(text)
            val (audio, _) = createAudioFromStyleVector(
                phonemes = phonemes,
                voice = style,
                speed = speed,
                session = session
            )
            if (shouldSaveFile) {
                saveAudio(audio, context)
            }
            playAudio(audio, scope) {}
        } catch (e: Exception) {
            Log.e("Kokoro", "Error: ${e.message}")
        } finally {
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}

private fun saveStyleConfig(
    context: Context,
    styles: List<String>,
    weights: Map<String, Float>,
    mode: InterpolationMode
) {
    val prefs = context.getSharedPreferences("mixer_config", Context.MODE_PRIVATE)
    val styleString = styles.joinToString(",") { s -> "$s|${weights[s] ?: 1f}" }
    prefs.edit()
        .putString("styles", styleString)
        .putString("mode", mode.name)
        .apply()
}

private fun loadStyleConfig(context: Context): Triple<List<String>, Map<String, Float>, InterpolationMode>? {
    val prefs = context.getSharedPreferences("mixer_config", Context.MODE_PRIVATE)
    val saved = prefs.getString("styles", null) ?: return null
    val mode = prefs.getString("mode", InterpolationMode.LINEAR.name) ?: InterpolationMode.LINEAR.name
    val styles = mutableListOf<String>()
    val weights = mutableMapOf<String, Float>()
    saved.split(',').forEach { entry ->
        val parts = entry.split('|')
        if (parts.size == 2) {
            styles.add(parts[0])
            weights[parts[0]] = parts[1].toFloatOrNull() ?: 1f
        }
    }
    return Triple(styles, weights, InterpolationMode.valueOf(mode))
}




@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun StyleSelector(
    styleNames: List<String>,
    selectedStyles: List<String>,
    onAddStyle: (String) -> Unit,
    onRemoveStyle: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text("Selected Styles:", style = MaterialTheme.typography.labelLarge)


        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedStyles.forEach { style ->
                SuggestionChip(
                    onClick = { onRemoveStyle(style) },
                    label = { Text(style) },
                    icon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove"
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                placeholder = { Text("Add style...") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                styleNames.filter { it !in selectedStyles }.forEach { style ->
                    DropdownMenuItem(
                        text = { Text(style) },
                        onClick = {
                            onAddStyle(style)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightSliders(
    selectedStyles: List<String>,
    weights: Map<String, Float>,
    onWeightChanged: (String, Float) -> Unit
) {
    Column {
        Text("Style Weights:", style = MaterialTheme.typography.labelLarge)

        selectedStyles.forEach { style ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = style, modifier = Modifier.width(120.dp))
                Slider(
                    value = weights[style] ?: 0f,
                    onValueChange = { onWeightChanged(style, it) },
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
                Text(text = "%.2f".format(weights[style] ?: 0f))
            }
        }
    }
}

@Composable
private fun InterpolationModeSelector(
    currentMode: InterpolationMode,
    onModeSelected: (InterpolationMode) -> Unit
) {
    Column {
        Text("Interpolation Mode:", style = MaterialTheme.typography.labelLarge)

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            InterpolationMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelected(mode) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentMode == mode,
                        onClick = { onModeSelected(mode) }
                    )
                    Text(mode.displayName)
                }
            }
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun MixerPreview() {
//    OnnxRuntimeManager.initialize(LocalContext.current)
//
//    MixerScreen(
//        styleLoader = StyleLoader(LocalContext.current),
//        session = OnnxRuntimeManager.getSession()
//    )
//}
