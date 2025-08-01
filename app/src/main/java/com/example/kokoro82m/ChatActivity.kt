package com.example.kokoro82m

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kokoro.chat.ChatScreen
import com.example.kokoro.chat.ChatViewModel
import com.example.kokoro.chat.LlmInference
import com.example.kokoro82m.data.ModelManager
import com.example.kokoro82m.data.Model
import com.example.kokoro82m.utils.DebugLogger
import java.io.File

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugLogger.initialize(this)

        val modelManager = ModelManager(applicationContext)
        val downloaded = modelManager.models.filter { it.isDownloaded }

        if (downloaded.isEmpty()) {
            Toast.makeText(
                this,
                "No chat models downloaded. Redirecting to model page.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    putExtra(EXTRA_START_SCREEN, "Models")
                }
            )
            finish()
            return
        }

        if (downloaded.size == 1) {
            startChat(downloaded.first())
        } else {
            selectModel(downloaded)
        }
    }

    private fun selectModel(models: List<Model>) {
        val names = models.map { it.name }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Chat Model")
            .setItems(names) { _, which ->
                startChat(models[which])
            }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun startChat(model: Model) {
        val modelFile = File(filesDir, "models/${model.id}.task")

        val llmInference = LlmInference(
            context = applicationContext,
            modelPath = modelFile.absolutePath
        )
        llmInference.initialize()

        val viewModel: ChatViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(llmInference) as T
                }
            }
        }

        setContent {
            ChatScreen(
                viewModel = viewModel,
                modelName = model.name,
                onBackPressed = { finish() }
            )
        }
    }
}
