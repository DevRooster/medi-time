package com.example.medi_time_up.ui.components.buttons
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.medi_time_up.ui.theme.MediDark // Sombra
import com.example.medi_time_up.ui.theme.MediLight // Borde

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true
) {
    val cornerShape = RoundedCornerShape(8.dp)

    // Simulación del "estilo 3D clásico" usando un contenedor de sombra
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(50.dp)
            .offset(y = (-4).dp) // Eleva el botón sobre la sombra
            .background(MediDark, cornerShape), // Color de la sombra/base
        shape = cornerShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary, // MediDark
            contentColor = MaterialTheme.colorScheme.onPrimary, // White
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        ),
        border = BorderStroke(2.dp, MediLight), // Borde claro para resaltar la elevación
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp) // Desactivamos la elevación nativa
    ) {
        Text(text.uppercase())
    }
    // La sombra 3D es el fondo de MediDark, que sobresale por debajo.
}