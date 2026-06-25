package com.example.purrsistence.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes as MaterialShapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,

    tertiary = Tertiary,
    onTertiary = OnTertiary,

    tertiaryContainer = Accent,
    onTertiaryContainer = OnAccent,

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,

    // Extra roles
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnSurface,
    surfaceDim = Disabled,
    outline = Outline,
    error = Error
)

@Composable
fun PurrsistenceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // DYNAMIC COLOR SCHEME WITH DARK MODE
    // Uncomment If needed!!
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }

    // Sticking to LightColorScheme only for now
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
        // corner radius -> shapes
        shapes = MaterialShapes(
            small = Shapes.inputs,
            medium = Shapes.buttons,
            large = Shapes.cards
        )
    )
}