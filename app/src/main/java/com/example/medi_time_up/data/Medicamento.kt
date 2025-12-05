package com.example.medi_time_up.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa la tabla 'medicamentos' en la base de datos Room.
 */
@Entity(tableName = "medicamentos")
data class Medicamento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String, // Amoxicilina
    val cantidad: String, // 500 mg
    val frecuencia: String, // 3 veces al día
    val hora: String, // 08:00 AM
    val tipo: String // Pastilla, Inyección, Gota (Para el icono)
)