package com.andes.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_documento")
public class SolicitudDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @Column(name = "id_siniestro", nullable = false)
    private Integer idSiniestro;

    @Column(name = "id_tipo_documento", nullable = false)
    private Integer idTipoDocumento;

    @Column(name = "comentario", length = 300)
    private String comentario;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "id_solicitante", nullable = false)
    private Integer idSolicitante;

    // Getters y Setters
    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public Integer getIdSiniestro() { return idSiniestro; }
    public void setIdSiniestro(Integer idSiniestro) { this.idSiniestro = idSiniestro; }

    public Integer getIdTipoDocumento() { return idTipoDocumento; }
    public void setIdTipoDocumento(Integer idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Integer getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(Integer idSolicitante) { this.idSolicitante = idSolicitante; }
}