package com.andes.bpo.model

import com.andes.bpo.data.local.entity.UsuarioEntity

data class LoginUiState(
    val rut: String = "",
    val contrasena: String = "",
    val rutError: String? = null,
    val contrasenaError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val requiereCambioClave: Boolean = false,
    val usuarioAutenticado: UsuarioEntity? = null
)

data class RegistroUiState(
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val rolSeleccionado: String = "ASEGURADO_NATURAL",
    val rutError: String? = null,
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val correoError: String? = null,
    val claveError: String? = null,
    val confirmarClaveError: String? = null,
    val isLoading: Boolean = false,
    val registroExitoso: Boolean = false
)

data class PerfilUiState(
    val idUsuario: Int = 0,
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val rol: String = "",
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val correoError: String? = null,
    val claveActual: String = "",
    val claveNueva: String = "",
    val confirmarClaveNueva: String = "",
    val claveActualError: String? = null,
    val claveNuevaError: String? = null,
    val confirmarClaveNuevaError: String? = null,
    val guardadoExitoso: Boolean = false,
    val cambioClaveExitoso: Boolean = false
)

data class RecuperarClaveUiState(
    val rut: String = "",
    val correo: String = "",
    val codigoIngresado: String = "",
    val codigoGeneradoDemo: String? = null,
    val nuevaClave: String = "",
    val confirmarNuevaClave: String = "",
    val codigoEnviado: Boolean = false,
    val rutError: String? = null,
    val correoError: String? = null,
    val codigoError: String? = null,
    val nuevaClaveError: String? = null,
    val confirmarNuevaClaveError: String? = null,
    val recuperacionExitosa: Boolean = false
)
