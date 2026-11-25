package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.AvisoEntity
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
    val especialidad: String = "",
    val casos: List<SiniestroEntity> = emptyList(),
    val avisos: List<AvisoEntity> = emptyList(),
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

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = db.siniestroDao()
    private val usuarioDao = db.usuarioDao()

    private val _uiState = MutableStateFlow(BandejaLiquidadorUiState())
    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null

    private suspend fun resolverIdLiquidador(rut: String, idFallback: Int): Int {
        return withContext(Dispatchers.IO) {
            usuarioDao.buscarPorRut(rut)?.idUsuario ?: idFallback
        }
    }

    fun cargarCasosAsignados() {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            _uiState.value = BandejaLiquidadorUiState()
            val usuario = SesionUsuario.obtenerSesion()

            if (usuario?.rol?.startsWith("LIQUIDADOR") != true) {
                _uiState.update {
                    it.copy(cargando = false, error = "No tienes permiso para consultar estos casos")
                }
                return@launch
            }

            val idLiquidadorLocal = resolverIdLiquidador(usuario.rut, usuario.idUsuario)

            launch {
                dao.getAvisosPorUsuario(idLiquidadorLocal).collectLatest { listaAvisos ->
                    _uiState.update { it.copy(avisos = listaAvisos) }
                }
            }

            try {
                dao.getSiniestrosAsignados(idLiquidadorLocal).collectLatest { lista ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            nombreLiquidador = "${usuario.nombre} ${usuario.apellido}".trim(),
                            especialidad = usuario.equipo ?: "GENERAL",
                            casos = lista,
                            error = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron cargar los casos asignados")
                }
            }
        }
    }

    fun marcarAvisoLeido(idAviso: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.actualizarEstadoAviso(idAviso, "LEIDO")
        }
    }

    fun aceptarCaso(idSiniestro: Int) {
        val usuario = SesionUsuario.obtenerSesion()
        if (usuario?.rol?.startsWith("LIQUIDADOR") != true) return

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val idLiquidadorLocal = resolverIdLiquidador(usuario.rut, usuario.idUsuario)
                withContext(Dispatchers.IO) {
                    dao.aceptarCasoLiquidador(idSiniestro, idLiquidadorLocal, fecha)
                }
                _uiState.update { it.copy(mensaje = "Caso aceptado. Inicia la liquidaciÃ³n (D.S. NÂ° 1.055)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = e.message ?: "No se pudo aceptar el caso") }
            }
        }
    }

    fun rechazarYDerivar(idSiniestro: Int, motivo: String, detalle: String) {
        val usuario = SesionUsuario.obtenerSesion()
        if (usuario?.rol?.startsWith("LIQUIDADOR") != true) return

        val motivoCompleto = "$motivo:${detalle.trim()}"
        if (detalle.trim().length < 10) {
            _uiState.update { it.copy(mensaje = "Explica el motivo (mÃ­n. 10 caracteres)") }
            return
        }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val idLiquidadorLocal = resolverIdLiquidador(usuario.rut, usuario.idUsuario)
                withContext(Dispatchers.IO) {
                    val respaldo = usuarioDao.obtenerLiquidadorRespaldo(usuario.equipo ?: "VEHICULOS")
                    dao.rechazarYDerivarALiquidador2(
                        idSiniestro = idSiniestro,
                        idLiquidador1 = idLiquidadorLocal,
                        idLiquidador2 = respaldo?.idUsuario,
                        nombreLiquidador2 = respaldo?.let { "${it.nombre} ${it.apellido}".trim() },
                        motivoRechazo = motivoCompleto,
                        fechaHora = fecha
                    )
                }
                _uiState.update {
                    it.copy(mensaje = "Expediente rechazado y derivado para reasignaciÃ³n")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = e.message ?: "No se pudo derivar el caso") }
            }
        }
    }

    fun registrarGestion(idSiniestro: Int, descripcion: String, visibleAsegurado: Boolean) {
        val usuario = SesionUsuario.obtenerSesion()
        if (usuario?.rol?.startsWith("LIQUIDADOR") != true || _uiState.value.guardandoGestion) return

        val texto = descripcion.trim()
        if (texto.length !in 5..500) {
            _uiState.update { it.copy(errorGestion = "Escribe entre 5 y 500 caracteres") }
            return
        }

        _uiState.update { it.copy(guardandoGestion = true, errorGestion = null) }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val idLiquidadorLocal = resolverIdLiquidador(usuario.rut, usuario.idUsuario)
                withContext(Dispatchers.IO) {
                    dao.registrarGestionLiquidador(idSiniestro, idLiquidadorLocal, texto, fecha, visibleAsegurado)
                }
                _uiState.update {
                    it.copy(guardandoGestion = false, mensaje = "GestiÃ³n registrada correctamente", errorGestion = null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardandoGestion = false, errorGestion = e.message ?: "No se pudo guardar la gestiÃ³n")
                }
            }
        }
    }

    fun avanzarALiquidacion(idSiniestro: Int) {
        val usuario = SesionUsuario.obtenerSesion()
        if (usuario?.rol?.startsWith("LIQUIDADOR") != true || _uiState.value.avanzandoEtapa) return

        _uiState.update { it.copy(avanzandoEtapa = true, mensaje = null) }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val idLiquidadorLocal = resolverIdLiquidador(usuario.rut, usuario.idUsuario)
                withContext(Dispatchers.IO) {
                    dao.avanzarALiquidacion(idSiniestro, idLiquidadorLocal, fecha)
                }
                _uiState.update {
                    it.copy(avanzandoEtapa = false, mensaje = "El siniestro pasÃ³ a En liquidaciÃ³n")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(avanzandoEtapa = false, mensaje = e.message ?: "No se pudo cambiar la etapa")
                }
            }
        }
    }

    fun cerrarSiniestro(idSiniestro: Int, resultado: String, mensajeCierre: String) {
        val usuario = SesionUsuario.obtenerSesion()
        if (usuario?.rol?.startsWith("LIQUIDADOR") != true || _uiState.value.cerrandoSiniestro) return

        _uiState.update { it.copy(cerrandoSiniestro = true, mensaje = null) }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val idLiquidadorLocal = resolverIdLiquidador(usuario.rut, usuario.idUsuario)
                withContext(Dispatchers.IO) {
                    dao.cerrarSiniestroConHistorial(idSiniestro, idLiquidadorLocal, resultado, mensajeCierre, fecha)
                }
                _uiState.update {
                    it.copy(cerrandoSiniestro = false, mensaje = "Informe emitido y siniestro cerrado")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cerrandoSiniestro = false, mensaje = e.message ?: "No se pudo cerrar el siniestro")
                }
            }
        }
    }

    fun solicitarDocumento(idSiniestro: Int, tipoDocumento: String, comentario: String) {
        val usuario = SesionUsuario.obtenerSesion()
        if (usuario?.rol?.startsWith("LIQUIDADOR") != true || _uiState.value.solicitandoDocumento) return

        if (tipoDocumento.isBlank() || comentario.trim().length !in 5..300) {
            _uiState.update {
                it.copy(errorSolicitud = "Selecciona el documento y escribe un comentario de 5 a 300 caracteres")
            }
            return
        }

        _uiState.update {
            it.copy(solicitandoDocumento = true, errorSolicitud = null, solicitudRegistrada = false)
        }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val idLiquidadorLocal = resolverIdLiquidador(usuario.rut, usuario.idUsuario)
                withContext(Dispatchers.IO) {
                    dao.solicitarDocumentoConHistorial(idSiniestro, idLiquidadorLocal, tipoDocumento, comentario, fecha)
                }
                _uiState.update {
                    it.copy(
                        solicitandoDocumento = false,
                        solicitudRegistrada = true,
                        mensaje = "Solicitud enviada al asegurado"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        solicitandoDocumento = false,
                        errorSolicitud = e.message ?: "No se pudo solicitar el documento"
                    )
                }
            }
        }
    }

    fun limpiarEstadoSolicitud() {
        _uiState.update { it.copy(errorSolicitud = null, solicitudRegistrada = false) }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun limpiarErrorGestion() {
        _uiState.update { it.copy(errorGestion = null) }
    }
}
