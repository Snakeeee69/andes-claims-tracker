package com.andes.backend.controller;

import com.andes.backend.entity.Gestion;
import com.andes.backend.service.GestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gestiones")
public class GestionController {

    @Autowired
    private GestionService gestionService;

    // Endpoint para que el asegurado vea su historial: GET /api/gestiones/siniestro/1
    @GetMapping("/siniestro/{idSiniestro}")
    public ResponseEntity<List<Gestion>> verHistorial(@PathVariable Integer idSiniestro) {
        List<Gestion> historial = gestionService.obtenerHistorialVisibleParaAsegurado(idSiniestro);
        return new ResponseEntity<>(historial, HttpStatus.OK);
    }

    // Endpoint para que el liquidador registre una acción: POST /api/gestiones
    @PostMapping
    public ResponseEntity<Gestion> registrarNuevaGestion(@RequestBody Gestion gestion) {
        Gestion nuevaGestion = gestionService.registrarGestion(gestion);
        return new ResponseEntity<>(nuevaGestion, HttpStatus.CREATED);
    }
}