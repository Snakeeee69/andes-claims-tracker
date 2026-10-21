package com.andes.bpo.model

enum class EstadoSiniestro(val descripcion: String) {
    RECIBIDO("Recibido"),
    EN_EVALUACION("En evaluación"),
    EN_LIQUIDACION("En liquidación"),
    CERRADO("Cerrado")
}

data class HistorialGestion(
    val fecha: String,
    val titulo: String,
    val detalle: String
)

data class Siniestro(
    val id: String,
    val tipo: String,
    val subtipo: String,
    val fechaOcurrencia: String,
    val fechaReporte: String,
    val estado: EstadoSiniestro,
    val vehiculo: String,
    val poliza: String,
    val aseguradora: String,
    val liquidadorAsignado: String,
    val historial: List<HistorialGestion>,
    val evidencias: List<String>
)