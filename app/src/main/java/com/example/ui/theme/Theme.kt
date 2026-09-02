package com.example.ui.theme

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
    primary = DarkGeoPrimary,
    onPrimary = DarkGeoOnPrimary,
    primaryContainer = DarkGeoPrimaryContainer,
    onPrimaryContainer = DarkGeoOnPrimaryContainer,
    secondary = SecondaryContainerPurple,
    onSecondary = OnSecondaryContainerPurple,
    secondaryContainer = DarkGeoSurfaceVariant,
    onSecondaryContainer = DarkGeoOnSurface,
    tertiary = TertiaryContainerRose,
    onTertiary = OnTertiaryContainerRose,
    tertiaryContainer = TertiaryRose,
    background = DarkGeoBackground,
    onBackground = DarkGeoOnSurface,
    surface = DarkGeoSurface,
    onSurface = DarkGeoOnSurface,
    surfaceVariant = DarkGeoSurfaceVariant,
    onSurfaceVariant = DarkGeoOnSurfaceVariant,
    outline = DarkGeoOutline
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = PurpleOnPrimaryContainer,
    secondary = SecondaryPurple,
    onSecondary = PurpleOnPrimary,
    secondaryContainer = SecondaryContainerPurple,
    onSecondaryContainer = OnSecondaryContainerPurple,
    tertiary = TertiaryRose,
    onTertiary = PurpleOnPrimary,
    tertiaryContainer = TertiaryContainerRose,
    onTertiaryContainer = OnTertiaryContainerRose,
    background = GeoBackground,
    onBackground = GeoOnSurface,
    surface = GeoSurface,
    onSurface = GeoOnSurface,
    surfaceVariant = GeoSurfaceVariant,
    onSurfaceVariant = GeoOnSurfaceVariant,
    outline = GeoOutline,
    outlineVariant = GeoOutlineVariant,
    error = AlertRed,
    errorContainer = TertiaryContainerRose,
    onErrorContainer = OnTertiaryContainerRose
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent Geometric Balance theme
    content: @Composable () -> Unit,
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
