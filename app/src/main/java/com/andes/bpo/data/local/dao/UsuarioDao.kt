package com.andes.bpo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.andes.bpo.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrarUsuario(
        usuario: UsuarioEntity
    ): Long

    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun buscarPorRut(
        rut: String
    ): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE idUsuario = :id LIMIT 1")
    suspend fun buscarPorId(
        id: Int
    ): UsuarioEntity?

    @Update
    suspend fun actualizarUsuario(
        usuario: UsuarioEntity
    )

    @Query("SELECT * FROM usuarios ORDER BY rol ASC, nombre ASC")
    fun obtenerTodosLosUsuarios(): Flow<List<UsuarioEntity>>

    @Query(
        """
        SELECT * FROM usuarios
        WHERE rol LIKE 'LIQUIDADOR%'
        AND equipo = :equipo
        ORDER BY nombre, apellido
        """
    )
    fun obtenerLiquidadoresPorEquipo(
        equipo: String
    ): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE rol = 'LIQUIDADOR_RESPALDO' AND equipo = :equipo LIMIT 1")
    suspend fun obtenerLiquidadorRespaldo(equipo: String): UsuarioEntity?

    @Query(
        """
        UPDATE usuarios
        SET codigoRecuperacionHash = :codigoHash
        WHERE idUsuario = :idUsuario
        """
    )
    suspend fun guardarCodigoRecuperacion(
        idUsuario: Int,
        codigoHash: String
    ): Int

    @Query(
        """
        UPDATE usuarios
        SET passwordHash = :nuevoHash,
            passwordSalt = :nuevaSal,
            codigoRecuperacionHash = NULL
        WHERE idUsuario = :idUsuario
          AND codigoRecuperacionHash = :codigoHash
        """
    )
    suspend fun recuperarClaveConCodigo(
        idUsuario: Int,
        codigoHash: String,
        nuevoHash: String,
        nuevaSal: String
    ): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND idUsuario != :idUsuario")
    suspend fun contarCorreoEnOtrosUsuarios(correo: String, idUsuario: Int): Int

    @Query(
        """
        UPDATE usuarios
        SET nombre = :nombre,
            apellido = :apellido,
            correo = :correo
        WHERE idUsuario = :idUsuario
        """
    )
    suspend fun actualizarDatosPerfil(
        idUsuario: Int,
        nombre: String,
        apellido: String,
        correo: String
    ): Int

    @Query(
        """
        UPDATE usuarios
        SET passwordHash = :nuevoHash,
            passwordSalt = :nuevaSal,
            codigoRecuperacionHash = NULL
        WHERE idUsuario = :idUsuario
        """
    )
    suspend fun actualizarClaveDesdePerfil(
        idUsuario: Int,
        nuevoHash: String,
        nuevaSal: String
    ): Int

    @Query(
        """
        UPDATE usuarios
        SET passwordHash = :nuevoHash,
            passwordSalt = :nuevaSal,
            requiereCambioClave = 0,
            codigoRecuperacionHash = NULL
        WHERE idUsuario = :idUsuario
        """
    )
    suspend fun completarCambioClaveObligatorio(
        idUsuario: Int,
        nuevoHash: String,
        nuevaSal: String
    ): Int

    @Query(
        """
        UPDATE usuarios
        SET rol = :nuevoRol,
            equipo = :nuevoEquipo,
            tipoLiquidador = :tipoLiquidador
        WHERE idUsuario = :idUsuario
        """
    )
    suspend fun actualizarRolYEquipoAdmin(
        idUsuario: Int,
        nuevoRol: String,
        nuevoEquipo: String?,
        tipoLiquidador: String?
    ): Int
}