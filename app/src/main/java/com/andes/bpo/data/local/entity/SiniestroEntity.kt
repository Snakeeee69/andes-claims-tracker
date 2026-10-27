package com.andes.bpo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "siniestros")
data class SiniestroEntity(
    @PrimaryKey(autoGenerate = true)
    val idSiniestro: Int = 0,
    val numero: String,
    val idAsegurado: Int,
    val companiaNombre: String,
    val numeroPoliza: String,
    val tipoSiniestroNombre: String,
    val modeloVehiculo: String = "Suzuki Swift 2022",
    val fechaOcurrencia: String,
    val fechaReporte: String,
    val comunaNombre: String,
    val descripcion: String,
    val etapaNombre: String,
    val liquidadorNombre: String?,
    val proximoPasoTexto: String,
    val reporteFueraDePlazo: Boolean = false,
    val resultadoCierre: String? = null
)