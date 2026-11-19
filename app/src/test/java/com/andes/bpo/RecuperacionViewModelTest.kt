package com.andes.bpo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecuperacionViewModelTest {

    // PRUEBA 9
    @Test
    fun codigoDeRecuperacionVacio_muestraError() {

        val error = RecuperacionViewModel.validarDatosRecuperacion(
            rutIngresado = "12.345.678-K",
            codigoIngresado = "",
            nuevaClave = "ClaveNueva123",
            confirmarClave = "ClaveNueva123"
        )

        assertEquals(
            "Ingresa tu código de recuperación",
            error
        )
    }

    // PRUEBA 10
    @Test
    fun contrasenaNueva_debeSerSeguraYCoincidir() {

        val claveDebil =
            RecuperacionViewModel.validarDatosRecuperacion(
                rutIngresado = "12.345.678-K",
                codigoIngresado = "CODIGO-DE-PRUEBA",
                nuevaClave = "SoloLetras",
                confirmarClave = "SoloLetras"
            )

        assertEquals(
            "La contraseña debe contener letras y números",
            claveDebil
        )

        val clavesDiferentes =
            RecuperacionViewModel.validarDatosRecuperacion(
                rutIngresado = "12.345.678-K",
                codigoIngresado = "CODIGO-DE-PRUEBA",
                nuevaClave = "ClaveNueva123",
                confirmarClave = "OtraClave123"
            )

        assertEquals(
            "Las contraseñas no coinciden",
            clavesDiferentes
        )

        val datosCorrectos =
            RecuperacionViewModel.validarDatosRecuperacion(
                rutIngresado = "12.345.678-K",
                codigoIngresado = "CODIGO-DE-PRUEBA",
                nuevaClave = "ClaveNueva123",
                confirmarClave = "ClaveNueva123"
            )

        assertNull(datosCorrectos)
    }
}
