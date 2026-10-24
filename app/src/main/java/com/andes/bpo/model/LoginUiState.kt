package com.andes.bpo.model

data class LoginUiState(
    val rut: String = "",
    val contrasena: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false
)