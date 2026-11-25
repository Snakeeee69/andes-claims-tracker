package com.andes.bpo.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
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
    val uriPersistente: String? = null,
    val esImagen: Boolean,
    val esPoliza: Boolean = false
)

data class NuevoSiniestroUiState(
    val idArea: Int? = null,
    val polizaSeleccionada: String = "",
    val numeroPoliza: String = "",
    val compania: String = "",
    val tipoSiniestro: String = "",
    val patente: String = "",
    val modeloVehiculo: String = "",
    val detalleBien: String = "",
    val fechaOcurrencia: String = "",
    val region: String = "",
    val comuna: String = "",
    val descripcion: String = "",
    val adjuntos: List<AdjuntoBorradorItem> = emptyList(),
    val isSaving: Boolean = false,
    val errArea: String? = null,
    val errPoliza: String? = null,
    val errNumPoliza: String? = null,
    val errCompania: String? = null,
    val errTipo: String? = null,
    val errFecha: String? = null,
    val errRegion: String? = null,
    val errComuna: String? = null,
    val errDescripcion: String? = null,
    val errPolizaDoc: String? = null,
    val errDocs: String? = null
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

    private val _uiState = MutableStateFlow(NuevoSiniestroUiState())
    val uiState: StateFlow<NuevoSiniestroUiState> = _uiState.asStateFlow()

    private var requisitosJob: Job? = null

    val companiasDisponibles = listOf(
        "BCI Seguros",
        "HDI Seguros",
        "Mapfre Chile",
        "Liberty Seguros",
        "Sura Seguros",
        "Chilena Consolidada / Zurich",
        "Consorcio Seguros",
        "MetLife Chile"
    )

    val comunasPorRegion: Map<String, List<String>> = linkedMapOf(
        "Arica y Parinacota" to listOf("Arica", "Camarones", "Putre", "General Lagos"),
        "TarapacÃ¡" to listOf("Iquique", "Alto Hospicio", "Pozo Almonte", "CamiÃ±a", "Colchane", "Huara", "Pica"),
        "Antofagasta" to listOf("Antofagasta", "Mejillones", "Sierra Gorda", "Taltal", "Calama", "OllagÃ¼e", "San Pedro de Atacama", "Tocopilla", "MarÃ­a Elena"),
        "Atacama" to listOf("CopiapÃ³", "Caldera", "Tierra Amarilla", "ChaÃ±aral", "Diego de Almagro", "Vallenar", "Alto del Carmen", "Freirina", "Huasco"),
        "Coquimbo" to listOf("La Serena", "Coquimbo", "Andacollo", "La Higuera", "Paiguano", "VicuÃ±a", "Illapel", "Canela", "Los Vilos", "Salamanca", "Ovalle", "CombarbalÃ¡", "Monte Patria", "Punitaqui", "RÃ­o Hurtado"),
        "ValparaÃ­so" to listOf("ValparaÃ­so", "ViÃ±a del Mar", "ConcÃ³n", "QuilpuÃ©", "Villa Alemana", "Casablanca", "Quillota", "La Calera", "La Cruz", "Limache", "OlmuÃ©", "San Antonio", "Cartagena", "El Quisco", "El Tabo", "Algarrobo", "San Felipe", "Los Andes", "La Ligua", "Petorca", "Zapallar"),
        "Metropolitana de Santiago" to listOf("Santiago", "Cerrillos", "Cerro Navia", "ConchalÃ­", "El Bosque", "EstaciÃ³n Central", "Huechuraba", "Independencia", "La Cisterna", "La Florida", "La Granja", "La Pintana", "La Reina", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado", "Macul", "MaipÃº", "Ã‘uÃ±oa", "Pedro Aguirre Cerda", "PeÃ±alolÃ©n", "Providencia", "Pudahuel", "Quilicura", "Quinta Normal", "Recoleta", "Renca", "San JoaquÃ­n", "San Miguel", "San RamÃ³n", "Vitacura", "Puente Alto", "Pirque", "San JosÃ© de Maipo", "Colina", "Lampa", "Tiltil", "San Bernardo", "Buin", "Paine", "Calera de Tango", "Melipilla", "Talagante", "PeÃ±aflor", "Padre Hurtado", "El Monte", "Isla de Maipo"),
        "Libertador General Bernardo O'Higgins" to listOf("Rancagua", "MachalÃ­", "Graneros", "Mostazal", "Codegua", "RequÃ­noa", "Rengo", "San Vicente", "Pichidegua", "Peumo", "San Fernando", "Chimbarongo", "Santa Cruz", "Pichilemu"),
        "Maule" to listOf("Talca", "ConstituciÃ³n", "Curepto", "Empedrado", "Maule", "Pelarco", "Pencahue", "RÃ­o Claro", "San Clemente", "San Rafael", "CuricÃ³", "Molina", "Teno", "Romeral", "Linares", "San Javier", "Parral", "Cauquenes"),
        "Ã‘uble" to listOf("ChillÃ¡n", "ChillÃ¡n Viejo", "Bulnes", "QuillÃ³n", "San Carlos", "Coihueco", "Yungay", "El Carmen", "Pinto", "Coelemu", "Quirihue"),
        "BiobÃ­o" to listOf("ConcepciÃ³n", "Talcahuano", "HualpÃ©n", "San Pedro de la Paz", "Chiguayante", "Coronel", "Lota", "Penco", "TomÃ©", "Hualqui", "Santa Juana", "Los Ãngeles", "Cabrero", "MulchÃ©n", "Nacimiento", "Laja", "CaÃ±ete", "Lebu", "Arauco"),
        "La AraucanÃ­a" to listOf("Temuco", "Padre Las Casas", "VilcÃºn", "Lautaro", "Perquenco", "Nueva Imperial", "Carahue", "PitrufquÃ©n", "Freire", "Gorbea", "Loncoche", "Villarrica", "PucÃ³n", "Angol", "Collipulli", "Victoria", "TraiguÃ©n", "CuracautÃ­n"),
        "Los RÃ­os" to listOf("Valdivia", "Corral", "Lanco", "Los Lagos", "MÃ¡fil", "Mariquina", "Paillaco", "Panguipulli", "La UniÃ³n", "Futrono", "Lago Ranco", "RÃ­o Bueno"),
        "Los Lagos" to listOf("Puerto Montt", "Puerto Varas", "Llanquihue", "Frutillar", "Fresia", "Los Muermos", "Calbuco", "MaullÃ­n", "Osorno", "Purranque", "Puyehue", "RÃ­o Negro", "Castro", "Ancud", "QuellÃ³n", "Dalcahue", "Chonchi"),
        "AysÃ©n del General Carlos IbÃ¡Ã±ez del Campo" to listOf("Coyhaique", "Lago Verde", "AysÃ©n", "Cisnes", "Guaitecas", "Cochrane", "O'Higgins", "Tortel", "Chile Chico", "RÃ­o IbÃ¡Ã±ez"),
        "Magallanes y de la AntÃ¡rtica Chilena" to listOf("Punta Arenas", "Laguna Blanca", "RÃ­o Verde", "San Gregorio", "Porvenir", "Primavera", "Timaukel", "Natales", "Torres del Paine")
    )

    val regionesDisponibles: List<String> = comunasPorRegion.keys.toList()

    init {
        viewModelScope.launch {
            areaDao.asegurarAreasIniciales()
            areaDao.obtenerAreas().collect { lista ->
                _areas.value = lista
            }
        }
    }

    fun obtenerComunasPorRegion(region: String): List<String> =
        comunasPorRegion[region].orEmpty()

    fun obtenerTiposProblemaPorArea(idArea: Int?): List<String> = when (idArea) {
        1 -> listOf(
            "ColisiÃ³n o Choque",
            "Robo Total o Parcial de VehÃ­culo",
            "Volcamiento o Desbarrancamiento",
            "DaÃ±os a Terceros (Responsabilidad Civil)",
            "DaÃ±os Materiales / Actos Maliciosos"
        )
        2 -> listOf(
            "Incendio Estructural o Contenido",
            "Sismo / DaÃ±os por Terremoto",
            "Robo con Fuerza en Vivienda",
            "Rotura de CaÃ±erÃ­as / FiltraciÃ³n de Agua",
            "DaÃ±os ElÃ©ctricos / Cristales"
        )
        3 -> listOf(
            "Reembolso AtenciÃ³n Ambulatoria",
            "HospitalizaciÃ³n o IntervenciÃ³n QuirÃºrgica",
            "AtenciÃ³n de Urgencia MÃ©dica",
            "Incapacidad / Accidente Personal",
            "Fallecimiento (Seguro de Vida)"
        )
        else -> emptyList()
    }

    fun obtenerPolizasPorArea(area: AreaEntity?): List<String> {
        val desdeBd = area?.polizasDisponibles
            ?.split("|")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        if (desdeBd.isNotEmpty()) return desdeBd

        return when (area?.idArea) {
            1 -> listOf("PÃ³liza Automotriz Cobertura Total", "PÃ³liza PÃ©rdida Total y Robo", "PÃ³liza Responsabilidad Civil", "SOAP")
            2 -> listOf("PÃ³liza Incendio y Sismo", "PÃ³liza Hogar Protegido Integral", "PÃ³liza Robo y Contenido")
            3 -> listOf("PÃ³liza Salud Complementaria", "PÃ³liza CatastrÃ³fica (CAEC)", "PÃ³liza Vida y Accidentes")
            else -> emptyList()
        }
    }

    fun onAreaSelected(idArea: Int) {
        _uiState.update {
            it.copy(
                idArea = idArea,
                polizaSeleccionada = "",
                tipoSiniestro = "",
                errArea = null,
                errPoliza = null,
                errTipo = null,
                errDocs = null
            )
        }
        cargarRequisitosDeArea(idArea)
    }

    fun onPolizaSelected(poliza: String) {
        _uiState.update { it.copy(polizaSeleccionada = poliza, errPoliza = null) }
    }

    fun onNumeroPolizaChange(valor: String) {
        val limpio = valor.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(numeroPoliza = limpio, errNumPoliza = null) }
    }

    fun onCompaniaChange(valor: String) {
        _uiState.update { it.copy(compania = valor, errCompania = null) }
    }

    fun onTipoSiniestroSelected(tipo: String) {
        _uiState.update { it.copy(tipoSiniestro = tipo, errTipo = null) }
    }

    fun onPatenteChange(valor: String) {
        val limpia = valor.replace(Regex("[^A-Za-z0-9]"), "").uppercase(Locale.ROOT).take(6)
        _uiState.update { it.copy(patente = limpia) }
    }

    fun onModeloVehiculoChange(valor: String) {
        _uiState.update { it.copy(modeloVehiculo = valor) }
    }

    fun onDetalleBienChange(valor: String) {
        _uiState.update { it.copy(detalleBien = valor) }
    }

    fun onFechaOcurrenciaSelected(fecha: String) {
        _uiState.update { it.copy(fechaOcurrencia = fecha, errFecha = null) }
    }

    fun onRegionSelected(region: String) {
        _uiState.update {
            it.copy(
                region = region,
                comuna = "",
                errRegion = null,
                errComuna = null
            )
        }
    }

    fun onComunaSelected(comuna: String) {
        _uiState.update { it.copy(comuna = comuna, errComuna = null) }
    }

    fun onDescripcionChange(valor: String) {
        _uiState.update { it.copy(descripcion = valor, errDescripcion = null) }
    }

    private fun cargarRequisitosDeArea(idArea: Int) {
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

    fun procesarUriAdjunto(
        uri: Uri,
        tipoDocumento: String,
        esPoliza: Boolean = false
    ): Result<AdjuntoBorradorItem> {
        return runCatching {
            val app = getApplication<Application>()
            val resolver = app.contentResolver

            runCatching {
                resolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            val mime = resolver.getType(uri) ?: error("No se reconoce el formato del archivo")
            val esImg = mime.lowercase(Locale.ROOT).startsWith("image/")
            val ext = when (mime.lowercase(Locale.ROOT)) {
                "application/pdf" -> "pdf"
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> if (esImg) "jpg" else error("Solo se permiten imÃ¡genes (JPG/PNG) o documentos PDF")
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
                        check(total <= 10L * 1024L * 1024L) { "El archivo supera el mÃ¡ximo permitido de 10 MB" }
                        output.write(buffer, 0, leidos)
                    }
                    check(total > 0) { "El archivo seleccionado estÃ¡ vacÃ­o" }
                }
            } ?: error("No se pudo leer el archivo seleccionado")

            val nuevoItem = AdjuntoBorradorItem(
                tipoDocumento = tipoDocumento,
                nombreArchivo = nombreOriginal ?: destino.name,
                rutaLocal = destino.absolutePath,
                uriPersistente = uri.toString(),
                esImagen = esImg,
                esPoliza = esPoliza
            )

            agregarAdjuntoALista(nuevoItem)
            nuevoItem
        }
    }

    fun procesarBitmapCamara(
        bitmap: Bitmap,
        tipoDocumento: String,
        esPoliza: Boolean = false
    ): Result<AdjuntoBorradorItem> {
        return runCatching {
            val app = getApplication<Application>()
            val carpeta = File(app.filesDir, "evidencias")
            if (!carpeta.exists()) carpeta.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nombreArchivo = "FOTO_$timestamp.jpg"
            val destino = File(carpeta, "${UUID.randomUUID()}.jpg")

            FileOutputStream(destino).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)) {
                    "No se pudo guardar la fotografÃ­a capturada"
                }
            }

            val nuevoItem = AdjuntoBorradorItem(
                tipoDocumento = tipoDocumento,
                nombreArchivo = nombreArchivo,
                rutaLocal = destino.absolutePath,
                uriPersistente = Uri.fromFile(destino).toString(),
                esImagen = true,
                esPoliza = esPoliza
            )

            agregarAdjuntoALista(nuevoItem)
            nuevoItem
        }
    }

    private fun agregarAdjuntoALista(nuevoItem: AdjuntoBorradorItem) {
        _uiState.update { state ->
            val listaBase = if (nuevoItem.esPoliza) {
                state.adjuntos.filterNot { it.esPoliza }.also {
                    state.adjuntos.filter { it.esPoliza }.forEach { viejo -> File(viejo.rutaLocal).delete() }
                }
            } else {
                state.adjuntos
            }
            state.copy(
                adjuntos = listaBase + nuevoItem,
                errPolizaDoc = if (nuevoItem.esPoliza) null else state.errPolizaDoc,
                errDocs = if (!nuevoItem.esPoliza) null else state.errDocs
            )
        }
    }

    fun eliminarAdjunto(item: AdjuntoBorradorItem) {
        runCatching { File(item.rutaLocal).delete() }
        _uiState.update { state ->
            state.copy(adjuntos = state.adjuntos.filterNot { it.idTemp == item.idTemp })
        }
    }

    fun setErrorDocumento(esPoliza: Boolean, mensaje: String?) {
        _uiState.update {
            if (esPoliza) it.copy(errPolizaDoc = mensaje) else it.copy(errDocs = mensaje)
        }
    }

    fun validarFormulario(): Boolean {
        val state = _uiState.value
        val reqs = _requisitos.value

        val errArea = if (state.idArea == null) "Selecciona el Ã¡rea del siniestro" else null
        val errTipo = if (state.tipoSiniestro.isBlank()) "Selecciona el tipo de problema o evento" else null
        val errPoliza = if (state.polizaSeleccionada.isBlank()) "Selecciona el tipo de pÃ³liza" else null
        val errNumPoliza = if (state.numeroPoliza.length < 4) "Ingresa al menos 4 dÃ­gitos de pÃ³liza" else null
        val errCompania = if (state.compania.isBlank()) "Selecciona o ingresa la compaÃ±Ã­a aseguradora" else null
        val errFecha = if (!esFechaOcurrenciaValida(state.fechaOcurrencia)) "Selecciona una fecha vÃ¡lida no futura" else null
        val errRegion = if (state.region.isBlank()) "Selecciona la regiÃ³n del siniestro" else null
        val errComuna = if (state.comuna.isBlank()) "Selecciona la comuna de ocurrencia" else null
        val errDescripcion = if (state.descripcion.trim().length < 10) "Describe lo ocurrido (mÃ­nimo 10 caracteres)" else null

        val tienePolizaAdjunta = state.adjuntos.any { it.esPoliza }
        val errPolizaDoc = if (!tienePolizaAdjunta) "Adjunta el respaldo de tu pÃ³liza (PDF o foto)" else null

        val obligatoriosFaltantes = reqs.filter { it.esObligatorio }
            .filter { req -> state.adjuntos.none { !it.esPoliza && it.tipoDocumento == req.nombreDocumento } }

        val errDocs = if (obligatoriosFaltantes.isNotEmpty()) {
            "Faltan documentos obligatorios: ${obligatoriosFaltantes.joinToString(", ") { it.nombreDocumento }}"
        } else null

        _uiState.update {
            it.copy(
                errArea = errArea,
                errTipo = errTipo,
                errPoliza = errPoliza,
                errNumPoliza = errNumPoliza,
                errCompania = errCompania,
                errFecha = errFecha,
                errRegion = errRegion,
                errComuna = errComuna,
                errDescripcion = errDescripcion,
                errPolizaDoc = errPolizaDoc,
                errDocs = errDocs
            )
        }

        return listOf(
            errArea,
            errTipo,
            errPoliza,
            errNumPoliza,
            errCompania,
            errFecha,
            errRegion,
            errComuna,
            errDescripcion,
            errPolizaDoc,
            errDocs
        ).all { it == null }
    }

    fun guardarSiniestroCompleto(
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!validarFormulario()) return
        if (_uiState.value.isSaving) return

        val state = _uiState.value
        val usuario = SesionUsuario.obtenerSesion()
        if (usuario == null) {
            onError("No hay una sesiÃ³n activa. Inicia sesiÃ³n nuevamente.")
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val fechaLimiteLegal = LocalDate.now().plusDays(45)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                val fechaAhora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                val prefijoArea = when (state.idArea) {
                    1 -> "VehÃ­culo - "
                    2 -> "Hogar - "
                    else -> "Salud - "
                }

                val bienAsegurado = if (state.idArea == 1) {
                    listOf(state.patente.trim(), state.modeloVehiculo.trim())
                        .filter { it.isNotBlank() }
                        .joinToString(" - ")
                } else {
                    state.detalleBien.trim()
                }

                val nuevoSiniestro = SiniestroEntity(
                    numero = "SIN-26-${(100000..999999).random()}",
                    idAsegurado = usuario.idUsuario,
                    companiaNombre = state.compania.trim(),
                    numeroPoliza = "${state.polizaSeleccionada} #${state.numeroPoliza}",
                    tipoSiniestroNombre = "$prefijoArea${state.tipoSiniestro.trim()}",
                    modeloVehiculo = bienAsegurado,
                    fechaOcurrencia = state.fechaOcurrencia,
                    fechaReporte = fechaAhora,
                    comunaNombre = state.comuna.trim(),
                    descripcion = state.descripcion.trim(),
                    etapaNombre = "Recibido",
                    liquidadorNombre = "Por asignar",
                    proximoPasoTexto = "RevisiÃ³n inicial de antecedentes (Plazo legal 45 dÃ­as)",
                    idArea = state.idArea,
                    fechaLimiteLegal = fechaLimiteLegal
                )

                withContext(Dispatchers.IO) {
                    val idGenerado = repository.registrarNuevoSiniestro(nuevoSiniestro).toInt()

                    state.adjuntos.forEach { adj ->
                        repository.guardarNuevaEvidencia(
                            DocumentoEntity(
                                idSiniestro = idGenerado,
                                nombreTipoDocumento = adj.tipoDocumento,
                                comentario = if (adj.esPoliza) "Documento de pÃ³liza respaldada" else "Adjuntado al reportar siniestro",
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
                            mensaje = "Siniestro ${nuevoSiniestro.numero} ingresado. Plazo legal de respuesta: $fechaLimiteLegal (45 dÃ­as, D.S. NÂ° 1.055).",
                            fechaHora = fechaAhora,
                            leido = false,
                            requiereAccion = false,
                            estadoAviso = "PENDIENTE"
                        )
                    )
                }

                _uiState.update { it.copy(isSaving = false) }
                onExito()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                onError(e.message ?: "No se pudo guardar el siniestro. IntÃ©ntalo nuevamente.")
            }
        }
    }
}
