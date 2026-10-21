package com.andes.bpo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    // Estados del formulario que observará la LoginScreen
    var rutState by mutableStateOf("")
        private set

    var passwordState by mutableStateOf("")
        private set

    // Funciones para actualizar los estados desde la vista
    fun onRutChanged(newRut: String) {
        rutState = newRut
    }

    fun onPasswordChanged(newPassword: String) {
        passwordState = newPassword
    }

    // Función para procesar el intento de inicio de sesión
    fun login(onSuccess: () -> Unit) {
        if (rutState.isNotBlank() && passwordState.isNotBlank()) {
            // Por ahora simula éxito directo; luego conectaremos Room aquí
            onSuccess()
        }
    }
}