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
            val siniestroDao = db.siniestroDao()
            val passBase = PasswordUtils.generarHash("Andes2026!")

            val usuariosOficiales = listOf(
                UsuarioEntity(
                    rut = "158423715",
                    nombre = "Camila",
                    apellido = "Soto",
                    correo = "camila.soto@correo.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "ASEGURADO_NATURAL",
                    aseguradora = "BCI Seguros"
                ),
                UsuarioEntity(
                    rut = "761234560",
                    nombre = "Transportes Pacífico",
                    apellido = "SpA",
                    correo = "siniestros@tpacifico.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "ASEGURADO_EMPRESA",
                    aseguradora = "HDI Seguros"
                ),
                UsuarioEntity(
                    rut = "765432103",
                    nombre = "Pollos Hermanos",
                    apellido = "SpA",
                    correo = "contacto@polloshermanos.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "ASEGURADO_EMPRESA",
                    direccion = "Av. Matta 1420, Santiago",
                    aseguradora = "Chilena Consolidada (Zurich)"
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
                    tipoLiquidador = "VEHICULOS",
                    idArea = 1
                ),
                UsuarioEntity(
                    rut = "555555555",
                    nombre = "Elena",
                    apellido = "Vargas",
                    correo = "e.vargas@liquidadores.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "LIQUIDADOR_EXTERNO",
                    equipo = "SALUD",
                    tipoLiquidador = "SALUD",
                    idArea = 3,
                    requiereCambioClave = false
                ),
                UsuarioEntity(
                    rut = "666666666",
                    nombre = "Nacho",
                    apellido = "Vargas",
                    correo = "n.vargas@liquidadores.cl",
                    passwordHash = passBase.hash,
                    passwordSalt = passBase.salt,
                    rol = "LIQUIDADOR_EXTERNO",
                    equipo = "HOGAR",
                    tipoLiquidador = "HOGAR",
                    idArea = 2
                )
            )

            usuariosOficiales.forEach { usuario ->
                val existente = usuarioDao.buscarPorRut(usuario.rut)
                if (existente == null) {
                    usuarioDao.registrarUsuario(usuario)
                } else if (usuario.rut in listOf("444444444", "555555555", "666666666")) {
                    usuarioDao.actualizarUsuario(
                        existente.copy(
                            nombre = usuario.nombre,
                            apellido = usuario.apellido,
                            rol = usuario.rol,
                            equipo = usuario.equipo,
                            tipoLiquidador = usuario.tipoLiquidador,
                            idArea = usuario.idArea,
                            requiereCambioClave = false
                        )
                    )
                }
            }

            val pollosHermanos = usuarioDao.buscarPorRut("765432103")
            val analistaRodrigo = usuarioDao.buscarPorRut("222222222")
            val adminAndes = usuarioDao.buscarPorRut("333333333")

            if (pollosHermanos != null) {
                val existente9001 = siniestroDao.obtenerSiniestroActual(9001)
                if (existente9001 == null || existente9001.numero == "SIN-2026-8842") {
                    siniestroDao.insertSiniestro(
                        SiniestroEntity(
                            idSiniestro = 9001,
                            numero = "SIN-2026-8850",
                            idAsegurado = pollosHermanos.idUsuario,
                            companiaNombre = "HDI Seguros",
                            numeroPoliza = "POL-FLT-40912",
                            tipoSiniestroNombre = "Vehículo - Robo con Intimidación en Ruta",
                            modeloVehiculo = "Flota Camiones Frigoríficos Scania P320 (2 unidades)",
                            fechaOcurrencia = "20/11/2026",
                            fechaReporte = "21/11/2026 08:15",
                            comunaNombre = "Lampa (Ruta 5 Norte)",
                            descripcion = "Asalto armado en carretera a dos camiones frigoríficos de distribución de Pollos Hermanos SpA durante traslado de carga entre centros.",
                            etapaNombre = "Recibido",
                            liquidadorNombre = "Por asignar",
                            proximoPasoTexto = "En revisión por Analista para asignar liquidador de Vehículos.",
                            idArea = 1
                        )
                    )

                    if (existente9001 == null) {
                        siniestroDao.insertGestion(
                            GestionEntity(
                                idSiniestro = 9001,
                                fechaHora = "21/11/2026 08:15",
                                descripcion = "Denuncio ingresado por Pollos Hermanos SpA: Robo de camiones frigoríficos en Ruta 5 Norte.",
                                responsable = "Pollos Hermanos SpA",
                                visibleAsegurado = true
                            )
                        )
                        siniestroDao.insertDocumento(
                            DocumentoEntity(
                                idSiniestro = 9001,
                                nombreTipoDocumento = "Parte Policial Carabineros",
                                comentario = "Constancia de robo con intimidación en carretera",
                                fechaSolicitud = "21/11/2026 08:15",
                                estado = "Entregado",
                                nombreArchivo = "Parte_Policial_Camiones_Ruta5.pdf",
                                rutaUri = "local://parte_camiones_pollos",
                                fechaRecepcion = "21/11/2026 08:15"
                            )
                        )
                        siniestroDao.insertDocumento(
                            DocumentoEntity(
                                idSiniestro = 9001,
                                nombreTipoDocumento = "Padrón y Guía de Despacho",
                                comentario = "Acreditación de flota y carga transportada",
                                fechaSolicitud = "21/11/2026 08:15",
                                estado = "Entregado",
                                nombreArchivo = "Padron_Flota_Frigorifica.pdf",
                                rutaUri = "local://padron_camiones_pollos",
                                fechaRecepcion = "21/11/2026 08:15"
                            )
                        )

                        listOfNotNull(analistaRodrigo, adminAndes).forEach { dest ->
                            siniestroDao.insertAviso(
                                AvisoEntity(
                                    fechaHora = "21/11/2026 08:15",
                                    mensaje = "Nuevo siniestro recibido: SIN-2026-8850 (Robo camiones frigoríficos) de Pollos Hermanos SpA. Pendiente de asignación.",
                                    leido = false,
                                    idUsuario = dest.idUsuario,
                                    requiereAccion = true,
                                    estadoAviso = "PENDIENTE"
                                )
                            )
                        }
                    }
                }

                if (siniestroDao.obtenerSiniestroActual(9002) == null) {
                    siniestroDao.insertSiniestro(
                        SiniestroEntity(
                            idSiniestro = 9002,
                            numero = "SIN-2026-8851",
                            idAsegurado = pollosHermanos.idUsuario,
                            companiaNombre = "Chilena Consolidada (Zurich)",
                            numeroPoliza = "POL-INC-88421",
                            tipoSiniestroNombre = "Hogar - Incendio Planta Lavandería",
                            modeloVehiculo = "Lavandería Industrial Brillante - Lavado de Uniformes",
                            fechaOcurrencia = "21/11/2026",
                            fechaReporte = "22/11/2026 09:30",
                            comunaNombre = "Quilicura",
                            descripcion = "Incendio estructural con pérdida total en planta de lavandería industrial donde se lavaban los uniformes del personal de Pollos Hermanos SpA.",
                            etapaNombre = "Recibido",
                            liquidadorNombre = "Por asignar",
                            proximoPasoTexto = "En revisión por Analista para asignar liquidador especialista en Hogar/Incendio.",
                            idArea = 2
                        )
                    )

                    siniestroDao.insertGestion(
                        GestionEntity(
                            idSiniestro = 9002,
                            fechaHora = "22/11/2026 09:30",
                            descripcion = "Denuncio ingresado por Pollos Hermanos SpA: Incendio total en lavandería industrial de uniformes.",
                            responsable = "Pollos Hermanos SpA",
                            visibleAsegurado = true
                        )
                    )
                    siniestroDao.insertDocumento(
                        DocumentoEntity(
                            idSiniestro = 9002,
                            nombreTipoDocumento = "Póliza Incendio Comercial",
                            comentario = "Cobertura instalaciones industriales y maquinaria",
                            fechaSolicitud = "22/11/2026 09:30",
                            estado = "Entregado",
                            nombreArchivo = "Poliza_Lavanderia_PollosHermanos.pdf",
                            rutaUri = "local://poliza_lavanderia_pollos",
                            fechaRecepcion = "22/11/2026 09:30"
                        )
                    )
                    siniestroDao.insertDocumento(
                        DocumentoEntity(
                            idSiniestro = 9002,
                            nombreTipoDocumento = "Informe de Bomberos",
                            comentario = "Peritaje preliminar incendio en nave de lavandería",
                            fechaSolicitud = "22/11/2026 09:30",
                            estado = "Entregado",
                            nombreArchivo = "Informe_Bomberos_Lavanderia.pdf",
                            rutaUri = "local://bomberos_lavanderia_pollos",
                            fechaRecepcion = "22/11/2026 09:30"
                        )
                    )

                    listOfNotNull(analistaRodrigo, adminAndes).forEach { dest ->
                        siniestroDao.insertAviso(
                            AvisoEntity(
                                fechaHora = "22/11/2026 09:30",
                                mensaje = "Nuevo siniestro recibido: SIN-2026-8851 (Incendio lavandería de uniformes) de Pollos Hermanos SpA. Pendiente de asignación.",
                                leido = false,
                                idUsuario = dest.idUsuario,
                                requiereAccion = true,
                                estadoAviso = "PENDIENTE"
                            )
                        )
                    }
                }
            }
        }
    }
}