package com.andes.bpo.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["rut"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val idUsuario: Int = 0,
    val rut: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val passwordHash: String,
    val passwordSalt: String,
    val rol: String = "ASEGURADO",
    val equipo: String? = null,
    val codigoRecuperacionHash: String? = null
)