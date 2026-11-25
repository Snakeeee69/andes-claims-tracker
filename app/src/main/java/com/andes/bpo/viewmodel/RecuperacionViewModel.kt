package com.andes.bpo.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.remote.ConfirmarClaveRequest
import com.andes.bpo.data.remote.LoginRequest
import com.andes.bpo.data.remote.RecuperarRequest
import com.andes.bpo.data.remote.RetrofitClient
import com.andes.bpo.utils.CodigoRecuperacionUtils
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

data class RecuperacionUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val codigoGenerado: String? = null,
    val codigoEnviadoCorreo: Boolean = false,
    val mensajeEnvio: String? = null,
    val claveRestablecida: Boolean = false
)

class RecuperacionViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RecuperacionUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        fun normalizarRutRecuperacion(rut: String): String =
            rut.replace(Regex("[^0-9kK]"), "")
                .uppercase(Locale.ROOT)

        fun validarDatosRecuperacion(
            rutIngresado: String,
            codigoIngresado: String,
            nuevaClave: String,
            confirmarClave: String
        ): String? {
            val rut = normalizarRutRecuperacion(rutIngresado)

            return when {
                !rut.matches(Regex("[0-9]{7,8}[0-9K]")) ->
                    "Ingresa un RUT vÃ¡lido, como en el inicio de sesiÃ³n"

                codigoIngresado.isBlank() ->
                    "Ingresa tu cÃ³digo de recuperaciÃ³n"

                nuevaClave.length !in 8..72 ->
                    "La contraseÃ±a debe tener entre 8 y 72 caracteres"

                !nuevaClave.any { it.isLetter() } || !nuevaClave.any { it.isDigit() } ->
                    "La contraseÃ±a debe contener letras y nÃºmeros"

                nuevaClave != confirmarClave ->
                    "Las contraseÃ±as no coinciden"

                else -> null
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    fun generarCodigo(contrasenaActual: String) {
        if (_uiState.value.cargando) return
        _uiState.update { it.copy(cargando = true, error = null, codigoGenerado = null) }

        viewModelScope.launch {
            try {
                val codigo = withContext(Dispatchers.IO) {
                    val sesion = SesionUsuario.obtenerSesion()
                        ?: error("Debes iniciar sesiÃ³n para configurar la recuperaciÃ³n")

                    val loginResp = RetrofitClient.api.login(
                        LoginRequest(sesion.rut, contrasenaActual)
                    )
                    if (!loginResp.isSuccessful || loginResp.body() == null) {
                        error("La contraseÃ±a actual no es correcta")
                    }

                    val authBody = loginResp.body()!!
                    RetrofitClient.jwtToken = authBody.token

                    val nuevoCodigo = CodigoRecuperacionUtils.generarCodigo()
                    val codigoHash = CodigoRecuperacionUtils.obtenerHash(nuevoCodigo)

                    val usuarioSanitizado = RetrofitClient.sanitizarUsuario(authBody.usuario)
                    val usuarioActualizado = usuarioSanitizado.copy(
                        codigoRecuperacionHash = codigoHash
                    )
                    RetrofitClient.usuarioLogueado = usuarioActualizado

                    val updateResp = RetrofitClient.api.actualizarUsuario(
                        sesion.idUsuario,
                        usuarioActualizado
                    )
                    if (!updateResp.isSuccessful) {
                        val msg = RetrofitClient.extraerMensajeError(
                            updateResp.errorBody()?.string(),
                            "No se pudo guardar el cÃ³digo en el servidor"
                        )
                        throw Exception(msg)
                    }

                    nuevoCodigo
                }
                _uiState.update {
                    it.copy(cargando = false, codigoGenerado = codigo, error = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "Sin conexiÃ³n con el servidor (10.0.2.2:8080)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = e.message ?: "No se pudo generar el cÃ³digo"
                    )
                }
            }
        }
    }

    fun solicitarCodigoCorreo(rutIngresado: String, correoIngresado: String) {
        if (_uiState.value.cargando) return

        val rut = normalizarRutRecuperacion(rutIngresado)
        val correo = correoIngresado.trim()

        if (!rut.matches(Regex("[0-9]{7,8}[0-9K]"))) {
            _uiState.update { it.copy(error = "Ingresa un RUT vÃ¡lido") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _uiState.update { it.copy(error = "Ingresa un correo electrÃ³nico vÃ¡lido") }
            return
        }

        _uiState.update {
            it.copy(
                cargando = true,
                error = null,
                codigoEnviadoCorreo = false,
                mensajeEnvio = null
            )
        }

        viewModelScope.launch {
            try {
                val mensaje = withContext(Dispatchers.IO) {
                    val response = RetrofitClient.api.solicitarCodigo(
                        RecuperarRequest(rut = rut, correo = correo)
                    )
                    if (response.isSuccessful) {
                        response.body()?.mensaje ?: "CÃ³digo de 6 dÃ­gitos enviado a $correo"
                    } else {
                        val msg = RetrofitClient.extraerMensajeError(
                            response.errorBody()?.string(),
                            "El RUT y el correo no coinciden"
                        )
                        throw Exception(msg)
                    }
                }
                _uiState.update {
                    it.copy(
                        cargando = false,
                        codigoEnviadoCorreo = true,
                        mensajeEnvio = mensaje,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "Sin conexiÃ³n con el servidor para enviar el correo"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = e.message ?: "No se pudo enviar el cÃ³digo de verificaciÃ³n"
                    )
                }
            }
        }
    }

    fun restablecerClave(
        rutIngresado: String,
        codigoIngresado: String,
        nuevaClave: String,
        confirmarClave: String
    ) {
        if (_uiState.value.cargando) return
        _uiState.update { it.copy(cargando = true, error = null, claveRestablecida = false) }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val errorDatos = validarDatosRecuperacion(
                        rutIngresado = rutIngresado,
                        codigoIngresado = codigoIngresado,
                        nuevaClave = nuevaClave,
                        confirmarClave = confirmarClave
                    )
                    require(errorDatos == null) { errorDatos ?: "Datos invÃ¡lidos" }

                    val rut = normalizarRutRecuperacion(rutIngresado)
                    val response = RetrofitClient.api.confirmarNuevaClave(
                        ConfirmarClaveRequest(
                            rut = rut,
                            codigo = codigoIngresado.trim(),
                            nuevaClave = nuevaClave
                        )
                    )
                    if (!response.isSuccessful) {
                        val msg = RetrofitClient.extraerMensajeError(
                            response.errorBody()?.string(),
                            "CÃ³digo de recuperaciÃ³n invÃ¡lido o expirado"
                        )
                        throw Exception(msg)
                    }
                }
                _uiState.update {
                    it.copy(cargando = false, claveRestablecida = true, error = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "Sin conexiÃ³n con el servidor (10.0.2.2:8080)"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = e.message ?: "No se pudo recuperar la contraseÃ±a"
                    )
                }
            }
        }
    }
}
