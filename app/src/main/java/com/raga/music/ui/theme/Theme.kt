package com.raga.music.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Brand Colors (Spotify-style) ─────────────────────────────────────────────
private val RagaBrandGreen = Color(0xFF1DB954)  // Spotify green
private val RagaDark       = Color(0xFF191414)  // Spotify dark
private val RagaCard       = Color(0xFF282828)  // Spotify card
private val RagaSurface    = Color(0xFF121212)  // Spotify surface

private val DarkColorScheme = darkColorScheme(
    primary          = RagaBrandGreen,
    onPrimary        = Color.Black,
    primaryContainer = Color(0xFF1ED760),  // Brighter green
    background       = RagaDark,
    surface          = RagaCard,
    surfaceVariant   = RagaSurface,
    onBackground     = Color.White,
    onSurface        = Color.White,
    secondary        = Color(0xFF1DB954),  // Spotify green
    tertiary         = Color(0xFF1ED760)   // Bright green
)

private val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF0F7A38),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFB7F0CB),
    background       = Color(0xFFF8FAF8),
    surface          = Color.White,
    onBackground     = Color(0xFF0D1117),
    onSurface        = Color(0xFF0D1117)
)

@Composable
fun RagaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,       // Material You wallpaper colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx)
            else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = RagaTypography,
        content     = content
    )
}
