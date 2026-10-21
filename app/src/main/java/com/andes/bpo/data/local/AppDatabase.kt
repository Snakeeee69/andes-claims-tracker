package com.andes.bpo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.andes.bpo.data.local.dao.SiniestroDao
import com.andes.bpo.data.local.entity.AvisoEntity
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.data.local.entity.GestionEntity
import com.andes.bpo.data.local.entity.SiniestroEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SiniestroEntity::class,
        GestionEntity::class,
        DocumentoEntity::class,
        AvisoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun siniestroDao(): SiniestroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "andes_siniestros_db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.siniestroDao())
                }
            }
        }

        suspend fun populateDatabase(dao: SiniestroDao) {
            dao.insertSiniestrosIniciales(
                listOf(
                    SiniestroEntity(
                        idSiniestro = 1,
                        numero = "SIN-26-004801",
                        idAsegurado = 1,
                        companiaNombre = "Seguros Cordillera",
                        numeroPoliza = "POL-ASG01-77812",
                        tipoSiniestroNombre = "Vehículo – choque o colisión",
                        fechaOcurrencia = "02-09-2026",
                        fechaReporte = "02-09-2026 18:40",
                        comunaNombre = "Maipú",
                        descripcion = "Colisión en intersección. Daños en parachoques delantero.",
                        etapaNombre = "En liquidación",
                        liquidadorNombre = "Paulina F. – Equipo Vehículos",
                        proximoPasoTexto = "Tenemos todo lo necesario. Estamos definiendo la resolución con tu compañía de seguros."
                    ),
                    SiniestroEntity(
                        idSiniestro = 2,
                        numero = "SIN-26-004838",
                        idAsegurado = 1,
                        companiaNombre = "Seguros Cordillera",
                        numeroPoliza = "POL-ASG01-99231",
                        tipoSiniestroNombre = "Vehículo – robo",
                        fechaOcurrencia = "20-09-2026",
                        fechaReporte = "21-09-2026 10:15",
                        comunaNombre = "La Florida",
                        descripcion = "Robo de accesorios en vehículo estacionado.",
                        etapaNombre = "En evaluación",
                        liquidadorNombre = "Paulina F. – Equipo Vehículos",
                        proximoPasoTexto = "Necesitamos que nos envíes los documentos pendientes para continuar con tu caso."
                    )
                )
            )

            dao.insertGestionesIniciales(
                listOf(
                    GestionEntity(1, 1, "02-09-2026 18:40", "Recibimos el reporte de tu siniestro.", "Asegurado"),
                    GestionEntity(2, 1, "03-09-2026 09:15", "Tu caso fue asignado a Paulina F. – Equipo Vehículos.", "N. Reyes"),
                    GestionEntity(3, 1, "05-09-2026 11:02", "Necesitamos el presupuesto de reparación del taller.", "P. Fernández"),
                    GestionEntity(4, 1, "08-09-2026 16:30", "Recibimos tu presupuesto de reparación.", "Asegurado"),
                    GestionEntity(5, 1, "18-09-2026 15:45", "Tu caso pasó a En liquidación.", "P. Fernández")
                )
            )

            dao.insertDocumentosIniciales(
                listOf(
                    DocumentoEntity(1, 1, "Presupuesto de reparación", "Adjuntar copia del taller", "05-09-2026", "Entregado", "presupuesto_taller.pdf", "08-09-2026"),
                    DocumentoEntity(2, 2, "Constancia de denuncia policial", "Copia legible de carabineros", "23-09-2026", "Pendiente"),
                    DocumentoEntity(3, 2, "Padrón del vehículo", "Copia del padrón a nombre del asegurado", "23-09-2026", "Pendiente")
                )
            )
        }
    }
}