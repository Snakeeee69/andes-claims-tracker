
package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.data.repository.SiniestroRepository
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.andes.bpo.data.local.entity.AreaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
class NuevoSiniestroViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(
        application,
        viewModelScope
    )

    private val repository = SiniestroRepository(
        db.siniestroDao()
    )

    private val areaDao = AppDatabase.getDatabase(
        application,
        viewModelScope
    ).areaDao()

    private val _areas = MutableStateFlow<List<AreaEntity>>(
        emptyList()
    )

    val areas: StateFlow<List<AreaEntity>> =
        _areas.asStateFlow()

    init {
        viewModelScope.launch {
            areaDao.asegurarAreasIniciales()

            areaDao.obtenerAreas().collect { lista ->
                _areas.value = lista
            }
        }
    }


    fun esFechaOcurrenciaValida(fecha: String): Boolean {
        return validarFechaOcurrencia(fecha)
    }

    companion object {

        fun validarFechaOcurrencia(
            fecha: String,
            hoy: LocalDate = LocalDate.now()
        ): Boolean {

            val valor = fecha.trim()

            if (!Regex("""\d{2}/\d{2}/\d{4}""").matches(valor)) {
                return false
            }

            return try {
                val formato = DateTimeFormatter
                    .ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT)

                val fechaIngresada = LocalDate.parse(
                    valor,
                    formato
                )

                !fechaIngresada.isAfter(hoy)

            } catch (e: DateTimeParseException) {
                false
            }
        }
    }


    fun guardarSiniestro(
        siniestro: SiniestroEntity,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val usuario = SesionUsuario.obtenerSesion()

                if (usuario == null) {
                    onError(
                        "No hay una sesión activa. " +
                                "Inicia sesión nuevamente."
                    )
                    return@launch
                }

                val siniestroConUsuario = siniestro.copy(
                    idAsegurado = usuario.idUsuario
                )

                withContext(Dispatchers.IO) {
                    repository.registrarNuevoSiniestro(
                        siniestroConUsuario
                    )
                }

                onExito()

            } catch (e: Exception) {
                onError(
                    "No se pudo guardar el siniestro. " +
                            "Inténtalo nuevamente."
                )
            }
        }
    }
}
