package com.andes.bpo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_gestiones")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siniestro_id: String,
    val fecha: String,
    val etapa: String,
    val descripcion: String
)