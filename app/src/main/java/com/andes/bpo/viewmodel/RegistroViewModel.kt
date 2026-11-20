package com.andes.bpo.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.UsuarioEntity
import com.andes.bpo.data.remote.RetrofitClient
import com.andes.bpo.utils.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class RegistroUiState(
    val tipoAsegurado: String = "ASEGURADO_NATURAL",
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val aseguradora: String = "",
    val region: String = "",
    val direccion: String = "",
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

    companion object {
        val ASEGURADORAS = listOf(
            "Seguros Falabella",
            "BCI Seguros",
            "Consorcio",
            "HDI Seguros",
            "Chilena Consolidada (Zurich)",
            "SURA",
            "Mapfre",
            "Liberty Seguros",
            "MetLife"
        )

        val REGIONES_CHILE = listOf(
            "Región de Arica y Parinacota",
            "Región de Tarapacá",
            "Región de Antofagasta",
            "Región de Atacama",
            "Región de Coquimbo",
            "Región de Valparaíso",
            "Región Metropolitana de Santiago",
            "Región del Libertador General Bernardo O'Higgins",
            "Región del Maule",
            "Región de Ñuble",
            "Región del Biobío",
            "Región de La Araucanía",
            "Región de Los Ríos",
            "Región de Los Lagos",
            "Región de Aysén del General Carlos Ibáñez del Campo",
            "Región de Magallanes y de la Antártica Chilena"
        )

        fun esContrasenaRegistroValida(contrasena: String): Boolean {
            return contrasena.length >= 8 &&
                    contrasena.any { it.isLetter() } &&
                    contrasena.any { it.isDigit() }
        }

        fun coincidenContrasenasRegistro(
            contrasena: String,
            confirmacion: String
        ): Boolean {
            return contrasena == confirmacion
        }
    }

    fun cambiarTipoAsegurado(nuevoTipo: String) {
        _uiState.update { actual ->
            actual.copy(
                tipoAsegurado = nuevoTipo,
                errores = actual.errores - "nombre" - "apellido",
                errorGeneral = null
            )
        }
    }

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
                "aseguradora" -> actual.copy(aseguradora = valor)
                "region" -> actual.copy(region = valor)
                "direccion" -> actual.copy(direccion = valor)
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

        val esEmpresa = datos.tipoAsegurado == "ASEGURADO_EMPRESA"
        val errores = mutableMapOf<String, String>()

        if (!rutValido(datos.rut)) {
            errores["rut"] = "Ingresa un RUT válido"
        }

        if (datos.nombre.trim().length < 2) {
            errores["nombre"] = if (esEmpresa) "Ingresa la razón social" else "Ingresa tu nombre"
        }

        if (!esEmpresa && datos.apellido.trim().length < 2) {
            errores["apellido"] = "Ingresa tu apellido"
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(datos.correo.trim()).matches()) {
            errores["correo"] = "Ingresa un correo válido"
        }

        if (datos.aseguradora.isBlank()) {
            errores["aseguradora"] = "Selecciona tu aseguradora"
        }

        if (datos.region.isBlank()) {
            errores["region"] = "Selecciona tu región"
        }

        if (!esContrasenaRegistroValida(datos.contrasena)) {
            errores["contrasena"] = "Mínimo 8 caracteres, con letras y números"
        }

        if (!coincidenContrasenasRegistro(datos.contrasena, datos.confirmacion)) {
            errores["confirmacion"] = "Las contraseñas no coinciden"
        }

        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(errores = errores) }
            return
        }

        _uiState.update {
            it.copy(guardando = true, errorGeneral = null)
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val apellidoFinal = if (esEmpresa) {
                        datos.apellido.trim().ifBlank { "Empresa" }
                    } else {
                        datos.apellido.trim()
                    }

                    val direccionCompleta = if (datos.direccion.trim().isNotBlank()) {
                        "${datos.direccion.trim()}, ${datos.region}"
                    } else {
                        datos.region
                    }

                    val usuarioParaBackend = UsuarioEntity(
                        rut = datos.rut,
                        nombre = datos.nombre.trim(),
                        apellido = apellidoFinal,
                        correo = datos.correo.trim().lowercase(Locale.ROOT),
                        passwordHash = datos.contrasena,
                        passwordSalt = "",
                        rol = datos.tipoAsegurado,
                        direccion = direccionCompleta,
                        aseguradora = datos.aseguradora
                    )

                    val response = RetrofitClient.api.registrarUsuario(usuarioParaBackend)

                    if (response.isSuccessful) {
                        val passwordLocal = PasswordUtils.generarHash(datos.contrasena)
                        val cuerpoRemoto = response.body()
                        val usuarioLocal = (cuerpoRemoto ?: usuarioParaBackend).copy(
                            passwordHash = passwordLocal.hash,
                            passwordSalt = passwordLocal.salt,
                            rol = cuerpoRemoto?.rol ?: datos.tipoAsegurado,
                            direccion = cuerpoRemoto?.direccion ?: direccionCompleta,
                            aseguradora = cuerpoRemoto?.aseguradora ?: datos.aseguradora
                        )
                        runCatching {
                            usuarioDao.registrarUsuario(usuarioLocal)
                        }
                    } else if (response.code() == 400 || response.code() == 409) {
                        throw IllegalArgumentException("Este RUT o correo ya está registrado en el servidor")
                    } else {
                        throw Exception("Error del servidor (${response.code()})")
                    }
                }

                _uiState.update {
                    it.copy(
                        guardando = false,
                        registrado = true,
                        contrasena = "",
                        confirmacion = ""
                    )
                }

            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errores = mapOf(
                            "rut" to (e.message ?: "Este RUT ya está registrado")
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = e.message ?: "No se pudo conectar con el servidor."
                    )
                }
            }
        }
    }
}