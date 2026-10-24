package com.andes.bpo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.TrackingViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications

@Composable
fun TrackingScreen(
    navController: NavController,
    rut: String,
    viewModel: TrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Variable para controlar si se muestra la alerta de cierre de sesión
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(rut) {
        viewModel.cargarDatosSiniestro(rut)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1E40AF))
        }
    } else {
        val textoEstado = when(uiState.estadoActual) {
            1 -> "Recibido"
            2 -> "En evaluación"
            3 -> "En liquidación"
            4 -> "Cerrado"
            else -> "En progreso"
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F6F9))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { navController.navigate("notificaciones") },
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        tint = Color(0xFF1E40AF)
                    )
                }
            }
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E40AF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MI SINIESTRO ACTIVO",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BadgeEstado(textoEstado)
                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    // En lugar de salir directo, mostramos la alerta
                                    showLogoutDialog = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Cerrar sesión",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = uiState.numeroSiniestro,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoBoxItem(titulo = "Tipo", valor = uiState.tipo)
                        InfoBoxItem(titulo = "Fecha ocurrencia", valor = uiState.fechaOcurrencia)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InfoBoxItem(titulo = "Vehículo", valor = uiState.patente)
                        InfoBoxItem(titulo = "Reporte", valor = uiState.fechaReporte)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    StepperSiniestro(pasoActual = uiState.estadoActual)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF818CF8),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.nombreLiquidador.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Liquidador/a asignado/a", color = Color.Gray, fontSize = 12.sp)
                        Text(uiState.nombreLiquidador, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                        Text(uiState.empresaLiquidador, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("evidencia") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver / Adjuntar Evidencia", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("historial") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Historial de Gestiones", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalles del siniestro", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(12.dp))
                    DetalleFila("Subtipo", uiState.subtipo)
                    DetalleFila("Modelo", uiState.modeloVehiculo)
                    DetalleFila("Póliza", uiState.numeroPoliza)
                    DetalleFila("Aseguradora", uiState.aseguradora)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Te notificaremos de inmediato ante cualquier cambio de estado. No necesitas llamar ni visitar una oficina.",
                        color = Color(0xFF1E40AF),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // --- ALERTA DE CIERRE DE SESIÓN ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("¿Estás seguro de que deseas salir? Tendrás que volver a ingresar tu RUT para ver el estado de tu siniestro.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Sí, salir", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InfoBoxItem(titulo: String, valor: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.width(150.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(titulo, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Text(valor, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun BadgeEstado(texto: String) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFFFEF3C7)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = Color(0xFFD97706), modifier = Modifier.size(6.dp)) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(texto, color = Color(0xFF92400E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StepperSiniestro(pasoActual: Int) {
    val pasos = listOf("Recibido", "En evaluación", "En liquidación", "Cerrado")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        pasos.forEachIndexed { index, paso ->
            val numero = index + 1
            val completado = numero <= pasoActual
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = if (completado) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (completado && numero < pasoActual) "✓" else "$numero",
                            color = if (completado) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = paso,
                    color = if (completado) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = if (completado) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DetalleFila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, color = Color.Gray, fontSize = 13.sp)
        Text(valor, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF374151))
    }
}