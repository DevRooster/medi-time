package com.example.medi_time_up.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalendarScreen(onSwitchView: (String) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onSwitchView("WEEK") }) { Text("Semanal") }
            Button(onClick = { onSwitchView("MONTH") }) { Text("Mensual") }
        }
        Spacer(Modifier.height(12.dp))

        // Placeholder: integra una librería de calendario (ej. Compose Calendar, MaterialDatePicker, etc.)
        Surface(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Text("Calendario (vista placeholder). Aquí puedes integrar una librería de calendario.")
        }
    }
}