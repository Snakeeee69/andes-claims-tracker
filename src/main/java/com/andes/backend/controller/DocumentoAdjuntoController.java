package com.andes.backend.controller;

import com.andes.backend.entity.DocumentoAdjunto;
import com.andes.backend.service.DocumentoAdjuntoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoAdjuntoController {

    @Autowired
    private DocumentoAdjuntoService documentoService;

    @PostMapping
    public ResponseEntity<?> subirDocumento(@RequestBody DocumentoAdjunto documento) {
        try {
            DocumentoAdjunto nuevoDocumento = documentoService.guardarDocumento(documento);
            return new ResponseEntity<>(nuevoDocumento, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Si no cumple la RN-10, devolvemos un error 400 con el mensaje
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}