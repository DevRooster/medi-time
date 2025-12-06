package com.example.medi_time_up

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medi_time_up.data.AppDatabase
import com.example.medi_time_up.data.Medicamento
import com.example.medi_time_up.ui.screens.AddMedicationScreen
import com.example.medi_time_up.ui.screens.ReminderListScreen
import com.example.medi_time_up.ui.theme.MeditimeupTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MeditimeupTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "list") {
                    composable("list") {
                        // pasa la navegación para abrir el formulario
                        ReminderListScreen(
                            onAddMedicationClick = { navController.navigate("add") }
                        )
                    }

                    composable("add") {
                        // Lambda que recibirá los datos desde AddMedicationScreen
                        val onSaveMedication: (
                            name: String,
                            dosage: String,
                            time: String,
                            days: List<String>,
                            remindBefore: Boolean
                        ) -> Unit = { name, dosage, time, days, remindBefore ->
                            // guardamos en la DB en un coroutine
                            lifecycleScope.launch {
                                try {
                                    Log.d(TAG, "Guardando medicamento: $name, $dosage, $time, days=${days.size}, remindBefore=$remindBefore")
                                    val db = AppDatabase.getDatabase(applicationContext)
                                    val dao = db.medicamentoDao()

                                    // Convertimos 'days' en una cadena simple para guardar en 'frecuencia'
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

                                    // --- Ejecutar la inserción en IO (evita bloquear/composition) ---
                                    withContext(Dispatchers.IO) {
                                        dao.insertar(med)
                                    }

                                    // --- Solo después de insertar navegamos de vuelta ---
                                    navController.popBackStack()

                                    // Feedback al usuario (Toast) en hilo Main
                                    Toast.makeText(applicationContext, "Recordatorio guardado", Toast.LENGTH_SHORT).show()
                                    Log.d(TAG, "Medicamento guardado correctamente")

                                } catch (t: Throwable) {
                                    // Manejo robusto de errores: log + toast
                                    Log.e(TAG, "Error guardando medicamento", t)
                                    Toast.makeText(applicationContext, "Error al guardar: ${t.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        // Llamada a la pantalla con navController y la lambda de guardado
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