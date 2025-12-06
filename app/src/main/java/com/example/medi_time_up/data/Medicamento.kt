package com.example.medi_time_up.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val cantidad: String?,
    val frecuencia: String?,   // "Una vez", "Diario", "Semanal", "Mensual"
    val hora: String?,         // texto de hora (ej. "08:00")
    // Nuevos campos:
    val recordatorioTipo: String = "ALARMA", // por ahora "ALARMA" (puede extenderse)
    val remindBeforeMinutes: Int = 0,        // cuántos minutos antes recordar (0 = no)
    val tomado: Boolean = false,             // si se marcó como "tomado" (para reporte)
    val createdAt: Long = System.currentTimeMillis()
)
