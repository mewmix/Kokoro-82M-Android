package com.example.kokoro82m.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Represents a group of credits that can contain entries and nested child clusters
data class CreditCluster(
    val name: String,
    val entries: List<String> = emptyList(),
    val children: List<CreditCluster> = emptyList()
)

@Composable
fun CreditsConstellationScreen() {
    // Original credits from upstream author preserved exactly
    val originalCredits = remember {
        CreditCluster(
            name = "puff-dayo",
            children = listOf(
                CreditCluster(
                    name = "Acknowledgements",
                    entries = listOf(
                        "Kokoro: a frontier TTS model (Apache 2.0)",
                        "Kokoro-ONNX: converted kokoro (MIT)",
                        "CMU dict: a pronunciation dictionary",
                        "IPA Transcribers: language transliterators (GPL-3.0)",
                        "Android NNAPI: a machine learning API"
                    )
                )
            )
        )
    }

    // Our own constellation for additional acknowledgements
    val ourCredits = remember {
        CreditCluster(
            name = "Our Credit Wall",
            entries = listOf(
                "AGI - project guidance"
            )
        )
    }

    val clusters = listOf(originalCredits, ourCredits)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(clusters) { cluster ->
            StarCluster(cluster = cluster)
        }
    }
}

@Composable
private fun StarCluster(cluster: CreditCluster, level: Int = 0, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = cluster.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(if (level == 0) 56.dp else 32.dp)
                    .clickable { expanded = !expanded }
            )
            Text(
                text = cluster.name,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = if (level == 0) 64.dp else 40.dp)
            )
        }
        AnimatedVisibility(expanded) {
            ConstellationEntries(cluster = cluster, level = level)
        }
    }
}

@Composable
private fun ConstellationEntries(cluster: CreditCluster, level: Int) {
    val radius = if (level == 0) 100.dp else 60.dp
    Box(
        modifier = Modifier.size(radius * 2),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val total = cluster.entries.size + cluster.children.size
        cluster.entries.forEachIndexed { index, entry ->
            val angle = 2 * PI * index / total
            Text(
                text = entry,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.offset {
                    val r = with(density) { radius.toPx() }
                    IntOffset(
                        x = (cos(angle) * r).roundToInt(),
                        y = (sin(angle) * r).roundToInt()
                    )
                }
            )
        }
        cluster.children.forEachIndexed { index, child ->
            val angle = 2 * PI * (cluster.entries.size + index) / total
            StarCluster(
                cluster = child,
                level = level + 1,
                modifier = Modifier.offset {
                    val r = with(density) { radius.toPx() }
                    IntOffset(
                        x = (cos(angle) * r).roundToInt(),
                        y = (sin(angle) * r).roundToInt()
                    )
                }
            )
        }
    }
}

