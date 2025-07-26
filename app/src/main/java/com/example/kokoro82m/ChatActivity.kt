package com.example.kokoro82m

import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.kokoro82m.edgechat.ChatScreen
import com.example.kokoro82m.edgechat.ChatViewModel
import kotlinx.coroutines.launch

class ChatActivity : ComponentActivity() {
    private val viewModel by viewModels<ChatViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val msgs = viewModel.messages
            ChatScreen(msgs.value) { text ->
                viewModel.sendUserMessage(text)
            }
        }

        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val cameraId = cameraManager.cameraIdList.firstOrNull()

        lifecycleScope.launch {
            viewModel.actionFlow.collect { token ->
                cameraId?.let {
                    if (token.contains("flashlight on", true)) {
                        cameraManager.setTorchMode(it, true)
                    }
                }
                if (token.contains("volume up", true)) {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        0
                    )
                }
            }
        }
    }
}

