package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.repository.SiniestroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class TrackingUiState(
    val isLoading: Boolean = true,
    val numeroSiniestro: String = "",
    val tipo: String = "",
    val fechaOcurrencia: String = "",
    val fechaReporte: String = "",
    val patente: String = "",
    val estadoActual: Int = 1,
    val nombreLiquidador: String = "",
    val empresaLiquidador: String = "",
    val subtipo: String = "",
    val modeloVehiculo: String = "",
    val numeroPoliza: String = "",
    val aseguradora: String = ""
)

class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SiniestroRepository

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = SiniestroRepository(db.siniestroDao())
        cargarDatosSiniestroDesdeRoom(idSiniestro = 1)
    }

    fun cargarDatosSiniestro(rut: String) {
        cargarDatosSiniestroDesdeRoom(idSiniestro = 1)
    }

    fun cargarDatosSiniestroDesdeRoom(idSiniestro: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getSiniestroPorId(idSiniestro).collectLatest { entity ->
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
                        modeloVehiculo = entity.comunaNombre,
                        numeroPoliza = entity.numeroPoliza,
                        aseguradora = entity.companiaNombre
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}