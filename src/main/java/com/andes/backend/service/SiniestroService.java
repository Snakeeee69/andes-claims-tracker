package com.andes.backend.service;

import com.andes.backend.entity.Siniestro;
import com.andes.backend.repository.SiniestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiniestroService {

    @Autowired
    private SiniestroRepository siniestroRepository;

    public List<Siniestro> obtenerTodosLosSiniestros() {
        return siniestroRepository.findAll();
    }

    public Siniestro crearSiniestro(Siniestro siniestro) {
        return siniestroRepository.save(siniestro);
    }
}