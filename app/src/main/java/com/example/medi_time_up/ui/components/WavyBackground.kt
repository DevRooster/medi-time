package com.example.medi_time_up.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun WavyBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // ✅ Colores SE OBTIENEN AQUÍ (contexto composable válido)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.20f),
                        backgroundColor
                    )
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(730.dp)
        ) {
            val width = size.width
            val height = size.height

            val path = Path().apply {
                moveTo(0f, height * 0.65f)

                quadraticTo(
                    width * 0.25f, height * 0.55f,
                    width * 0.5f, height * 0.65f
                )

                quadraticTo(
                    width * 0.75f, height * 0.75f,
                    width, height * 0.6f
                )

                lineTo(width, 0f)
                lineTo(0f, 0f)
                close()
            }

            // ✅ AQUÍ YA NO HAY NADA COMPOSABLE
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        secondaryColor.copy(alpha = 0.25f)
                    )
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            content()
        }
    }
}