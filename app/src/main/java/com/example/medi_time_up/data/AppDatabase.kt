package com.example.medi_time_up.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.medi_time_up.data.dao.MedicamentoDao

@Database(entities = [Medicamento::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicamentoDao(): MedicamentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from 1 -> 2: add columns for new fields with defaults
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // add recordatorioTipo (TEXT), remindBeforeMinutes (INTEGER), tomado (INTEGER default 0)
                db.execSQL("ALTER TABLE medicamentos ADD COLUMN recordatorioTipo TEXT DEFAULT 'ALARMA' NOT NULL")
                db.execSQL("ALTER TABLE medicamentos ADD COLUMN remindBeforeMinutes INTEGER DEFAULT 0 NOT NULL")
                db.execSQL("ALTER TABLE medicamentos ADD COLUMN tomado INTEGER DEFAULT 0 NOT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meditime_db"
                )
                    .addMigrations(MIGRATION_1_2) // aplica migración segura
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}