package com.andes.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gestion")
public class Gestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idGestion;

    @Column(name = "id_siniestro", nullable = false)
    private Integer idSiniestro;

    @Column(name = "id_tipo_gestion", nullable = false)
    private Integer idTipoGestion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "descripcion", length = 300, nullable = false)
    private String descripcion;

    @Column(name = "visible_asegurado", nullable = false)
    private Boolean visibleAsegurado = true;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_etapa_destino")
    private Integer idEtapaDestino;

    // Getters y Setters
    public Integer getIdGestion() { return idGestion; }
    public void setIdGestion(Integer idGestion) { this.idGestion = idGestion; }

    public Integer getIdSiniestro() { return idSiniestro; }
    public void setIdSiniestro(Integer idSiniestro) { this.idSiniestro = idSiniestro; }

    public Integer getIdTipoGestion() { return idTipoGestion; }
    public void setIdTipoGestion(Integer idTipoGestion) { this.idTipoGestion = idTipoGestion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getVisibleAsegurado() { return visibleAsegurado; }
    public void setVisibleAsegurado(Boolean visibleAsegurado) { this.visibleAsegurado = visibleAsegurado; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdEtapaDestino() { return idEtapaDestino; }
    public void setIdEtapaDestino(Integer idEtapaDestino) { this.idEtapaDestino = idEtapaDestino; }
}