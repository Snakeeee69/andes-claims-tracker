package com.andes.backend.controller;

import com.andes.backend.entity.Aviso;
import com.andes.backend.service.AvisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avisos")
public class AvisoController {

    @Autowired
    private AvisoService avisoService;

    // Obtener todos los avisos de un usuario (GET /api/avisos/usuario/1)
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Aviso>> obtenerAvisos(@PathVariable Integer idUsuario) {
        return new ResponseEntity<>(avisoService.obtenerAvisosPorUsuario(idUsuario), HttpStatus.OK);
    }

    // Crear un nuevo aviso (POST /api/avisos)
    @PostMapping
    public ResponseEntity<Aviso> crearAviso(@RequestBody Aviso aviso) {
        return new ResponseEntity<>(avisoService.crearAviso(aviso), HttpStatus.CREATED);
    }

    // Marcar un aviso como leído (PUT /api/avisos/1/leer)
    @PutMapping("/{idAviso}/leer")
    public ResponseEntity<?> marcarLeido(@PathVariable Integer idAviso) {
        try {
            Aviso avisoActualizado = avisoService.marcarComoLeido(idAviso);
            return new ResponseEntity<>(avisoActualizado, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}