package com.andes.bpo.data.repository

import com.andes.bpo.data.local.dao.AreaDao
import com.andes.bpo.data.local.dao.SiniestroDao
import com.andes.bpo.data.local.dao.UsuarioDao
import com.andes.bpo.data.local.entity.*
import com.andes.bpo.data.remote.AndesApiService
import com.andes.bpo.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class SiniestroRepository(
    private val dao: SiniestroDao,
    private val usuarioDao: UsuarioDao? = null,
    private val areaDao: AreaDao? = null,
    private val api: AndesApiService = RetrofitClient.api
) {

    fun getSiniestrosPorUsuario(idUsuario: Int): Flow<List<SiniestroEntity>> =
        dao.getSiniestrosPorUsuario(idUsuario)

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
        val gestionInicial = GestionEntity(
            idSiniestro = idGenerado.toInt(),
            fechaHora = siniestro.fechaReporte,
            descripcion = "Recibimos el reporte de tu siniestro.",
            responsable = "Asegurado"
        )
        dao.insertGestion(gestionInicial)

        runCatching {
            api.crearSiniestro(siniestro.copy(idSiniestro = idGenerado.toInt()))
            api.registrarGestion(gestionInicial)
        }
        return idGenerado
    }

    suspend fun adjuntarDocumento(idDocumento: Int, nombreArchivo: String, fechaActual: String) =
        dao.marcarDocumentoEntregado(idDocumento, nombreArchivo, fechaActual)

    suspend fun guardarNuevaEvidencia(documento: DocumentoEntity): Long {
        val id = dao.insertDocumento(documento)
        runCatching { api.registrarEvidencia(documento.copy(idDocumento = id.toInt())) }
        return id
    }

    suspend fun eliminarEvidencia(idDocumento: Int) {
        dao.eliminarDocumentoPorId(idDocumento)
        runCatching { api.eliminarEvidencia(idDocumento) }
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
        idLiquidador2: Int,
        nombreLiquidador2: String,
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
}