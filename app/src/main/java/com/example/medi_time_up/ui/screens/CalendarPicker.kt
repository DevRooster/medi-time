package com.example.medi_time_up.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medi_time_up.ui.theme.MediLight
import com.example.medi_time_up.ui.theme.MediVeryDark

/**
 * CalendarPicker:
 * - mode = "WEEK" -> devuelve lista de días de la semana seleccionados (1 = Lunes .. 7 = Domingo)
 * - mode = "MONTH" -> devuelve lista de días de mes (1..31) seleccionados
 *
 * Se muestra como Dialog modal con contenido básico; puedes mejorar con un calendario real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPicker(
    mode: String = "WEEK",
    initiallySelected: List<Int> = emptyList(),
    onDismiss: () -> Unit,
    onApply: (List<Int>) -> Unit
) {
    val selected = remember { mutableStateListOf<Int>().apply { addAll(initiallySelected) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onApply(selected.toList()) }) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text(if (mode == "WEEK") "Seleccionar días de la semana" else "Seleccionar días del mes") },
        text = {
            if (mode == "WEEK") {
                WeekDaySelector(selected = selected)
            } else {
                MonthDaySelector(selected = selected)
            }
        }
    )
}

@Composable
private fun WeekDaySelector(selected: MutableList<Int>) {
    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEachIndexed { idx, label ->
                val dayNumber = idx + 1 // 1..7
                val isSelected = selected.contains(dayNumber)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selected.remove(dayNumber) else selected.add(dayNumber)
                    },
                    label = { Text(label) },
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Selecciona los días en los que se debe tomar el medicamento.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MonthDaySelector(selected: MutableList<Int>) {
    // Generamos filas de 7 días (1..31)
    val days = (1..31).toList()
    Column {
        val rows = days.chunked(7)
        rows.forEach { rowDays ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowDays.forEach { day ->
                    val isSelected = selected.contains(day)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selected.remove(day) else selected.add(day)
                        },
                        label = { Text(day.toString()) },
                        modifier = Modifier.sizeIn(minWidth = 36.dp, minHeight = 36.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Selecciona los días del mes (ej. 1, 15, 30).", style = MaterialTheme.typography.bodySmall)
    }
}