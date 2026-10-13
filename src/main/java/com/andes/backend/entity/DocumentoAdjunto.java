package com.andes.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documento_adjunto")
public class DocumentoAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer idDocumento;

    @Column(name = "id_siniestro", nullable = false)
    private Integer idSiniestro;

    @Column(name = "id_tipo_documento", nullable = false)
    private Integer idTipoDocumento;

    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @Column(name = "nombre_archivo", length = 150, nullable = false)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", length = 255, nullable = false)
    private String rutaArchivo;

    @Column(name = "tamano_kb", nullable = false)
    private Integer tamanoKb;

    @Column(name = "comentario", length = 300)
    private String comentario;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column(name = "id_usuario_carga", nullable = false)
    private Integer idUsuarioCarga;

    // Getters y Setters
    public Integer getIdDocumento() { return idDocumento; }
    public void setIdDocumento(Integer idDocumento) { this.idDocumento = idDocumento; }

    public Integer getIdSiniestro() { return idSiniestro; }
    public void setIdSiniestro(Integer idSiniestro) { this.idSiniestro = idSiniestro; }

    public Integer getIdTipoDocumento() { return idTipoDocumento; }
    public void setIdTipoDocumento(Integer idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public Integer getTamanoKb() { return tamanoKb; }
    public void setTamanoKb(Integer tamanoKb) { this.tamanoKb = tamanoKb; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }

    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }

    public Integer getIdUsuarioCarga() { return idUsuarioCarga; }
    public void setIdUsuarioCarga(Integer idUsuarioCarga) { this.idUsuarioCarga = idUsuarioCarga; }
}