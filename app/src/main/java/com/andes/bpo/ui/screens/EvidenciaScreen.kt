package com.andes.bpo.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.andes.bpo.ui.theme.PrimaryAndes
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EvidenciaUiModel(
    val id: Int,
    val nombreArchivo: String,
    val tipoArchivo: String,
    val uri: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenciaScreen(navController: NavController, siniestroId: String = "SIN-26-004801") {
    val context = LocalContext.current
    val colorAndes = PrimaryAndes

    var showDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val evidenciasMock = remember {
        mutableStateListOf(
            EvidenciaUiModel(1, "Declaracion_Jurada.pdf", "PDF"),
            EvidenciaUiModel(2, "Foto_Choque_Frontal.jpg", "IMAGEN")
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            evidenciasMock.add(
                EvidenciaUiModel(
                    id = evidenciasMock.size + 1,
                    nombreArchivo = "Galeria_${System.currentTimeMillis()}.jpg",
                    tipoArchivo = "IMAGEN",
                    uri = it.toString()
                )
            )
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            evidenciasMock.add(
                EvidenciaUiModel(
                    id = evidenciasMock.size + 1,
                    nombreArchivo = "Documento_${System.currentTimeMillis()}.pdf",
                    tipoArchivo = "PDF",
                    uri = it.toString()
                )
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            evidenciasMock.add(
                EvidenciaUiModel(
                    id = evidenciasMock.size + 1,
                    nombreArchivo = "Camara_${System.currentTimeMillis()}.jpg",
                    tipoArchivo = "IMAGEN",
                    uri = tempCameraUri.toString()
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Evidencia Adjunta", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(siniestroId, fontSize = 14.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorAndes
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = colorAndes,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Subir Evidencia")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Documentos y fotografías de respaldo asociadas a tu siniestro.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(evidenciasMock) { evidencia ->
                    EvidenciaItemCard(evidencia = evidencia, colorAndes = colorAndes)
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Adjuntar Evidencia", fontWeight = FontWeight.Bold) },
                text = { Text("Selecciona cómo deseas agregar la documentación a tu caso (máx. 5 MB).") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", context.externalCacheDir)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                imageFile
                            )
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara")
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                showDialog = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galería")
                        }
                        TextButton(
                            onClick = {
                                showDialog = false
                                pdfLauncher.launch("application/pdf")
                            }
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EvidenciaItemCard(evidencia: EvidenciaUiModel, colorAndes: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorAndes.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (evidencia.tipoArchivo == "PDF") Icons.Filled.Description else Icons.Filled.Image,
                    contentDescription = null,
                    tint = colorAndes
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evidencia.nombreArchivo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Text(
                    text = "Tipo: ${evidencia.tipoArchivo}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}