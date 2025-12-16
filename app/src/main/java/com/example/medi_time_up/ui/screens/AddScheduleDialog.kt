package com.example.medi_time_up.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.medi_time_up.data.ScheduledMedication
import com.example.medi_time_up.data.dao.ScheduledMedicationDao
import com.example.medi_time_up.util.AlarmScheduler
import com.example.medi_time_up.util.hhmmFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

private const val TAG = "AddScheduleDialog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
    preselectedEpochDay: Long,
    dao: ScheduledMedicationDao,
    existing: ScheduledMedication? = null,
    onClose: () -> Unit,
    onSaved: (ScheduledMedication) -> Unit
) {
    val context = LocalContext.current
    val initialDate = LocalDate.ofEpochDay(preselectedEpochDay)

    var nombre by remember { mutableStateOf(existing?.nombre ?: "") }
    var tipo by remember { mutableStateOf(existing?.tipo ?: "Pastilla") }
    var dosis by remember { mutableStateOf(existing?.dosis ?: "") }

    var startEpochDay by remember { mutableStateOf(existing?.startEpochDay ?: initialDate.toEpochDay()) }
    var endEpochDay by remember { mutableStateOf(existing?.endEpochDay ?: initialDate.toEpochDay()) }

    // ✅ Inicializar modo correctamente al editar
    var modeInterval by remember {
        mutableStateOf(existing == null || existing.timesCsv.contains(","))
    }

    var intervalHours by remember { mutableStateOf(8) }
    var timesPerDay by remember { mutableStateOf(3) }

    var initialHour by remember {
        mutableStateOf(
            existing?.timesCsv
                ?.split(",")
                ?.firstOrNull()
                ?.let {
                    val p = it.split(":")
                    LocalTime.of(p[0].toInt(), p[1].toInt())
                } ?: LocalTime.of(8, 0)
        )
    }

    var remindBefore by remember { mutableStateOf(existing?.remindBeforeMinutes ?: 0) }

    val timePicker = TimePickerDialog(
        context,
        { _, h, m -> initialHour = LocalTime.of(h, m) },
        initialHour.hour,
        initialHour.minute,
        true
    )

    fun showDatePicker(initial: LocalDate, onPick: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, y, mo, d -> onPick(LocalDate.of(y, mo + 1, d)) },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(if (existing == null) "Agregar recordatorio" else "Editar recordatorio") },
        text = {
            Column(Modifier.fillMaxWidth()) {

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = dosis,
                    onValueChange = { dosis = it },
                    label = { Text("Dosis (ej. 500 mg)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { modeInterval = true }) { Text("Intervalo (hrs)") }
                    Button(onClick = { modeInterval = false }) { Text("Veces por día") }
                }

                Spacer(Modifier.height(8.dp))

                if (modeInterval) {
                    OutlinedTextField(
                        value = intervalHours.toString(),
                        onValueChange = {
                            intervalHours = it.toIntOrNull()?.coerceAtLeast(1) ?: intervalHours
                        },
                        label = { Text("Intervalo en horas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = timesPerDay.toString(),
                        onValueChange = {
                            timesPerDay = it.toIntOrNull()?.coerceAtLeast(1) ?: timesPerDay
                        },
                        label = { Text("Veces por día") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(onClick = { timePicker.show() }) {
                    Text("Hora inicial: ${initialHour.format(hhmmFormatter)}")
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        showDatePicker(LocalDate.ofEpochDay(startEpochDay)) {
                            startEpochDay = it.toEpochDay()
                        }
                    }) {
                        Text("Inicio: ${LocalDate.ofEpochDay(startEpochDay)}")
                    }

                    Button(onClick = {
                        showDatePicker(LocalDate.ofEpochDay(endEpochDay)) {
                            endEpochDay = it.toEpochDay()
                        }
                    }) {
                        Text("Fin: ${LocalDate.ofEpochDay(endEpochDay)}")
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = remindBefore.toString(),
                    onValueChange = {
                        remindBefore = it.toIntOrNull()?.coerceAtLeast(0) ?: remindBefore
                    },
                    label = { Text("Recordar X minutos antes (0 = no)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {

                if (nombre.isBlank()) return@TextButton

                val times = mutableListOf<LocalTime>()

                if (modeInterval) {
                    var t = initialHour
                    repeat(24) {
                        times.add(t)
                        t = t.plusHours(intervalHours.toLong())
                    }
                } else {
                    val spacing = 24.0 / timesPerDay
                    repeat(timesPerDay) { i ->
                        val minutes =
                            ((initialHour.toSecondOfDay() / 60) + spacing * 60 * i).toInt() % 1440
                        times.add(LocalTime.of(minutes / 60, minutes % 60))
                    }
                }

                val timesCsv = times.joinToString(",") { it.format(hhmmFormatter) }

                val toSave = ScheduledMedication(
                    id = existing?.id ?: 0,
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

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (existing == null) {
                            val newId = dao.insert(toSave)
                            val saved = toSave.copy(id = newId)
                            AlarmScheduler.scheduleForSchedule(context, saved)
                            withContext(Dispatchers.Main) { onSaved(saved) }
                        } else {
                            AlarmScheduler.cancelSchedule(context, existing)
                            dao.update(toSave)
                            AlarmScheduler.scheduleForSchedule(context, toSave)
                            withContext(Dispatchers.Main) { onSaved(toSave) }
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "Error saving schedule", t)
                    }
                }
            }) {
                Text(if (existing == null) "Guardar" else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Cancelar") }
        }
    )
}