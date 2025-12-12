package com.example.medi_time_up.data.dao

import androidx.room.*
import com.example.medi_time_up.data.ScheduledMedication
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMedicationDao {
    @Query("SELECT * FROM scheduled_medications ORDER BY startEpochDay ASC")
    fun getAllSchedules(): Flow<List<ScheduledMedication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduledMedication): Long

    @Update
    suspend fun update(schedule: ScheduledMedication)

    @Delete
    suspend fun delete(schedule: ScheduledMedication)

    @Query("SELECT * FROM scheduled_medications WHERE active = 1")
    suspend fun getActiveSchedules(): List<ScheduledMedication>

    @Query("SELECT * FROM scheduled_medications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduledMedication?

    @Query("SELECT * FROM scheduled_medications WHERE NOT (endEpochDay < :fromEpochDay OR startEpochDay > :toEpochDay)")
    suspend fun getSchedulesBetween(fromEpochDay: Long, toEpochDay: Long): List<ScheduledMedication>
}