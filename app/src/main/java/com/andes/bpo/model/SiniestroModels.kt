package com.andes.bpo.model

enum class EstadoSiniestro(val descripcion: String) {
    RECIBIDO("Recibido"),
    EN_EVALUACION("En evaluación"),
    EN_LIQUIDACION("En liquidación"),
    CERRADO("Cerrado")
}

enum class RolUsuario(val etiqueta: String) {
    ASEGURADO_NATURAL("Asegurado (Persona Natural)"),
    ASEGURADO_EMPRESA("Asegurado (Empresa / Corredor)"),
    ANALISTA("Analista"),
    ADMINISTRADOR("Administrador"),
    LIQUIDADOR_EXTERNO("Liquidador Externo"),
    LIQUIDADOR_RESPALDO("Liquidador Respaldo")
}

data class HistorialGestion(
    val fecha: String,
    val titulo: String,
    val detalle: String,
    val responsable: String = "Sistema"
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