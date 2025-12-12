package com.example.medi_time_up.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.medi_time_up.data.ScheduledMedication
import com.example.medi_time_up.data.dao.ScheduledMedicationDao
import com.example.medi_time_up.util.hhmmFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun AddScheduleDialog(
    preselectedEpochDay: Long,
    dao: ScheduledMedicationDao,
    onClose: () -> Unit,
    onSaved: (ScheduledMedication) -> Unit
) {
    val context = LocalContext.current
    val initialDate = LocalDate.ofEpochDay(preselectedEpochDay)

    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Pastilla") }
    var dosis by remember { mutableStateOf("") }

    var startEpochDay by remember { mutableStateOf(initialDate.toEpochDay()) }
    var endEpochDay by remember { mutableStateOf(initialDate.toEpochDay()) }

    var modeInterval by remember { mutableStateOf(true) } // true = intervalo en horas, false = timesPerDay
    var intervalHours by remember { mutableStateOf(8) }
    var timesPerDay by remember { mutableStateOf(3) }

    // hora inicial
    var initialHour by remember { mutableStateOf(LocalTime.of(8, 0)) }

    var remindBefore by remember { mutableStateOf(0) }

    val timePicker = TimePickerDialog(context, { _, h, m ->
        initialHour = LocalTime.of(h, m)
    }, initialHour.hour, initialHour.minute, true)

    fun showDatePicker(initial: LocalDate, onPick: (LocalDate) -> Unit) {
        val dp = DatePickerDialog(context, { _, y, mo, d ->
            onPick(LocalDate.of(y, mo+1, d)) // month param in DatePickerDialog is 0-based in some APIs; adjust if necessary
        }, initial.year, initial.monthValue - 1, initial.dayOfMonth)
        dp.show()
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Agregar recordatorio") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = dosis, onValueChange = { dosis = it }, label = { Text("Dosis (ej. 500 mg)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                // Modo
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { modeInterval = true }) { Text("Intervalo (hrs)") }
                    Button(onClick = { modeInterval = false }) { Text("Veces por día") }
                }
                Spacer(Modifier.height(8.dp))

                if (modeInterval) {
                    OutlinedTextField(value = intervalHours.toString(), onValueChange = { intervalHours = it.toIntOrNull() ?: 8 }, label = { Text("Intervalo en horas (ej. 8)") }, modifier = Modifier.fillMaxWidth())
                } else {
                    OutlinedTextField(value = timesPerDay.toString(), onValueChange = { timesPerDay = it.toIntOrNull() ?: 1 }, label = { Text("Veces por día (ej. 3)") }, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(8.dp))
                Row {
                    Button(onClick = { timePicker.show() }) { Text("Seleccionar hora inicial: ${initialHour.format(hhmmFormatter)}") }
                }
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showDatePicker(LocalDate.ofEpochDay(startEpochDay)) { startEpochDay = it.toEpochDay() } }) { Text("Inicio: ${LocalDate.ofEpochDay(startEpochDay)}") }
                    Button(onClick = { showDatePicker(LocalDate.ofEpochDay(endEpochDay)) { endEpochDay = it.toEpochDay() } }) { Text("Fin: ${LocalDate.ofEpochDay(endEpochDay)}") }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = remindBefore.toString(), onValueChange = { remindBefore = it.toIntOrNull() ?: 0 }, label = { Text("Recordar X minutos antes (0 = no)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Calculamos timesCsv
                val times = mutableListOf<LocalTime>()
                if (modeInterval) {
                    var t = initialHour
                    while (t.hour < 24) {
                        times.add(t)
                        t = t.plusHours(intervalHours.toLong())
                        if (times.size > 24) break
                    }
                } else {
                    // Generar evenly spaced times starting from initialHour
                    val spacing = 24.0 / timesPerDay
                    for (i in 0 until timesPerDay) {
                        val minutes = (initialHour.toSecondOfDay() / 60 + (spacing * 60 * i)).toInt() % (24*60)
                        val hh = minutes / 60
                        val mm = minutes % 60
                        times.add(LocalTime.of(hh, mm))
                    }
                }
                val timesCsv = times.joinToString(",") { it.format(hhmmFormatter) }

                // Insert schedule
                val schedule = ScheduledMedication(
                    nombre = nombre,
                    tipo = tipo,
                    dosis = dosis,
                    timesCsv = timesCsv,
                    startEpochDay = startEpochDay,
                    endEpochDay = endEpochDay,
                    selectedDatesCsv = null,
                    remindBeforeMinutes = remindBefore,
                    active = true
                )

                // Insert async
                CoroutineScope(Dispatchers.IO).launch {
                    val id = dao.insert(schedule)
                    // schedule alarms via helper (on main thread or in IO)
                    AlarmScheduler.scheduleForSchedule(context, schedule.copy(id = id))
                }
                onSaved(schedule)
                onClose()
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Cancelar") }
        }
    )
}