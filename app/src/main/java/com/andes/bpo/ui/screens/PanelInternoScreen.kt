package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.utils.SesionUsuario

@Composable
fun PanelInternoScreen(
    navController: NavController,
    rolRequerido: String
) {
    val sesion by SesionUsuario.usuarioActual.collectAsState()
    val usuario = sesion

    val esRolValido = when (rolRequerido) {
        "LIQUIDADOR" -> usuario != null && usuario.rol.startsWith("LIQUIDADOR")
        "ANALISTA" -> usuario != null && (usuario.rol == "ANALISTA" || usuario.rol == "ADMINISTRADOR")
        else -> usuario != null && usuario.rol == rolRequerido
    }

    if (!esRolValido || usuario == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "No tienes permiso para acceder a esta pantalla.",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    SesionUsuario.cerrarSesion()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes)
            ) {
                Text("Volver al inicio de sesiÃ³n")
            }
        }
        return
    }

    when {
        usuario.rol == "ANALISTA" || usuario.rol == "ADMINISTRADOR" -> {
            BandejaAnalistaScreen(
                navController = navController,
                esModoAdmin = usuario.rol == "ADMINISTRADOR"
            )
        }
        usuario.rol.startsWith("LIQUIDADOR") -> {
            BandejaLiquidadorScreen(navController = navController)
        }
    }
}
