package com.example.medi_time_up.ui.components.buttons

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.medi_time_up.ui.theme.MediLight
import com.example.medi_time_up.ui.theme.MediVeryDark
import kotlinx.coroutines.flow.collect

/**
 * Botón con estética 3D clásico:
 * - Capa base (sombra oscura desplazada)
 * - Capa superior (superficie) con gradiente sutil
 * - Borde claro y highlight superior para sensación de volumen
 * - Estado "pressed" que reduce el offset simulando que se aplasta
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    cornerRadius: Dp = 12.dp
) {
    val shape = RoundedCornerShape(cornerRadius)

    // Interaction state (detecta pressed)
    val interactionSource = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }

    // Animaciones:
    val baseOffset by animateDpAsState(targetValue = if (pressed) 4.dp else 8.dp)
    val topOffset by animateDpAsState(targetValue = if (pressed) (-2).dp else (-6).dp)
    val surfaceElevationScale by animateFloatAsState(targetValue = if (pressed) 0.98f else 1f)

    // Collect interactions to update 'pressed'
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            pressed = when (interaction) {
                is androidx.compose.foundation.interaction.PressInteraction.Press -> true
                is androidx.compose.foundation.interaction.PressInteraction.Release -> false
                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> false
                else -> pressed
            }
        }
    }

    Box(modifier = modifier.height(height + baseOffset)) {
        // 1) Capa base - sombra / "pedestal"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = baseOffset)
                .clip(shape)
                .background(MediVeryDark)
        )

        // 2) Capa superior - superficie con gradiente y borde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .align(Alignment.TopStart)
                .offset(y = topOffset)
                .clip(shape)
                .drawBehind {
                    // ligero inner highlight superior (sutil)
                    val highlight = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                    )
                    drawRect(highlight)
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                        )
                    ),
                    shape = shape
                )
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
                .drawBehind {
                    // borde exterior claro para resaltar (simula canto iluminado)
                    val strokeWidth = 4f
                    drawIntoCanvas { canvas ->
                        withTransform({
                            // no transform needed, kept for future tweaks
                        }) {
                            val paint = androidx.compose.ui.graphics.Paint().apply {
                                this.color = MediLight.copy(alpha = 0.65f)
                                this.asFrameworkPaint().strokeWidth = strokeWidth
                                this.asFrameworkPaint().style = android.graphics.Paint.Style.STROKE
                                this.asFrameworkPaint().isAntiAlias = true
                            }
                            val r = size
                            canvas.drawRoundRect(
                                left = 0f,
                                top = 0f,
                                right = r.width,
                                bottom = r.height,
                                radiusX = cornerRadius.toPx(),
                                radiusY = cornerRadius.toPx(),
                                paint = paint
                            )
                        }
                    }
                }
                .semantics(mergeDescendants = true) {}
                .graphicsLayer {
                    // aquí sí podemos usar scaleX/scaleY porque estamos en graphicsLayer
                    scaleX = surfaceElevationScale
                    scaleY = surfaceElevationScale
                }
        ) {
            // Texto centrado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}