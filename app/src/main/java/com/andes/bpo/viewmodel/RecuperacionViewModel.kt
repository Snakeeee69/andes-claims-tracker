package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.utils.CodigoRecuperacionUtils
import com.andes.bpo.utils.PasswordUtils
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class RecuperacionUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val codigoGenerado: String? = null,
    val claveRestablecida: Boolean = false
)

class RecuperacionViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application, viewModelScope).usuarioDao()

    private val _uiState = MutableStateFlow(RecuperacionUiState())
    val uiState = _uiState.asStateFlow()

    companion object {

        fun normalizarRutRecuperacion(rut: String): String =
            rut.replace(Regex("[^0-9kK]"), "")
                .uppercase(Locale.ROOT)

        // Devuelve null cuando los datos son válidos; de lo contrario,
        // devuelve el mensaje que verá el usuario.
        fun validarDatosRecuperacion(
            rutIngresado: String,
            codigoIngresado: String,
            nuevaClave: String,
            confirmarClave: String
        ): String? {
            val rut = normalizarRutRecuperacion(rutIngresado)

            return when {
                !rut.matches(Regex("[0-9]{7,8}[0-9K]")) ->
                    "Ingresa un RUT válido, como en el inicio de sesión"

                codigoIngresado.isBlank() ->
                    "Ingresa tu código de recuperación"

                nuevaClave.length !in 8..72 ->
                    "La contraseña debe tener entre 8 y 72 caracteres"

                !nuevaClave.any { it.isLetter() } || !nuevaClave.any { it.isDigit() } ->
                    "La contraseña debe contener letras y números"

                nuevaClave != confirmarClave ->
                    "Las contraseñas no coinciden"

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
                        ?: error("Debes iniciar sesión para configurar la recuperación")
                    val usuario = usuarioDao.buscarPorId(sesion.idUsuario)
                        ?: error("No se encontró la cuenta")

                    check(
                        PasswordUtils.verificarContrasena(
                            contrasenaActual,
                            usuario.passwordHash,
                            usuario.passwordSalt
                        )
                    ) { "La contraseña actual no es correcta" }

                    val nuevoCodigo = CodigoRecuperacionUtils.generarCodigo()
                    val filas = usuarioDao.guardarCodigoRecuperacion(
                        idUsuario = usuario.idUsuario,
                        codigoHash = CodigoRecuperacionUtils.obtenerHash(nuevoCodigo)
                    )
                    check(filas == 1) { "No se pudo guardar el código" }
                    nuevoCodigo
                }
                _uiState.update {
                    it.copy(cargando = false, codigoGenerado = codigo, error = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = e.message ?: "No se pudo generar el código"
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
                    require(errorDatos == null) { errorDatos ?: "Datos inválidos" }

                    val rut = normalizarRutRecuperacion(rutIngresado)

                    val usuario = usuarioDao.buscarPorRut(rut)
                        ?: error("El RUT o el código de recuperación no es correcto")
                    val hashCodigo = CodigoRecuperacionUtils.obtenerHash(codigoIngresado)
                    check(usuario.codigoRecuperacionHash == hashCodigo) {
                        "El RUT o el código de recuperación no es correcto"
                    }

                    val nueva = PasswordUtils.generarHash(nuevaClave)
                    val filas = usuarioDao.recuperarClaveConCodigo(
                        idUsuario = usuario.idUsuario,
                        codigoHash = hashCodigo,
                        nuevoHash = nueva.hash,
                        nuevaSal = nueva.salt
                    )
                    check(filas == 1) { "El código ya no es válido" }
                }
                _uiState.update {
                    it.copy(cargando = false, claveRestablecida = true, error = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.message ?: "No se pudo recuperar la contraseña")
                }
            }
        }
    }
}
