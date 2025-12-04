package com.example.medi_time_up.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medi_time_up.data.Medicamento
import com.example.medi_time_up.data.dao.MedicamentoDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Define el estado de la UI del formulario de agregar medicamento */
data class AddMedicationUiState(
    val nombre: String = "",
    val cantidad: String = "",
    val frecuencia: String = "",
    val hora: String = "",
    val tipo: String = "Pastilla", // Tipo por defecto
    val isFormValid: Boolean = false,
    val showSuccessToast: Boolean = false
)

class AddMedicationViewModel(private val dao: MedicamentoDao) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState

    // --- Lógica de Actualización de Campos ---

    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
        validateForm()
    }

    fun updateCantidad(cantidad: String) {
        _uiState.update { it.copy(cantidad = cantidad) }
        validateForm()
    }

    fun updateFrecuencia(frecuencia: String) {
        _uiState.update { it.copy(frecuencia = frecuencia) }
        validateForm()
    }

    fun updateHora(hora: String) {
        _uiState.update { it.copy(hora = hora) }
        validateForm()
    }

    fun updateTipo(tipo: String) {
        _uiState.update { it.copy(tipo = tipo) }
    }

    // --- Lógica de Validación y Guardado ---

    private fun validateForm() {
        _uiState.update {
            it.copy(
                isFormValid = it.nombre.isNotBlank() &&
                        it.cantidad.isNotBlank() &&
                        it.hora.isNotBlank()
            )
        }
    }

    fun saveMedication(onSaveSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isFormValid) {
            val nuevoMedicamento = Medicamento(
                nombre = state.nombre,
                cantidad = state.cantidad,
                frecuencia = state.frecuencia,
                hora = state.hora,
                tipo = state.tipo
            )

            viewModelScope.launch {
                dao.insertar(nuevoMedicamento)
                // Mostrar notificación y navegar
                _uiState.update { it.copy(showSuccessToast = true) }
                onSaveSuccess()
            }
        }
    }

    fun toastShown() {
        // Reiniciar el flag de notificación después de que se muestra
        _uiState.update { it.copy(showSuccessToast = false) }
    }
}