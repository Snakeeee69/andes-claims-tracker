package com.andes.backend.repository;

import com.andes.backend.entity.DocumentoAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoAdjuntoRepository extends JpaRepository<DocumentoAdjunto, Integer> {
    // Por ahora solo necesitamos las operaciones básicas de guardado que ya incluye JpaRepository
}