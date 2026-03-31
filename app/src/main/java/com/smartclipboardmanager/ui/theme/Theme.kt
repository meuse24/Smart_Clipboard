package com.smartclipboardmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SlateBlue,
    secondary = BlueGrey,
    tertiary = Coral,
    background = Cream,
    surface = Cream,
    onPrimary = Cream,
    onSecondary = Cream,
    onTertiary = Ink,
    onBackground = Ink,
    onSurface = Ink
)

private val DarkColors = darkColorScheme(
    primary = ColorTokens.PrimaryDark,
    secondary = ColorTokens.SecondaryDark,
    tertiary = ColorTokens.TertiaryDark,
    background = ColorTokens.BackgroundDark,
    surface = ColorTokens.SurfaceDark,
    onPrimary = ColorTokens.OnPrimaryDark,
    onSecondary = ColorTokens.OnSecondaryDark,
    onTertiary = ColorTokens.OnTertiaryDark,
    onBackground = ColorTokens.OnBackgroundDark,
    onSurface = ColorTokens.OnSurfaceDark
)

@Composable
fun SmartClipboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}

private object ColorTokens {
    val PrimaryDark = SlateBlue.copy(alpha = 0.92f)
    val SecondaryDark = BlueGrey.copy(alpha = 0.9f)
    val TertiaryDark = Coral.copy(alpha = 0.85f)
    val BackgroundDark = Ink
    val SurfaceDark = Ink
    val OnPrimaryDark = Cream
    val OnSecondaryDark = Cream
    val OnTertiaryDark = Cream
    val OnBackgroundDark = Cream
    val OnSurfaceDark = Cream
}
