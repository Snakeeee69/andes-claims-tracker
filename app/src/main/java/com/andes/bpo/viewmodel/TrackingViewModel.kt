package com.andes.bpo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.model.TrackingUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        cargarDatosSiniestro()
    }

    private fun cargarDatosSiniestro() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulamos la espera de conectarse al backend de Andes BPO
            delay(1500)

            // Inyectamos los datos reales del siniestro
            _uiState.update {
                it.copy(
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
    }
}