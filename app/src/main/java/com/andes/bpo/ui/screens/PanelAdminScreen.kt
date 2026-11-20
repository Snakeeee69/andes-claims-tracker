package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.utils.SesionUsuario
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelAdminScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context, scope) }
    val usuarios by db.usuarioDao().obtenerTodosLosUsuarios().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administrador Andes BPO") },
                actions = {
                    TextButton(onClick = {
                        SesionUsuario.cerrarSesion()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }) { Text("Salir") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Gestión de Usuarios y Roles (${usuarios.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Catálogo de 6 roles y equipos activos en el sistema.", style = MaterialTheme.typography.bodySmall)
            }
            items(usuarios, key = { it.idUsuario }) { user ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${user.nombre} ${user.apellido}", fontWeight = FontWeight.Bold)
                        Text("RUT: ${user.rut} • ${user.correo}", style = MaterialTheme.typography.bodySmall)
                        Text("Rol: ${user.rol}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        user.equipo?.let { Text("Equipo: $it (${user.tipoLiquidador ?: "Interno"})", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}