package com.example.medi_time_up.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medi_time_up.data.ScheduledMedication
import com.example.medi_time_up.util.epochDayToLocalDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * CalendarMonthScreen:
 *
 * @param schedulesForMonth lista completa de schedules (Room -> Flow convertido a lista en MainActivity)
 * @param onAddSchedule lambda que se llama SOLO si el día no tiene ya registros
 * @param monthOffset 0 = mes actual, -1 = mes anterior, +1 = siguiente (opcional)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarMonthScreen(
    schedulesForMonth: List<ScheduledMedication>,
    onAddSchedule: (selectedEpochDay: Long) -> Unit,
    monthOffset: Int = 0
) {
    val todayEpoch = LocalDate.now().toEpochDay()

    // Día seleccionado por el usuario
    var selectedDay by remember { mutableStateOf<Long?>(null) }

    // Mes mostrado
    val displayedMonth = YearMonth.now().plusMonths(monthOffset.toLong())
    val firstOfMonth = displayedMonth.atDay(1)
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // 0 = domingo
    val daysInMonth = displayedMonth.lengthOfMonth()

    // Construimos celdas (Long epochDay o null)
    val cells = remember(displayedMonth) {
        val list = mutableListOf<Long?>()
        repeat(firstDayOfWeek) { list.add(null) }
        for (d in 1..daysInMonth) list.add(firstOfMonth.withDayOfMonth(d).toEpochDay())
        while (list.size % 7 != 0) list.add(null)
        list
    }

    // Precalcular set de epochDays que tienen uno o más eventos (expandiendo rangos start..end)
    val daysWithEvents: Set<Long> = remember(schedulesForMonth) {
        schedulesForMonth.flatMap { sched ->
            (sched.startEpochDay..sched.endEpochDay).toList()
        }.toSet()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header: mes y año
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayedMonth.year}",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Spacer(Modifier.height(12.dp))

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Dom", "Lun", "Mar", "Mi", "Jue", "Vie", "Sáb").forEach { wd ->
                Text(
                    wd,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp), // evitar ocupar todo el alto
            userScrollEnabled = false
        ) {
            items(items = cells) { epochDay: Long? ->
                val bgColor = when {
                    epochDay == null -> MaterialTheme.colorScheme.surface
                    epochDay == todayEpoch -> MaterialTheme.colorScheme.primary.copy(alpha = 0.36f)
                    epochDay in daysWithEvents -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }

                // Si el día está seleccionado, lo marcamos un poco más oscuro
                val isSelected = selectedDay != null && epochDay != null && epochDay == selectedDay
                val finalBg = if (isSelected) bgColor.copy(alpha = 0.85f) else bgColor

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(46.dp)
                        .background(finalBg, shape = MaterialTheme.shapes.small)
                        .clickable(enabled = epochDay != null) {
                            epochDay?.let { eDay ->
                                // Si ya existe un registro en ese día -> no abrir el formulario,
                                // solo seleccionamos para mostrar la lista de eventos
                                if (eDay in daysWithEvents) {
                                    selectedDay = eDay
                                } else {
                                    // No hay eventos -> abrir formulario (navegación)
                                    onAddSchedule(eDay)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (epochDay != null) {
                        val d = epochDayToLocalDate(epochDay)
                        Text(d.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Lista de registros del día seleccionado (si hay)
        Text("Eventos del día", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))

        if (selectedDay == null) {
            Text("Selecciona un día para ver los registros.", style = MaterialTheme.typography.bodySmall)
        } else {
            val eventsForDay = schedulesForMonth.filter { s ->
                selectedDay!! in s.startEpochDay..s.endEpochDay
            }

            if (eventsForDay.isEmpty()) {
                Text("No hay registros para este día.", style = MaterialTheme.typography.bodySmall)
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    eventsForDay.forEach { sched ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(text = sched.nombre, style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                Text(text = "Tipo: ${sched.tipo}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Dosis: ${sched.dosis}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Horario(s): ${sched.timesCsv}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "Desde: ${epochDayToLocalDate(sched.startEpochDay)}  —  Hasta: ${epochDayToLocalDate(sched.endEpochDay)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}