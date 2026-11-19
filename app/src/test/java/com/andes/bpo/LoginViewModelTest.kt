package com.andes.bpo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginViewModelTest {

    // PRUEBA 4
    @Test
    fun rutConFormatoSeNormalizaCorrectamente() {

        val resultado =
            LoginViewModel.normalizarRutEntrada(
                "12.345.678-k"
            )

        assertEquals("12345678K", resultado)
    }

    // PRUEBA 5
    @Test
    fun rutConMasDeNueveCaracteresEsRechazado() {

        val resultado =
            LoginViewModel.normalizarRutEntrada(
                "12345678901"
            )

        assertNull(resultado)
    }

    // PRUEBA 6
    @Test
    fun loginExigeRutYContrasena() {

        val errorSinRut =
            LoginViewModel.validarCamposLogin(
                "",
                "Clave123"
            )

        val errorSinContrasena =
            LoginViewModel.validarCamposLogin(
                "12345678K",
                ""
            )

        val sinError =
            LoginViewModel.validarCamposLogin(
                "12345678K",
                "Clave123"
            )

        assertEquals(
            "El RUT y la contraseña son obligatorios",
            errorSinRut
        )

        assertEquals(
            "El RUT y la contraseña son obligatorios",
            errorSinContrasena
        )

        assertNull(sinError)
    }
}
