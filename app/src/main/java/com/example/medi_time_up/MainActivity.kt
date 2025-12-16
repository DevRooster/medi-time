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
import com.example.medi_time_up.data.ScheduledMedication
import com.example.medi_time_up.ui.screens.AddScheduleDialog
import com.example.medi_time_up.ui.screens.CalendarMonthScreen
import com.example.medi_time_up.ui.theme.MeditimeupTheme
import com.example.medi_time_up.util.AlarmScheduler
import com.example.medi_time_up.util.NotificationChannels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear canal de notificaciones
        NotificationChannels.create(this)

        // Permiso de notificaciones (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        setContent {
            MeditimeupTheme {

                val navController = rememberNavController()
                val context = LocalContext.current

                val scheduleDao = remember {
                    AppDatabase.getDatabase(context).scheduledMedicationDao()
                }

                val schedulesState by scheduleDao
                    .getAllSchedules()
                    .collectAsState(initial = emptyList())

                NavHost(
                    navController = navController,
                    startDestination = "calendar"
                ) {

                    // ================= CALENDAR =================
                    composable("calendar") {
                        CalendarMonthScreen(
                            schedulesForMonth = schedulesState,
                            onAddSchedule = { epochDay ->
                                navController.navigate("addSchedule/$epochDay")
                            },
                            onEditSchedule = { sched ->
                                navController.navigate("editSchedule/${sched.id}")
                            },
                            onDeleteSchedule = { sched ->
                                lifecycleScope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            AlarmScheduler.cancelSchedule(
                                                applicationContext,
                                                sched
                                            )
                                            AppDatabase.getDatabase(applicationContext)
                                                .scheduledMedicationDao()
                                                .delete(sched)
                                        }
                                        Toast.makeText(
                                            applicationContext,
                                            "Registro eliminado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (t: Throwable) {
                                        Log.e(TAG, "Error eliminando schedule", t)
                                    }
                                }
                            }
                        )
                    }

                    // ================= ADD =================
                    composable(
                        route = "addSchedule/{epochDay}",
                        arguments = listOf(
                            navArgument("epochDay") {
                                type = NavType.LongType
                            }
                        )
                    ) { backStackEntry ->

                        val epochDay =
                            backStackEntry.arguments?.getLong("epochDay")
                                ?: LocalDate.now().toEpochDay()

                        val dao = AppDatabase
                            .getDatabase(applicationContext)
                            .scheduledMedicationDao()

                        AddScheduleDialog(
                            preselectedEpochDay = epochDay,
                            dao = dao,
                            onClose = { navController.popBackStack() },
                            onSaved = {
                                Toast.makeText(
                                    applicationContext,
                                    "Recordatorio guardado",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            }
                        )
                    }

                    // ================= EDIT =================
                    composable(
                        route = "editSchedule/{id}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.LongType
                            }
                        )
                    ) { backStackEntry ->

                        val id = backStackEntry.arguments!!.getLong("id")
                        val dao = AppDatabase
                            .getDatabase(applicationContext)
                            .scheduledMedicationDao()

                        var schedule by remember {
                            mutableStateOf<ScheduledMedication?>(null)
                        }

                        LaunchedEffect(id) {
                            schedule = withContext(Dispatchers.IO) {
                                dao.getById(id)
                            }
                        }

                        schedule?.let { sched ->
                            AddScheduleDialog(
                                preselectedEpochDay = sched.startEpochDay,
                                dao = dao,
                                existing = sched,
                                onClose = { navController.popBackStack() },
                                onSaved = {
                                    Toast.makeText(
                                        applicationContext,
                                        "Recordatorio actualizado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}