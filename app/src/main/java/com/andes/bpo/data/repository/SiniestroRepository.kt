package com.andes.bpo.data.repository

import android.util.Log
import com.andes.bpo.data.local.dao.AreaDao
import com.andes.bpo.data.local.dao.SiniestroDao
import com.andes.bpo.data.local.dao.UsuarioDao
import com.andes.bpo.data.local.entity.*
import com.andes.bpo.data.remote.AndesApiService
import com.andes.bpo.data.remote.DocumentoRemoteDto
import com.andes.bpo.data.remote.RetrofitClient
import com.andes.bpo.data.remote.SiniestroRemoteDto
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class SiniestroRepository(
    private val dao: SiniestroDao,
    private val usuarioDao: UsuarioDao? = null,
    private val areaDao: AreaDao? = null,
    private val api: AndesApiService = RetrofitClient.api
) {
    companion object {
        private val localToRemoteId = ConcurrentHashMap<Int, Long>()
    }

    fun getSiniestrosPorUsuario(idUsuario: Int): Flow<List<SiniestroEntity>> =
        dao.getSiniestrosPorUsuario(idUsuario)

    fun getTodosLosSiniestros(): Flow<List<SiniestroEntity>> =
        dao.getTodosLosSiniestros()

    fun getSiniestroPorId(idSiniestro: Int): Flow<SiniestroEntity?> =
        dao.getSiniestroPorId(idSiniestro)

    fun getGestiones(idSiniestro: Int): Flow<List<GestionEntity>> =
        dao.getGestionesVisibles(idSiniestro)

    fun getDocumentos(idSiniestro: Int): Flow<List<DocumentoEntity>> =
        dao.getDocumentosPorSiniestro(idSiniestro)

    fun getAvisos(): Flow<List<AvisoEntity>> =
        dao.getAvisos()

    fun getSiniestrosSinAsignar(): Flow<List<SiniestroEntity>> =
        dao.getSiniestrosSinAsignar()

    fun getSiniestrosAsignados(idLiquidador: Int): Flow<List<SiniestroEntity>> =
        dao.getSiniestrosAsignados(idLiquidador)

    fun getAvisosPorUsuario(idUsuario: Int): Flow<List<AvisoEntity>> =
        dao.getAvisosPorUsuario(idUsuario)

    fun getAreas(): Flow<List<AreaEntity>> =
        areaDao?.obtenerAreas() ?: emptyFlow()

    fun getRequisitosPorArea(idArea: Int): Flow<List<RequisitoAreaEntity>> =
        areaDao?.obtenerRequisitosPorArea(idArea) ?: emptyFlow()

    fun getTodosLosUsuarios(): Flow<List<UsuarioEntity>> =
        usuarioDao?.obtenerTodosLosUsuarios() ?: emptyFlow()

    suspend fun registrarNuevoSiniestro(siniestro: SiniestroEntity): Long {
        val idGenerado = dao.insertSiniestro(siniestro)
        val localId = idGenerado.toInt()

        val gestionInicial = GestionEntity(
            idSiniestro = localId,
            fechaHora = siniestro.fechaReporte,
            descripcion = "Recibimos el reporte de tu siniestro.",
            responsable = "Asegurado"
        )
        dao.insertGestion(gestionInicial)

        val sesion = SesionUsuario.obtenerSesion()
        val nombreAsegurado = listOfNotNull(sesion?.nombre, sesion?.apellido)
            .joinToString(" ")
            .trim()
            .ifBlank { "Asegurado Andes" }

        dao.obtenerAnalistasYAdmins().forEach { analista ->
            dao.insertAviso(
                AvisoEntity(
                    fechaHora = siniestro.fechaReporte,
                    mensaje = "Nuevo siniestro recibido: ${siniestro.numero} (${siniestro.tipoSiniestroNombre}) reportado por $nombreAsegurado. Pendiente de asignación.",
                    leido = false,
                    idUsuario = analista.idUsuario,
                    requiereAccion = true,
                    estadoAviso = "PENDIENTE"
                )
            )
        }

        runCatching {
            val rutFormateado = formatearRutSinPuntos(sesion?.rut ?: "")
            val dto = SiniestroRemoteDto(
                numeroSiniestro = siniestro.numero.take(30),
                rutAsegurado = rutFormateado.take(12),
                nombreAsegurado = nombreAsegurado.take(150),
                polizaNumero = siniestro.numeroPoliza.take(50),
                idArea = siniestro.idArea ?: 1,
                descripcion = buildString {
                    append(siniestro.tipoSiniestroNombre)
                    if (siniestro.modeloVehiculo.isNotBlank()) append(" | Bien: ${siniestro.modeloVehiculo}")
                    if (siniestro.comunaNombre.isNotBlank()) append(" | Comuna: ${siniestro.comunaNombre}")
                    append(" | ${siniestro.descripcion}")
                }.take(1000),
                fechaOcurrencia = convertirFechaAIso(siniestro.fechaOcurrencia),
                fechaDenuncio = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                estado = siniestro.etapaNombre.take(40)
            )

            val response = api.crearSiniestro(dto)
            if (response.isSuccessful) {
                response.body()?.let { creado ->
                    creado.idSiniestro?.let { remoteId ->
                        localToRemoteId[localId] = remoteId
                    }
                    if (creado.numeroSiniestro.isNotBlank() && creado.numeroSiniestro != siniestro.numero) {
                        dao.actualizarSiniestro(
                            siniestro.copy(
                                idSiniestro = localId,
                                numero = creado.numeroSiniestro
                            )
                        )
                    }
                }
            } else {
                Log.e("SiniestroSync", "Error POST /api/siniestros (${response.code()}): ${response.errorBody()?.string()}")
            }
        }.onFailure {
            Log.e("SiniestroSync", "Fallo red al crear siniestro en backend", it)
        }

        return idGenerado
    }

    suspend fun sincronizarSiniestrosDeUsuario(rut: String, idUsuario: Int) {
        runCatching {
            val rutSinPuntos = formatearRutSinPuntos(rut)
            var response = api.listarSiniestrosPorAsegurado(rutSinPuntos)
            if (response.isSuccessful && response.body().isNullOrEmpty()) {
                val rutSoloNumeros = rut.replace(Regex("[^0-9kK]"), "").uppercase(Locale.ROOT)
                if (rutSoloNumeros != rutSinPuntos) {
                    response = api.listarSiniestrosPorAsegurado(rutSoloNumeros)
                }
            }

            if (response.isSuccessful) {
                val remotos = response.body().orEmpty()
                val locales = dao.getSiniestrosPorUsuario(idUsuario).firstOrNull().orEmpty()

                remotos.forEach { rem ->
                    rem.idSiniestro?.let { rId ->
                        val matchLocal = locales.firstOrNull { it.numero == rem.numeroSiniestro }
                        if (matchLocal != null) {
                            localToRemoteId[matchLocal.idSiniestro] = rId
                            val etapaActualizada = rem.estado.ifBlank { matchLocal.etapaNombre }
                            val liqActualizado = rem.rutLiquidadorAsignado?.let { "Liquidador ($it)" }
                                ?: matchLocal.liquidadorNombre
                            if (matchLocal.etapaNombre != etapaActualizada || matchLocal.liquidadorNombre != liqActualizado) {
                                dao.actualizarSiniestro(
                                    matchLocal.copy(
                                        etapaNombre = etapaActualizada,
                                        liquidadorNombre = liqActualizado
                                    )
                                )
                            }
                        } else {
                            val nombreArea = when (rem.idArea) {
                                1 -> "Vehículo - Siniestro"
                                2 -> "Hogar - Siniestro"
                                3 -> "Salud - Siniestro"
                                else -> "Siniestro General"
                            }
                            val nuevoLocalId = dao.insertSiniestro(
                                SiniestroEntity(
                                    numero = rem.numeroSiniestro,
                                    idAsegurado = idUsuario,
                                    companiaNombre = "Andes BPO Seguros",
                                    numeroPoliza = rem.polizaNumero,
                                    tipoSiniestroNombre = nombreArea,
                                    fechaOcurrencia = convertirFechaDesdeIso(rem.fechaOcurrencia),
                                    fechaReporte = convertirFechaHoraDesdeIso(rem.fechaDenuncio),
                                    comunaNombre = "Santiago",
                                    descripcion = rem.descripcion,
                                    etapaNombre = rem.estado.ifBlank { "Recibido" },
                                    liquidadorNombre = rem.rutLiquidadorAsignado ?: "Por asignar",
                                    proximoPasoTexto = "Seguimiento sincronizado con central",
                                    idArea = rem.idArea
                                )
                            ).toInt()
                            localToRemoteId[nuevoLocalId] = rId
                        }
                    }
                }
            }
        }.onFailure {
            Log.w("SiniestroSync", "No se pudo sincronizar lista desde backend: ${it.message}")
        }
    }

    suspend fun sincronizarHistorialDeSiniestro(localSiniestroId: Int) {
        runCatching {
            val localSiniestro = dao.obtenerSiniestroActual(localSiniestroId) ?: return
            var remoteId = localToRemoteId[localSiniestroId]

            if (remoteId == null) {
                val respSiniestros = api.listarSiniestros()
                if (respSiniestros.isSuccessful) {
                    val encontrado = respSiniestros.body()
                        ?.firstOrNull { it.numeroSiniestro == localSiniestro.numero }
                    if (encontrado?.idSiniestro != null) {
                        remoteId = encontrado.idSiniestro
                        localToRemoteId[localSiniestroId] = remoteId
                    }
                }
            }

            val idRemotoFinal = remoteId ?: return
            val response = api.listarHistorialSiniestro(idRemotoFinal)
            if (response.isSuccessful) {
                val remotos = response.body().orEmpty()
                val existentes = dao.getGestionesVisibles(localSiniestroId).firstOrNull().orEmpty()

                remotos.forEach { item ->
                    val textoEvento = listOfNotNull(
                        item.evento?.trim()?.takeIf { it.isNotBlank() },
                        item.detalle?.trim()?.takeIf { it.isNotBlank() }
                    ).joinToString(": ").ifBlank { "Actualización registrada en central" }

                    val fechaFormateada = convertirFechaHoraDesdeIso(item.fechaEvento)
                        .ifBlank { localSiniestro.fechaReporte }

                    val yaExiste = existentes.any {
                        it.descripcion.equals(textoEvento, ignoreCase = true) &&
                                it.fechaHora == fechaFormateada
                    }

                    if (!yaExiste) {
                        dao.insertGestion(
                            GestionEntity(
                                idSiniestro = localSiniestroId,
                                fechaHora = fechaFormateada,
                                descripcion = textoEvento,
                                responsable = item.usuarioResponsable?.takeIf { it.isNotBlank() }
                                    ?: "Sistema Andes BPO",
                                visibleAsegurado = true
                            )
                        )
                    }
                }
            }
        }.onFailure {
            Log.w("SiniestroSync", "No se pudo sincronizar historial remoto: ${it.message}")
        }
    }

    suspend fun adjuntarDocumento(idDocumento: Int, nombreArchivo: String, fechaActual: String) =
        dao.marcarDocumentoEntregado(idDocumento, nombreArchivo, fechaActual)

    suspend fun guardarNuevaEvidencia(documento: DocumentoEntity): Long {
        val id = dao.insertDocumento(documento)
        runCatching {
            val remoteSiniestroId = localToRemoteId[documento.idSiniestro] ?: documento.idSiniestro.toLong()
            val docDto = DocumentoRemoteDto(
                siniestroId = remoteSiniestroId,
                nombreArchivo = (documento.nombreArchivo ?: "archivo_adjunto").take(150),
                tipoDocumento = documento.nombreTipoDocumento.take(50),
                urlArchivo = (documento.rutaUri ?: "local://evidencia/${id}").take(500),
                fechaSubida = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            val resp = api.subirDocumentoSiniestro(remoteSiniestroId, docDto)
            if (!resp.isSuccessful) {
                Log.e("SiniestroSync", "Error POST documento (${resp.code()}): ${resp.errorBody()?.string()}")
            }
        }.onFailure {
            Log.e("SiniestroSync", "Fallo red al subir documento", it)
        }
        return id
    }

    suspend fun eliminarEvidencia(idDocumento: Int) {
        dao.eliminarDocumentoPorId(idDocumento)
    }

    suspend fun aplicarProrroga(
        idSiniestro: Int,
        idAnalista: Int,
        diasProrroga: Int,
        motivo: String,
        fechaHora: String
    ) {
        dao.aplicarProrrogaAnalista(idSiniestro, idAnalista, diasProrroga, motivo, fechaHora)
    }

    suspend fun aceptarCasoLiquidador(idSiniestro: Int, idLiquidador: Int, fechaHora: String) {
        dao.aceptarCasoLiquidador(idSiniestro, idLiquidador, fechaHora)
    }

    suspend fun rechazarYDerivarALiquidador2(
        idSiniestro: Int,
        idLiquidador1: Int,
        idLiquidador2: Int?,
        nombreLiquidador2: String?,
        motivoRechazo: String,
        fechaHora: String
    ) {
        dao.rechazarYDerivarALiquidador2(
            idSiniestro = idSiniestro,
            idLiquidador1 = idLiquidador1,
            idLiquidador2 = idLiquidador2,
            nombreLiquidador2 = nombreLiquidador2,
            motivoRechazo = motivoRechazo,
            fechaHora = fechaHora
        )
    }

    private fun formatearRutSinPuntos(rut: String): String {
        val limpio = rut.replace(Regex("[^0-9kK]"), "").uppercase(Locale.ROOT)
        return if (limpio.length >= 2) {
            "${limpio.dropLast(1)}-${limpio.last()}"
        } else {
            limpio
        }
    }

    private fun convertirFechaAIso(fechaDdMmYyyy: String): String {
        return runCatching {
            val entrada = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(fechaDdMmYyyy.trim(), entrada).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrElse {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    private fun convertirFechaDesdeIso(fechaIso: String?): String {
        if (fechaIso.isNullOrBlank()) return ""
        return runCatching {
            LocalDate.parse(fechaIso.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }.getOrDefault(fechaIso)
    }

    private fun convertirFechaHoraDesdeIso(fechaHoraIso: String?): String {
        if (fechaHoraIso.isNullOrBlank()) return ""
        return runCatching {
            LocalDateTime.parse(fechaHoraIso.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        }.getOrDefault(fechaHoraIso)
    }
}