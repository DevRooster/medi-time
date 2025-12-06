package com.example.medi_time_up.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medi_time_up.data.Medicamento
import com.example.medi_time_up.data.dao.MedicamentoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ReminderListVM"

data class ReminderListUiState(
    val medicamentos: List<Medicamento> = emptyList(),
    val isLoading: Boolean = false
)

class ReminderListViewModel(private val dao: MedicamentoDao) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderListUiState(isLoading = true))
    val uiState: StateFlow<ReminderListUiState> = _uiState.asStateFlow()

    init {
        // Recogemos la lista de Room de forma segura: onStart -> set loading,
        // catch -> loguea errores, collect -> actualiza estado
        viewModelScope.launch {
            dao.obtenerTodos()
                .onStart {
                    _uiState.update { it.copy(isLoading = true) }
                }
                .catch { t ->
                    Log.e(TAG, "Error recogiendo medicamentos desde DAO", t)
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { list ->
                    _uiState.update { it.copy(medicamentos = list, isLoading = false) }
                }
        }
    }

    /**
     * Elimina un medicamento de la base de datos sin bloquear la UI.
     */
    fun deleteMedication(medicamento: Medicamento) {
        viewModelScope.launch {
            try {
                // Ejecutar en IO por seguridad
                withContext(Dispatchers.IO) {
                    dao.eliminar(medicamento)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error al eliminar medicamento", t)
            }
        }
    }
}