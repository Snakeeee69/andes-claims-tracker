package com.andes.bpo.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.PerfilViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var claveActual by remember { mutableStateOf("") }
    var claveNueva by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var avatarBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(estado.rut) {
        if (estado.rut.isNotBlank()) {
            val archivo = File(context.filesDir, "avatar_${estado.rut}.jpg")
            if (archivo.exists()) {
                avatarBitmap = BitmapFactory.decodeFile(archivo.absolutePath)?.asImageBitmap()
            }
        }
    }

    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && estado.rut.isNotBlank()) {
            avatarBitmap = guardarAvatarLocal(context, uri, estado.rut)
        }
    }

    LaunchedEffect(estado.claveActualizada) {
        if (estado.claveActualizada) {
            claveActual = ""
            claveNueva = ""
            confirmarClave = ""
            viewModel.limpiarConfirmacionClave()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clickable { selectorFoto.launch("image/*") },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap!!,
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val iniciales = buildString {
                                    estado.nombre.firstOrNull()?.let { append(it.uppercaseChar()) }
                                    estado.apellido.firstOrNull()?.let { append(it.uppercaseChar()) }
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (iniciales.isNotBlank()) {
                                        Text(
                                            text = iniciales,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Sin foto",
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar foto",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    TextButton(onClick = { selectorFoto.launch("image/*") }) {
                        Text(if (avatarBitmap != null) "Cambiar foto de perfil" else "Agregar foto de perfil")
                    }

                    val nombreCompleto = "${estado.nombre} ${estado.apellido}".trim()
                    if (nombreCompleto.isNotBlank()) {
                        Text(
                            text = nombreCompleto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider()

                Text("Datos personales", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = estado.rut,
                    onValueChange = {},
                    label = { Text("RUT (no editable)") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = viewModel::cambiarNombre,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorNombre != null,
                    supportingText = { estado.errorNombre?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = estado.apellido,
                    onValueChange = viewModel::cambiarApellido,
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorApellido != null,
                    supportingText = { estado.errorApellido?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = estado.correo,
                    onValueChange = viewModel::cambiarCorreo,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorCorreo != null,
                    supportingText = { estado.errorCorreo?.let { Text(it) } }
                )
                Button(
                    onClick = viewModel::guardarPerfil,
                    enabled = !estado.guardandoPerfil && !estado.cambiandoClave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.guardandoPerfil) "Guardando..." else "Guardar cambios")
                }

                HorizontalDivider()
                Text("Cambiar contraseña", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = claveActual,
                    onValueChange = { claveActual = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorClaveActual != null,
                    supportingText = { estado.errorClaveActual?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = claveNueva,
                    onValueChange = { claveNueva = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorClaveNueva != null,
                    supportingText = { estado.errorClaveNueva?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = confirmarClave,
                    onValueChange = { confirmarClave = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorConfirmacion != null,
                    supportingText = { estado.errorConfirmacion?.let { Text(it) } }
                )
                Button(
                    onClick = {
                        viewModel.cambiarContrasena(claveActual, claveNueva, confirmarClave)
                    },
                    enabled = !estado.cambiandoClave && !estado.guardandoPerfil,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.cambiandoClave) "Actualizando..." else "Cambiar contraseña")
                }

                estado.errorGeneral?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
                estado.mensaje?.let { mensaje ->
                    Text(mensaje, color = MaterialTheme.colorScheme.primary)
                }

                OutlinedButton(
                    onClick = { navController.navigate("configurar_recuperacion") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurar código de recuperación")
                }
            }
        }
    }
}

private fun guardarAvatarLocal(context: Context, uri: Uri, rut: String): ImageBitmap? {
    return runCatching {
        val original = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null

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

        val archivo = File(context.filesDir, "avatar_$rut.jpg")
        FileOutputStream(archivo).use { out ->
            bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        bitmapFinal.asImageBitmap()
    }.getOrNull()
}