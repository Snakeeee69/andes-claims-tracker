package com.andes.bpo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.andes.bpo.ui.theme.PrimaryAndes

data class GestionUiModel(
    val id: Int,
    val fechaHora: String,
    val descripcion: String,
    val etapa: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(navController: NavController, siniestroId: String = "SIN-26-004801") {
    val colorAndes = PrimaryAndes

    val historialMock = remember {
        listOf(
            GestionUiModel(3,
                "18 ago 2026 • 14:20",
                "Revisión inicial de antecedentes completada. Se solicitan documentos adicionales.",
                "En evaluación"
            ),
            GestionUiModel(2,
                "17 ago 2026 • 09:15",
                "Se asignó el liquidador María González al caso.",
                "En evaluación"
            ),
            GestionUiModel(1,
                "16 ago 2026 • 10:30",
                "Siniestro reportado e ingresado al sistema de Andes BPO.",
                "Recibido"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Historial de Gestiones", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                text = "Sigue paso a paso las acciones realizadas en tu caso.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(historialMock.size) { index ->
                    val gestion = historialMock[index]
                    val isLastItem = index == historialMock.size - 1

                    HistorialItem(gestion = gestion, colorAndes = colorAndes, isLastItem = isLastItem)
                }
            }
        }
    }
}

@Composable
fun HistorialItem(gestion: GestionUiModel, colorAndes: Color, isLastItem: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(30.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colorAndes)
            )

            if (!isLastItem) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(colorAndes.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = gestion.fechaHora,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = gestion.etapa,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAndes
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = gestion.descripcion,
                        fontSize = 14.sp,
                        color = Color(0xFF263C35),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}