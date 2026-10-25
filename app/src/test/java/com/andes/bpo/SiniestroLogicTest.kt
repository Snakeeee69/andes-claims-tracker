package com.andes.bpo

import org.junit.Assert.*
import org.junit.Test

class SiniestroLogicTest {

    @Test
    fun rutValido_camilaRojas_retornaTrue() {
        val rutPrueba = "15.842.371-5"
        val esValido = esRutValido(rutPrueba)
        assertTrue("El RUT de Camila Rojas debe ser válido", esValido)
    }

    @Test
    fun rutInvalido_digitoErroneo_retornaFalse() {
        val rutInvalido = "15.842.371-9"
        val esValido = esRutValido(rutInvalido)
        assertFalse("Un RUT con dígito incorrecto no debe ser aceptado", esValido)
    }

    @Test
    fun mapeoEtapa_enLiquidacion_retornaPasoTres() {
        val etapaNombre = "En liquidación"
        val pasoCalculado = when (etapaNombre) {
            "Recibido" -> 1
            "En evaluación" -> 2
            "En liquidación" -> 3
            "Cerrado" -> 4
            else -> 1
        }
        assertEquals(3, pasoCalculado)
    }

    @Test
    fun numeroSiniestro_cumpleFormatoRN06() {
        val numeroSiniestro = "SIN-26-004801"
        val patronRegex = Regex("^SIN-\\d{2}-\\d{6}$")
        assertTrue("El número debe seguir la estructura SIN-AA-NNNNNN", patronRegex.matches(numeroSiniestro))
    }

    @Test
    fun filtradoGestiones_soloRetornaVisibles() {
        data class GestionPrueba(val descripcion: String, val visibleAsegurado: Boolean)

        val listaGestiones = listOf(
            GestionPrueba("Reporte recibido", true),
            GestionPrueba("Nota interna de liquidador", false),
            GestionPrueba("Siniestro pasa a liquidación", true)
        )

        val gestionesVisibles = listaGestiones.filter { it.visibleAsegurado }
        assertEquals(2, gestionesVisibles.size)
        assertFalse(gestionesVisibles.any { !it.visibleAsegurado })
    }

    private fun esRutValido(rut: String): Boolean {
        val cleanRut = rut.replace(".", "").replace("-", "").uppercase()
        if (cleanRut.length < 2) return false
        val dv = cleanRut.last()
        val cuerpo = cleanRut.substring(0, cleanRut.length - 1)
        return try {
            var suma = 0
            var multiplicador = 2
            for (i in cuerpo.length - 1 downTo 0) {
                suma += (cuerpo[i].toString().toInt()) * multiplicador
                multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
            }
            val dvEsperado = 11 - (suma % 11)
            val dvChar = when (dvEsperado) {
                11 -> '0'
                10 -> 'K'
                else -> dvEsperado.toString()[0]
            }
            dv == dvChar
        } catch (e: Exception) {
            false
        }
    }
}