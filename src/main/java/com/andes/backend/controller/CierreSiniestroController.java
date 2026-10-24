package com.andes.backend.controller;

import com.andes.backend.entity.CierreSiniestro;
import com.andes.backend.service.CierreSiniestroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cierres")
public class CierreSiniestroController {

    @Autowired
    private CierreSiniestroService cierreService;

    @PostMapping
    public ResponseEntity<CierreSiniestro> registrarCierre(@RequestBody CierreSiniestro cierre) {
        CierreSiniestro nuevoCierre = cierreService.registrarCierre(cierre);
        return new ResponseEntity<>(nuevoCierre, HttpStatus.CREATED);
    }
}