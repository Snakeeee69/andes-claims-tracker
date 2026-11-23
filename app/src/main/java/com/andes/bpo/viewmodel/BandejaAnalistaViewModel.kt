package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.AreaEntity
import com.andes.bpo.data.local.entity.AvisoEntity
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.data.local.entity.UsuarioEntity
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BandejaAnalistaUiState(
    val cargando: Boolean = true,
    val nombreUsuario: String = "",
    val todosLosCasos: List<SiniestroEntity> = emptyList(),
    val todosLosUsuarios: List<UsuarioEntity> = emptyList(),
    val areasCatalogo: List<AreaEntity> = emptyList(),
    val avisosAnalista: List<AvisoEntity> = emptyList(),
    val documentosCasoSeleccionado: List<DocumentoEntity> = emptyList(),
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

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val siniestroDao = db.siniestroDao()
    private val usuarioDao = db.usuarioDao()
    private val areaDao = db.areaDao()

    private val _uiState = MutableStateFlow(BandejaAnalistaUiState())
    val uiState = _uiState.asStateFlow()

    private var cargaJob: Job? = null
    private var liquidadoresJob: Job? = null

    fun cargarCasos() {
        cargaJob?.cancel()

        cargaJob = viewModelScope.launch {
            val usuario = SesionUsuario.obtenerSesion()
            if (usuario?.rol != "ANALISTA" && usuario?.rol != "ADMINISTRADOR") {
                _uiState.update {
                    it.copy(cargando = false, error = "No tienes permiso para acceder a esta vista")
                }
                return@launch
            }

            _uiState.update {
                it.copy(nombreUsuario = "${usuario.nombre} ${usuario.apellido}".trim())
            }

            launch {
                usuarioDao.obtenerTodosLosUsuarios().collectLatest { listaUsers ->
                    _uiState.update { it.copy(todosLosUsuarios = listaUsers) }
                }
            }
            launch {
                areaDao.obtenerAreas().collectLatest { listaAreas ->
                    _uiState.update { it.copy(areasCatalogo = listaAreas) }
                }
            }
            launch {
                siniestroDao.getAvisosPorUsuario(usuario.idUsuario).collectLatest { avisos ->
                    _uiState.update { it.copy(avisosAnalista = avisos) }
                }
            }

            try {
                siniestroDao.getTodosLosSiniestros().collectLatest { lista ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            todosLosCasos = lista,
                            error = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudieron cargar los casos")
                }
            }
        }
    }

    fun prepararAsignacion(siniestro: SiniestroEntity) {
        val tipo = siniestro.tipoSiniestroNombre.trim()
        val equipo = when {
            siniestro.idArea == 1 ||
                    tipo.startsWith("Vehículo", true) ||
                    tipo.startsWith("Vehiculo", true) ||
                    tipo.equals("Colisión", true) ||
                    tipo.equals("Choque", true) -> "VEHICULOS"

            siniestro.idArea == 2 ||
                    tipo.startsWith("Hogar", true) ||
                    tipo.contains("Incendio", true) -> "HOGAR"

            siniestro.idArea == 3 ||
                    tipo.startsWith("Salud", true) ||
                    tipo.startsWith("Vida", true) -> "SALUD"

            else -> "VEHICULOS"
        }

        liquidadoresJob?.cancel()

        _uiState.update {
            it.copy(
                idCasoSeleccionado = siniestro.idSiniestro,
                equipoSeleccionado = equipo,
                liquidadores = emptyList(),
                documentosCasoSeleccionado = emptyList(),
                cargandoLiquidadores = true,
                errorAsignacion = null
            )
        }

        liquidadoresJob = viewModelScope.launch {
            try {
                val docs = siniestroDao.getDocumentosPorSiniestro(siniestro.idSiniestro).first()
                val lista = usuarioDao.obtenerLiquidadoresPorEquipo(equipo).first()

                _uiState.update {
                    if (it.idCasoSeleccionado == siniestro.idSiniestro) {
                        it.copy(
                            documentosCasoSeleccionado = docs,
                            liquidadores = lista,
                            cargandoLiquidadores = false
                        )
                    } else it
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargandoLiquidadores = false, errorAsignacion = "No se pudieron cargar los liquidadores")
                }
            }
        }
    }

    fun asignarLiquidador(idLiquidador: Int) {
        val usuario = SesionUsuario.obtenerSesion()
        val idCaso = _uiState.value.idCasoSeleccionado

        if ((usuario?.rol != "ANALISTA" && usuario?.rol != "ADMINISTRADOR") ||
            idCaso == null ||
            _uiState.value.asignando
        ) return

        _uiState.update { it.copy(asignando = true, errorAsignacion = null) }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

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
                        mensaje = "Liquidador asignado y notificado correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(asignando = false, errorAsignacion = e.message ?: "No se pudo asignar el siniestro")
                }
            }
        }
    }

    fun aplicarProrroga(idSiniestro: Int, diasProrroga: Int, motivo: String) {
        val usuario = SesionUsuario.obtenerSesion()
        if (usuario?.rol != "ANALISTA" && usuario?.rol != "ADMINISTRADOR") return

        if (diasProrroga !in listOf(90, 180) || motivo.trim().length < 15) {
            _uiState.update {
                it.copy(errorAsignacion = "Selecciona 90 o 180 días y fundamenta en al menos 15 caracteres")
            }
            return
        }

        viewModelScope.launch {
            try {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                withContext(Dispatchers.IO) {
                    siniestroDao.aplicarProrrogaAnalista(
                        idSiniestro = idSiniestro,
                        idAnalista = usuario.idUsuario,
                        diasProrroga = diasProrroga,
                        motivo = motivo.trim(),
                        fechaHora = fecha
                    )
                }
                _uiState.update {
                    it.copy(
                        idCasoSeleccionado = null,
                        mensaje = "Prórroga de $diasProrroga días registrada y notificada"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorAsignacion = e.message ?: "No se pudo aplicar la prórroga") }
            }
        }
    }

    fun marcarAvisoLeido(idAviso: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            siniestroDao.actualizarEstadoAviso(idAviso, "LEIDO")
        }
    }

    fun cambiarRolUsuarioAdmin(idUsuario: Int, nuevoRol: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val equipo = when (nuevoRol) {
                "LIQUIDADOR_VEHICULOS" -> "VEHICULOS"
                "LIQUIDADOR_HOGAR" -> "HOGAR"
                "LIQUIDADOR_SALUD" -> "SALUD"
                else -> null
            }
            val rolReal = if (nuevoRol.startsWith("LIQUIDADOR")) "LIQUIDADOR_EXTERNO" else nuevoRol
            usuarioDao.actualizarRolYEquipoAdmin(idUsuario, rolReal, equipo, equipo)
            _uiState.update { it.copy(mensaje = "Rol y especialidad actualizados") }
        }
    }

    fun eliminarUsuarioAdmin(idUsuario: Int) {
        val adminActual = SesionUsuario.obtenerSesion()
        if (adminActual?.rol != "ADMINISTRADOR") {
            _uiState.update { it.copy(mensaje = "Solo un administrador puede eliminar usuarios") }
            return
        }
        if (adminActual.idUsuario == idUsuario) {
            _uiState.update { it.copy(mensaje = "No puedes eliminar tu propia cuenta activa") }
            return
        }

        val tieneCasosActivos = _uiState.value.todosLosCasos.any {
            (it.idLiquidador == idUsuario || it.idAsegurado == idUsuario) && it.etapaNombre != "Cerrado"
        }
        if (tieneCasosActivos) {
            _uiState.update {
                it.copy(mensaje = "No se puede eliminar: el usuario tiene siniestros activos en curso")
            }
            return
        }

        viewModelScope.launch {
            try {
                val filas = withContext(Dispatchers.IO) {
                    usuarioDao.eliminarUsuarioPorId(idUsuario)
                }
                _uiState.update {
                    it.copy(
                        mensaje = if (filas > 0) "Usuario eliminado correctamente" else "No se encontró el usuario"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(mensaje = e.message ?: "Error al eliminar el usuario")
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
                documentosCasoSeleccionado = emptyList(),
                errorAsignacion = null
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
}