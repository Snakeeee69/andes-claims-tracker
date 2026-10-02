package com.andes.bpo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "siniestros")
data class SiniestroEntity(
    @PrimaryKey val id: String,
    val tipo_siniestro: String,
    val fecha_ocurrencia: String,
    val fecha_reporte: String,
    val estado: String,
    val colaborador_asignado: String?
)