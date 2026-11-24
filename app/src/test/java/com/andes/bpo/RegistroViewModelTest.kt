package com.andes.bpo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroViewModelTest {

    @Test
    fun aseguradorasNoContienenDuplicadosNiNombresVacios() {
        val aseguradoras = RegistroViewModel.ASEGURADORAS

        assertFalse(aseguradoras.isEmpty())
        assertTrue(aseguradoras.all { it.isNotBlank() })
        assertEquals(aseguradoras.size, aseguradoras.toSet().size)
    }

    @Test
    fun regionesContienenLasDieciseisRegionesSinDuplicados() {
        val regiones = RegistroViewModel.REGIONES_CHILE

        assertEquals(16, regiones.size)
        assertEquals(regiones.size, regiones.toSet().size)
        assertTrue("Metropolitana de Santiago" in regiones)
    }
}
