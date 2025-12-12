package com.example.medi_time_up.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_medications")
data class ScheduledMedication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val tipo: String,
    val dosis: String,
    val timesCsv: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val selectedDatesCsv: String? = null,
    val remindBeforeMinutes: Int = 0,
    val active: Boolean = true
)