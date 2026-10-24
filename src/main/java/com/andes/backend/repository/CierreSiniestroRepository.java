package com.andes.backend.repository;

import com.andes.backend.entity.CierreSiniestro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CierreSiniestroRepository extends JpaRepository<CierreSiniestro, Integer> {
}