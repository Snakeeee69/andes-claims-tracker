package com.andes.bpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.data.repository.SiniestroRepository
import kotlinx.coroutines.launch

class NuevoSiniestroViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SiniestroRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = SiniestroRepository(db.siniestroDao())
    }

    fun guardarSiniestro(siniestro: SiniestroEntity, onExito: () -> Unit) {
        viewModelScope.launch {
            repository.registrarNuevoSiniestro(siniestro)
            onExito()
        }
    }
}