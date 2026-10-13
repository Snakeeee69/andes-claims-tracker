package com.andes.backend.service;

import com.andes.backend.entity.Gestion;
import com.andes.backend.repository.GestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GestionService {

    @Autowired
    private GestionRepository gestionRepository;

    // Retorna solo el historial que el asegurado tiene permitido ver (RN-13)
    public List<Gestion> obtenerHistorialVisibleParaAsegurado(Integer idSiniestro) {
        return gestionRepository.findByIdSiniestroAndVisibleAseguradoTrue(idSiniestro);
    }

    // Registra una nueva gestión (para cuando el liquidador trabaja en el caso)
    public Gestion registrarGestion(Gestion gestion) {
        // Aseguramos que la gestión tenga la fecha y hora actual si no viene en el JSON
        if (gestion.getFechaHora() == null) {
            gestion.setFechaHora(LocalDateTime.now());
        }
        return gestionRepository.save(gestion);
    }
}