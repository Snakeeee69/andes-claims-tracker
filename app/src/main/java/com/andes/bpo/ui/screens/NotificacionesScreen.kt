package com.andes.bpo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class AvisoUiModel(
    val id: Int,
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    var leido: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(navController: NavController) {
    val colorAndes = Color(0xFF1E40AF)

    val notificacionesMock = remember {
        mutableStateListOf(
            AvisoUiModel(1,
                "Actualización de Estado",
                "Tu caso ha pasado a la etapa 'En evaluación'.",
                "Hace 2 horas",
                false
            ),
            AvisoUiModel(2,
                "Documentación requerida",
                "Necesitamos que adjuntes una copia de tu carnet de identidad.",
                "Ayer",
                false
            ),
            AvisoUiModel(3,
                "Siniestro Ingresado",
                "Hemos recibido tu reporte de siniestro exitosamente.",
                "16 ago",
                true
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(notificacionesMock) { notificacion ->
                    NotificacionItem(notificacion = notificacion, colorAndes = colorAndes) {
                        // Al hacer clic, marcamos como leído (simulación)
                        val index = notificacionesMock.indexOf(notificacion)
                        if (index != -1) {
                            notificacionesMock[index] = notificacion.copy(leido = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(notificacion: AvisoUiModel, colorAndes: Color, onClick: () -> Unit) {
    val backgroundColor = if (notificacion.leido) Color(0xFFF3F4F6) else Color(0xFFE0E7FF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (notificacion.leido) Color.LightGray else colorAndes, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificacion.titulo,
                        fontWeight = if (notificacion.leido) FontWeight.Normal else FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = notificacion.fecha,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notificacion.mensaje,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }

            if (!notificacion.leido) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}