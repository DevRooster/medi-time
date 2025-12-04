package com.example.medi_time_up

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext // Necesario para obtener el Contexto si se requiere
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medi_time_up.ui.screens.AddMedicationScreen
import com.example.medi_time_up.ui.screens.ReminderListScreen
import com.example.medi_time_up.ui.theme.MeditimeupTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MeditimeupTheme {
                AppNavigation()
            }
        }
    }
}

/**
 * Define el grafo de navegación de la aplicación usando Jetpack Compose Navigation.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Ya no necesitas 'context' aquí para pasarlo a AddMedicationScreen.

    NavHost(navController = navController, startDestination = "recordatorios") {

        // RUTA 1: Pantalla de Listado (Tus Recordatorios)
        composable("recordatorios") {
            ReminderListScreen(
                onAddMedicationClick = { navController.navigate("agregar_medicamento") }
            )
        }

        // RUTA 2: Pantalla de Agregar Medicamento
        composable("agregar_medicamento") {
            // CORRECCIÓN: Eliminado el parámetro 'context'
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ... otras rutas
    }
}