package com.example.medi_time_up.ui.components.utility

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ToastNotification(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val context = LocalContext.current
    // Usamos el Toast nativo de Android por simplicidad en notificaciones rápidas
    Toast.makeText(context, message, duration).show()
}