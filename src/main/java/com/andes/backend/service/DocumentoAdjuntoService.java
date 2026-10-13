package com.andes.backend.service;

import com.andes.backend.entity.DocumentoAdjunto;
import com.andes.backend.repository.DocumentoAdjuntoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DocumentoAdjuntoService {

    @Autowired
    private DocumentoAdjuntoRepository documentoRepository;

    public DocumentoAdjunto guardarDocumento(DocumentoAdjunto documento) {
        
        // Validación Regla RN-10: El archivo debe pesar entre 1 y 5120 KB
        if (documento.getTamanoKb() == null || documento.getTamanoKb() < 1 || documento.getTamanoKb() > 5120) {
            throw new IllegalArgumentException("Error RN-10: El tamaño del archivo debe estar entre 1 y 5120 KB.");
        }

        // Asignamos la fecha de carga automáticamente si el cliente no la envía
        if (documento.getFechaCarga() == null) {
            documento.setFechaCarga(LocalDateTime.now());
        }

        return documentoRepository.save(documento);
    }
}