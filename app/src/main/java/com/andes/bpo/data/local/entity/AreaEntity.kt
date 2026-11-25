package com.andes.bpo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey
    val idArea: Int,
    val nombre: String,
    val polizasDisponibles: String = ""
)
