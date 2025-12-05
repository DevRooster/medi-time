package com.example.medi_time_up.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medi_time_up.data.Medicamento
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para interactuar con la tabla de medicamentos.
 * Pertenece a la capa de Datos (Infraestructura/Backend).
 */
@Dao
interface MedicamentoDao {

    /** Obtiene todos los medicamentos ordenados por hora. */
    @Query("SELECT * FROM medicamentos ORDER BY hora ASC")
    fun obtenerTodos(): Flow<List<Medicamento>>

    /** Inserta un nuevo medicamento. Ignora si hay conflicto. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(medicamento: Medicamento)

    /** Actualiza un medicamento existente. */
    @Update
    suspend fun actualizar(medicamento: Medicamento)

    /** Elimina un medicamento. */
    @Delete
    suspend fun eliminar(medicamento: Medicamento)

    /** Elimina todos los medicamentos. Útil para desarrollo/pruebas. */
    @Query("DELETE FROM medicamentos")
    suspend fun eliminarTodos()
}