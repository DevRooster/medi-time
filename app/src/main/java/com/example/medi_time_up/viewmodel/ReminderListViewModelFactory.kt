package com.example.medi_time_up.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medi_time_up.data.dao.MedicamentoDao
import java.lang.IllegalArgumentException

/**
 * Factory para crear ReminderListViewModel.
 */
class ReminderListViewModelFactory(private val dao: MedicamentoDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderListViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}