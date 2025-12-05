package com.example.medi_time_up.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medi_time_up.data.AppDatabase
import com.example.medi_time_up.data.dao.MedicamentoDao
import com.example.medi_time_up.ui.components.buttons.PrimaryButton
import com.example.medi_time_up.ui.components.form.CustomTextField
import com.example.medi_time_up.ui.components.utility.ToastNotification
import com.example.medi_time_up.viewmodel.AddMedicationViewModel
import java.lang.IllegalArgumentException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,

) {

    val context = LocalContext.current

    val dao: MedicamentoDao = AppDatabase.getDatabase(context).medicamentoDao()

    val viewModel: AddMedicationViewModel = viewModel(
        factory = AddMedicationViewModelFactory(dao)
    )

    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agregar medicamento") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // 1. Nombre del medicamento
            CustomTextField(
                value = state.nombre,
                onValueChange = viewModel::updateNombre,
                label = "Nombre del medicamento"
            )

            Spacer(Modifier.height(16.dp))

            // 2. Cantidad (Input con KeyboardType.Number)
            CustomTextField(
                value = state.cantidad,
                onValueChange = viewModel::updateCantidad,
                label = "Cantidad (ej. 500 mg)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(Modifier.height(16.dp))

            // 3. Frecuencia y Hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomTextField(
                    value = state.frecuencia,
                    onValueChange = viewModel::updateFrecuencia,
                    label = "Frecuencia",
                    modifier = Modifier.weight(1f)
                )
                CustomTextField(
                    value = state.hora,
                    onValueChange = viewModel::updateHora,
                    label = "Hora (ej. 8:00 AM)",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Botón Escanear receta
            Button(
                onClick = { /* Lógica de escaner */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                // ICONOS ARREGLADOS
                Icon(Icons.Filled.CameraAlt, contentDescription = "Escanear")
                Spacer(Modifier.width(8.dp))
                Text("Escanear receta")
            }

            Spacer(Modifier.height(16.dp))

            // Botón Guardar
            PrimaryButton(
                text = "Guardar",
                onClick = { viewModel.saveMedication(onNavigateBack) },
                enabled = state.isFormValid
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    // Mostrar Toast si el ViewModel lo indica
    if (state.showSuccessToast) {
        ToastNotification(message = "Recordatorio guardado con éxito.")
        viewModel.toastShown() // Limpiar el estado
    }
}

/**
 * Clase Factory simple para que el ViewModel funcione con el DAO de Room.
 * Definida localmente para resolver el error 'Unresolved reference'.
 */
class AddMedicationViewModelFactory(private val dao: MedicamentoDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddMedicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddMedicationViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}