package com.example.medi_time_up.data.dao

import androidx.room.*
import com.example.medi_time_up.data.Medicamento
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {

    @Query("SELECT * FROM medicamentos ORDER BY hora ASC")
    fun obtenerTodos(): Flow<List<Medicamento>>

    @Query("SELECT * FROM medicamentos WHERE tomado = 1 ORDER BY hora DESC")
    fun obtenerTomados(): Flow<List<Medicamento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(medicamento: Medicamento): Long

    @Update
    suspend fun actualizar(medicamento: Medicamento)

    @Delete
    suspend fun eliminar(medicamento: Medicamento)

    @Query("DELETE FROM medicamentos")
    suspend fun eliminarTodos()

    // Helper para marcar como tomado / desmarcar
    @Query("UPDATE medicamentos SET tomado = :value WHERE id = :id")
    suspend fun marcarTomadoById(id: Long, value: Boolean)
}