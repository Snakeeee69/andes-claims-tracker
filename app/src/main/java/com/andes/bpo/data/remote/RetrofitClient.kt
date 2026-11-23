package com.andes.bpo.data.remote

import com.andes.bpo.data.local.entity.UsuarioEntity
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val GATEWAY_URL = "http://10.0.2.2:8080/"
    var jwtToken: String? = null
    var usuarioLogueado: UsuarioEntity? = null
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            jwtToken?.let { builder.addHeader("Authorization", "Bearer $it") }
            chain.proceed(builder.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: AndesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GATEWAY_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AndesApiService::class.java)
    }

    fun sanitizarUsuario(raw: UsuarioEntity): UsuarioEntity {
        return UsuarioEntity(
            idUsuario = raw.idUsuario,
            rut = raw.rut.orEmpty(),
            nombre = raw.nombre.orEmpty(),
            apellido = raw.apellido.orEmpty(),
            correo = raw.correo.orEmpty(),
            passwordHash = raw.passwordHash.orEmpty(),
            passwordSalt = raw.passwordSalt.orEmpty(),
            rol = raw.rol.orEmpty().ifBlank { "ASEGURADO_NATURAL" },
            direccion = raw.direccion,
            aseguradora = raw.aseguradora,
            equipo = raw.equipo,
            tipoLiquidador = raw.tipoLiquidador,
            idArea = raw.idArea,
            requiereCambioClave = raw.requiereCambioClave,
            codigoRecuperacionHash = raw.codigoRecuperacionHash
        )
    }

    fun extraerMensajeError(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) return fallback
        return runCatching {
            gson.fromJson(errorBody, MensajeResponse::class.java)?.mensaje
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
    }
}