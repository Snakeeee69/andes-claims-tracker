package com.andes.bpo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evidencias")
data class EvidenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siniestro_id: String,
    val ruta_archivo: String,
    val tipo_archivo: String
)