package com.example.borow_buddy_frontend.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryLightPurple,
    tertiary = AccentTeal,
    background = BackgroundDark,
    surface = DarkPurple,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryLightPurple,
    tertiary = AccentTeal,
    background = BackgroundLight,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun Borow_Buddy_FrontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false to ensure brand colors are displayed
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}