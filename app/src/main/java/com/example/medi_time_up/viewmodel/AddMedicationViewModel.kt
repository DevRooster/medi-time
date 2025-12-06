package com.example.medi_time_up.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medi_time_up.data.Medicamento
import com.example.medi_time_up.data.dao.MedicamentoDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de UI para la pantalla de agregar medicamento.
 * Ajusta los campos según lo que uses en la UI.
 */
data class AddMedicationUiState(
    val nombre: String = "",
    val cantidad: String = "",
    val frecuencia: String? = null,
    val hora: String = "",
    val isFormValid: Boolean = false,
    val showSuccessToast: Boolean = false
)

class AddMedicationViewModel(
    private val dao: MedicamentoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    // Datos temporales para el recordatorio (se envían al save)
    private var reminderTipoLocal: String = "ALARMA"
    private var remindBeforeLocal: Int = 0

    // --- Actualizadores de campos (llamados desde la UI) ---
    fun updateNombre(new: String) {
        _uiState.update { it.copy(nombre = new, isFormValid = validateForm(new, it.cantidad, it.hora)) }
    }

    fun updateCantidad(new: String) {
        _uiState.update { it.copy(cantidad = new, isFormValid = validateForm(it.nombre, new, it.hora)) }
    }

    fun updateFrecuencia(new: String) {
        _uiState.update { it.copy(frecuencia = new, isFormValid = validateForm(it.nombre, it.cantidad, it.hora)) }
    }

    fun updateHora(new: String) {
        _uiState.update { it.copy(hora = new, isFormValid = validateForm(it.nombre, it.cantidad, new)) }
    }

    // Validación simple: nombre y hora no vacíos (puedes mejorar)
    private fun validateForm(nombre: String, cantidad: String, hora: String): Boolean {
        return nombre.isNotBlank() && hora.isNotBlank()
    }

    // --- Reminder extras ---
    fun setReminderExtras(recordatorioTipo: String, remindBeforeMinutes: Int) {
        reminderTipoLocal = recordatorioTipo
        remindBeforeLocal = remindBeforeMinutes
    }

    // --- Guardar medicamento en base de datos ---
    fun saveMedication(onDone: () -> Unit = {}) {
        val state = _uiState.value
        viewModelScope.launch {
            val med = Medicamento(
                nombre = state.nombre,
                cantidad = state.cantidad.ifBlank { null },
                frecuencia = state.frecuencia,
                hora = state.hora.ifBlank { null },
                recordatorioTipo = reminderTipoLocal,
                remindBeforeMinutes = remindBeforeLocal,
                tomado = false
            )
            dao.insertar(med)
            // mostrar toast y limpiar formulario (opcional)
            _uiState.update { it.copy(showSuccessToast = true) }
            // Llamada al callback (navegar atrás, etc.)
            onDone()
        }
    }

    // Llamar desde la UI para resetear el flag del toast
    fun toastShown() {
        _uiState.update { it.copy(showSuccessToast = false) }
    }
}