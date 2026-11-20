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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.data.local.entity.AvisoEntity
import com.andes.bpo.ui.theme.BackgroundAndes
import com.andes.bpo.ui.theme.MintAndes
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(navController: NavController, rut: String = "") {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context, scope) }
    val sesion = SesionUsuario.obtenerSesion()

    val avisos by remember(sesion?.idUsuario) {
        if (sesion != null) db.siniestroDao().getAvisosPorUsuario(sesion.idUsuario)
        else db.siniestroDao().getAvisos()
    }.collectAsState(initial = emptyList())

    var avisoSeleccionado by remember { mutableStateOf<AvisoEntity?>(null) }

    avisoSeleccionado?.let { aviso ->
        AlertDialog(
            onDismissRequest = { avisoSeleccionado = null },
            title = { Text("Aviso del Siniestro", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(aviso.fechaHora, fontSize = 12.sp, color = Color.Gray)
                    Text(aviso.mensaje, fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { avisoSeleccionado = null }) { Text("Entendido") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrimaryAndes)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundAndes, titleContentColor = PrimaryAndes)
            )
        },
        containerColor = BackgroundAndes
    ) { paddingValues ->
        if (avisos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No tienes notificaciones registradas aún.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(avisos, key = { it.idAviso }) { aviso ->
                    val bg = if (aviso.leido) Color.White else MintAndes
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            avisoSeleccionado = aviso
                            if (!aviso.leido) {
                                scope.launch {
                                    db.siniestroDao().actualizarEstadoAviso(aviso.idAviso, "LEIDO")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bg)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier.size(40.dp).background(if (aviso.leido) Color.LightGray else PrimaryAndes, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(aviso.fechaHora, fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aviso.mensaje,
                                    fontSize = 13.sp,
                                    fontWeight = if (aviso.leido) FontWeight.Normal else FontWeight.Bold,
                                    color = Color(0xFF263C35)
                                )
                            }
                            if (!aviso.leido) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PrimaryAndes))
                            }
                        }
                    }
                }
            }
        }
    }
}