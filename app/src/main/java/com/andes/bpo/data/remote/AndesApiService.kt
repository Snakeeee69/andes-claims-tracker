package com.andes.bpo.data.remote

import com.andes.bpo.data.local.entity.*
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val rut: String, val clave: String)
data class AuthResponse(val token: String, val usuario: UsuarioEntity)
data class RecuperarRequest(val rut: String, val correo: String)
data class ConfirmarClaveRequest(val rut: String, val codigo: String, val nuevaClave: String)
data class MensajeResponse(val mensaje: String)

interface AndesApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/recuperar/solicitar")
    suspend fun solicitarCodigo(@Body request: RecuperarRequest): Response<MensajeResponse>

    @POST("api/auth/recuperar/confirmar")
    suspend fun confirmarNuevaClave(@Body request: ConfirmarClaveRequest): Response<MensajeResponse>

    @POST("api/usuarios/registro")
    suspend fun registrarUsuario(@Body usuario: UsuarioEntity): Response<UsuarioEntity>

    @PUT("api/usuarios/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Int, @Body usuario: UsuarioEntity): Response<UsuarioEntity>

    @GET("api/siniestros")
    suspend fun listarSiniestros(): Response<List<SiniestroEntity>>

    @POST("api/siniestros")
    suspend fun crearSiniestro(@Body siniestro: SiniestroEntity): Response<SiniestroEntity>

    @PUT("api/siniestros/{id}")
    suspend fun actualizarSiniestro(@Path("id") id: Int, @Body siniestro: SiniestroEntity): Response<SiniestroEntity>

    @POST("api/historial")
    suspend fun registrarGestion(@Body gestion: GestionEntity): Response<GestionEntity>

    @POST("api/evidencias")
    suspend fun registrarEvidencia(@Body documento: DocumentoEntity): Response<DocumentoEntity>

    @DELETE("api/evidencias/{id}")
    suspend fun eliminarEvidencia(@Path("id") id: Int): Response<Unit>

    @POST("api/notificaciones")
    suspend fun crearAviso(@Body aviso: AvisoEntity): Response<AvisoEntity>
}