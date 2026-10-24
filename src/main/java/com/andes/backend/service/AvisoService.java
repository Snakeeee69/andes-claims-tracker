package com.andes.backend.service;

import com.andes.backend.entity.Aviso;
import com.andes.backend.repository.AvisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AvisoService {

    @Autowired
    private AvisoRepository avisoRepository;

    public Aviso crearAviso(Aviso aviso) {
        if (aviso.getFechaHora() == null) {
            aviso.setFechaHora(LocalDateTime.now());
        }
        return avisoRepository.save(aviso);
    }

    public List<Aviso> obtenerAvisosPorUsuario(Integer idUsuario) {
        return avisoRepository.findByIdDestinatario(idUsuario);
    }

    public Aviso marcarComoLeido(Integer idAviso) {
        Optional<Aviso> avisoOpt = avisoRepository.findById(idAviso);
        if (avisoOpt.isPresent()) {
            Aviso aviso = avisoOpt.get();
            aviso.setLeido(true);
            return avisoRepository.save(aviso);
        }
        throw new IllegalArgumentException("Aviso no encontrado");
    }
}