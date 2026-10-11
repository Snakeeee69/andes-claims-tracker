package com.andes.backend.repository;

import com.andes.backend.entity.Siniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface SiniestroRepository extends JpaRepository<Siniestro, Integer> {
    // Al extender de JpaRepository, Spring Boot te regala automáticamente 
    // métodos como save(), findAll(), y findById() sin que escribas nada más.
}