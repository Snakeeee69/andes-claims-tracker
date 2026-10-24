package com.andes.backend.controller;

import com.andes.backend.entity.SolicitudDocumento;
import com.andes.backend.service.SolicitudDocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudDocumentoController {

    @Autowired
    private SolicitudDocumentoService solicitudService;

    // Crear una nueva solicitud de documento (POST /api/solicitudes)
    @PostMapping
    public ResponseEntity<SolicitudDocumento> crearSolicitud(@RequestBody SolicitudDocumento solicitud) {
        SolicitudDocumento nuevaSolicitud = solicitudService.crearSolicitud(solicitud);
        return new ResponseEntity<>(nuevaSolicitud, HttpStatus.CREATED);
    }

    // Listar las solicitudes de un caso (GET /api/solicitudes/siniestro/1)
    @GetMapping("/siniestro/{idSiniestro}")
    public ResponseEntity<List<SolicitudDocumento>> obtenerSolicitudes(@PathVariable Integer idSiniestro) {
        return new ResponseEntity<>(solicitudService.obtenerSolicitudesPorSiniestro(idSiniestro), HttpStatus.OK);
    }
}