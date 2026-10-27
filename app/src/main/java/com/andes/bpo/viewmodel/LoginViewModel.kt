package com.andes.bpo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.model.LoginUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onRutChange(input: String) {
        val limpio = input.replace(Regex("[^0-9kK]"), "").uppercase()
        if (limpio.length <= 9) {
            _uiState.update { it.copy(rut = limpio, error = null) }
        }
    }

    fun onContrasenaChange(nuevaContrasena: String) {
        _uiState.update { it.copy(contrasena = nuevaContrasena, error = null) }
    }

    fun login() {
        val rutActual = _uiState.value.rut
        val contrasenaActual = _uiState.value.contrasena

        if (rutActual.isBlank() || contrasenaActual.isBlank()) {
            _uiState.update { it.copy(error = "El RUT y la contraseña son obligatorios") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            delay(1500)

            val rutLimpio = rutActual.replace(".", "").replace("-", "")
            val rutsValidos = listOf("158423715", "987654321")

            if (rutLimpio in rutsValidos && contrasenaActual == "123456") {
                _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "RUT o contraseña incorrectos. Usa RUT: 15.842.371-5 o 9.876.543-2 y Pass: 123456"
                    )
                }
            }
        }
    }
}