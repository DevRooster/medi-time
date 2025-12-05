package com.example.medi_time_up.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color // Asegúrate de tener esta importación

private val DarkColorScheme = darkColorScheme(
    primary = MediLight,
    onPrimary = MediVeryDark,
    // Usamos MediMedium aquí como color secundario
    secondary = MediMedium,
    onSecondary = White,
    background = MediVeryDark,
    onBackground = White,
    surface = MediDark,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = MediDark,
    onPrimary = White,
    // Usamos MediMedium aquí como color secundario
    secondary = MediMedium,
    onSecondary = White, // El texto sobre el color secundario puede ser blanco
    background = Color.White,
    onBackground = Black,
    surface = MediLight,
    onSurface = Black
)

@Composable
fun MeditimeupTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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