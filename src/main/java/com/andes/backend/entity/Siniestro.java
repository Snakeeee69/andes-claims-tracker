package com.andes.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "siniestro") // Así se llamará la tabla en la BD de tu amigo
public class Siniestro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSiniestro;

    @Column(unique = true, nullable = false, length = 15)
    private String numero;

    @Column(nullable = false)
    private Integer idAsegurado;

    @Column(nullable = false)
    private Integer idCompania;

    @Column(length = 20)
    private String numeroPoliza;

    @Column(nullable = false)
    private Integer idTipoSiniestro;

    @Column(nullable = false)
    private LocalDate fechaOcurrencia;

    private LocalTime horaAproximada;

    @Column(nullable = false)
    private LocalDateTime fechaReporte;

    @Column(nullable = false)
    private Integer idComuna;

    @Column(length = 150)
    private String referenciaLugar;

    @Column(nullable = false, length = 500)
    private String descripcion;

    private Boolean huboTerceros;

    @Column(length = 6)
    private String denunciaPolicial;

    @Column(nullable = false)
    private Integer idEtapa;

    private Integer idLiquidador;

    public Integer getIdSiniestro() {
        return idSiniestro;
    }

    public void setIdSiniestro(Integer idSiniestro) {
        this.idSiniestro = idSiniestro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getIdAsegurado() {
        return idAsegurado;
    }

    public void setIdAsegurado(Integer idAsegurado) {
        this.idAsegurado = idAsegurado;
    }

    public Integer getIdCompania() {
        return idCompania;
    }

    public void setIdCompania(Integer idCompania) {
        this.idCompania = idCompania;
    }

    public String getNumeroPoliza() {
        return numeroPoliza;
    }

    public void setNumeroPoliza(String numeroPoliza) {
        this.numeroPoliza = numeroPoliza;
    }

    public Integer getIdTipoSiniestro() {
        return idTipoSiniestro;
    }

    public void setIdTipoSiniestro(Integer idTipoSiniestro) {
        this.idTipoSiniestro = idTipoSiniestro;
    }

    public LocalDate getFechaOcurrencia() {
        return fechaOcurrencia;
    }

    public void setFechaOcurrencia(LocalDate fechaOcurrencia) {
        this.fechaOcurrencia = fechaOcurrencia;
    }

    public LocalTime getHoraAproximada() {
        return horaAproximada;
    }

    public void setHoraAproximada(LocalTime horaAproximada) {
        this.horaAproximada = horaAproximada;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public Integer getIdComuna() {
        return idComuna;
    }

    public void setIdComuna(Integer idComuna) {
        this.idComuna = idComuna;
    }

    public String getReferenciaLugar() {
        return referenciaLugar;
    }

    public void setReferenciaLugar(String referenciaLugar) {
        this.referenciaLugar = referenciaLugar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getHuboTerceros() {
        return huboTerceros;
    }

    public void setHuboTerceros(Boolean huboTerceros) {
        this.huboTerceros = huboTerceros;
    }

    public String getDenunciaPolicial() {
        return denunciaPolicial;
    }

    public void setDenunciaPolicial(String denunciaPolicial) {
        this.denunciaPolicial = denunciaPolicial;
    }

    public Integer getIdEtapa() {
        return idEtapa;
    }

    public void setIdEtapa(Integer idEtapa) {
        this.idEtapa = idEtapa;
    }

    public Integer getIdLiquidador() {
        return idLiquidador;
    }

    public void setIdLiquidador(Integer idLiquidador) {
        this.idLiquidador = idLiquidador;
    }

}