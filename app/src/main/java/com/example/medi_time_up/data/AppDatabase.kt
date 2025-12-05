package com.example.medi_time_up.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.medi_time_up.data.dao.MedicamentoDao

@Database(entities = [Medicamento::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicamentoDao(): MedicamentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Patrón Singleton para evitar múltiples instancias de la base de datos
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meditime_db"
                )
                    // .fallbackToDestructiveMigration() // sólo en dev si lo necesitas
                    .build().also { INSTANCE = it }
            }
        }
    }
}