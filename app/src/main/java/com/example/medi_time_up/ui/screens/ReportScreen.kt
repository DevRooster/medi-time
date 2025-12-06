package com.example.medi_time_up.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medi_time_up.data.dao.MedicamentoDao
import com.example.medi_time_up.data.Medicamento
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ReportScreen(dao: MedicamentoDao) {
    var tomados by remember { mutableStateOf<List<Medicamento>>(emptyList()) }

    LaunchedEffect(Unit) {
        dao.obtenerTomados().collectLatest { list ->
            tomados = list
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Reporte: Medicamentos tomados", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (tomados.isEmpty()) {
            Text("No hay registros de dosis tomadas todavía.")
        } else {
            LazyColumn { items(tomados) { med ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(med.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("Hora: ${med.hora}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } }
        }
    }
}