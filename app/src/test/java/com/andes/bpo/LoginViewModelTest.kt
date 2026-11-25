package com.andes.bpo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun rutConFormatoSeNormalizaCorrectamente() {
        val resultado = LoginViewModel.normalizarRutEntrada("12.345.678-k")
        assertEquals("12345678K", resultado)
    }

    @Test
    fun rutConMasDeNueveCaracteresEsRechazado() {
        val resultado = LoginViewModel.normalizarRutEntrada("12345678901")
        assertNull(resultado)
    }

    @Test
    fun loginSinRutRetornaError() {
        val error = LoginViewModel.validarCamposLogin("", "Clave123")
        assertEquals("El RUT y la contraseÃ±a son obligatorios", error)
    }

    @Test
    fun loginSinContrasenaRetornaError() {
        val error = LoginViewModel.validarCamposLogin("12345678K", "")
        assertEquals("El RUT y la contraseÃ±a son obligatorios", error)
    }

    @Test
    fun loginConDatosCompletosRetornaNull() {
        val error = LoginViewModel.validarCamposLogin("12345678K", "Clave123")
        assertNull(error)
    }
}
