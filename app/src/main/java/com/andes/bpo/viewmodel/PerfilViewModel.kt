package com.andes.bpo.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.entity.UsuarioEntity
import com.andes.bpo.data.remote.CambioClaveObligatorioRequest
import com.andes.bpo.data.remote.LoginRequest
import com.andes.bpo.data.remote.RetrofitClient
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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
        val sesion = SesionUsuario.obtenerSesion()
        val remoto = RetrofitClient.usuarioLogueado

        if (sesion == null && remoto == null) {
            _uiState.update { it.copy(cargando = false, errorGeneral = "Debes iniciar sesiÃ³n") }
        } else {
            _uiState.update {
                it.copy(
                    cargando = false,
                    nombre = remoto?.nombre ?: sesion?.nombre.orEmpty(),
                    apellido = remoto?.apellido ?: sesion?.apellido.orEmpty(),
                    correo = remoto?.correo.orEmpty(),
                    rut = remoto?.rut ?: sesion?.rut.orEmpty(),
                    errorGeneral = null
                )
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
        val errorCorreo = if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) "Correo no vÃ¡lido" else null

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
                    val sesion = SesionUsuario.obtenerSesion() ?: error("SesiÃ³n expirada")
                    val base = RetrofitClient.usuarioLogueado ?: UsuarioEntity(
                        idUsuario = sesion.idUsuario,
                        rut = sesion.rut,
                        nombre = nombre,
                        apellido = apellido,
                        correo = correo,
                        passwordHash = "",
                        passwordSalt = "",
                        rol = sesion.rol
                    )
                    val actualizado = base.copy(
                        nombre = nombre,
                        apellido = apellido,
                        correo = correo
                    )

                    val response = RetrofitClient.api.actualizarUsuario(sesion.idUsuario, actualizado)
                    if (!response.isSuccessful) {
                        val msg = RetrofitClient.extraerMensajeError(
                            response.errorBody()?.string(),
                            "No se pudo actualizar en el servidor (${response.code()})"
                        )
                        throw Exception(msg)
                    }

                    val remoto = response.body()?.let { RetrofitClient.sanitizarUsuario(it) } ?: actualizado
                    RetrofitClient.usuarioLogueado = remoto
                    SesionUsuario.iniciarSesion(remoto)
                }
                _uiState.update { it.copy(guardandoPerfil = false, mensaje = "Perfil actualizado correctamente") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(guardandoPerfil = false, errorGeneral = "Sin conexiÃ³n con el servidor (10.0.2.2:8080)")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardandoPerfil = false, errorGeneral = e.message ?: "No se pudo actualizar el perfil")
                }
            }
        }
    }

    fun cambiarContrasenaDirecta(
        actual: String,
        nueva: String,
        confirmacion: String
    ) {
        val estado = _uiState.value
        if (estado.cargando || estado.guardandoPerfil || estado.cambiandoClave) return

        val errorActual = if (actual.isBlank()) "Ingresa tu contraseÃ±a actual" else null
        val errorNueva = when {
            nueva.length !in 8..72 -> "Debe tener entre 8 y 72 caracteres"
            !nueva.any { it.isLetter() } || !nueva.any { it.isDigit() } -> "Usa letras y nÃºmeros"
            nueva == actual -> "La nueva clave debe ser diferente a la anterior"
            else -> null
        }
        val errorConfirmacion = if (nueva != confirmacion) "Las contraseÃ±as no coinciden" else null

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
                    val sesion = SesionUsuario.obtenerSesion() ?: error("SesiÃ³n expirada")

                    val loginResp = RetrofitClient.api.login(LoginRequest(sesion.rut, actual))
                    if (!loginResp.isSuccessful || loginResp.body() == null) {
                        throw Exception("La contraseÃ±a actual es incorrecta")
                    }
                    RetrofitClient.jwtToken = loginResp.body()!!.token
                    RetrofitClient.usuarioLogueado = RetrofitClient.sanitizarUsuario(loginResp.body()!!.usuario)

                    val cambioResp = RetrofitClient.api.cambiarClaveObligatoria(
                        CambioClaveObligatorioRequest(
                            idUsuario = sesion.idUsuario,
                            nuevaClave = nueva
                        )
                    )
                    if (!cambioResp.isSuccessful) {
                        val msg = RetrofitClient.extraerMensajeError(
                            cambioResp.errorBody()?.string(),
                            "No se pudo actualizar la contraseÃ±a (${cambioResp.code()})"
                        )
                        throw Exception(msg)
                    }
                }

                _uiState.update {
                    it.copy(
                        cambiandoClave = false,
                        claveActualizada = true,
                        mensaje = "ContraseÃ±a actualizada correctamente"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(cambiandoClave = false, errorGeneral = "Sin conexiÃ³n con el servidor (10.0.2.2:8080)")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cambiandoClave = false,
                        errorGeneral = e.message ?: "No se pudo cambiar la contraseÃ±a"
                    )
                }
            }
        }
    }

    fun limpiarConfirmacionClave() {
        _uiState.update { it.copy(claveActualizada = false) }
    }
}
