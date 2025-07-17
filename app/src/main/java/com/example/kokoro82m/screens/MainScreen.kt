package com.example.kokoro82m.screens

import ai.onnxruntime.OrtSession
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.kokoro82m.ui.chat.ChatScreen
import com.example.kokoro82m.ui.image.ImageScreen
import com.example.kokoro82m.utils.PhonemeConverter
import com.example.kokoro82m.utils.StyleLoader

sealed class Screen(val title: String) {
    object Basic : Screen("Basic TTS")
    object Mixer : Screen("Mixer")
    object Book : Screen("Audio Book")
    object Creations : Screen("Creations")
    object Settings : Screen("Settings")
    object About : Screen("About this app")
    object Chat : Screen("Chat")
    object Image : Screen("Image")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    session: OrtSession,
    phonemeConverter: PhonemeConverter,
    onGenerateAudio: (String, String, Float, Boolean, () -> Unit) -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Chat) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.title) }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Chat") },
                    label = { Text("Chat") },
                    selected = currentScreen == Screen.Chat,
                    onClick = { currentScreen = Screen.Chat }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Image") },
                    label = { Text("Image") },
                    selected = currentScreen == Screen.Image,
                    onClick = { currentScreen = Screen.Image }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Basic") },
                    label = { Text("Basic") },
                    selected = currentScreen == Screen.Basic,
                    onClick = { currentScreen = Screen.Basic }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Mixer") },
                    label = { Text("Mixer") },
                    selected = currentScreen == Screen.Mixer,
                    onClick = { currentScreen = Screen.Mixer }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Book") },
                    label = { Text("Book") },
                    selected = currentScreen == Screen.Book,
                    onClick = { currentScreen = Screen.Book }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Creations") },
                    label = { Text("Creations") },
                    selected = currentScreen == Screen.Creations,
                    onClick = { currentScreen = Screen.Creations }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentScreen == Screen.Settings,
                    onClick = { currentScreen = Screen.Settings }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                    label = { Text("About") },
                    selected = currentScreen == Screen.About,
                    onClick = { currentScreen = Screen.About }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Chat -> ChatScreen()
                Screen.Image -> ImageScreen()
                Screen.Basic -> BasicScreen(session = session, onGenerateAudio)
                Screen.Mixer -> MixerScreen(
                    session = session,
                    phonemeConverter = phonemeConverter,
                    styleLoader = StyleLoader(LocalContext.current)
                )
                Screen.Book -> BookScreen(
                    session = session,
                    phonemeConverter = phonemeConverter
                )
                Screen.Creations -> CreationsScreen()
                Screen.Settings -> SettingsScreen()
                Screen.About -> AboutScreen()
            }
        }
    }
}