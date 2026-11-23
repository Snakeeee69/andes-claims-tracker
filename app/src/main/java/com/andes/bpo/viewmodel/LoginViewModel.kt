package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.remote.CambioClaveObligatorioRequest
import com.andes.bpo.data.remote.LoginRequest
import com.andes.bpo.data.remote.RetrofitClient
import com.andes.bpo.model.LoginUiState
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    companion object {
        fun normalizarRutEntrada(input: String): String? {
            val limpio = input
                .replace(Regex("[^0-9kK]"), "")
                .uppercase()

            return limpio.takeIf { it.length <= 9 }
        }

        fun validarCamposLogin(rut: String, contrasena: String): String? {
            return if (rut.isBlank() || contrasena.isBlank()) {
                "El RUT y la contraseña son obligatorios"
            } else {
                null
            }
        }
    }

    fun onRutChange(input: String) {
        val limpio = normalizarRutEntrada(input) ?: return
        _uiState.update {
            it.copy(
                rut = limpio,
                rutError = null,
                error = null
            )
        }
    }

    fun onContrasenaChange(nuevaContrasena: String) {
        _uiState.update {
            it.copy(
                contrasena = nuevaContrasena,
                contrasenaError = null,
                error = null
            )
        }
    }

    fun login() {
        val rutActual = _uiState.value.rut
        val contrasenaActual = _uiState.value.contrasena

        val rutError = if (rutActual.isBlank()) "Ingresa tu RUT" else null
        val passError = if (contrasenaActual.isBlank()) "Ingresa tu contraseña" else null

        if (rutError != null || passError != null) {
            _uiState.update {
                it.copy(
                    rutError = rutError,
                    contrasenaError = passError,
                    error = validarCamposLogin(rutActual, contrasenaActual)
                )
            }
            return
        }

        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                isLoginSuccessful = false
            )
        }

        viewModelScope.launch {
            try {
                val usuarioRemoto = withContext(Dispatchers.IO) {
                    val response = RetrofitClient.api.login(
                        LoginRequest(rutActual, contrasenaActual)
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val authBody = response.body()!!
                        val usuarioLimpio = RetrofitClient.sanitizarUsuario(authBody.usuario)
                        RetrofitClient.jwtToken = authBody.token
                        RetrofitClient.usuarioLogueado = usuarioLimpio
                        usuarioLimpio
                    } else if (response.code() == 401) {
                        null
                    } else {
                        val msg = RetrofitClient.extraerMensajeError(
                            response.errorBody()?.string(),
                            "Error del servidor (${response.code()})"
                        )
                        throw Exception(msg)
                    }
                }

                if (usuarioRemoto != null) {
                    SesionUsuario.iniciarSesion(usuarioRemoto)
                    val exigeCambio = usuarioRemoto.requiereCambioClave || contrasenaActual == "123456"

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            requiereCambioClave = exigeCambio,
                            usuarioAutenticado = usuarioRemoto,
                            contrasena = "",
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "RUT o contraseña incorrectos"
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Sin conexión con el servidor (10.0.2.2:8080)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo iniciar sesión. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    fun completarCambioClaveObligatorio(
        nuevaClave: String,
        confirmarClave: String,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (nuevaClave == "123456") {
            onError("La nueva contraseña no puede ser la clave provisoria 123456")
            return
        }
        if (nuevaClave.length !in 8..72 || !nuevaClave.any { it.isLetter() } || !nuevaClave.any { it.isDigit() }) {
            onError("Debe tener entre 8 y 72 caracteres, con letras y números")
            return
        }
        if (nuevaClave != confirmarClave) {
            onError("Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            try {
                val sesion = SesionUsuario.obtenerSesion() ?: error("Sesión no activa")

                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.api.cambiarClaveObligatoria(
                        CambioClaveObligatorioRequest(
                            idUsuario = sesion.idUsuario,
                            nuevaClave = nuevaClave
                        )
                    )
                    if (!response.isSuccessful) {
                        val msg = RetrofitClient.extraerMensajeError(
                            response.errorBody()?.string(),
                            "No se pudo actualizar la contraseña (${response.code()})"
                        )
                        throw Exception(msg)
                    }
                }

                SesionUsuario.marcarClaveCambiada()
                _uiState.update { it.copy(requiereCambioClave = false) }
                onExito()
            } catch (e: IOException) {
                onError("Sin conexión con el servidor (10.0.2.2:8080)")
            } catch (e: Exception) {
                onError(e.message ?: "No se pudo actualizar la contraseña")
            }
        }
    }
}