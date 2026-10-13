package com.andes.backend.service;

import com.andes.backend.entity.CierreSiniestro;
import com.andes.backend.repository.CierreSiniestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CierreSiniestroService {

    @Autowired
    private CierreSiniestroRepository cierreRepository;

    public CierreSiniestro registrarCierre(CierreSiniestro cierre) {
        if (cierre.getFechaCierre() == null) {
            cierre.setFechaCierre(LocalDateTime.now());
        }
        return cierreRepository.save(cierre);
    }
}