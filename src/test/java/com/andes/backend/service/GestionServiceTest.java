package com.andes.backend.service;

import com.andes.backend.entity.Gestion;
import com.andes.backend.repository.GestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GestionServiceTest {

    @Mock
    private GestionRepository gestionRepository;

    @InjectMocks
    private GestionService gestionService;

    @Test
    void debeRetornarSoloGestionesVisibles() {
        // 1. Preparar los datos falsos (Mock)
        Gestion gestionVisible = new Gestion();
        gestionVisible.setIdSiniestro(1);
        gestionVisible.setVisibleAsegurado(true);

        when(gestionRepository.findByIdSiniestroAndVisibleAseguradoTrue(1))
                .thenReturn(Arrays.asList(gestionVisible));

        // 2. Ejecutar el método real del servicio
        List<Gestion> resultado = gestionService.obtenerHistorialVisibleParaAsegurado(1);

        // 3. Comprobar que funcionó
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getVisibleAsegurado());
        verify(gestionRepository, times(1)).findByIdSiniestroAndVisibleAseguradoTrue(1);
    }

    @Test
    void debeAsignarFechaActualSiVieneNula() {
        // 1. Preparar datos
        Gestion nuevaGestion = new Gestion();
        nuevaGestion.setIdSiniestro(1);
        // No le asignamos fechaHora a propósito

        when(gestionRepository.save(any(Gestion.class))).thenReturn(nuevaGestion);

        // 2. Ejecutar
        Gestion resultado = gestionService.registrarGestion(nuevaGestion);

        // 3. Comprobar
        assertNotNull(resultado.getFechaHora(), "El servicio debió asignar la fecha actual");
        verify(gestionRepository, times(1)).save(nuevaGestion);
    }
}