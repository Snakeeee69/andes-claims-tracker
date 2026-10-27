package com.andes.bpo.model

data class TrackingUiState(
    val numeroSiniestro: String = "",
    val estadoActual: Int = 1,
    val tipo: String = "",
    val patente: String = "",
    val fechaOcurrencia: String = "",
    val fechaReporte: String = "",
    val nombreLiquidador: String = "",
    val empresaLiquidador: String = "",
    val subtipo: String = "",
    val modeloVehiculo: String = "",
    val numeroPoliza: String = "",
    val aseguradora: String = "",
    val isLoading: Boolean = true
)