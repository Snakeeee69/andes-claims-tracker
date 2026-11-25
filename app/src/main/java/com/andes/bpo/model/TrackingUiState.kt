package com.andes.bpo.model

data class TrackingUiState(
    val idSiniestro: Int = 0,
    val nombreUsuario: String = "",
    val inicialesUsuario: String = "",
    val numeroSiniestro: String = "",
    val estadoActual: Int = 1,
    val tipo: String = "",
    val patente: String = "",
    val comuna: String = "",
    val fechaOcurrencia: String = "",
    val fechaReporte: String = "",
    val nombreLiquidador: String = "",
    val empresaLiquidador: String = "",
    val subtipo: String = "",
    val modeloVehiculo: String = "",
    val numeroPoliza: String = "",
    val aseguradora: String = "",
    val fechaLimiteLegal: String = "",
    val diasRestantesPlazoLegal: Int = 45,
    val diasProrroga: Int = 0,
    val aceptadoPorLiquidador: Boolean? = null,
    val resultadoCierre: String? = null,
    val mensajeCierre: String? = null,
    val isLoading: Boolean = true
)
