package com.andes.bpo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.andes.bpo.ui.theme.BackgroundAndes
import com.andes.bpo.ui.theme.PrimaryAndes

data class PreguntaFrecuente(
    val pregunta: String,
    val respuesta: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroAyudaScreen(navController: NavController) {
    val preguntas = remember {
        listOf(
            PreguntaFrecuente(
                "¿Cómo reporto un siniestro?",
                "Selecciona 'Reportar siniestro' en la app o ingresa directamente con tu RUT. Tu caso quedará registrado en la etapa 'Recibido'."
            ),
            PreguntaFrecuente(
                "¿Qué significa cada estado?",
                "Recibido: registramos tu reporte. En evaluación: revisamos los antecedentes. En liquidación: definimos la resolución. Cerrado: el proceso ha finalizado."
            ),
            PreguntaFrecuente(
                "¿Cómo adjunto documentos?",
                "En la pantalla de tu siniestro, presiona 'Ver / Adjuntar Evidencias'. Puedes capturar una foto con la cámara o subir archivos PDF."
            ),
            PreguntaFrecuente(
                "¿Necesito llamar a la aseguradora?",
                "No es necesario. Todos los avances y resoluciones de tu caso se actualizarán en tiempo real directamente en esta aplicación."
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centro de Ayuda", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PrimaryAndes
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundAndes,
                    titleContentColor = PrimaryAndes
                )
            )
        },
        containerColor = BackgroundAndes
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estamos para acompañarte. Resuelve tus dudas sobre el seguimiento de tu caso.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(preguntas) { item ->
                    PreguntaAcordeonCard(item)
                }
            }
        }
    }
}

@Composable
fun PreguntaAcordeonCard(item: PreguntaFrecuente) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = PrimaryAndes,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.pregunta,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF263C35)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.respuesta,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}