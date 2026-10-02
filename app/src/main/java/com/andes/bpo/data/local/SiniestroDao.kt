package com.andes.bpo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SiniestroDao {

    @Query("SELECT * FROM siniestros")
    fun getAllSiniestros(): Flow<List<SiniestroEntity>>

    @Query("SELECT * FROM siniestros WHERE id = :id")
    suspend fun getSiniestroById(id: String): SiniestroEntity?

    @Query("SELECT * FROM historial_gestiones WHERE siniestro_id = :siniestroId ORDER BY id DESC")
    fun getHistorialBySiniestroId(siniestroId: String): Flow<List<HistorialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiniestro(siniestro: SiniestroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorial(historial: HistorialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidencia(evidencia: EvidenciaEntity)
}