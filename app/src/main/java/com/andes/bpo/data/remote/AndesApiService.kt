package com.andes.bpo.data.remote

import com.andes.bpo.data.local.entity.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val rut: String, val clave: String)
data class AuthResponse(val token: String, val usuario: UsuarioEntity)
data class RecuperarRequest(val rut: String, val correo: String)
data class ConfirmarClaveRequest(val rut: String, val codigo: String, val nuevaClave: String)
data class CambioClaveObligatorioRequest(val idUsuario: Int, val nuevaClave: String)
data class MensajeResponse(val mensaje: String)

data class SiniestroRemoteDto(
    val idSiniestro: Long? = null,
    val numeroSiniestro: String,
    val rutAsegurado: String,
    val nombreAsegurado: String,
    val polizaNumero: String,
    val idArea: Int,
    val descripcion: String,
    val fechaOcurrencia: String? = null,
    val fechaDenuncio: String? = null,
    val estado: String = "Recibido",
    val montoReclamado: Double? = null,
    val rutAnalistaAsignado: String? = null,
    val rutLiquidadorAsignado: String? = null
)

data class DocumentoRemoteDto(
    val idDocumento: Long? = null,
    val siniestroId: Long,
    val nombreArchivo: String,
    val tipoDocumento: String,
    val urlArchivo: String,
    val fechaSubida: String? = null
)

data class HistorialRemoteDto(
    val idHistorial: Long? = null,
    val siniestroId: Long? = null,
    val evento: String? = null,
    val detalle: String? = null,
    val usuarioResponsable: String? = null,
    val fechaEvento: String? = null
)

interface AndesApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/cambiar-clave-obligatoria")
    suspend fun cambiarClaveObligatoria(@Body request: CambioClaveObligatorioRequest): Response<MensajeResponse>

    @POST("api/auth/recuperar/solicitar")
    suspend fun solicitarCodigo(@Body request: RecuperarRequest): Response<MensajeResponse>

    @POST("api/auth/recuperar/confirmar")
    suspend fun confirmarNuevaClave(@Body request: ConfirmarClaveRequest): Response<ResponseBody>

    @POST("api/usuarios/registro")
    suspend fun registrarUsuario(@Body usuario: UsuarioEntity): Response<UsuarioEntity>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Int, @Body usuario: UsuarioEntity): Response<UsuarioEntity>

    @GET("api/siniestros")
    suspend fun listarSiniestros(): Response<List<SiniestroRemoteDto>>

    @GET("api/siniestros/asegurado/{rut}")
    suspend fun listarSiniestrosPorAsegurado(@Path("rut") rut: String): Response<List<SiniestroRemoteDto>>

    @POST("api/siniestros")
    suspend fun crearSiniestro(@Body siniestro: SiniestroRemoteDto): Response<SiniestroRemoteDto>

    @POST("api/siniestros/{siniestroId}/documentos")
    suspend fun subirDocumentoSiniestro(
        @Path("siniestroId") siniestroId: Long,
        @Body documento: DocumentoRemoteDto
    ): Response<DocumentoRemoteDto>

    @GET("api/siniestros/{siniestroId}/documentos")
    suspend fun listarDocumentosSiniestro(
        @Path("siniestroId") siniestroId: Long
    ): Response<List<DocumentoRemoteDto>>

    @GET("api/siniestros/{siniestroId}/historial")
    suspend fun listarHistorialSiniestro(
        @Path("siniestroId") siniestroId: Long
    ): Response<List<HistorialRemoteDto>>
}
