package com.andes.bpo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.R
import com.andes.bpo.ui.theme.BackgroundAndes
import com.andes.bpo.ui.theme.MintAndes
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.viewmodel.TrackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    navController: NavController,
    rut: String,
    viewModel: TrackingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAlertaSiniestroActivo by remember { mutableStateOf(false) }

    LaunchedEffect(rut) {
        viewModel.cargarDatosSiniestro(rut)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryAndes)
        }
    } else {
        val textoEstado = when(uiState.estadoActual) {
            1 -> "Recibido"
            2 -> "En evaluación"
            3 -> "En liquidación"
            4 -> "Cerrado"
            else -> "En progreso"
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                                contentDescription = "Logo Andes BPO",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Andes BPO",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = PrimaryAndes
                            )
                        }
                    },
                    actions = {

                        IconButton(onClick = {

                            if (uiState.numeroSiniestro.isNotEmpty() && uiState.estadoActual < 4) {
                                showAlertaSiniestroActivo = true
                            } else {
                                navController.navigate("nuevo_siniestro")
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Nuevo Siniestro",
                                tint = PrimaryAndes
                            )
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Surface(
                                shape = CircleShape,
                                color = MintAndes,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "CR",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryAndes
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BackgroundAndes
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryAndes,
                            selectedTextColor = PrimaryAndes,
                            indicatorColor = MintAndes
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("historial") },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Mis Siniestros") },
                        label = { Text("Siniestros", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("notificaciones") },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Avisos") },
                        label = { Text("Avisos", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("ayuda") },
                        icon = { Icon(Icons.Default.HelpOutline, contentDescription = "Centro de Ayuda") },
                        label = { Text("Ayuda", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            },
            containerColor = BackgroundAndes
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "¡Hola, Camila!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263C35)
                )
                Text(
                    text = "Revisa aquí el avance y detalle de tu caso en tiempo real.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryAndes),
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

                            BadgeEstado(textoEstado)
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
                            color = PrimaryAndes.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = uiState.nombreLiquidador.take(1).uppercase(),
                                    color = PrimaryAndes,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Liquidador/a asignado/a", color = Color.Gray, fontSize = 12.sp)
                            Text(uiState.nombreLiquidador, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF263C35))
                            Text(uiState.empresaLiquidador, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("evidencia") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes)
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver / Adjuntar Evidencias", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Detalles del siniestro", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF263C35))
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
                    color = MintAndes,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("ℹ️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Te notificaremos de inmediato ante cualquier cambio de estado. No necesitas llamar ni visitar una oficina.",
                            color = PrimaryAndes,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
    if (showAlertaSiniestroActivo) {
        AlertDialog(
            onDismissRequest = { showAlertaSiniestroActivo = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Siniestro en curso", fontWeight = FontWeight.Bold, color = Color.Red)
                }
            },
            text = {
                Text("Ya posees el siniestro ${uiState.numeroSiniestro} en proceso de evaluación. Para añadir antecedentes o reportar un nuevo evento de la póliza, contacta directamente a tu liquidador asignado.")
            },
            confirmButton = {
                TextButton(onClick = { showAlertaSiniestroActivo = false }) {
                    Text("Entendido", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas salir? Tendrás que volver a ingresar tu RUT para ver el estado de tu siniestro.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                ) { Text("Sí, salir", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
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
        Text(valor, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF263C35))
    }
}