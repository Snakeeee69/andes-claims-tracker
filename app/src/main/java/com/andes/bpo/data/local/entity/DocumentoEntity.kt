package com.andes.bpo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documentos")
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true)
    val idDocumento: Int = 0,
    val idSiniestro: Int,
    val nombreTipoDocumento: String,
    val comentario: String?,
    val fechaSolicitud: String,
    val estado: String,
    val nombreArchivo: String? = null,
    val fechaRecepcion: String? = null
)