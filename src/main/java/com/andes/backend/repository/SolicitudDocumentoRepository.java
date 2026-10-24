package com.andes.backend.repository;

import com.andes.backend.entity.SolicitudDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudDocumentoRepository extends JpaRepository<SolicitudDocumento, Integer> {
    
    // Busca todas las solicitudes de documentos asociadas a un caso
    List<SolicitudDocumento> findByIdSiniestro(Integer idSiniestro);
}