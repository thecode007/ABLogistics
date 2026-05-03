package org.safieddine.ablogistics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandingWhite,
    onPrimary = BrandingBlack,
    secondary = BrandingGray,
    onSecondary = BrandingWhite,
    tertiary = TertiaryGray,
    background = BrandingBlack,
    surface = BrandingBlack,
    onBackground = BrandingWhite,
    onSurface = BrandingWhite,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = BrandingBlack,
    onPrimary = BrandingWhite,
    secondary = SecondaryGray,
    onSecondary = BrandingWhite,
    tertiary = TertiaryGray,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = BrandingBlack,
    onSurface = BrandingBlack,
    error = ErrorRed
)

@Composable
fun ABLogisticsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
