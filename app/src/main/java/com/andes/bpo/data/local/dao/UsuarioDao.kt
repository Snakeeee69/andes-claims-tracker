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


    @Query(
        """
    SELECT * FROM usuarios
    WHERE rol = 'LIQUIDADOR'
    AND equipo = :equipo
    ORDER BY nombre, apellido
    """
    )
    fun obtenerLiquidadoresPorEquipo(
        equipo: String
    ): Flow<List<UsuarioEntity>>


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

}
