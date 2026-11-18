
package com.andes.bpo.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class PasswordData(
    val hash: String,
    val salt: String
)

object PasswordUtils {

    private const val ITERACIONES = 210_000
    private const val LONGITUD_CLAVE = 256

    fun generarHash(contrasena: String): PasswordData {

        require(contrasena.isNotBlank()) {
            "La contraseña no puede estar vacía"
        }

        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val hash = derivarClave(contrasena, salt)

        return PasswordData(
            hash = Base64.getEncoder().encodeToString(hash),
            salt = Base64.getEncoder().encodeToString(salt)
        )
    }

    fun verificarContrasena(
        contrasena: String,
        hashGuardado: String,
        saltGuardado: String
    ): Boolean {

        val hashOriginal: ByteArray
        val salt: ByteArray

        try {
            hashOriginal = Base64.getDecoder().decode(hashGuardado)
            salt = Base64.getDecoder().decode(saltGuardado)
        } catch (e: IllegalArgumentException) {
            return false
        }

        val hashIngresado = derivarClave(contrasena, salt)

        return MessageDigest.isEqual(
            hashOriginal,
            hashIngresado
        )
    }

    private fun derivarClave(
        contrasena: String,
        salt: ByteArray
    ): ByteArray {

        val caracteres = contrasena.toCharArray()

        val especificacion = PBEKeySpec(
            caracteres,
            salt,
            ITERACIONES,
            LONGITUD_CLAVE
        )

        return try {
            val algoritmo = SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            )

            algoritmo.generateSecret(especificacion).encoded

        } finally {
            especificacion.clearPassword()
            caracteres.fill('\u0000')
        }
    }
}
