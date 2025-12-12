package com.example.medi_time_up

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.Flow // opcional, pero ayuda a entender tipos
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

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
                // **IMPORTANTE**: especificamos el tipo del initial para ayudar al compilador
                val schedulesState by scheduleDao.getAllSchedules().collectAsState(initial = emptyList<ScheduledMedication>())

                NavHost(navController = navController, startDestination = "calendar") {
                    // ---------- Calendar (pantalla principal) ----------
                    composable("calendar") {
                        CalendarMonthScreen(
                            schedulesForMonth = schedulesState,
                            onAddSchedule = { epochDay ->
                                // Navegar a la pantalla de añadir schedule pasando epochDay como argumento
                                navController.navigate("addSchedule/$epochDay")
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
                            onClose = { navController.popBackStack() },
                            onSaved = { schedule ->
                                // El diálogo ya programa alarms; podemos mostrar toast y volver
                                Toast.makeText(applicationContext, "Recordatorio programado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        )
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