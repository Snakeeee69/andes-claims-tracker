package com.andes.bpo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requisitos_area")
data class RequisitoAreaEntity(
    @PrimaryKey(autoGenerate = true)
    val idRequisito: Int = 0,
    val idArea: Int,
    val nombreDocumento: String,
    val esObligatorio: Boolean
)