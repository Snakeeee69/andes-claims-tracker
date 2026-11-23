package com.andes.bpo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.utils.AvatarManager
import com.andes.bpo.utils.AvatarUsuario
import com.andes.bpo.utils.SesionUsuario
import com.andes.bpo.viewmodel.PerfilViewModel

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

    var verClaveActual by remember { mutableStateOf(false) }
    var verClaveNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }

    var mostrarSelectorFoto by remember { mutableStateOf(false) }
    var mostrarConfirmacionSalir by remember { mutableStateOf(false) }

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null && estado.rut.isNotBlank()) {
            AvatarManager.guardarDesdeBitmap(context, bitmap, estado.rut)
        }
    }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && estado.rut.isNotBlank()) {
            AvatarManager.guardarDesdeUri(context, uri, estado.rut)
        }
    }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            launcherCamara.launch(null)
        } else {
            Toast.makeText(context, "Activa el permiso de Cámara para tomar una foto", Toast.LENGTH_SHORT).show()
        }
    }

    val permisoGaleriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            launcherGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            Toast.makeText(context, "Activa el permiso de Galería para elegir una foto", Toast.LENGTH_SHORT).show()
        }
    }

    fun abrirCamara() {
        mostrarSelectorFoto = false
        val permiso = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
            launcherCamara.launch(null)
        } else {
            permisoCamaraLauncher.launch(permiso)
        }
    }

    fun abrirGaleria() {
        mostrarSelectorFoto = false
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
            launcherGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            permisoGaleriaLauncher.launch(permiso)
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

    if (mostrarSelectorFoto) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorFoto = false },
            title = { Text("Foto de perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { abrirCamara() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar foto con Cámara")
                    }
                    OutlinedButton(
                        onClick = { abrirGaleria() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir desde Galería")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarSelectorFoto = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarConfirmacionSalir) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionSalir = false },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas salir de tu cuenta?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacionSalir = false
                        SesionUsuario.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Sí, cerrar sesión", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionSalir = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrimaryAndes)
                    }
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
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryAndes)
                }
            } else {
                val iniciales = buildString {
                    estado.nombre.firstOrNull()?.let { append(it.uppercaseChar()) }
                    estado.apellido.firstOrNull()?.let { append(it.uppercaseChar()) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clickable { mostrarSelectorFoto = true },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        AvatarUsuario(
                            rut = estado.rut,
                            iniciales = iniciales,
                            size = 108.dp,
                            fontSize = 32.sp,
                            border = BorderStroke(2.dp, PrimaryAndes)
                        )

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryAndes),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    TextButton(onClick = { mostrarSelectorFoto = true }) {
                        Text("Cambiar foto (Cámara o Galería)", color = PrimaryAndes)
                    }

                    val nombreCompleto = "${estado.nombre} ${estado.apellido}".trim()
                    if (nombreCompleto.isNotBlank()) {
                        Text(
                            text = nombreCompleto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider()

                Text("Datos personales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryAndes)

                OutlinedTextField(
                    value = estado.rut,
                    onValueChange = {},
                    label = { Text("RUT") },
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes)
                ) {
                    Text(if (estado.guardandoPerfil) "Guardando..." else "Guardar datos personales")
                }

                HorizontalDivider()

                Text("Cambiar contraseña", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryAndes)

                OutlinedTextField(
                    value = claveActual,
                    onValueChange = { claveActual = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = if (verClaveActual) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verClaveActual = !verClaveActual }) {
                            Icon(
                                imageVector = if (verClaveActual) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorClaveActual != null,
                    supportingText = { estado.errorClaveActual?.let { Text(it) } }
                )

                OutlinedTextField(
                    value = claveNueva,
                    onValueChange = { claveNueva = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = if (verClaveNueva) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verClaveNueva = !verClaveNueva }) {
                            Icon(
                                imageVector = if (verClaveNueva) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorClaveNueva != null,
                    supportingText = { estado.errorClaveNueva?.let { Text(it) } }
                )

                OutlinedTextField(
                    value = confirmarClave,
                    onValueChange = { confirmarClave = it },
                    label = { Text("Confirmar nueva contraseña") },
                    visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verConfirmar = !verConfirmar }) {
                            Icon(
                                imageVector = if (verConfirmar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorConfirmacion != null,
                    supportingText = { estado.errorConfirmacion?.let { Text(it) } }
                )

                Button(
                    onClick = {
                        viewModel.cambiarContrasenaDirecta(
                            actual = claveActual,
                            nueva = claveNueva,
                            confirmacion = confirmarClave
                        )
                    },
                    enabled = !estado.cambiandoClave && !estado.guardandoPerfil,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes)
                ) {
                    Text(if (estado.cambiandoClave) "Actualizando..." else "Cambiar contraseña")
                }

                TextButton(
                    onClick = { navController.navigate("recuperar") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("¿Olvidaste tu contraseña actual? Recuperar por correo", color = PrimaryAndes, fontSize = 13.sp)
                }

                estado.errorGeneral?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                estado.mensaje?.let { mensaje ->
                    Text(mensaje, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { mostrarConfirmacionSalir = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFD32F2F)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Cerrar sesión",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}