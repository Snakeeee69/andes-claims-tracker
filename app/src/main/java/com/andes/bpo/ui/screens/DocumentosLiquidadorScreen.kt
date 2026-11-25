package com.andes.bpo.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.viewmodel.DocumentosLiquidadorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosLiquidadorScreen(
    navController: NavController,
    idSiniestro: Int,
    viewModel: DocumentosLiquidadorViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var errorArchivo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(idSiniestro) { viewModel.cargarDocumentos(idSiniestro) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Documentos del siniestro")
                        if (estado.numeroSiniestro.isNotBlank()) {
                            Text(
                                estado.numeroSiniestro,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            estado.cargando -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            estado.error != null -> Box(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Solicitudes y documentos entregados",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Solo lectura: no puedes modificar los archivos del asegurado.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    errorArchivo?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { errorArchivo = null }) { Text("Ocultar aviso") }
                    }
                }
                if (estado.documentos.isEmpty()) {
                    item { Text("AÃºn no hay documentos solicitados ni entregados.") }
                } else {
                    items(estado.documentos, key = { it.idDocumento }) { documento ->
                        DocumentoLiquidadorCard(
                            documento = documento,
                            onAbrirPdf = {
                                try {
                                    val archivo = File(documento.rutaUri.orEmpty())
                                    check(archivo.isFile) { "No se encuentra el archivo" }
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
                                    errorArchivo = "No se pudo abrir el PDF. Comprueba que el archivo exista y tengas un lector instalado."
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentoLiquidadorCard(
    documento: DocumentoEntity,
    onAbrirPdf: () -> Unit
) {
    val ruta = documento.rutaUri.orEmpty()
    val entregado = documento.estado == "Entregado"
    val esPdf = File(ruta).extension.equals("pdf", ignoreCase = true)

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(documento.nombreTipoDocumento, style = MaterialTheme.typography.titleMedium)
            Text("Estado: ${documento.estado}")
            documento.comentario?.takeIf { it.isNotBlank() }?.let {
                Text("Solicitud: $it")
            }
            if (documento.fechaSolicitud.isNotBlank()) {
                Text("Solicitado: ${documento.fechaSolicitud}")
            }
            documento.fechaRecepcion?.takeIf { it.isNotBlank() }?.let {
                Text("Recibido: $it")
            }

            if (!entregado) {
                Text("Esperando el archivo del asegurado")
            } else {
                Text("Archivo: ${documento.nombreArchivo ?: "Sin nombre"}")
                if (esPdf) {
                    OutlinedButton(onClick = onAbrirPdf) { Text("Abrir PDF") }
                } else if (ruta.isNotBlank()) {
                    val imagen by produceState<ImageBitmap?>(initialValue = null, key1 = ruta) {
                        value = withContext(Dispatchers.IO) {
                            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeFile(ruta, limites)
                            if (limites.outWidth <= 0 || limites.outHeight <= 0) {
                                null
                            } else {
                                var escala = 1
                                while (limites.outWidth / escala > 1024 ||
                                    limites.outHeight / escala > 1024) escala *= 2
                                val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
                                BitmapFactory.decodeFile(ruta, opciones)?.asImageBitmap()
                            }
                        }
                    }
                    if (imagen != null) {
                        Image(
                            bitmap = imagen!!,
                            contentDescription = "Imagen entregada",
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("No se pudo mostrar la vista previa")
                    }
                }
            }
        }
    }
}
