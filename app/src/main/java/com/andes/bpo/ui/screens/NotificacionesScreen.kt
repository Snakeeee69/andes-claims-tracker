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
import com.andes.bpo.ui.theme.BackgroundAndes
import com.andes.bpo.ui.theme.MintAndes
import com.andes.bpo.ui.theme.PrimaryAndes

data class AvisoUiModel(
    val id: Int,
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    var leido: Boolean,
    val detalleExtendido: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(navController: NavController, rut: String = "158423715") {
    val colorAndes = PrimaryAndes
    val rutLimpio = rut.replace(".", "").replace("-", "")

    var notificacionSeleccionada by remember { mutableStateOf<AvisoUiModel?>(null) }

    val notificacionesMock = remember(rutLimpio) {
        if (rutLimpio == "158423715") {
            mutableStateListOf(
                AvisoUiModel(
                    id = 1,
                    titulo = "Actualización de Estado",
                    mensaje = "Tu caso ha pasado a la etapa 'En evaluación'.",
                    fecha = "Hace 2 horas",
                    leido = false,
                    detalleExtendido = "El liquidador asignado ya revisó tus antecedentes y el siniestro SIN-26-004801 avanza según lo programado. Estaremos en contacto directo contigo para coordinar la inspección del vehículo si es necesario."
                ),
                AvisoUiModel(
                    id = 2,
                    titulo = "Documentación requerida",
                    mensaje = "Necesitamos que adjuntes una copia de tu carnet de identidad.",
                    fecha = "Ayer",
                    leido = false,
                    detalleExtendido = "Para agilizar la orden de reparación de tu vehículo (patente BJKR-94), por favor sube una fotografía por ambos lados de tu Cédula de Identidad vigente desde la pestaña 'Evidencias'."
                ),
                AvisoUiModel(
                    id = 3,
                    titulo = "Siniestro Ingresado",
                    mensaje = "Hemos recibido tu reporte de siniestro exitosamente.",
                    fecha = "16 ago",
                    leido = true,
                    detalleExtendido = "Tu reporte de siniestro ha sido registrado correctamente en la plataforma de Andes BPO. Tu número de gestión oficial para seguimiento es SIN-26-004801."
                )
            )
        } else {
            mutableStateListOf(
                AvisoUiModel(
                    id = 1,
                    titulo = "Bienvenido a Andes BPO",
                    mensaje = "Tu cuenta está habilitada para realizar nuevos reportes.",
                    fecha = "Hoy",
                    leido = false,
                    detalleExtendido = "Bienvenido al portal de asegurados. Desde aquí podrás ingresar nuevas solicitudes de siniestro y seguir su tramitación en tiempo real."
                )
            )
        }
    }

    if (notificacionSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { notificacionSeleccionada = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MintAndes, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = colorAndes,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = notificacionSeleccionada!!.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF263C35)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = notificacionSeleccionada!!.fecha,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = notificacionSeleccionada!!.detalleExtendido,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { notificacionSeleccionada = null }
                ) {
                    Text(
                        text = "Entendido",
                        color = colorAndes,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
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
                    containerColor = BackgroundAndes,
                    titleContentColor = colorAndes
                )
            )
        },
        containerColor = BackgroundAndes
    ) { paddingValues ->
        AnimacionEntrada {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(notificacionesMock) { notificacion ->
                        NotificacionItem(notificacion = notificacion, colorAndes = colorAndes) {
                            val index = notificacionesMock.indexOf(notificacion)
                            if (index != -1) {
                                notificacionesMock[index] = notificacion.copy(leido = true)
                                notificacionSeleccionada = notificacionesMock[index]
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(notificacion: AvisoUiModel, colorAndes: Color, onClick: () -> Unit) {
    val backgroundColor = if (notificacion.leido) Color.White else MintAndes

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
                        fontSize = 15.sp,
                        color = Color(0xFF263C35)
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
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }

            if (!notificacion.leido) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colorAndes)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}