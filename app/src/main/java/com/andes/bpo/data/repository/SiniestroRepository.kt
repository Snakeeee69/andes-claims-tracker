package com.andes.bpo.data.repository

import com.andes.bpo.data.local.dao.SiniestroDao
import com.andes.bpo.data.local.entity.AvisoEntity
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.data.local.entity.GestionEntity
import com.andes.bpo.data.local.entity.SiniestroEntity
import kotlinx.coroutines.flow.Flow

class SiniestroRepository(private val dao: SiniestroDao) {

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

    suspend fun registrarNuevoSiniestro(siniestro: SiniestroEntity): Long {
        val idGenerado = dao.insertSiniestro(siniestro)
        dao.insertGestion(
            GestionEntity(
                idSiniestro = idGenerado.toInt(),
                fechaHora = siniestro.fechaReporte,
                descripcion = "Recibimos el reporte de tu siniestro.",
                responsable = "Asegurado"
            )
        )
        return idGenerado
    }

    suspend fun adjuntarDocumento(idDocumento: Int, nombreArchivo: String, fechaActual: String) =
        dao.marcarDocumentoEntregado(idDocumento, nombreArchivo, fechaActual)
}