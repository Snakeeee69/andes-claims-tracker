package com.andes.bpo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.andes.bpo.data.local.entity.AreaEntity
import com.andes.bpo.data.local.entity.RequisitoAreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {

    @Query("SELECT * FROM areas ORDER BY idArea ASC")
    fun obtenerAreas(): Flow<List<AreaEntity>>

    @Query("SELECT * FROM requisitos_area WHERE idArea = :idArea ORDER BY esObligatorio DESC, idRequisito ASC")
    fun obtenerRequisitosPorArea(idArea: Int): Flow<List<RequisitoAreaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAreas(areas: List<AreaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRequisitos(requisitos: List<RequisitoAreaEntity>)

    @Query("DELETE FROM requisitos_area")
    suspend fun limpiarRequisitos()

    @Transaction
    suspend fun asegurarAreasIniciales() {
        insertarAreas(
            listOf(
                AreaEntity(
                    idArea = 1,
                    nombre = "Vehículos / Auto",
                    polizasDisponibles = "Póliza Automotriz Full Cobertura|Póliza Responsabilidad Civil|Póliza Flota Comercial"
                ),
                AreaEntity(
                    idArea = 2,
                    nombre = "Hogar / Incendio",
                    polizasDisponibles = "Póliza Incendio y Sismo|Póliza Robo y Contenido Hogar|Póliza Condominio"
                ),
                AreaEntity(
                    idArea = 3,
                    nombre = "Salud / Vida",
                    polizasDisponibles = "Póliza Complementaria Salud|Póliza Seguro de Vida|Póliza Accidentes Personales"
                )
            )
        )

        limpiarRequisitos()
        insertarRequisitos(
            listOf(
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Fotos daños y patente", esObligatorio = true),
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Declaración Jurada Simple (Ley 20.931)", esObligatorio = true),
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Licencia y Padrón", esObligatorio = true),
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Parte policial (robo/lesionados)", esObligatorio = false),
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Presupuesto de taller", esObligatorio = false),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Fotos/videos de daños", esObligatorio = true),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Presupuesto o cotización", esObligatorio = true),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Denuncia Carabineros/PDI", esObligatorio = false),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Informe de Bomberos", esObligatorio = false),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Boletas/facturas de bienes", esObligatorio = false),
                RequisitoAreaEntity(idArea = 3, nombreDocumento = "Boletas/facturas/bonos médicos", esObligatorio = true),
                RequisitoAreaEntity(idArea = 3, nombreDocumento = "Informe o receta médica", esObligatorio = true),
                RequisitoAreaEntity(idArea = 3, nombreDocumento = "Certificado de defunción", esObligatorio = false),
                RequisitoAreaEntity(idArea = 3, nombreDocumento = "Epicrisis hospitalaria", esObligatorio = false)
            )
        )
    }
}