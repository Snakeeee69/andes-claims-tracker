package com.andes.bpo.utils

import com.andes.bpo.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SesionActiva(
    val idUsuario: Int,
    val rut: String,
    val nombre: String,
    val apellido: String,
    val rol: String
)

object SesionUsuario {

    private val _usuarioActual =
        MutableStateFlow<SesionActiva?>(null)

    val usuarioActual = _usuarioActual.asStateFlow()

    fun iniciarSesion(usuario: UsuarioEntity) {
        _usuarioActual.value = SesionActiva(
            idUsuario = usuario.idUsuario,
            rut = usuario.rut,
            nombre = usuario.nombre,
            apellido = usuario.apellido,
            rol = usuario.rol
        )
    }

    fun obtenerSesion(): SesionActiva? {
        return _usuarioActual.value
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }
}
