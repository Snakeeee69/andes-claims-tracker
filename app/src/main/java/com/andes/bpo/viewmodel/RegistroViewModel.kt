package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.entity.UsuarioEntity
import com.andes.bpo.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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
    val aceptaTerminos: Boolean = false,
    val errores: Map<String, String> = emptyMap(),
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val registrado: Boolean = false
)

class RegistroViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        val ASEGURADORAS = listOf(
            "HDI Seguros",
            "BCI Seguros",
            "Mapfre",
            "Seguros SURA",
            "Liberty Seguros",
            "Chilena Consolidada",
            "Consorcio",
            "MetLife",
            "Bupa Chile",
            "Banmédica",
            "Colmena"
        )

        val REGIONES_CHILE = listOf(
            "Arica y Parinacota",
            "Tarapacá",
            "Antofagasta",
            "Atacama",
            "Coquimbo",
            "Valparaíso",
            "Metropolitana de Santiago",
            "Libertador General Bernardo O'Higgins",
            "Maule",
            "Ñuble",
            "Biobío",
            "La Araucanía",
            "Los Ríos",
            "Los Lagos",
            "Aysén del General Carlos Ibáñez del Campo",
            "Magallanes y de la Antártica Chilena"
        )

        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState = _uiState.asStateFlow()

    fun cambiarTipoAsegurado(tipo: String) {
        _uiState.update {
            it.copy(
                tipoAsegurado = tipo,
                errores = it.errores - "nombre" - "apellido",
                errorGeneral = null
            )
        }
    }

    fun cambiarAceptacionTerminos(acepta: Boolean) {
        _uiState.update {
            it.copy(
                aceptaTerminos = acepta,
                errores = if (acepta) it.errores - "terminos" else it.errores,
                errorGeneral = null
            )
        }
    }

    fun cambiarCampo(campo: String, valor: String) {
        _uiState.update { actual ->
            val erroresActualizados = actual.errores - campo

            when (campo) {
                "rut" -> actual.copy(
                    rut = limpiarRutSinFormato(valor),
                    errores = erroresActualizados,
                    errorGeneral = null
                )
                "nombre" -> actual.copy(nombre = valor, errores = erroresActualizados, errorGeneral = null)
                "apellido" -> actual.copy(apellido = valor, errores = erroresActualizados, errorGeneral = null)
                "correo" -> actual.copy(correo = valor.trim(), errores = erroresActualizados, errorGeneral = null)
                "aseguradora" -> actual.copy(aseguradora = valor, errores = erroresActualizados, errorGeneral = null)
                "region" -> actual.copy(region = valor, errores = erroresActualizados, errorGeneral = null)
                "direccion" -> actual.copy(direccion = valor, errores = erroresActualizados, errorGeneral = null)
                "contrasena" -> actual.copy(contrasena = valor, errores = erroresActualizados, errorGeneral = null)
                "confirmacion" -> actual.copy(confirmacion = valor, errores = erroresActualizados, errorGeneral = null)
                else -> actual
            }
        }
    }

    fun registrar() {
        val s = _uiState.value
        if (s.guardando) return

        val esEmpresa = s.tipoAsegurado == "ASEGURADO_EMPRESA"
        val nuevosErrores = mutableMapOf<String, String>()
        val rutSinFormato = limpiarRutSinFormato(s.rut)

        if (rutSinFormato.isBlank()) {
            nuevosErrores["rut"] = "Ingresa tu RUT"
        } else if (!esRutValido(rutSinFormato)) {
            nuevosErrores["rut"] = "RUT inválido (ej: 12.345.678-K)"
        }

        if (s.nombre.trim().length < 2) {
            nuevosErrores["nombre"] = if (esEmpresa) "Ingresa la razón social" else "Ingresa tu nombre"
        }

        if (s.apellido.trim().length < 2) {
            nuevosErrores["apellido"] = if (esEmpresa) "Ingresa el tipo de sociedad" else "Ingresa tu apellido"
        }

        if (s.correo.isBlank()) {
            nuevosErrores["correo"] = "Ingresa tu correo electrónico"
        } else if (!EMAIL_REGEX.matches(s.correo.trim())) {
            nuevosErrores["correo"] = "Formato de correo inválido"
        }

        if (s.aseguradora.isBlank()) {
            nuevosErrores["aseguradora"] = "Selecciona una aseguradora"
        }

        if (s.region.isBlank()) {
            nuevosErrores["region"] = "Selecciona una región"
        }

        if (s.contrasena == "123456" || s.contrasena.length < 6) {
            nuevosErrores["contrasena"] = "La contraseña debe tener al menos 6 caracteres y no ser 123456"
        }

        if (s.confirmacion != s.contrasena) {
            nuevosErrores["confirmacion"] = "Las contraseñas no coinciden"
        }

        if (!s.aceptaTerminos) {
            nuevosErrores["terminos"] = "Debes aceptar los Términos y Condiciones"
        }

        if (nuevosErrores.isNotEmpty()) {
            _uiState.update { it.copy(errores = nuevosErrores, errorGeneral = null) }
            return
        }

        _uiState.update { it.copy(guardando = true, errorGeneral = null) }

        viewModelScope.launch {
            try {
                val correoLimpio = s.correo.trim().lowercase()
                val direccionCompleta = listOf(s.direccion.trim(), s.region.trim())
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                val payload = UsuarioEntity(
                    rut = rutSinFormato,
                    nombre = s.nombre.trim(),
                    apellido = s.apellido.trim(),
                    correo = correoLimpio,
                    passwordHash = s.contrasena,
                    passwordSalt = "",
                    rol = s.tipoAsegurado,
                    direccion = direccionCompleta.ifBlank { null },
                    aseguradora = s.aseguradora,
                    equipo = null,
                    tipoLiquidador = null,
                    idArea = null,
                    requiereCambioClave = false,
                    codigoRecuperacionHash = null
                )

                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.api.registrarUsuario(payload)
                    if (!response.isSuccessful) {
                        val msg = RetrofitClient.extraerMensajeError(
                            response.errorBody()?.string(),
                            "No se pudo registrar en el servidor (${response.code()})"
                        )
                        throw Exception(msg)
                    }
                }

                _uiState.update {
                    it.copy(
                        guardando = false,
                        registrado = true,
                        errores = emptyMap(),
                        errorGeneral = null
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = "Sin conexión con el servidor (10.0.2.2:8080)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = e.message ?: "No se pudo completar el registro"
                    )
                }
            }
        }
    }

    private fun limpiarRutSinFormato(rutInput: String): String {
        return rutInput
            .replace(Regex("[^0-9kK]"), "")
            .uppercase()
            .take(9)
    }

    private fun esRutValido(rutLimpio: String): Boolean {
        if (rutLimpio.length !in 7..9) return false

        val cuerpo = rutLimpio.dropLast(1)
        val dvIngresado = rutLimpio.last()

        if (!cuerpo.all { it.isDigit() }) return false
        if (dvIngresado !in '0'..'9' && dvIngresado != 'K') return false

        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += cuerpo[i].digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = 11 - (suma % 11)
        val dvEsperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }

        return dvIngresado == dvEsperado
    }
}