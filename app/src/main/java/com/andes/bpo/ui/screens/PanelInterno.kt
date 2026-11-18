package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.andes.bpo.utils.SesionUsuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelInternoScreen(
    navController: NavController,
    rolRequerido: String
) {
    val sesion by SesionUsuario.usuarioActual.collectAsState()
    val usuario = sesion

    // Comprobar que el rol corresponde a esta pantalla
    if (usuario == null || usuario.rol != rolRequerido) {
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
                }
            ) {
                Text("Volver al inicio de sesión")
            }
        }
        return
    }

    val esLiquidador = rolRequerido == "LIQUIDADOR"

    val titulo = if (esLiquidador) {
        "Panel del liquidador"
    } else {
        "Panel del analista"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¡Hola, ${usuario.nombre}!",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Bienvenido al sistema de gestión de siniestros Andes BPO.",
                style = MaterialTheme.typography.bodyMedium
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (esLiquidador) {
                            "Funciones del liquidador"
                        } else {
                            "Funciones del analista"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (esLiquidador) {
                        Text("• Consultar siniestros asignados")
                        Text("• Registrar gestiones")
                        Text("• Solicitar documentos")
                        Text("• Avanzar etapas")
                    } else {
                        Text("• Consultar siniestros")
                        Text("• Revisar casos sin asignar")
                        Text("• Asignar liquidadores")
                        Text("• Reasignar casos")
                    }

                    Text(
                        text = "Estas funciones se conectarán en los siguientes pasos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (esLiquidador) {
                Button(
                    onClick = {
                        navController.navigate("bandeja_liquidador")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver mis casos asignados")
                }
            } else {
                Button(
                    onClick = {
                        navController.navigate("bandeja_analista")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver siniestros sin asignar")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    SesionUsuario.cerrarSesion()

                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
