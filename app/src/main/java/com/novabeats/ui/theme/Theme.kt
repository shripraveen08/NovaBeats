package com.novabeats.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Brand Colors (fallback when dynamic color unavailable) ───────────────────
private val NovaBrandGreen = Color(0xFF1DB954)
private val NovaDark       = Color(0xFF0D1117)
private val NovaCard       = Color(0xFF161B22)
private val NovaSurface    = Color(0xFF1C2128)

private val DarkColorScheme = darkColorScheme(
    primary          = NovaBrandGreen,
    onPrimary        = Color.Black,
    primaryContainer = Color(0xFF0F7A38),
    background       = NovaDark,
    surface          = NovaCard,
    surfaceVariant   = NovaSurface,
    onBackground     = Color.White,
    onSurface        = Color.White,
    secondary        = Color(0xFF5DCAA5),
    tertiary         = Color(0xFF7F77DD)
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
fun NovaBeatTheme(
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
        typography  = NovaBeatTypography,
        content     = content
    )
}
