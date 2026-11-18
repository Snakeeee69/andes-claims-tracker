
package com.andes.bpo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.andes.bpo.data.local.dao.SiniestroDao
import com.andes.bpo.data.local.dao.UsuarioDao
import com.andes.bpo.data.local.entity.AvisoEntity
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.data.local.entity.GestionEntity
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.data.local.entity.UsuarioEntity
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        SiniestroEntity::class,
        DocumentoEntity::class,
        GestionEntity::class,
        AvisoEntity::class,
        UsuarioEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun siniestroDao(): SiniestroDao

    abstract fun usuarioDao(): UsuarioDao

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

                // Asociar siniestros con liquidadores
                db.execSQL(
                    """
            ALTER TABLE siniestros
            ADD COLUMN idLiquidador INTEGER
            """.trimIndent()
                )

                // Identificar el equipo de cada usuario interno
                db.execSQL(
                    """
            ALTER TABLE usuarios
            ADD COLUMN equipo TEXT
            """.trimIndent()
                )
            }
        }


        private val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
            ALTER TABLE siniestros
            ADD COLUMN mensajeCierre TEXT
            """.trimIndent()
                )
            }
        }


        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            ALTER TABLE usuarios
            ADD COLUMN codigoRecuperacionHash TEXT
            """.trimIndent()
                )
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
                        MIGRATION_5_6
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
