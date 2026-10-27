package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.repository.SiniestroRepository
import com.andes.bpo.model.TrackingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SiniestroRepository

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = SiniestroRepository(db.siniestroDao())
    }

    fun cargarDatosSiniestro(rut: String) {
        val rutLimpio = rut.replace(".", "").replace("-", "")
        val idUsuario = if (rutLimpio == "158423715") 1 else 2

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getSiniestrosPorUsuario(idUsuario).collectLatest { listaSiniestros ->
                val entity = listaSiniestros.firstOrNull()

                if (entity != null) {
                    val paso = when (entity.etapaNombre) {
                        "Recibido" -> 1
                        "En evaluación" -> 2
                        "En liquidación" -> 3
                        "Cerrado" -> 4
                        else -> 2
                    }

                    _uiState.value = TrackingUiState(
                        isLoading = false,
                        numeroSiniestro = entity.numero,
                        tipo = entity.tipoSiniestroNombre,
                        fechaOcurrencia = entity.fechaOcurrencia,
                        fechaReporte = entity.fechaReporte,
                        patente = "BJKR-94",
                        estadoActual = paso,
                        nombreLiquidador = entity.liquidadorNombre ?: "Por asignar",
                        empresaLiquidador = entity.companiaNombre,
                        subtipo = entity.descripcion,
                        modeloVehiculo = entity.modeloVehiculo,
                        numeroPoliza = entity.numeroPoliza,
                        aseguradora = entity.companiaNombre
                    )
                } else {
                    _uiState.value = TrackingUiState(
                        isLoading = false,
                        numeroSiniestro = "",
                        estadoActual = 0
                    )
                }
            }
        }
    }
}