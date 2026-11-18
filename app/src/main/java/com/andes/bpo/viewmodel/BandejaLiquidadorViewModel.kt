
package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BandejaLiquidadorUiState(
    val cargando: Boolean = true,
    val nombreLiquidador: String = "",
    val casos: List<SiniestroEntity> = emptyList(),
    val guardandoGestion: Boolean = false,
    val avanzandoEtapa: Boolean = false,
    val errorGestion: String? = null,
    val mensaje: String? = null,
    val cerrandoSiniestro: Boolean = false,
    val error: String? = null,
    val solicitandoDocumento: Boolean = false,
    val errorSolicitud: String? = null,
    val solicitudRegistrada: Boolean = false
)

class BandejaLiquidadorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(
        application,
        viewModelScope
    ).siniestroDao()

    private val _uiState = MutableStateFlow(
        BandejaLiquidadorUiState()
    )

    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    fun cargarCasosAsignados() {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            _uiState.value = BandejaLiquidadorUiState()

            val usuario = SesionUsuario.obtenerSesion()

            if (usuario?.rol != "LIQUIDADOR") {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No tienes permiso para consultar estos casos"
                    )
                }
                return@launch
            }

            try {
                dao.getSiniestrosAsignados(
                    usuario.idUsuario
                ).collectLatest { lista ->

                    _uiState.update {
                        it.copy(
                            cargando = false,
                            nombreLiquidador = usuario.nombre,
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
                        error = "No se pudieron cargar los casos asignados"
                    )
                }
            }
        }
    }

    fun registrarGestion(
        idSiniestro: Int,
        descripcion: String,
        visibleAsegurado: Boolean
    ) {
        val usuario = SesionUsuario.obtenerSesion()

        if (usuario?.rol != "LIQUIDADOR") {
            _uiState.update {
                it.copy(
                    errorGestion = "No tienes permisos para registrar gestiones"
                )
            }
            return
        }

        if (_uiState.value.guardandoGestion) return

        val texto = descripcion.trim()

        if (texto.length !in 5..500) {
            _uiState.update {
                it.copy(
                    errorGestion =
                        "Escribe entre 5 y 500 caracteres"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                guardandoGestion = true,
                errorGestion = null
            )
        }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())

                withContext(Dispatchers.IO) {
                    dao.registrarGestionLiquidador(
                        idSiniestro = idSiniestro,
                        idLiquidador = usuario.idUsuario,
                        descripcion = texto,
                        fechaHora = fecha,
                        visibleAsegurado = visibleAsegurado
                    )
                }

                _uiState.update {
                    it.copy(
                        guardandoGestion = false,
                        mensaje = "Gestión registrada correctamente",
                        errorGestion = null
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardandoGestion = false,
                        errorGestion = when (e) {
                            is IllegalArgumentException,
                            is IllegalStateException ->
                                e.message ?: "No se pudo guardar la gestión"
                            else -> "No se pudo guardar la gestión"
                        }
                    )
                }
            }
        }
    }

    fun avanzarALiquidacion(idSiniestro: Int) {

        val usuario = SesionUsuario.obtenerSesion()

        if (usuario == null || usuario.rol != "LIQUIDADOR") {
            _uiState.update {
                it.copy(
                    mensaje = "No tienes permiso para cambiar etapas"
                )
            }
            return
        }

        if (_uiState.value.avanzandoEtapa) return

        _uiState.update {
            it.copy(
                avanzandoEtapa = true,
                mensaje = null
            )
        }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())

                withContext(Dispatchers.IO) {
                    dao.avanzarALiquidacion(
                        idSiniestro = idSiniestro,
                        idLiquidador = usuario.idUsuario,
                        fechaHora = fecha
                    )
                }

                _uiState.update {
                    it.copy(
                        avanzandoEtapa = false,
                        mensaje =
                            "El siniestro pasó a En liquidación"
                    )
                }

            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(avanzandoEtapa = false)
                }
                throw e

            } catch (e: Exception) {
                val mensajeError = when (e) {
                    is IllegalStateException,
                    is IllegalArgumentException ->
                        e.message ?: "No se pudo cambiar la etapa"

                    else -> "No se pudo cambiar la etapa"
                }

                _uiState.update {
                    it.copy(
                        avanzandoEtapa = false,
                        mensaje = mensajeError
                    )
                }
            }
        }
    }


    fun cerrarSiniestro(
        idSiniestro: Int,
        resultado: String,
        mensajeCierre: String
    ) {
        val usuario = SesionUsuario.obtenerSesion()

        if (usuario?.rol != "LIQUIDADOR") {
            _uiState.update {
                it.copy(mensaje = "No tienes permiso para cerrar casos")
            }
            return
        }

        if (_uiState.value.cerrandoSiniestro) return

        _uiState.update {
            it.copy(cerrandoSiniestro = true, mensaje = null)
        }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())

                withContext(Dispatchers.IO) {
                    dao.cerrarSiniestroConHistorial(
                        idSiniestro = idSiniestro,
                        idLiquidador = usuario.idUsuario,
                        resultado = resultado,
                        mensaje = mensajeCierre,
                        fechaHora = fecha
                    )
                }

                _uiState.update {
                    it.copy(
                        cerrandoSiniestro = false,
                        mensaje = "Siniestro cerrado correctamente"
                    )
                }

            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(cerrandoSiniestro = false)
                }
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cerrandoSiniestro = false,
                        mensaje = e.message ?: "No se pudo cerrar el siniestro"
                    )
                }
            }
        }
    }

    fun solicitarDocumento(
        idSiniestro: Int,
        tipoDocumento: String,
        comentario: String
    ) {
        val usuario = SesionUsuario.obtenerSesion()

        if (usuario?.rol != "LIQUIDADOR") {
            _uiState.update {
                it.copy(
                    errorSolicitud =
                        "No tienes permisos para solicitar documentos"
                )
            }
            return
        }

        if (_uiState.value.solicitandoDocumento) return

        if (tipoDocumento.isBlank() ||
            comentario.trim().length !in 5..300
        ) {
            _uiState.update {
                it.copy(
                    errorSolicitud =
                        "Selecciona el documento y escribe un comentario de 5 a 300 caracteres"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                solicitandoDocumento = true,
                errorSolicitud = null,
                solicitudRegistrada = false
            )
        }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())

                withContext(Dispatchers.IO) {
                    dao.solicitarDocumentoConHistorial(
                        idSiniestro = idSiniestro,
                        idLiquidador = usuario.idUsuario,
                        tipoDocumento = tipoDocumento,
                        comentario = comentario,
                        fechaHora = fecha
                    )
                }

                _uiState.update {
                    it.copy(
                        solicitandoDocumento = false,
                        solicitudRegistrada = true,
                        mensaje = "Documento solicitado correctamente"
                    )
                }

            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(solicitandoDocumento = false)
                }
                throw e

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        solicitandoDocumento = false,
                        errorSolicitud = when (e) {
                            is IllegalStateException,
                            is IllegalArgumentException ->
                                e.message ?: "No se pudo solicitar el documento"
                            else ->
                                "No se pudo solicitar el documento"
                        }
                    )
                }
            }
        }
    }

    fun limpiarEstadoSolicitud() {
        _uiState.update {
            it.copy(
                errorSolicitud = null,
                solicitudRegistrada = false
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.update {
            it.copy(mensaje = null)
        }
    }

    fun limpiarErrorGestion() {
        _uiState.update {
            it.copy(errorGestion = null)
        }
    }
}
