
package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.model.LoginUiState
import com.andes.bpo.utils.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.andes.bpo.utils.SesionUsuario

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(
        application,
        viewModelScope
    ).usuarioDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    companion object {

        fun normalizarRutEntrada(input: String): String? {
            val limpio = input
                .replace(Regex("[^0-9kK]"), "")
                .uppercase()

            return limpio.takeIf {
                it.length <= 9
            }
        }

        fun validarCamposLogin(
            rut: String,
            contrasena: String
        ): String? {

            return if (
                rut.isBlank() ||
                contrasena.isBlank()
            ) {
                "El RUT y la contraseña son obligatorios"
            } else {
                null
            }
        }
    }


    fun onRutChange(input: String) {
        val limpio = normalizarRutEntrada(input)
            ?: return

        _uiState.update {
            it.copy(
                rut = limpio,
                error = null
            )
        }
    }

    fun onContrasenaChange(nuevaContrasena: String) {
        _uiState.update {
            it.copy(
                contrasena = nuevaContrasena,
                error = null
            )
        }
    }

    fun login() {
        val rutActual = _uiState.value.rut
        val contrasenaActual = _uiState.value.contrasena

        val errorCampos = validarCamposLogin(
            rutActual,
            contrasenaActual
        )

        if (errorCampos != null) {
            _uiState.update {
                it.copy(error = errorCampos)
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
                val usuarioValido = withContext(Dispatchers.IO) {

                    val usuario = usuarioDao.buscarPorRut(rutActual)

                    if (usuario != null &&
                        PasswordUtils.verificarContrasena(
                            contrasenaActual,
                            usuario.passwordHash,
                            usuario.passwordSalt
                        )
                    ) {
                        usuario
                    } else {
                        null
                    }
                }

                if (usuarioValido != null) {

                    SesionUsuario.iniciarSesion(usuarioValido)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
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

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudo iniciar sesión. Inténtalo nuevamente."
                    )
                }
            }
        }
    }
}
