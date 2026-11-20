package com.andes.bpo.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.*
import com.andes.bpo.data.repository.SiniestroRepository
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.*

data class AdjuntoBorradorItem(
    val idTemp: String = UUID.randomUUID().toString(),
    val tipoDocumento: String,
    val nombreArchivo: String,
    val rutaLocal: String,
    val esImagen: Boolean
)

class NuevoSiniestroViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = SiniestroRepository(db.siniestroDao(), db.usuarioDao(), db.areaDao())
    private val areaDao = db.areaDao()

    private val _areas = MutableStateFlow<List<AreaEntity>>(emptyList())
    val areas: StateFlow<List<AreaEntity>> = _areas.asStateFlow()

    private val _requisitos = MutableStateFlow<List<RequisitoAreaEntity>>(emptyList())
    val requisitos: StateFlow<List<RequisitoAreaEntity>> = _requisitos.asStateFlow()

    private var requisitosJob: Job? = null

    init {
        viewModelScope.launch {
            areaDao.asegurarAreasIniciales()
            areaDao.obtenerAreas().collect { lista ->
                _areas.value = lista
            }
        }
    }

    fun cargarRequisitosDeArea(idArea: Int) {
        requisitosJob?.cancel()
        requisitosJob = viewModelScope.launch {
            areaDao.obtenerRequisitosPorArea(idArea).collectLatest { lista ->
                _requisitos.value = lista
            }
        }
    }

    fun esFechaOcurrenciaValida(fecha: String): Boolean = validarFechaOcurrencia(fecha)

    companion object {
        fun validarFechaOcurrencia(
            fecha: String,
            hoy: LocalDate = LocalDate.now()
        ): Boolean {
            val valor = fecha.trim()
            if (!Regex("""\d{2}/\d{2}/\d{4}""").matches(valor)) return false
            return try {
                val formato = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT)
                val fechaIngresada = LocalDate.parse(valor, formato)
                !fechaIngresada.isAfter(hoy)
            } catch (e: DateTimeParseException) {
                false
            }
        }
    }

    fun copiarArchivoLocal(uri: Uri, tipoDocumento: String): Result<AdjuntoBorradorItem> {
        return runCatching {
            val app = getApplication<Application>()
            val resolver = app.contentResolver
            val mime = resolver.getType(uri) ?: error("No se reconoce el formato del archivo")
            val esImg = mime.lowercase(Locale.ROOT).startsWith("image/")
            val ext = when (mime.lowercase(Locale.ROOT)) {
                "application/pdf" -> "pdf"
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> if (esImg) "jpg" else error("Solo se permiten imágenes o PDF")
            }

            var nombreOriginal: String? = null
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) nombreOriginal = c.getString(0)
            }

            val carpeta = File(app.filesDir, "evidencias")
            if (!carpeta.exists()) carpeta.mkdirs()
            val destino = File(carpeta, "${UUID.randomUUID()}.$ext")

            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destino).use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val leidos = input.read(buffer)
                        if (leidos == -1) break
                        total += leidos
                        check(total <= 5L * 1024L * 1024L) { "El archivo supera los 5 MB" }
                        output.write(buffer, 0, leidos)
                    }
                    check(total > 0) { "El archivo está vacío" }
                }
            } ?: error("No se pudo leer el archivo")

            AdjuntoBorradorItem(
                tipoDocumento = tipoDocumento,
                nombreArchivo = nombreOriginal ?: destino.name,
                rutaLocal = destino.absolutePath,
                esImagen = esImg
            )
        }
    }

    fun guardarSiniestroCompleto(
        siniestro: SiniestroEntity,
        adjuntos: List<AdjuntoBorradorItem>,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val usuario = SesionUsuario.obtenerSesion()
                if (usuario == null) {
                    onError("No hay una sesión activa. Inicia sesión nuevamente.")
                    return@launch
                }

                val fechaLimiteLegal = LocalDate.now().plusDays(45)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                val fechaAhora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                val siniestroConUsuario = siniestro.copy(
                    idAsegurado = usuario.idUsuario,
                    fechaLimiteLegal = fechaLimiteLegal
                )

                withContext(Dispatchers.IO) {
                    val idGenerado = repository.registrarNuevoSiniestro(siniestroConUsuario).toInt()

                    adjuntos.forEach { adj ->
                        db.siniestroDao().insertDocumento(
                            DocumentoEntity(
                                idSiniestro = idGenerado,
                                nombreTipoDocumento = adj.tipoDocumento,
                                comentario = "Adjuntado al reportar siniestro",
                                fechaSolicitud = fechaAhora,
                                estado = "Entregado",
                                nombreArchivo = adj.nombreArchivo,
                                rutaUri = adj.rutaLocal,
                                fechaRecepcion = fechaAhora,
                                esImagen = adj.esImagen
                            )
                        )
                    }

                    db.siniestroDao().insertAviso(
                        AvisoEntity(
                            idSiniestro = idGenerado,
                            idUsuario = usuario.idUsuario,
                            mensaje = "Siniestro ${siniestro.numero} ingresado. Plazo legal de respuesta: $fechaLimiteLegal (45 días, D.S. N° 1.055).",
                            fechaHora = fechaAhora,
                            leido = false,
                            requiereAccion = false,
                            estadoAviso = "PENDIENTE"
                        )
                    )
                }

                onExito()
            } catch (e: Exception) {
                onError(e.message ?: "No se pudo guardar el siniestro. Inténtalo nuevamente.")
            }
        }
    }
}