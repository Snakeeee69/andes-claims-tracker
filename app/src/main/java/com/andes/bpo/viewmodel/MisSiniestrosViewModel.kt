
package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.data.repository.SiniestroRepository
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MisSiniestrosUiState(
    val cargando: Boolean = true,
    val nombreUsuario: String = "",
    val siniestros: List<SiniestroEntity> = emptyList(),
    val error: String? = null
)

class MisSiniestrosViewModel(
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
        MisSiniestrosUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    fun cargarSiniestros(
        @Suppress("UNUSED_PARAMETER") rut: String
    ) {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            _uiState.value = MisSiniestrosUiState(
                cargando = true
            )

            val usuario = SesionUsuario.obtenerSesion()

            if (usuario == null) {
                _uiState.value = MisSiniestrosUiState(
                    cargando = false,
                    error = "Debes iniciar sesión nuevamente"
                )
                return@launch
            }

            try {
                repository.getSiniestrosPorUsuario(
                    usuario.idUsuario
                ).collectLatest { lista ->

                    _uiState.value = MisSiniestrosUiState(
                        cargando = false,
                        nombreUsuario = usuario.nombre,
                        siniestros = lista
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = MisSiniestrosUiState(
                    cargando = false,
                    error = "No se pudieron cargar los siniestros"
                )
            }
        }
    }
}
