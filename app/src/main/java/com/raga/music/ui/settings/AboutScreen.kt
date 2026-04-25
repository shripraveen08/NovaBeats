package com.raga.music.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * NovaBeats — Free Music Player for Android
 * Copyright (C) 2024 NovaBeats Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * NovaBeats is architecturally inspired by OuterTune (https://github.com/OuterTune/OuterTune)
 * and InnerTune (https://github.com/z-huang/InnerTune), both GPL-3.0 licensed.
 * No source code was copied. Inspiration is not copyright infringement.
 */

// ─── Data ─────────────────────────────────────────────────────────────────────

data class OpenSourceLib(
    val name: String,
    val author: String,
    val license: String,
    val url: String
)

val OPEN_SOURCE_LIBS = listOf(
    OpenSourceLib("AndroidX Media3 / ExoPlayer", "Google LLC", "Apache 2.0", "https://github.com/androidx/media"),
    OpenSourceLib("Jetpack Compose", "Google LLC", "Apache 2.0", "https://developer.android.com/jetpack/compose"),
    OpenSourceLib("Material 3", "Google LLC", "Apache 2.0", "https://m3.material.io"),
    OpenSourceLib("Room Database", "Google LLC", "Apache 2.0", "https://developer.android.com/jetpack/androidx/releases/room"),
    OpenSourceLib("Hilt", "Google LLC", "Apache 2.0", "https://dagger.dev/hilt/"),
    OpenSourceLib("Retrofit", "Square, Inc.", "Apache 2.0", "https://square.github.io/retrofit/"),
    OpenSourceLib("OkHttp", "Square, Inc.", "Apache 2.0", "https://square.github.io/okhttp/"),
    OpenSourceLib("Coil", "Coil Contributors", "Apache 2.0", "https://coil-kt.github.io/coil/"),
    OpenSourceLib("Kotlin Coroutines", "JetBrains", "Apache 2.0", "https://github.com/Kotlin/kotlinx.coroutines"),
    OpenSourceLib("Gson", "Google LLC", "Apache 2.0", "https://github.com/google/gson"),
    OpenSourceLib("WorkManager", "Google LLC", "Apache 2.0", "https://developer.android.com/topic/libraries/architecture/workmanager"),
    OpenSourceLib("DataStore", "Google LLC", "Apache 2.0", "https://developer.android.com/topic/libraries/architecture/datastore"),
    OpenSourceLib("Palette", "Google LLC", "Apache 2.0", "https://developer.android.com/reference/androidx/palette/graphics/Palette"),
    OpenSourceLib("SplashScreen API", "Google LLC", "Apache 2.0", "https://developer.android.com/guide/topics/ui/splash-screen")
)

data class Inspiration(
    val name: String,
    val description: String,
    val url: String,
    val license: String
)

val INSPIRATIONS = listOf(
    Inspiration(
        "OuterTune",
        "Material 3 YouTube Music client & local player. Architecturally inspired this app.",
        "https://github.com/OuterTune/OuterTune",
        "GPL-3.0"
    ),
    Inspiration(
        "InnerTune",
        "The original project OuterTune was forked from.",
        "https://github.com/z-huang/InnerTune",
        "GPL-3.0"
    )
)

// ─── About Screen ─────────────────────────────────────────────────────────────

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // App header
        item {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎵", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(8.dp))
                Text("NovaBeats", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Free music player — no subscription, no ads, no tracking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // License badge
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("License", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "NovaBeats is open source software licensed under the GNU General Public License v3.0 (GPL-3.0). You are free to use, study, modify, and distribute this software under the same terms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { uriHandler.openUri("https://www.gnu.org/licenses/gpl-3.0.html") }
                    ) {
                        Text("View GPL-3.0 License")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.OpenInNew, null, Modifier.size(14.dp))
                    }
                }
            }
        }

        // Privacy
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Privacy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "NovaBeats collects zero user data. No analytics, no tracking, no accounts. Everything stays on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Music sources
        item {
            Text("Music Sources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri("https://www.jamendo.com") }) {
                ListItem(
                    headlineContent   = { Text("Jamendo") },
                    supportingContent = { Text("Creative Commons licensed music — free for everyone") },
                    trailingContent   = { Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp)) }
                )
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri("https://archive.org") }) {
                ListItem(
                    headlineContent   = { Text("Internet Archive") },
                    supportingContent = { Text("Public domain music — no copyright restrictions") },
                    trailingContent   = { Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp)) }
                )
            }
        }

        // Inspirations
        item {
            Spacer(Modifier.height(8.dp))
            Text("Inspired by", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(INSPIRATIONS) { proj ->
            Card(modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(proj.url) }) {
                ListItem(
                    headlineContent   = { Text(proj.name) },
                    supportingContent = { Text("${proj.description} • ${proj.license}") },
                    trailingContent   = { Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp)) }
                )
            }
        }

        // Open source libraries
        item {
            Spacer(Modifier.height(8.dp))
            Text("Open Source Libraries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(OPEN_SOURCE_LIBS) { lib ->
            Card(modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(lib.url) }) {
                ListItem(
                    headlineContent   = { Text(lib.name) },
                    supportingContent = { Text("${lib.author} • ${lib.license}") },
                    trailingContent   = { Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp)) }
                )
            }
        }

        // Disclaimer
        item {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Disclaimer", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "NovaBeats is not affiliated with YouTube, Google, Spotify, Apple, or any commercial music service. All music accessed through this app is either Creative Commons licensed or in the public domain. Music rights belong to their respective creators.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
