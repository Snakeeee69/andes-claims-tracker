package com.andes.bpo.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class EvidenciaRoomUiState(
    val cargando: Boolean = true,
    val numeroSiniestro: String = "",
    val cerrado: Boolean = false,
    val evidencias: List<DocumentoEntity> = emptyList(),
    val solicitudes: List<DocumentoEntity> = emptyList(),
    val guardando: Boolean = false,
    val procesandoDocumentoId: Int? = null,
    val error: String? = null,
    val mensaje: String? = null
)

class EvidenciaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application, viewModelScope).siniestroDao()
    private val _uiState = MutableStateFlow(EvidenciaRoomUiState())
    val uiState = _uiState.asStateFlow()
    private var cargaJob: Job? = null

    fun cargarEvidencias(idSiniestro: Int) {
        cargaJob?.cancel()
        cargaJob = viewModelScope.launch {
            _uiState.value = EvidenciaRoomUiState()
            try {
                val usuario = SesionUsuario.obtenerSesion()
                    ?: error("Debes iniciar sesión")
                check(usuario.rol.startsWith("ASEGURADO")) {
                    "Esta pantalla es para el asegurado"
                }
                val siniestro = dao.getSiniestroPorId(idSiniestro).first()
                    ?: error("Siniestro no encontrado")
                check(siniestro.idAsegurado == usuario.idUsuario) {
                    "No tienes acceso a este siniestro"
                }
                combine(
                    dao.getEvidenciasPorSiniestro(idSiniestro),
                    dao.getSolicitudesPorSiniestro(idSiniestro),
                    dao.getSiniestroPorId(idSiniestro)
                ) { evidencias, solicitudes, casoActual ->
                    Triple(evidencias, solicitudes, casoActual)
                }.collectLatest { (evidencias, solicitudes, casoActual) ->
                    check(casoActual != null && casoActual.idAsegurado == usuario.idUsuario) {
                        "No tienes acceso a este siniestro"
                    }
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            numeroSiniestro = casoActual.numero,
                            cerrado = casoActual.etapaNombre == "Cerrado",
                            evidencias = evidencias,
                            solicitudes = solicitudes,
                            error = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.message ?: "No se pudo cargar la evidencia")
                }
            }
        }
    }

    fun guardarEvidencia(idSiniestro: Int, uri: Uri, tipoDocumento: String) {
        if (_uiState.value.guardando || _uiState.value.procesandoDocumentoId != null) return
        _uiState.update { it.copy(guardando = true, error = null, mensaje = null) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    validarPropietario(idSiniestro, requiereAbierto = true)
                    check(dao.contarEvidencias(idSiniestro) < 15) {
                        "Solo se permiten 15 adjuntos por siniestro"
                    }
                    require(tipoDocumento.isNotBlank()) { "Selecciona el tipo de documento" }
                    val nombreVisible = obtenerNombreOriginal(uri)
                    val archivo = copiarArchivo(uri)
                    val esArchivoImagen = archivo.extension.lowercase(Locale.ROOT) != "pdf"
                    try {
                        dao.insertDocumento(
                            DocumentoEntity(
                                idSiniestro = idSiniestro,
                                nombreTipoDocumento = tipoDocumento,
                                comentario = null,
                                fechaSolicitud = "",
                                estado = "Borrador",
                                nombreArchivo = nombreVisible ?: archivo.name,
                                rutaUri = archivo.absolutePath,
                                fechaRecepcion = null,
                                esImagen = esArchivoImagen
                            )
                        )
                    } catch (e: Exception) {
                        archivo.delete()
                        throw e
                    }
                }
                _uiState.update { it.copy(guardando = false, mensaje = "Evidencia guardada como borrador") }
            } catch (e: CancellationException) {
                _uiState.update { it.copy(guardando = false) }
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardando = false, error = e.message ?: "No se pudo guardar el archivo")
                }
            }
        }
    }

    fun enviarBorrador(idSiniestro: Int, idDocumento: Int) {
        if (_uiState.value.procesandoDocumentoId != null || _uiState.value.guardando) return
        _uiState.update { it.copy(procesandoDocumentoId = idDocumento, error = null, mensaje = null) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val usuario = validarPropietario(idSiniestro, requiereAbierto = true)
                    dao.enviarEvidenciaConHistorial(
                        idDocumento = idDocumento,
                        idSiniestro = idSiniestro,
                        idAsegurado = usuario.idUsuario,
                        fechaHora = fechaAhora()
                    )
                }
                _uiState.update {
                    it.copy(procesandoDocumentoId = null, mensaje = "Evidencia enviada y registrada en el historial")
                }
            } catch (e: CancellationException) {
                _uiState.update { it.copy(procesandoDocumentoId = null) }
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(procesandoDocumentoId = null, error = e.message ?: "No se pudo enviar la evidencia")
                }
            }
        }
    }

    fun eliminarBorrador(idSiniestro: Int, idDocumento: Int) {
        if (_uiState.value.procesandoDocumentoId != null || _uiState.value.guardando) return
        _uiState.update { it.copy(procesandoDocumentoId = idDocumento, error = null, mensaje = null) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    validarPropietario(idSiniestro, requiereAbierto = true)
                    val documento = dao.getDocumentoPorId(idDocumento)
                        ?: error("El documento ya no existe")
                    check(documento.idSiniestro == idSiniestro && documento.estado == "Borrador") {
                        "Solo se pueden eliminar borradores de este siniestro"
                    }
                    val filas = dao.eliminarDocumentoBorrador(idDocumento, idSiniestro)
                    check(filas == 1) { "No se pudo eliminar el borrador" }
                    documento.rutaUri?.let { ruta ->
                        val directorio = File(getApplication<Application>().filesDir, "evidencias").canonicalFile
                        val archivo = File(ruta).canonicalFile
                        if (archivo.parentFile == directorio) archivo.delete()
                    }
                }
                _uiState.update { it.copy(procesandoDocumentoId = null, mensaje = "Borrador eliminado") }
            } catch (e: CancellationException) {
                _uiState.update { it.copy(procesandoDocumentoId = null) }
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(procesandoDocumentoId = null, error = e.message ?: "No se pudo eliminar el borrador")
                }
            }
        }
    }

    private suspend fun validarPropietario(idSiniestro: Int, requiereAbierto: Boolean): com.andes.bpo.utils.SesionActiva {
        val usuario = SesionUsuario.obtenerSesion() ?: error("Debes iniciar sesión")
        check(usuario.rol.startsWith("ASEGURADO")) { "No tienes permiso para esta acción" }
        val siniestro = dao.getSiniestroPorId(idSiniestro).first()
            ?: error("Siniestro no encontrado")
        check(siniestro.idAsegurado == usuario.idUsuario) { "El siniestro no te pertenece" }
        if (requiereAbierto) check(siniestro.etapaNombre != "Cerrado") {
            "No puedes modificar documentos de un caso cerrado"
        }
        return usuario
    }

    private fun fechaAhora(): String = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
    ).format(Date())

    private fun obtenerNombreOriginal(uri: Uri): String? {
        val resolver = getApplication<Application>().contentResolver
        return try {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        } catch (_: Exception) { null }
    }

    private fun copiarArchivo(uri: Uri): File {
        val app = getApplication<Application>()
        val resolver = app.contentResolver
        val mime = resolver.getType(uri) ?: error("No se reconoce el formato del archivo")
        val extension = when (mime.lowercase(Locale.ROOT)) {
            "application/pdf" -> "pdf"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic", "image/heif" -> "heic"
            else -> if (mime.startsWith("image/")) "img" else error("Solo se permiten imágenes y PDF")
        }
        val carpeta = File(app.filesDir, "evidencias")
        check(carpeta.exists() || carpeta.mkdirs()) { "No se pudo preparar el almacenamiento" }
        val archivo = File(carpeta, "${UUID.randomUUID()}.$extension")
        try {
            val entrada = resolver.openInputStream(uri) ?: error("No se pudo leer el archivo")
            entrada.use { input ->
                FileOutputStream(archivo).use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val leidos = input.read(buffer)
                        if (leidos == -1) break
                        total += leidos
                        check(total <= 5L * 1024L * 1024L) {
                            "El archivo supera el límite de 5 MB"
                        }
                        output.write(buffer, 0, leidos)
                    }
                    check(total > 0) { "El archivo está vacío" }
                }
            }
            return archivo
        } catch (e: Exception) {
            archivo.delete()
            throw e
        }
    }

    fun limpiarMensaje() { _uiState.update { it.copy(mensaje = null) } }
    fun limpiarError() { _uiState.update { it.copy(error = null) } }
}