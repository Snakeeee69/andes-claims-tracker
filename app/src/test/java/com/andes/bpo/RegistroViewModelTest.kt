package com.andes.bpo.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroViewModelTest {

    // PRUEBA 7
    @Test
    fun contrasenaDebeCumplirRequisitos() {

        val contrasenaCorrecta =
            RegistroViewModel.esContrasenaRegistroValida(
                "Clave123"
            )

        val contrasenaSinNumeros =
            RegistroViewModel.esContrasenaRegistroValida(
                "ClaveSegura"
            )

        val contrasenaMuyCorta =
            RegistroViewModel.esContrasenaRegistroValida(
                "Ab12"
            )

        assertTrue(contrasenaCorrecta)
        assertFalse(contrasenaSinNumeros)
        assertFalse(contrasenaMuyCorta)
    }

    // PRUEBA 8
    @Test
    fun confirmacionDebeCoincidirConContrasena() {

        val confirmacionCorrecta =
            RegistroViewModel.coincidenContrasenasRegistro(
                "Clave123",
                "Clave123"
            )

        val confirmacionIncorrecta =
            RegistroViewModel.coincidenContrasenasRegistro(
                "Clave123",
                "OtraClave123"
            )

        assertTrue(confirmacionCorrecta)
        assertFalse(confirmacionIncorrecta)
    }
}
