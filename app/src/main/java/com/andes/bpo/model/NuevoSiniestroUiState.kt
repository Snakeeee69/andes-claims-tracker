package com.andes.bpo.model

import com.andes.bpo.data.local.entity.AreaEntity
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.data.local.entity.RequisitoAreaEntity

data class NuevoSiniestroUiState(
    val areasDisponibles: List<AreaEntity> = emptyList(),
    val areaSeleccionada: AreaEntity? = null,
    val polizasDisponibles: List<String> = emptyList(),
    val polizaSeleccionada: String = "",
    val numeroPoliza: String = "",
    val archivoPolizaNombre: String? = null,
    val archivoPolizaUri: String? = null,
    val companiaNombre: String = "",
    val fechaOcurrencia: String = "",
    val comunaNombre: String = "",
    val detalleExtraVehiculo: String = "",
    val descripcion: String = "",
    val requisitosArea: List<RequisitoAreaEntity> = emptyList(),
    val adjuntosBorrador: List<DocumentoEntity> = emptyList(),
    val areaError: String? = null,
    val polizaError: String? = null,
    val numeroPolizaError: String? = null,
    val archivoPolizaError: String? = null,
    val fechaOcurrenciaError: String? = null,
    val comunaError: String? = null,
    val descripcionError: String? = null,
    val evidenciasError: String? = null,
    val isGuardando: Boolean = false,
    val guardadoExitoso: Boolean = false
)