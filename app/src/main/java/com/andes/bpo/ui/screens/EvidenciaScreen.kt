package com.andes.bpo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Modelo temporal para la UI, alineado con el EvidenciaEntity de tu compañero
data class EvidenciaUiModel(
    val id: Int,
    val nombreArchivo: String, // En la DB tu amigo guardará la ruta_archivo, aquí mostramos un nombre amigable
    val tipoArchivo: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenciaScreen(navController: NavController, siniestroId: String = "SIN-26-004801") {
    val colorAndes = Color(0xFF1E40AF)

    // Lista simulada de evidencias que luego vendrán de la base de datos de tu compañero
    val evidenciasMock = remember {
        mutableStateListOf(
            EvidenciaUiModel(1, "Declaracion_Jurada.pdf", "PDF"),
            EvidenciaUiModel(2, "Foto_Choque_Frontal.jpg", "IMAGEN")
        )
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colorAndes
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Aquí iría la lógica para abrir la cámara o galería
                    // Por ahora, simulamos agregar un archivo nuevo para probar la UI
                    evidenciasMock.add(
                        EvidenciaUiModel(
                            id = evidenciasMock.size + 1,
                            nombreArchivo = "Nueva_Evidencia_${evidenciasMock.size + 1}.jpg",
                            tipoArchivo = "IMAGEN"
                        )
                    )
                },
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

            if (evidenciasMock.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay evidencia adjunta todavía.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Espacio para que el FAB no tape el último item
                ) {
                    items(evidenciasMock) { evidencia ->
                        EvidenciaItemCard(evidencia = evidencia, colorAndes = colorAndes)
                    }
                }
            }
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
                    contentDescription = "Tipo de archivo",
                    tint = colorAndes
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evidencia.nombreArchivo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
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