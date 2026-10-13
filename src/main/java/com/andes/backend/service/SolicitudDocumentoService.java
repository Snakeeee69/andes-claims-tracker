package com.andes.backend.service;

import com.andes.backend.entity.SolicitudDocumento;
import com.andes.backend.repository.SolicitudDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudDocumentoService {

    @Autowired
    private SolicitudDocumentoRepository solicitudRepository;

    public SolicitudDocumento crearSolicitud(SolicitudDocumento solicitud) {
        if (solicitud.getFechaSolicitud() == null) {
            solicitud.setFechaSolicitud(LocalDateTime.now());
        }
        return solicitudRepository.save(solicitud);
    }

    public List<SolicitudDocumento> obtenerSolicitudesPorSiniestro(Integer idSiniestro) {
        return solicitudRepository.findByIdSiniestro(idSiniestro);
    }
}