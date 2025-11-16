package com.studyconnect.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueOnContainer,
    secondary = TealAccent,
    onSecondary = TealOnAccent,
    background = LightBackground,
    onBackground = LightOnBackground
)

private val DarkColors = darkColorScheme(
    primary = BlueContainer,
    onPrimary = BlueOnContainer,
    primaryContainer = BluePrimary,
    onPrimaryContainer = BlueOnPrimary,
    secondary = TealAccent,
    onSecondary = TealOnAccent,
    background = DarkBackground,
    onBackground = DarkOnBackground
)

@Composable
fun StudyConnectTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
