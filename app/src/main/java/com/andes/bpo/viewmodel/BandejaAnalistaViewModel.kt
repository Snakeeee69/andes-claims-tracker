
package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.data.local.entity.UsuarioEntity
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BandejaAnalistaUiState(
    val cargando: Boolean = true,
    val casos: List<SiniestroEntity> = emptyList(),
    val liquidadores: List<UsuarioEntity> = emptyList(),
    val idCasoSeleccionado: Int? = null,
    val equipoSeleccionado: String = "",
    val cargandoLiquidadores: Boolean = false,
    val asignando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
    val errorAsignacion: String? = null
)

class BandejaAnalistaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(
        application,
        viewModelScope
    )

    private val siniestroDao = db.siniestroDao()
    private val usuarioDao = db.usuarioDao()

    private val _uiState = MutableStateFlow(
        BandejaAnalistaUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null
    private var liquidadoresJob: Job? = null

    fun cargarCasos() {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {

            val usuario = SesionUsuario.obtenerSesion()

            if (usuario?.rol != "ANALISTA") {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No tienes permiso para ver estos casos"
                    )
                }
                return@launch
            }

            try {
                siniestroDao.getSiniestrosSinAsignar()
                    .collectLatest { lista ->
                        _uiState.update {
                            it.copy(
                                cargando = false,
                                casos = lista,
                                error = null
                            )
                        }
                    }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudieron cargar los casos"
                    )
                }
            }
        }
    }

    fun prepararAsignacion(siniestro: SiniestroEntity) {

        if (SesionUsuario.obtenerSesion()?.rol != "ANALISTA") {
            _uiState.update {
                it.copy(mensaje = "Acceso no autorizado")
            }
            return
        }

        val tipo = siniestro.tipoSiniestroNombre.trim()

        val equipo = when {
            tipo.startsWith("Vehículo", true) ||
                    tipo.startsWith("Vehiculo", true) ||
                    tipo.equals("Colisión", true) ||
                    tipo.equals("Choque", true) -> "VEHICULOS"

            tipo.startsWith("Hogar", true) -> "HOGAR"

            else -> null
        }

        if (equipo == null) {
            _uiState.update {
                it.copy(
                    mensaje = "No se reconoce el equipo del siniestro. " +
                            "Revisa el tipo registrado."
                )
            }
            return
        }

        liquidadoresJob?.cancel()

        _uiState.update {
            it.copy(
                idCasoSeleccionado = siniestro.idSiniestro,
                equipoSeleccionado = equipo,
                liquidadores = emptyList(),
                cargandoLiquidadores = true,
                errorAsignacion = null
            )
        }

        liquidadoresJob = viewModelScope.launch {
            try {
                val lista = usuarioDao
                    .obtenerLiquidadoresPorEquipo(equipo)
                    .first()

                _uiState.update {
                    if (it.idCasoSeleccionado ==
                        siniestro.idSiniestro
                    ) {
                        it.copy(
                            liquidadores = lista,
                            cargandoLiquidadores = false
                        )
                    } else {
                        it
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargandoLiquidadores = false,
                        errorAsignacion =
                            "No se pudieron cargar los liquidadores"
                    )
                }
            }
        }
    }

    fun asignarLiquidador(idLiquidador: Int) {

        val usuario = SesionUsuario.obtenerSesion()
        val idCaso = _uiState.value.idCasoSeleccionado

        if (usuario?.rol != "ANALISTA" ||
            idCaso == null ||
            _uiState.value.asignando
        ) {
            return
        }

        _uiState.update {
            it.copy(
                asignando = true,
                errorAsignacion = null
            )
        }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date())

                withContext(Dispatchers.IO) {
                    siniestroDao.asignarLiquidadorConHistorial(
                        idSiniestro = idCaso,
                        idLiquidador = idLiquidador,
                        idAnalista = usuario.idUsuario,
                        fechaHora = fecha
                    )
                }

                _uiState.update {
                    it.copy(
                        asignando = false,
                        idCasoSeleccionado = null,
                        liquidadores = emptyList(),
                        mensaje = "Siniestro asignado correctamente"
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        asignando = false,
                        errorAsignacion =
                            if (e is IllegalStateException) {
                                e.message
                                    ?: "No se pudo asignar el siniestro"
                            } else {
                                "No se pudo asignar el siniestro"
                            }
                    )
                }
            }
        }
    }

    fun cerrarSeleccion() {
        if (_uiState.value.asignando) return

        liquidadoresJob?.cancel()

        _uiState.update {
            it.copy(
                idCasoSeleccionado = null,
                liquidadores = emptyList(),
                errorAsignacion = null
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.update {
            it.copy(mensaje = null)
        }
    }
}
