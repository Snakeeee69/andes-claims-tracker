package com.andes.backend.repository;

import com.andes.backend.entity.Gestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GestionRepository extends JpaRepository<Gestion, Integer> {
    
    // Spring Boot construye la consulta SQL automáticamente con solo leer el nombre del método:
    // "Búscame por idSiniestro Y donde visibleAsegurado sea TRUE"
    List<Gestion> findByIdSiniestroAndVisibleAseguradoTrue(Integer idSiniestro);
}