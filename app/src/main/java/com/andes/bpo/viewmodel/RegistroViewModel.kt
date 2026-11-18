
package com.andes.bpo.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.UsuarioEntity
import com.andes.bpo.utils.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class RegistroUiState(
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmacion: String = "",
    val errores: Map<String, String> = emptyMap(),
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val registrado: Boolean = false
)

class RegistroViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(
        application,
        viewModelScope
    ).usuarioDao()

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState = _uiState.asStateFlow()

    fun cambiarCampo(campo: String, valor: String) {
        _uiState.update { actual ->
            val nuevo = when (campo) {
                "rut" -> actual.copy(
                    rut = valor.filter {
                        it.isDigit() || it == 'k' || it == 'K'
                    }.uppercase().take(9)
                )
                "nombre" -> actual.copy(nombre = valor)
                "apellido" -> actual.copy(apellido = valor)
                "correo" -> actual.copy(correo = valor)
                "contrasena" -> actual.copy(contrasena = valor)
                "confirmacion" -> actual.copy(confirmacion = valor)
                else -> actual
            }

            nuevo.copy(
                errores = nuevo.errores - campo,
                errorGeneral = null
            )
        }
    }

    private fun rutValido(rut: String): Boolean {
        if (rut.length !in 8..9) return false

        val cuerpo = rut.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return false

        var suma = 0
        var multiplicador = 2

        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador++
            if (multiplicador > 7) multiplicador = 2
        }

        val resultado = 11 - (suma % 11)
        val dv = when (resultado) {
            11 -> '0'
            10 -> 'K'
            else -> resultado.toString()[0]
        }

        return rut.last() == dv
    }

    fun registrar() {
        val datos = _uiState.value

        if (datos.guardando || datos.registrado) return

        val errores = mutableMapOf<String, String>()

        if (!rutValido(datos.rut)) {
            errores["rut"] = "Ingresa un RUT válido"
        }

        if (datos.nombre.trim().length < 2) {
            errores["nombre"] = "Ingresa tu nombre"
        }

        if (datos.apellido.trim().length < 2) {
            errores["apellido"] = "Ingresa tu apellido"
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(
                datos.correo.trim()
            ).matches()
        ) {
            errores["correo"] = "Ingresa un correo válido"
        }

        if (
            datos.contrasena.length < 8 ||
            !datos.contrasena.any { it.isLetter() } ||
            !datos.contrasena.any { it.isDigit() }
        ) {
            errores["contrasena"] =
                "Mínimo 8 caracteres, con letras y números"
        }

        if (datos.confirmacion != datos.contrasena) {
            errores["confirmacion"] =
                "Las contraseñas no coinciden"
        }

        if (errores.isNotEmpty()) {
            _uiState.update {
                it.copy(errores = errores)
            }
            return
        }

        _uiState.update {
            it.copy(guardando = true, errorGeneral = null)
        }

        viewModelScope.launch {
            try {
                val existe = withContext(Dispatchers.IO) {
                    usuarioDao.buscarPorRut(datos.rut) != null
                }

                if (existe) {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            errores = mapOf(
                                "rut" to "Este RUT ya está registrado"
                            )
                        )
                    }
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    val password = PasswordUtils.generarHash(
                        datos.contrasena
                    )

                    val usuario = UsuarioEntity(
                        rut = datos.rut,
                        nombre = datos.nombre.trim(),
                        apellido = datos.apellido.trim(),
                        correo = datos.correo.trim()
                            .lowercase(Locale.ROOT),
                        passwordHash = password.hash,
                        passwordSalt = password.salt,
                        rol = "ASEGURADO"
                    )

                    usuarioDao.registrarUsuario(usuario)
                }

                _uiState.update {
                    it.copy(
                        guardando = false,
                        registrado = true,
                        contrasena = "",
                        confirmacion = ""
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = "No se pudo registrar la cuenta. " +
                                "Comprueba los datos e inténtalo nuevamente."
                    )
                }
            }
        }
    }
}
