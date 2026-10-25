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
        DocumentoEntity::class,
        GestionEntity::class,
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
                lateinit var instance: AppDatabase
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "andes_siniestros_db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            instance.siniestroDao().insertSiniestrosIniciales(
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
                                    )
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
