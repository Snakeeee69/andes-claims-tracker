package com.andes.bpo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAndes,
    onPrimary = SurfaceAndes,
    primaryContainer = MintAndes,
    onPrimaryContainer = PrimaryAndes,
    secondary = MintAccentAndes,
    onSecondary = TextForegroundAndes,
    background = BackgroundAndes,
    onBackground = TextForegroundAndes,
    surface = SurfaceAndes,
    onSurface = TextForegroundAndes,
    outline = BorderAndes
)

@Composable
fun AndesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
