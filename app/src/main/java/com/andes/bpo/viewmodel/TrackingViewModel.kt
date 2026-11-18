package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.repository.SiniestroRepository
import com.andes.bpo.model.TrackingUiState
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrackingViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(
        application,
        viewModelScope
    )

    private val repository = SiniestroRepository(
        db.siniestroDao()
    )

    private val _uiState = MutableStateFlow(
        TrackingUiState()
    )

    val uiState: StateFlow<TrackingUiState> =
        _uiState.asStateFlow()

    private var cargaJob: Job? = null

    fun cargarDatosSiniestro(
        @Suppress("UNUSED_PARAMETER") rut: String
    ) {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            val usuario = SesionUsuario.obtenerSesion()

            if (usuario == null) {
                _uiState.value = TrackingUiState(
                    isLoading = false,
                    estadoActual = 0
                )
                return@launch
            }

            val nombre = usuario.nombre.trim()
                .substringBefore(" ")
                .ifBlank { "Usuario" }

            val iniciales = listOfNotNull(
                usuario.nombre.trim().firstOrNull(),
                usuario.apellido.trim().firstOrNull()
            ).joinToString("")
                .uppercase()
                .ifBlank { "US" }

            _uiState.value = TrackingUiState(
                isLoading = true,
                nombreUsuario = nombre,
                inicialesUsuario = iniciales
            )

            try {
                repository.getSiniestrosPorUsuario(
                    usuario.idUsuario
                ).collectLatest { listaSiniestros ->

                    val entity = listaSiniestros.firstOrNull()

                    if (entity != null) {
                        val paso = when (entity.etapaNombre) {
                            "Recibido" -> 1
                            "En evaluación" -> 2
                            "En liquidación" -> 3
                            "Cerrado" -> 4
                            else -> 0
                        }

                        _uiState.value = TrackingUiState(
                            isLoading = false,
                            nombreUsuario = nombre,
                            inicialesUsuario = iniciales,
                            numeroSiniestro = entity.numero,
                            tipo = entity.tipoSiniestroNombre,
                            comuna = entity.comunaNombre,
                            fechaOcurrencia = entity.fechaOcurrencia,
                            fechaReporte = entity.fechaReporte,
                            estadoActual = paso,
                            nombreLiquidador =
                                entity.liquidadorNombre
                                    ?: "Por asignar",
                            empresaLiquidador =
                                entity.companiaNombre,
                            subtipo = entity.descripcion,
                            modeloVehiculo = entity.modeloVehiculo,
                            numeroPoliza = entity.numeroPoliza,
                            aseguradora = entity.companiaNombre
                        )
                    } else {
                        _uiState.value = TrackingUiState(
                            isLoading = false,
                            nombreUsuario = nombre,
                            inicialesUsuario = iniciales,
                            numeroSiniestro = "",
                            estadoActual = 0
                        )
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = TrackingUiState(
                    isLoading = false,
                    nombreUsuario = nombre,
                    inicialesUsuario = iniciales,
                    estadoActual = 0
                )
            }
        }
    }
}
