package com.andes.bpo.model

data class TrackingUiState(
    val numeroSiniestro: String = "",
    val estadoActual: Int = 1, // 1: Recibido, 2: En evaluación, 3: En liquidación, 4: Cerrado
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