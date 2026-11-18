
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
