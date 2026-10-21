package com.andes.bpo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gestiones")
data class GestionEntity(
    @PrimaryKey(autoGenerate = true)
    val idGestion: Int = 0,
    val idSiniestro: Int,
    val fechaHora: String,
    val descripcion: String,
    val responsable: String,
    val visibleAsegurado: Boolean = true
)