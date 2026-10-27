package com.andes.bpo.repository

import com.andes.bpo.model.TrackingUiState
import kotlinx.coroutines.delay

class SiniestrosRepository {

    suspend fun obtenerSiniestroPorRut(rut: String): TrackingUiState {
        delay(1500)

        val rutLimpio = rut.replace(".", "").replace("-", "")

        return if (rutLimpio == "158423715") {
            TrackingUiState(
                numeroSiniestro = "SIN-26-004801",
                estadoActual = 3,
                tipo = "Vehículo – choque o colisión",
                patente = "BJKR-94",
                fechaOcurrencia = "02-09-2026",
                fechaReporte = "02-09-2026 18:40",
                nombreLiquidador = "Paulina F. – Equipo Vehículos",
                empresaLiquidador = "Seguros Cordillera",
                subtipo = "Colisión en intersección. Daños en parachoques delantero.",
                modeloVehiculo = "Suzuki Swift 2022",
                numeroPoliza = "POL-ASG01-77812",
                aseguradora = "Seguros Cordillera",
                isLoading = false
            )
        } else {
            TrackingUiState(
                numeroSiniestro = "",
                estadoActual = 0,
                isLoading = false
            )
        }
    }
}