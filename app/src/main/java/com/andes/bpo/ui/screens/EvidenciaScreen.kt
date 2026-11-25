package com.andes.bpo.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.utils.SesionUsuario
import com.andes.bpo.viewmodel.EvidenciaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenciaScreen(
    navController: NavController,
    idSiniestro: Int,
    viewModel: EvidenciaViewModel = viewModel()
) {
    val context = LocalContext.current
    val estado by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    val sesion = SesionUsuario.obtenerSesion()
    val esModoAuditoriaInterna = sesion?.rol == "ANALISTA" ||
            sesion?.rol == "ADMINISTRADOR" ||
            sesion?.rol?.startsWith("LIQUIDADOR") == true

    var mostrarOpciones by remember { mutableStateOf(false) }
    var tipoDocumento by remember { mutableStateOf("FotografÃ­a del daÃ±o") }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var documentoAEliminar by remember { mutableStateOf<DocumentoEntity?>(null) }
    var documentoVisorDigital by remember { mutableStateOf<DocumentoEntity?>(null) }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idSiniestro) { viewModel.cargarEvidencias(idSiniestro) }
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.guardarEvidencia(idSiniestro, it, tipoDocumento) }
    }

    val pdf = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.guardarEvidencia(idSiniestro, it, tipoDocumento) }
    }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            tempCameraUri?.let { viewModel.guardarEvidencia(idSiniestro, it, tipoDocumento) }
        } else {
            tempCameraFile?.delete()
        }
    }

    fun iniciarCamara() {
        try {
            val archivo = File.createTempFile("captura_", ".jpg", context.externalCacheDir ?: context.cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )
            tempCameraFile = archivo
            tempCameraUri = uri
            camara.launch(uri)
        } catch (e: Exception) {
            errorLocal = "No se pudo abrir la cÃ¡mara: ${e.message}"
        }
    }

    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) iniciarCamara() else errorLocal = "Se necesita permiso para usar la cÃ¡mara"
    }

    fun solicitarCamara() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            iniciarCamara()
        } else {
            permisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (esModoAuditoriaInterna) "Evidencias del Expediente" else "Evidencias")
                        if (estado.numeroSiniestro.isNotBlank()) {
                            Text(estado.numeroSiniestro, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (!esModoAuditoriaInterna && !estado.cargando && estado.error == null && !estado.cerrado) {
                FloatingActionButton(
                    onClick = { mostrarOpciones = true },
                    containerColor = PrimaryAndes
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adjuntar evidencia", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                if (esModoAuditoriaInterna) {
                    "RevisiÃ³n de documentos solicitados y respaldos adjuntos por el asegurado."
                } else {
                    "ImÃ¡genes y PDF asociados a este siniestro (mÃ¡ximo 15 archivos de 5 MB)."
                }
            )
            if (estado.cerrado) {
                Text(
                    "El caso estÃ¡ cerrado: solo puedes consultar sus documentos.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (estado.guardando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
            estado.error?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { viewModel.limpiarError() }) { Text("Ocultar error") }
            }
            errorLocal?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { errorLocal = null }) { Text("Ocultar error") }
            }
            Spacer(Modifier.height(12.dp))
            when {
                estado.cargando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                estado.error != null && estado.numeroSiniestro.isBlank() -> {
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "Documentos solicitados (${estado.solicitudes.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (estado.solicitudes.isEmpty()) {
                            item { Text("No hay solicitudes registradas para este siniestro.") }
                        } else {
                            items(
                                estado.solicitudes,
                                key = { "solicitud_${it.idDocumento}" }
                            ) { solicitud ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(
                                        Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            solicitud.nombreTipoDocumento,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("Estado: ${solicitud.estado}", color = MaterialTheme.colorScheme.primary)
                                        if (solicitud.fechaSolicitud.isNotBlank()) {
                                            Text("Solicitado: ${solicitud.fechaSolicitud}", fontSize = 12.sp)
                                        }
                                        solicitud.comentario?.takeIf { it.isNotBlank() }?.let {
                                            Text("Detalle: $it", fontSize = 13.sp)
                                        }
                                        if (!esModoAuditoriaInterna && solicitud.estado == "Pendiente" && !estado.cerrado) {
                                            OutlinedButton(
                                                onClick = {
                                                    tipoDocumento = solicitud.nombreTipoDocumento
                                                    mostrarOpciones = true
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Adjuntar este documento")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Archivos adjuntos (${estado.evidencias.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (estado.evidencias.isEmpty()) {
                            item { Text("TodavÃ­a no hay archivos adjuntos a este siniestro.") }
                        } else {
                            items(
                                estado.evidencias,
                                key = { "evidencia_${it.idDocumento}" }
                            ) { documento ->
                                EvidenciaGuardadaCard(
                                    documento = documento,
                                    cerrado = estado.cerrado || esModoAuditoriaInterna,
                                    procesando = estado.procesandoDocumentoId != null || estado.guardando,
                                    onEnviar = {
                                        viewModel.enviarBorrador(idSiniestro, documento.idDocumento)
                                    },
                                    onEliminar = { documentoAEliminar = documento },
                                    onAbrirPdf = {
                                        val ruta = documento.rutaUri.orEmpty()
                                        val archivo = File(ruta)
                                        if (ruta.startsWith("local://") || !archivo.exists()) {
                                            documentoVisorDigital = documento
                                        } else {
                                            try {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    archivo
                                                )
                                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, "application/pdf")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                documentoVisorDigital = documento
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    documentoVisorDigital?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentoVisorDigital = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryAndes)
                    Text(doc.nombreArchivo ?: doc.nombreTipoDocumento, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tipo documental: ${doc.nombreTipoDocumento}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Estado: ${doc.estado}", fontSize = 12.sp, color = Color(0xFF15803D))
                            if (!doc.fechaRecepcion.isNullOrBlank()) {
                                Text("Recepcionado: ${doc.fechaRecepcion}", fontSize = 12.sp)
                            }
                            if (!doc.comentario.isNullOrBlank()) {
                                Text("ObservaciÃ³n: ${doc.comentario}", fontSize = 12.sp)
                            }
                        }
                    }
                    Text(
                        "Documento validado en el expediente digital de ${estado.numeroSiniestro}.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { documentoVisorDigital = null }) { Text("Cerrar vista") }
            }
        )
    }

    if (mostrarOpciones) {
        AlertDialog(
            onDismissRequest = { mostrarOpciones = false },
            title = { Text("Agregar evidencia") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Indica quÃ© tipo de documento vas a adjuntar:")
                    listOf(
                        "FotografÃ­a del daÃ±o",
                        "Parte Policial / Denuncia",
                        "Informe de Bomberos",
                        "PÃ³liza / GuÃ­a de Despacho",
                        "Presupuesto o Factura",
                        "Orden MÃ©dica / Boleta",
                        "Otro documento"
                    ).forEach { opcion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { tipoDocumento = opcion },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tipoDocumento == opcion,
                                onClick = { tipoDocumento = opcion }
                            )
                            Text(opcion)
                        }
                    }
                    HorizontalDivider()
                    OutlinedButton(
                        onClick = {
                            mostrarOpciones = false
                            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Elegir imagen de la galerÃ­a") }
                    OutlinedButton(
                        onClick = {
                            mostrarOpciones = false
                            pdf.launch("application/pdf")
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Elegir PDF") }
                    OutlinedButton(
                        onClick = {
                            mostrarOpciones = false
                            solicitarCamara()
                        }, modifier = Modifier.fillMaxWidth()
                    ) { Text("Tomar una fotografÃ­a") }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarOpciones = false }) { Text("Cancelar") }
            }
        )
    }

    documentoAEliminar?.let { documento ->
        AlertDialog(
            onDismissRequest = { documentoAEliminar = null },
            title = { Text("Eliminar borrador") },
            text = { Text("Â¿Deseas eliminar '${documento.nombreArchivo}'? Esta acciÃ³n no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarBorrador(idSiniestro, documento.idDocumento)
                    documentoAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { documentoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun EvidenciaGuardadaCard(
    documento: DocumentoEntity,
    cerrado: Boolean,
    procesando: Boolean,
    onEnviar: () -> Unit,
    onEliminar: () -> Unit,
    onAbrirPdf: () -> Unit
) {
    val ruta = documento.rutaUri.orEmpty()
    val esPdfODigital = File(ruta).extension.equals("pdf", ignoreCase = true) ||
            ruta.startsWith("local://") ||
            documento.nombreArchivo?.endsWith(".pdf", ignoreCase = true) == true
    val esBorrador = documento.estado == "Borrador"

    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(documento.nombreArchivo ?: documento.nombreTipoDocumento, fontWeight = FontWeight.Bold)
            Text("Tipo: ${documento.nombreTipoDocumento}")
            Text("Estado: ${documento.estado}", color = MaterialTheme.colorScheme.primary)
            if (!documento.fechaRecepcion.isNullOrBlank()) {
                Text("Fecha: ${documento.fechaRecepcion}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!esPdfODigital && ruta.isNotBlank()) {
                val imagen by produceState<ImageBitmap?>(initialValue = null, key1 = ruta) {
                    value = withContext(Dispatchers.IO) {
                        val dimensiones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(ruta, dimensiones)
                        if (dimensiones.outWidth <= 0) return@withContext null
                        var escala = 1
                        while (dimensiones.outWidth / escala > 1024 ||
                            dimensiones.outHeight / escala > 1024
                        ) escala *= 2
                        val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
                        BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
                    }
                }
                if (imagen != null) {
                    Image(
                        bitmap = imagen!!,
                        contentDescription = "Vista previa de ${documento.nombreArchivo}",
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            if (esPdfODigital) {
                OutlinedButton(onClick = onAbrirPdf, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver / Abrir Documento")
                }
            }
            if (esBorrador && !cerrado) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onEnviar,
                        enabled = !procesando,
                        modifier = Modifier.weight(1f)
                    ) { Text("Enviar") }
                    OutlinedButton(
                        onClick = onEliminar,
                        enabled = !procesando,
                        modifier = Modifier.weight(1f)
                    ) { Text("Eliminar") }
                }
            }
        }
    }
}
