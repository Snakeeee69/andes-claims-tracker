package com.andes.bpo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecuperacionViewModelTest {

    @Test
    fun rutInvalido_muestraError() {
        val error = RecuperacionViewModel.validarDatosRecuperacion(
            rutIngresado = "123",
            codigoIngresado = "CODIGO-DE-PRUEBA",
            nuevaClave = "ClaveNueva123",
            confirmarClave = "ClaveNueva123"
        )
        assertEquals("Ingresa un RUT vÃ¡lido, como en el inicio de sesiÃ³n", error)
    }

    @Test
    fun codigoDeRecuperacionVacio_muestraError() {
        val error = RecuperacionViewModel.validarDatosRecuperacion(
            rutIngresado = "12.345.678-K",
            codigoIngresado = "",
            nuevaClave = "ClaveNueva123",
            confirmarClave = "ClaveNueva123"
        )
        assertEquals("Ingresa tu cÃ³digo de recuperaciÃ³n", error)
    }

    @Test
    fun contrasenaCorta_muestraErrorDeLongitud() {
        val error = RecuperacionViewModel.validarDatosRecuperacion(
            rutIngresado = "12.345.678-K",
            codigoIngresado = "CODIGO-DE-PRUEBA",
            nuevaClave = "Abc1",
            confirmarClave = "Abc1"
        )
        assertEquals("La contraseÃ±a debe tener entre 8 y 72 caracteres", error)
    }

    @Test
    fun contrasenaSinNumeros_muestraError() {
        val error = RecuperacionViewModel.validarDatosRecuperacion(
            rutIngresado = "12.345.678-K",
            codigoIngresado = "CODIGO-DE-PRUEBA",
            nuevaClave = "SoloLetras",
            confirmarClave = "SoloLetras"
        )
        assertEquals("La contraseÃ±a debe contener letras y nÃºmeros", error)
    }

    @Test
    fun contrasenasDiferentes_muestraError() {
        val error = RecuperacionViewModel.validarDatosRecuperacion(
            rutIngresado = "12.345.678-K",
            codigoIngresado = "CODIGO-DE-PRUEBA",
            nuevaClave = "ClaveNueva123",
            confirmarClave = "OtraClave123"
        )
        assertEquals("Las contraseÃ±as no coinciden", error)
    }

    @Test
    fun datosValidos_retornaNull() {
        val error = RecuperacionViewModel.validarDatosRecuperacion(
            rutIngresado = "12.345.678-K",
            codigoIngresado = "CODIGO-DE-PRUEBA",
            nuevaClave = "ClaveNueva123",
            confirmarClave = "ClaveNueva123"
        )
        assertNull(error)
    }
}
