package com.example.medi_time_up.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medi_time_up.data.Medicamento
import com.example.medi_time_up.data.dao.MedicamentoDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Define el estado de la UI para la pantalla de lista.
 */
data class ReminderListUiState(
    val medicamentos: List<Medicamento> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel para gestionar el listado de medicamentos.
 */
class ReminderListViewModel(private val dao: MedicamentoDao) : ViewModel() {

    // (El resto del código interno es correcto)

    private val _uiState = MutableStateFlow(ReminderListUiState())
    val uiState: StateFlow<ReminderListUiState> = _uiState

    init {
        // La implementación del DAO y Flow es correcta, asumiendo que el DAO tiene 'getAllMedicamentos'
        viewModelScope.launch {
            dao.obtenerTodos().collect { list -> // Usamos 'obtenerTodos' según tu DAO corregido
                _uiState.update { it.copy(medicamentos = list, isLoading = false) }
            }
        }
    }

    /**
     * Elimina un medicamento de la base de datos de forma asíncrona.
     */
    fun deleteMedication(medicamento: Medicamento) {
        viewModelScope.launch {
            // El DAO solo tiene 'eliminar', no 'deleteById' ni 'delete'.
            dao.eliminar(medicamento)
        }
    }
}