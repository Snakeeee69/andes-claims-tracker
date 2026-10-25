package com.andes.bpo.data.local.dao

import androidx.room.*
import com.andes.bpo.data.local.entity.AvisoEntity
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.data.local.entity.GestionEntity
import com.andes.bpo.data.local.entity.SiniestroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiniestroDao {

    @Query("SELECT * FROM siniestros WHERE idAsegurado = :idUsuario ORDER BY idSiniestro DESC")
    fun getSiniestrosPorUsuario(idUsuario: Int): Flow<List<SiniestroEntity>>

    @Query("SELECT * FROM siniestros WHERE idSiniestro = :idSiniestro LIMIT 1")
    fun getSiniestroPorId(idSiniestro: Int): Flow<SiniestroEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiniestro(siniestro: SiniestroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiniestrosIniciales(siniestros: List<SiniestroEntity>)

    @Query("SELECT * FROM gestiones WHERE idSiniestro = :idSiniestro AND visibleAsegurado = 1 ORDER BY idGestion DESC")
    fun getGestionesVisibles(idSiniestro: Int): Flow<List<GestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGestion(gestion: GestionEntity): Long

    @Query("SELECT * FROM documentos WHERE idSiniestro = :idSiniestro ORDER BY idDocumento ASC")
    fun getDocumentosPorSiniestro(idSiniestro: Int): Flow<List<DocumentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumento(documento: DocumentoEntity): Long

    // Retorna Int (filas modificadas) para evitar el error 'jvm signature V' en KSP
    @Query("UPDATE documentos SET estado = 'Entregado', nombreArchivo = :nombreArchivo, fechaRecepcion = :fechaActual WHERE idDocumento = :idDocumento")
    suspend fun marcarDocumentoEntregado(idDocumento: Int, nombreArchivo: String, fechaActual: String): Int

    @Query("SELECT * FROM avisos ORDER BY idAviso DESC")
    fun getAvisos(): Flow<List<AvisoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAviso(aviso: AvisoEntity): Long
}