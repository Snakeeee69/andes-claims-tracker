package com.andes.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aviso")
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aviso")
    private Integer idAviso;

    @Column(name = "id_gestion", nullable = false)
    private Integer idGestion;

    @Column(name = "id_destinatario", nullable = false)
    private Integer idDestinatario;

    @Column(name = "mensaje", length = 300, nullable = false)
    private String mensaje;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "leido", nullable = false)
    private Boolean leido = false; // Por defecto es falso según el MER

    // Getters y Setters
    public Integer getIdAviso() { return idAviso; }
    public void setIdAviso(Integer idAviso) { this.idAviso = idAviso; }

    public Integer getIdGestion() { return idGestion; }
    public void setIdGestion(Integer idGestion) { this.idGestion = idGestion; }

    public Integer getIdDestinatario() { return idDestinatario; }
    public void setIdDestinatario(Integer idDestinatario) { this.idDestinatario = idDestinatario; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Boolean getLeido() { return leido; }
    public void setLeido(Boolean leido) { this.leido = leido; }
}