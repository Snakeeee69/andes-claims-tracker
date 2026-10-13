package com.andes.backend.controller;

import com.andes.backend.entity.Siniestro;
import com.andes.backend.repository.SiniestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/siniestros")
public class SiniestroController {

    @Autowired 
    private SiniestroRepository siniestroRepository;

    // Endpoint GET: La app móvil llama aquí para obtener la lista de siniestros
    @GetMapping
    public List<Siniestro> listarSiniestros() {
        return siniestroRepository.findAll();
    }

    // Endpoint POST: La app móvil envía los datos aquí para crear un reporte nuevo
    @PostMapping
    public Siniestro registrarSiniestro(@RequestBody Siniestro nuevoSiniestro) {
        // Al usar el repository, Spring Boot crea automáticamente el comando INSERT en SQL
        return siniestroRepository.save(nuevoSiniestro);
    }
}