package com.example.medi_time_up.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.medi_time_up.data.dao.MedicamentoDao
import com.example.medi_time_up.data.dao.ScheduledMedicationDao

// Aumentamos version a 3 porque añadimos una nueva entidad ScheduledMedication
@Database(
    entities = [Medicamento::class, ScheduledMedication::class],
    version = 3,
    exportSchema = true // recomendable para generar el JSON del schema (útil para migraciones)
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun scheduledMedicationDao(): ScheduledMedicationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from 1 -> 2 already present in your file (if you keep it)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medicamentos ADD COLUMN recordatorioTipo TEXT DEFAULT 'ALARMA' NOT NULL")
                db.execSQL("ALTER TABLE medicamentos ADD COLUMN remindBeforeMinutes INTEGER DEFAULT 0 NOT NULL")
                db.execSQL("ALTER TABLE medicamentos ADD COLUMN tomado INTEGER DEFAULT 0 NOT NULL")
            }
        }

        // Migration 2 -> 3: crear tabla scheduled_medications con la estructura que definimos
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `scheduled_medications` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `nombre` TEXT NOT NULL,
                      `tipo` TEXT NOT NULL,
                      `dosis` TEXT NOT NULL,
                      `timesCsv` TEXT NOT NULL,
                      `startEpochDay` INTEGER NOT NULL,
                      `endEpochDay` INTEGER NOT NULL,
                      `selectedDatesCsv` TEXT,
                      `remindBeforeMinutes` INTEGER NOT NULL,
                      `active` INTEGER NOT NULL DEFAULT 1
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meditime_db"
                )
                    // Opción segura: aplicar migraciones conocidas
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    // Si prefieres en desarrollo borrar DB y crear de nuevo, comenta la línea superior y usa fallback:
                    // .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}