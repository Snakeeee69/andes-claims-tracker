package com.andes.bpo.repository

import com.andes.bpo.model.TrackingUiState
import kotlinx.coroutines.delay

class SiniestrosRepository {
    // Aquí usaremos Retrofit para hablar con la API real

    suspend fun obtenerSiniestroPorRut(rut: String): TrackingUiState {
        delay(1500) // Simula el tiempo de carga de internet

        return TrackingUiState(
            numeroSiniestro = "SIN-2026-08471",
            estadoActual = 2,
            tipo = "Accidente vehicular",
            patente = "BJKR-94",
            fechaOcurrencia = "15 ago 2026",
            fechaReporte = "16 ago 2026",
            nombreLiquidador = "María González",
            empresaLiquidador = "Liquidaciones Andes · Andes SpA",
            subtipo = "Colisión con daños materiales",
            modeloVehiculo = "Toyota Yaris 2022",
            numeroPoliza = "POL-88231-VH",
            aseguradora = "Aseguradora del Sur S.A.",
            isLoading = false
        )
    }
}