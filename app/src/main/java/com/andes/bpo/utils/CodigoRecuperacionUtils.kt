package com.andes.bpo.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale

object CodigoRecuperacionUtils {

    fun generarCodigo(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)

        val codigo = Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or
                    Base64.NO_WRAP or
                    Base64.NO_PADDING
        )

        return codigo.chunked(8).joinToString("-")
    }

    fun obtenerHash(codigo: String): String {
        val normalizado = codigo
            .trim()
            .replace("-", "")
            .replace(" ", "")
            .uppercase(Locale.ROOT)

        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(normalizado.toByteArray(Charsets.UTF_8))

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }
}
