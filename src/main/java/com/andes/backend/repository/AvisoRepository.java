package com.andes.backend.repository;

import com.andes.backend.entity.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Integer> {
    
    // Busca todos los avisos de un usuario específico (el destinatario)
    List<Aviso> findByIdDestinatario(Integer idDestinatario);
}