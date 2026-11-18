package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.GestionEntity
import com.andes.bpo.data.repository.SiniestroRepository
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class HistorialUiState(
    val cargando: Boolean = true,
    val numeroSiniestro: String = "",
    val etapaActual: String = "",
    val gestiones: List<GestionEntity> = emptyList(),
    val error: String? = null
)

class HistorialViewModel(
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
        HistorialUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    fun cargarHistorial(idSiniestro: Int) {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {

            _uiState.value = HistorialUiState()

            try {
                val usuario = SesionUsuario.obtenerSesion()

                if (usuario == null) {
                    _uiState.value = HistorialUiState(
                        cargando = false,
                        error = "Debes iniciar sesión"
                    )
                    return@launch
                }

                val siniestro = repository
                    .getSiniestroPorId(idSiniestro)
                    .first()

                if (siniestro == null ||
                    siniestro.idAsegurado != usuario.idUsuario
                ) {
                    _uiState.value = HistorialUiState(
                        cargando = false,
                        error = "No tienes acceso a este siniestro"
                    )
                    return@launch
                }

                repository.getGestiones(idSiniestro)
                    .collectLatest { lista ->

                        _uiState.value = HistorialUiState(
                            cargando = false,
                            numeroSiniestro = siniestro.numero,
                            etapaActual = siniestro.etapaNombre,
                            gestiones = lista
                        )
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = HistorialUiState(
                    cargando = false,
                    error = "No se pudo cargar el historial"
                )
            }
        }
    }
}
