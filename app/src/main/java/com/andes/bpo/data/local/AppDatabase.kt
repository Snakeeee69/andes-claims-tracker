package com.andes.bpo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.andes.bpo.data.local.dao.AreaDao
import com.andes.bpo.data.local.dao.SiniestroDao
import com.andes.bpo.data.local.dao.UsuarioDao
import com.andes.bpo.data.local.entity.*
import com.andes.bpo.utils.PasswordUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SiniestroEntity::class,
        DocumentoEntity::class,
        GestionEntity::class,
        AvisoEntity::class,
        UsuarioEntity::class,
        AreaEntity::class,
        RequisitoAreaEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun siniestroDao(): SiniestroDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun areaDao(): AreaDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS usuarios (
                        idUsuario INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        rut TEXT NOT NULL,
                        nombre TEXT NOT NULL,
                        apellido TEXT NOT NULL,
                        correo TEXT NOT NULL,
                        passwordHash TEXT NOT NULL,
                        passwordSalt TEXT NOT NULL,
                        rol TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_usuarios_rut ON usuarios (rut)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE siniestros ADD COLUMN idLiquidador INTEGER")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN equipo TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE siniestros ADD COLUMN mensajeCierre TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN codigoRecuperacionHash TEXT")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `areas` (
                        `idArea` INTEGER NOT NULL,
                        `nombre` TEXT NOT NULL,
                        PRIMARY KEY(`idArea`)
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE `siniestros` ADD COLUMN `idArea` INTEGER")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `usuarios` ADD COLUMN `tipoLiquidador` TEXT")
                db.execSQL("ALTER TABLE `usuarios` ADD COLUMN `idArea` INTEGER")
                db.execSQL("ALTER TABLE `usuarios` ADD COLUMN `requiereCambioClave` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `areas` ADD COLUMN `polizasDisponibles` TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `requisitos_area` (
                        `idRequisito` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `idArea` INTEGER NOT NULL,
                        `nombreDocumento` TEXT NOT NULL,
                        `esObligatorio` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE `siniestros` ADD COLUMN `fechaLimiteLegal` TEXT")
                db.execSQL("ALTER TABLE `siniestros` ADD COLUMN `diasProrroga` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `siniestros` ADD COLUMN `fechaLimiteRespuestaLiq` TEXT")
                db.execSQL("ALTER TABLE `siniestros` ADD COLUMN `aceptadoPorLiquidador` INTEGER")
                db.execSQL("ALTER TABLE `siniestros` ADD COLUMN `motivoRechazoLiquidador` TEXT")
                db.execSQL("ALTER TABLE `documentos` ADD COLUMN `esImagen` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `avisos` ADD COLUMN `idUsuario` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `avisos` ADD COLUMN `requiereAccion` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `avisos` ADD COLUMN `estadoAviso` TEXT NOT NULL DEFAULT 'PENDIENTE'")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `usuarios` ADD COLUMN `direccion` TEXT")
                db.execSQL("ALTER TABLE `usuarios` ADD COLUMN `aseguradora` TEXT")
            }
        }

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "andes_siniestros_db"
                )
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9
                    )
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            INSTANCE?.let { database ->
                                scope.launch(Dispatchers.IO) {
                                    asegurarDatosBase(database)
                                }
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private suspend fun asegurarDatosBase(db: AppDatabase) {
            db.areaDao().asegurarAreasIniciales()

            val usuarioDao = db.usuarioDao()
            val passBase = PasswordUtils.generarHash("Andes2026!")
            val passProvisoria = PasswordUtils.generarHash("123456")

            val usuariosOficiales = listOf(
                UsuarioEntity(
                    rut = "158423715",
                    nombre = "Camila",
                    apellido = "Soto",
                    correo = "camila.soto@correo.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "ASEGURADO_NATURAL"
                ),
                UsuarioEntity(
                    rut = "761234560",
                    nombre = "Transportes Pacífico",
                    apellido = "SpA",
                    correo = "siniestros@tpacifico.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "ASEGURADO_EMPRESA"
                ),
                UsuarioEntity(
                    rut = "222222222",
                    nombre = "Rodrigo",
                    apellido = "Muñoz",
                    correo = "r.munoz@andes.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "ANALISTA"
                ),
                UsuarioEntity(
                    rut = "333333333",
                    nombre = "Admin",
                    apellido = "Andes",
                    correo = "admin@andes.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "ADMINISTRADOR"
                ),
                UsuarioEntity(
                    rut = "444444444",
                    nombre = "Javier",
                    apellido = "Paredes",
                    correo = "j.paredes@liquidadores.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "LIQUIDADOR_EXTERNO",
                    equipo = "VEHICULOS",
                    tipoLiquidador = "EXTERNO",
                    idArea = 1
                ),
                UsuarioEntity(
                    rut = "555555555",
                    nombre = "Elena",
                    apellido = "Vargas",
                    correo = "e.vargas@liquidadores.cl",
                    passwordHash = passProvisoria.hash,
                    passwordSalt = passProvisoria.salt,
                    rol = "LIQUIDADOR_RESPALDO",
                    equipo = "VEHICULOS",
                    tipoLiquidador = "RESPALDO",
                    idArea = 1,
                    requiereCambioClave = true
                )
            )

            usuariosOficiales.forEach { usuario ->
                if (usuarioDao.buscarPorRut(usuario.rut) == null) {
                    usuarioDao.registrarUsuario(usuario)
                }
            }
        }
    }
}