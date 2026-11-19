package com.andes.bpo.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.utils.PasswordUtils
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PerfilUiState(
    val cargando: Boolean = true,
    val guardandoPerfil: Boolean = false,
    val cambiandoClave: Boolean = false,
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val rut: String = "",
    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorCorreo: String? = null,
    val errorClaveActual: String? = null,
    val errorClaveNueva: String? = null,
    val errorConfirmacion: String? = null,
    val errorGeneral: String? = null,
    val mensaje: String? = null,
    val claveActualizada: Boolean = false
)

class PerfilViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application, viewModelScope).usuarioDao()
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState = _uiState.asStateFlow()

    init { cargarPerfil() }

    fun cambiarNombre(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, mensaje = null) }
    }
    fun cambiarApellido(valor: String) {
        _uiState.update { it.copy(apellido = valor, errorApellido = null, mensaje = null) }
    }
    fun cambiarCorreo(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, mensaje = null) }
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                val usuario = withContext(Dispatchers.IO) {
                    val sesion = SesionUsuario.obtenerSesion()
                        ?: error("Debes iniciar sesión")
                    dao.buscarPorId(sesion.idUsuario) ?: error("Usuario no encontrado")
                }
                _uiState.update {
                    it.copy(
                        cargando = false,
                        nombre = usuario.nombre,
                        apellido = usuario.apellido,
                        correo = usuario.correo,
                        rut = usuario.rut,
                        errorGeneral = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    fun guardarPerfil() {
        val estado = _uiState.value
        if (estado.cargando || estado.guardandoPerfil || estado.cambiandoClave) return

        val nombre = estado.nombre.trim()
        val apellido = estado.apellido.trim()
        val correo = estado.correo.trim()
        val errorNombre = if (nombre.length !in 2..60) "Debe tener entre 2 y 60 caracteres" else null
        val errorApellido = if (apellido.length !in 2..60) "Debe tener entre 2 y 60 caracteres" else null
        val errorCorreo = if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) "Correo no válido" else null

        _uiState.update {
            it.copy(
                errorNombre = errorNombre,
                errorApellido = errorApellido,
                errorCorreo = errorCorreo,
                errorGeneral = null,
                mensaje = null
            )
        }
        if (errorNombre != null || errorApellido != null || errorCorreo != null) return

        _uiState.update { it.copy(guardandoPerfil = true) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val sesion = SesionUsuario.obtenerSesion() ?: error("Sesión expirada")
                    val usuario = dao.buscarPorId(sesion.idUsuario) ?: error("Usuario no encontrado")
                    check(dao.contarCorreoEnOtrosUsuarios(correo, sesion.idUsuario) == 0) {
                        "Este correo ya está registrado en otra cuenta"
                    }
                    check(dao.actualizarDatosPerfil(sesion.idUsuario, nombre, apellido, correo) == 1) {
                        "No se pudieron guardar los cambios"
                    }
                    // Actualiza el nombre mostrado en pantallas que leen la sesión.
                    SesionUsuario.iniciarSesion(
                        usuario.copy(nombre = nombre, apellido = apellido, correo = correo)
                    )
                }
                _uiState.update { it.copy(guardandoPerfil = false, mensaje = "Perfil actualizado correctamente") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardandoPerfil = false, errorGeneral = e.message ?: "No se pudo actualizar el perfil")
                }
            }
        }
    }

    fun cambiarContrasena(actual: String, nueva: String, confirmacion: String) {
        val estado = _uiState.value
        if (estado.cargando || estado.guardandoPerfil || estado.cambiandoClave) return

        val errorActual = if (actual.isBlank()) "Ingresa tu contraseña actual" else null
        val errorNueva = when {
            nueva.length !in 8..72 -> "Debe tener entre 8 y 72 caracteres"
            !nueva.any { it.isLetter() } || !nueva.any { it.isDigit() } -> "Usa letras y números"
            nueva == actual -> "La nueva clave debe ser diferente a la anterior"
            else -> null
        }
        val errorConfirmacion = if (nueva != confirmacion) "Las contraseñas no coinciden" else null
        _uiState.update {
            it.copy(
                errorClaveActual = errorActual,
                errorClaveNueva = errorNueva,
                errorConfirmacion = errorConfirmacion,
                claveActualizada = false,
                mensaje = null,
                errorGeneral = null
            )
        }
        if (errorActual != null || errorNueva != null || errorConfirmacion != null) return

        _uiState.update { it.copy(cambiandoClave = true) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val sesion = SesionUsuario.obtenerSesion() ?: error("Sesión expirada")
                    val usuario = dao.buscarPorId(sesion.idUsuario) ?: error("Usuario no encontrado")
                    check(PasswordUtils.verificarContrasena(actual, usuario.passwordHash, usuario.passwordSalt)) {
                        "La contraseña actual es incorrecta"
                    }
                    val nuevaHash = PasswordUtils.generarHash(nueva)
                    check(
                        dao.actualizarClaveDesdePerfil(
                            idUsuario = sesion.idUsuario,
                            nuevoHash = nuevaHash.hash,
                            nuevaSal = nuevaHash.salt
                        ) == 1
                    ) { "No se pudo actualizar la contraseña" }
                }
                _uiState.update {
                    it.copy(
                        cambiandoClave = false,
                        claveActualizada = true,
                        mensaje = "Contraseña actualizada. Genera un nuevo código de recuperación."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cambiandoClave = false,
                        errorGeneral = e.message ?: "No se pudo cambiar la contraseña"
                    )
                }
            }
        }
    }

    fun limpiarConfirmacionClave() {
        _uiState.update { it.copy(claveActualizada = false) }
    }
}
