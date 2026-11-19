package com.andes.bpo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.andes.bpo.data.local.entity.AreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {

    @Query("SELECT * FROM areas ORDER BY nombre ASC")
    fun obtenerAreas(): Flow<List<AreaEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarAreas(
        areas: List<AreaEntity>
    )

    @Transaction
    suspend fun asegurarAreasIniciales() {
        insertarAreas(
            listOf(
                AreaEntity(
                    idArea = 1,
                    nombre = "Vehículos"
                ),
                AreaEntity(
                    idArea = 2,
                    nombre = "Hogar"
                )
            )
        )
    }
}
