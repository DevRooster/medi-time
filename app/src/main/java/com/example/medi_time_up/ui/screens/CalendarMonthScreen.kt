package com.example.medi_time_up.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medi_time_up.data.ScheduledMedication
import com.example.medi_time_up.util.epochDayToLocalDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarMonthScreen(
    schedulesForMonth: List<ScheduledMedication>,
    onAddSchedule: (Long) -> Unit,
    onEditSchedule: (ScheduledMedication) -> Unit,
    onDeleteSchedule: (ScheduledMedication) -> Unit
) {
    val todayEpoch = LocalDate.now().toEpochDay()

    var monthOffset by remember { mutableStateOf(0) }
    var selectedDay by remember { mutableStateOf<Long?>(null) }

    val displayedMonth = remember(monthOffset) {
        YearMonth.now().plusMonths(monthOffset.toLong())
    }

    // Reset selección al cambiar de mes (evita pantalla blanca)
    LaunchedEffect(displayedMonth) {
        selectedDay = null
    }

    val firstOfMonth = displayedMonth.atDay(1)
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7
    val daysInMonth = displayedMonth.lengthOfMonth()

    val calendarCells = remember(displayedMonth) {
        buildList {
            repeat(firstDayOfWeek) { add(null) }
            for (d in 1..daysInMonth) {
                add(firstOfMonth.withDayOfMonth(d).toEpochDay())
            }
            while (size % 7 != 0) add(null)
        }
    }

    val daysWithEvents = remember(schedulesForMonth) {
        schedulesForMonth.flatMap {
            (it.startEpochDay..it.endEpochDay).toList()
        }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ---------- HEADER CON NAVEGACIÓN ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { monthOffset-- }) {
                Text("‹", style = MaterialTheme.typography.headlineMedium)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayedMonth.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = displayedMonth.year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { monthOffset++ }) {
                Text("›", style = MaterialTheme.typography.headlineMedium)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------- WEEK DAYS ----------
        Row(Modifier.fillMaxWidth()) {
            listOf("Dom", "Lun", "Mar", "Mi", "Jue", "Vie", "Sáb").forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ---------- CALENDAR GRID ----------
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.heightIn(min = 260.dp),
            userScrollEnabled = false
        ) {
            items(calendarCells) { epochDay ->
                CalendarDayCell(
                    epochDay = epochDay,
                    isToday = epochDay == todayEpoch,
                    isSelected = epochDay == selectedDay,
                    hasEvent = epochDay != null && epochDay in daysWithEvents,
                    onClick = {
                        epochDay?.let {
                            if (it in daysWithEvents) {
                                selectedDay = it
                            } else {
                                onAddSchedule(it)
                            }
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---------- EVENTS SECTION ----------
        Text(
            text = "Medicamentos del día",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        when {
            selectedDay == null ->
                EmptyState("Selecciona un día del calendario")

            schedulesForMonth.none {
                selectedDay!! in it.startEpochDay..it.endEpochDay
            } ->
                EmptyState("No hay medicamentos para este día")

            else -> {
                val events = schedulesForMonth.filter {
                    selectedDay!! in it.startEpochDay..it.endEpochDay
                }

                Column {
                    events.forEach { sched ->
                        ScheduleCard(
                            sched = sched,
                            onEdit = { onEditSchedule(sched) },
                            onDelete = { onDeleteSchedule(sched) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    epochDay: Long?,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            else -> MaterialTheme.colorScheme.surface
        }
    )

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(bgColor)
            .clickable(enabled = epochDay != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (epochDay != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = epochDayToLocalDate(epochDay).dayOfMonth.toString(),
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                if (hasEvent) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    sched: ScheduledMedication,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(sched.nombre, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(4.dp))
            Text("Tipo: ${sched.tipo}", style = MaterialTheme.typography.bodySmall)
            Text("Dosis: ${sched.dosis}", style = MaterialTheme.typography.bodySmall)
            Text("Horario: ${sched.timesCsv}", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.MedicalServices,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}