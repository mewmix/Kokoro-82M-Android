package com.example.kokoro82m.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { onNavigate("Creations") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Creations")
        }
        Button(
            onClick = { onNavigate("Settings") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }
    }
}