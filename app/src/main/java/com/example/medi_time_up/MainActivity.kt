package com.example.medi_time_up

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medi_time_up.data.AppDatabase
import com.example.medi_time_up.data.Medicamento
import com.example.medi_time_up.data.ScheduledMedication
import com.example.medi_time_up.ui.screens.AddMedicationScreen
import com.example.medi_time_up.ui.screens.AddScheduleDialog
import com.example.medi_time_up.ui.screens.CalendarMonthScreen
import com.example.medi_time_up.ui.screens.ReminderListScreen
import com.example.medi_time_up.ui.theme.MeditimeupTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MeditimeupTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                // DAO para schedules (remember para no recrearlo cada recomposición)
                val scheduleDao = remember { AppDatabase.getDatabase(context).scheduledMedicationDao() }

                // Observamos todos los schedules en la DB (Flow -> State)
                val schedulesState by scheduleDao.getAllSchedules().collectAsState(initial = emptyList())

                NavHost(navController = navController, startDestination = "calendar") {
                    // ---------- Calendar (pantalla principal) ----------
                    composable("calendar") {
                        CalendarMonthScreen(
                            schedulesForMonth = schedulesState,
                            onAddSchedule = { epochDay ->
                                // Navegar a la pantalla de añadir schedule pasando epochDay como argumento
                                navController.navigate("addSchedule/$epochDay")
                            },
                            onEditSchedule = { sched ->
                                // navegar a la pantalla de edición por id
                                navController.navigate("editSchedule/${sched.id}")
                            },
                            onDeleteSchedule = { sched ->
                                // cancelar alarms y borrar en background, luego toast
                                lifecycleScope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            AlarmScheduler.cancelSchedule(applicationContext, sched)
                                            AppDatabase.getDatabase(applicationContext).scheduledMedicationDao().delete(sched)
                                        }
                                        Toast.makeText(applicationContext, "Registro eliminado", Toast.LENGTH_SHORT).show()
                                    } catch (t: Throwable) {
                                        Log.e(TAG, "Error eliminando schedule", t)
                                        Toast.makeText(applicationContext, "Error eliminando: ${t.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }

                    // ---------- Add schedule (dialog) usando epochDay como argumento ----------
                    composable(
                        route = "addSchedule/{epochDay}",
                        arguments = listOf(navArgument("epochDay") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val epochDayArg = backStackEntry.arguments?.getLong("epochDay")
                            ?: LocalDate.now().toEpochDay()
                        // Obtener DAO para ScheduledMedication
                        val dao = AppDatabase.getDatabase(applicationContext).scheduledMedicationDao()

                        // Mostramos el diálogo composable que encapsula la lógica de inserción + scheduling
                        AddScheduleDialog(
                            preselectedEpochDay = epochDayArg,
                            dao = dao,
                            onClose = { navController.popBackStack() }, // cerrar si cancelas
                            onSaved = { saved ->
                                // esto se llama una vez que la BD y AlarmScheduler terminaron
                                Toast.makeText(applicationContext, "Recordatorio guardado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // <-- solo aquí navegamos
                            }
                        )
                    }

                    // ---------- Edit schedule (dialog) by id ----------
                    composable(
                        route = "editSchedule/{scheduleId}",
                        arguments = listOf(navArgument("scheduleId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: -1L
                        val dao = AppDatabase.getDatabase(applicationContext).scheduledMedicationDao()

                        // existing entity to edit
                        var existing by remember { mutableStateOf<ScheduledMedication?>(null) }

                        // Load the existing schedule once
                        LaunchedEffect(scheduleId) {
                            if (scheduleId > 0L) {
                                try {
                                    // Requiere que tu DAO tenga suspend fun getById(id: Long): ScheduledMedication?
                                    existing = withContext(Dispatchers.IO) {
                                        dao.getById(scheduleId)
                                    }
                                } catch (t: Throwable) {
                                    Log.e(TAG, "Error cargando schedule id=$scheduleId", t)
                                }
                            }
                        }

                        // Si ya cargamos la entidad, mostramos el diálogo para editarla
                        if (existing != null) {
                            AddScheduleDialog(
                                preselectedEpochDay = existing!!.startEpochDay,
                                dao = dao,
                                existing = existing,
                                onClose = { navController.popBackStack() },
                                onSaved = { saved ->
                                    Toast.makeText(applicationContext, "Registro actualizado", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            )
                        } else {
                            // Mientras carga, mostramos un dialogo simple o nada.
                            // Para evitar pantalla en blanco, mostramos un pequeño placeholder
                            // y la UI volverá cuando existing no sea null.
                        }
                    }

                    // ---------- Lista tradicional (opcional) ----------
                    composable("list") {
                        ReminderListScreen(
                            onAddMedicationClick = { navController.navigate("add") }
                        )
                    }

                    // ---------- Add medication (pantalla de formulario simple) ----------
                    composable("add") {
                        // Lambda que recibirá los datos desde AddMedicationScreen
                        val onSaveMedication: (
                            name: String,
                            dosage: String,
                            time: String,
                            days: List<String>,
                            remindBefore: Boolean
                        ) -> Unit = { name, dosage, time, days, remindBefore ->
                            lifecycleScope.launch {
                                try {
                                    Log.d(TAG, "Guardando medicamento: $name, $dosage, $time, days=${days.size}, remindBefore=$remindBefore")
                                    val db = AppDatabase.getDatabase(applicationContext)
                                    val dao = db.medicamentoDao()

                                    val frecuencia = if (days.isEmpty()) null else days.joinToString(separator = ";")

                                    val med = Medicamento(
                                        nombre = name,
                                        cantidad = if (dosage.isBlank()) null else dosage,
                                        frecuencia = frecuencia,
                                        hora = if (time.isBlank()) null else time,
                                        recordatorioTipo = "ALARMA",
                                        remindBeforeMinutes = if (remindBefore) 5 else 0,
                                        tomado = false
                                    )

                                    withContext(Dispatchers.IO) {
                                        dao.insertar(med)
                                    }

                                    navController.popBackStack()
                                    Toast.makeText(applicationContext, "Recordatorio guardado", Toast.LENGTH_SHORT).show()
                                    Log.d(TAG, "Medicamento guardado correctamente")

                                } catch (t: Throwable) {
                                    Log.e(TAG, "Error guardando medicamento", t)
                                    Toast.makeText(applicationContext, "Error al guardar: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        AddMedicationScreen(
                            navController = navController,
                            onSaveMedication = onSaveMedication
                        )
                    }
                }
            }
        }
    }
}