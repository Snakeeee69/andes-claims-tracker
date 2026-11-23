package com.andes.bpo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object AvatarManager {
    private val _version = MutableStateFlow(0)
    val version = _version.asStateFlow()

    fun normalizarRut(rut: String): String =
        rut.replace(Regex("[^0-9kK]"), "").uppercase(Locale.ROOT)

    fun archivoAvatar(context: Context, rut: String): File {
        val rutLimpio = normalizarRut(rut)
        return File(context.filesDir, "avatar_$rutLimpio.jpg")
    }

    fun cargarAvatar(context: Context, rut: String): ImageBitmap? {
        if (rut.isBlank()) return null
        val archivo = archivoAvatar(context, rut)
        if (!archivo.exists()) return null
        return runCatching {
            BitmapFactory.decodeFile(archivo.absolutePath)?.asImageBitmap()
        }.getOrNull()
    }

    fun guardarDesdeUri(context: Context, uri: Uri, rut: String): ImageBitmap? {
        return runCatching {
            val original = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return null
            guardarBitmapEscalado(context, original, rut)
        }.getOrNull()
    }

    fun guardarDesdeBitmap(context: Context, bitmap: Bitmap, rut: String): ImageBitmap? {
        return runCatching {
            guardarBitmapEscalado(context, bitmap, rut)
        }.getOrNull()
    }

    private fun guardarBitmapEscalado(context: Context, original: Bitmap, rut: String): ImageBitmap {
        val maxLado = 512
        val escala = minOf(
            maxLado.toFloat() / original.width,
            maxLado.toFloat() / original.height,
            1f
        )
        val bitmapFinal = if (escala < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * escala).toInt(),
                (original.height * escala).toInt(),
                true
            )
        } else {
            original
        }

        val archivo = archivoAvatar(context, rut)
        FileOutputStream(archivo).use { out ->
            bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 88, out)
        }
        _version.update { it + 1 }
        return bitmapFinal.asImageBitmap()
    }
}

@Composable
fun AvatarUsuario(
    rut: String,
    iniciales: String,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    fontSize: TextUnit = 12.sp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    border: BorderStroke? = null
) {
    val context = LocalContext.current
    val version by AvatarManager.version.collectAsState()

    val bitmap = remember(rut, version) {
        AvatarManager.cargarAvatar(context, rut)
    }

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = containerColor,
        border = border
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (iniciales.isNotBlank()) {
                    Text(
                        text = iniciales,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil de usuario",
                        tint = contentColor,
                        modifier = Modifier.size(size * 0.55f)
                    )
                }
            }
        }
    }
}