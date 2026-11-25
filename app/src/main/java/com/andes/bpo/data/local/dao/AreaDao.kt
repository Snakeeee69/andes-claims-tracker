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
                    nombre = "VehÃ­culos / Auto",
                    polizasDisponibles = "PÃ³liza Automotriz Full Cobertura|PÃ³liza Responsabilidad Civil|PÃ³liza Flota Comercial"
                ),
                AreaEntity(
                    idArea = 2,
                    nombre = "Hogar / Incendio",
                    polizasDisponibles = "PÃ³liza Incendio y Sismo|PÃ³liza Robo y Contenido Hogar|PÃ³liza Condominio"
                ),
                AreaEntity(
                    idArea = 3,
                    nombre = "Salud / Vida",
                    polizasDisponibles = "PÃ³liza Complementaria Salud|PÃ³liza Seguro de Vida|PÃ³liza Accidentes Personales"
                )
            )
        )

        limpiarRequisitos()
        insertarRequisitos(
            listOf(
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Fotos daÃ±os y patente", esObligatorio = true),
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "DeclaraciÃ³n Jurada Simple (Ley 20.931)", esObligatorio = true),
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Licencia y PadrÃ³n", esObligatorio = true),
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Parte policial (robo/lesionados)", esObligatorio = false),
                RequisitoAreaEntity(idArea = 1, nombreDocumento = "Presupuesto de taller", esObligatorio = false),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Fotos/videos de daÃ±os", esObligatorio = true),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Presupuesto o cotizaciÃ³n", esObligatorio = true),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Denuncia Carabineros/PDI", esObligatorio = false),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Informe de Bomberos", esObligatorio = false),
                RequisitoAreaEntity(idArea = 2, nombreDocumento = "Boletas/facturas de bienes", esObligatorio = false),
                RequisitoAreaEntity(idArea = 3, nombreDocumento = "Boletas/facturas/bonos mÃ©dicos", esObligatorio = true),
                RequisitoAreaEntity(idArea = 3, nombreDocumento = "Informe o receta mÃ©dica", esObligatorio = true),
                RequisitoAreaEntity(idArea = 3, nombreDocumento = "Certificado de defunciÃ³n", esObligatorio = false),
                RequisitoAreaEntity(idArea = 3, nombreDocumento = "Epicrisis hospitalaria", esObligatorio = false)
            )
        )
    }
}
