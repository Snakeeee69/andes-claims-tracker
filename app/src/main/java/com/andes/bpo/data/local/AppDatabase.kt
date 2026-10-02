package com.andes.bpo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SiniestroEntity::class, HistorialEntity::class, EvidenciaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun siniestroDao(): SiniestroDao
}