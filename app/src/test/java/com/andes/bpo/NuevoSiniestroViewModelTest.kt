package com.andes.bpo.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NuevoSiniestroViewModelTest {

    private val hoy = LocalDate.of(2026, 11, 24)

    @Test
    fun fechaAnterior_retornaTrue() {
        val resultado = NuevoSiniestroViewModel.validarFechaOcurrencia("23/11/2026", hoy)
        assertTrue(resultado)
    }

    @Test
    fun fechaDelMismoDia_retornaTrue() {
        val resultado = NuevoSiniestroViewModel.validarFechaOcurrencia("24/11/2026", hoy)
        assertTrue(resultado)
    }

    @Test
    fun fechaInexistente_retornaFalse() {
        val resultado = NuevoSiniestroViewModel.validarFechaOcurrencia("31/02/2026", hoy)
        assertFalse(resultado)
    }

    @Test
    fun fechaFutura_retornaFalse() {
        val resultado = NuevoSiniestroViewModel.validarFechaOcurrencia("25/11/2026", hoy)
        assertFalse(resultado)
    }
}
