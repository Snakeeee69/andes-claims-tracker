package com.andes.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cierre_siniestro")
public class CierreSiniestro {

    // Al ser PK y FK al mismo tiempo, no usa GeneratedValue
    @Id
    @Column(name = "id_siniestro")
    private Integer idSiniestro;

    @Column(name = "id_resultado", nullable = false)
    private Integer idResultado;

    @Column(name = "mensaje_cierre", length = 500, nullable = false)
    private String mensajeCierre;

    @Column(name = "motivo_desistimiento", length = 300)
    private String motivoDesistimiento;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDateTime fechaCierre;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    // Getters y Setters
    public Integer getIdSiniestro() { return idSiniestro; }
    public void setIdSiniestro(Integer idSiniestro) { this.idSiniestro = idSiniestro; }

    public Integer getIdResultado() { return idResultado; }
    public void setIdResultado(Integer idResultado) { this.idResultado = idResultado; }

    public String getMensajeCierre() { return mensajeCierre; }
    public void setMensajeCierre(String mensajeCierre) { this.mensajeCierre = mensajeCierre; }

    public String getMotivoDesistimiento() { return motivoDesistimiento; }
    public void setMotivoDesistimiento(String motivoDesistimiento) { this.motivoDesistimiento = motivoDesistimiento; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
}