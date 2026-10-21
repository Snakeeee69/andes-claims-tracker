package com.andes.bpo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avisos")
data class AvisoEntity(
    @PrimaryKey(autoGenerate = true)
    val idAviso: Int = 0,
    val idSiniestro: Int,
    val fechaHora: String,
    val mensaje: String,
    val leido: Boolean = false
)