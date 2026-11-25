package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DocumentosLiquidadorUiState(
    val cargando: Boolean = true,
    val numeroSiniestro: String = "",
    val documentos: List<DocumentoEntity> = emptyList(),
    val error: String? = null
)

class DocumentosLiquidadorViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application, viewModelScope).siniestroDao()
    private val _uiState = MutableStateFlow(DocumentosLiquidadorUiState())
    val uiState = _uiState.asStateFlow()
    private var cargaJob: Job? = null

    fun cargarDocumentos(idSiniestro: Int) {
        cargaJob?.cancel()
        _uiState.value = DocumentosLiquidadorUiState()

        cargaJob = viewModelScope.launch {
            try {
                val sesion = SesionUsuario.obtenerSesion()
                    ?: error("Debes iniciar sesiÃ³n")

                check(sesion.rol.startsWith("LIQUIDADOR")) {
                    "Solo un liquidador puede consultar esta bandeja"
                }

                combine(
                    dao.getSiniestroPorId(idSiniestro),
                    dao.getDocumentosParaLiquidador(idSiniestro)
                ) { siniestro, documentos ->
                    siniestro to documentos
                }.collectLatest { (siniestro, documentos) ->
                    check(siniestro != null) { "Siniestro no encontrado" }
                    check(siniestro.idLiquidador == sesion.idUsuario) {
                        "Este siniestro no estÃ¡ asignado a ti"
                    }
                    _uiState.value = DocumentosLiquidadorUiState(
                        cargando = false,
                        numeroSiniestro = siniestro.numero,
                        documentos = documentos
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = DocumentosLiquidadorUiState(
                    cargando = false,
                    error = e.message ?: "No se pudieron cargar los documentos"
                )
            }
        }
    }
}
