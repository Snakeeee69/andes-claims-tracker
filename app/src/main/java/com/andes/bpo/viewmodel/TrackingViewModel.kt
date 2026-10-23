package com.andes.bpo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.model.TrackingUiState
import com.andes.bpo.repository.SiniestrosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    // Instanciamos el repositorio
    private val repository = SiniestrosRepository()

    fun cargarDatosSiniestro(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val datosNuevos = repository.obtenerSiniestroPorRut(rut)

            _uiState.value = datosNuevos
        }
    }
}