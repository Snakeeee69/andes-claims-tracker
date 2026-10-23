package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContrasenaScreen(navController: NavController) {
    var correoORut by remember { mutableStateOf("") }
    var enviado by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (!enviado) {
                Text(
                    text = "Ingresa tu RUT o correo electrónico asociado y te enviaremos las instrucciones para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = correoORut,
                    onValueChange = { correoORut = it },
                    label = { Text("RUT o Correo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Aquí iría la lógica de conexión al backend en el futuro
                        enviado = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = correoORut.isNotBlank()
                ) {
                    Text("Enviar instrucciones")
                }
            } else {
                // Mensaje de éxito
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Puedes cambiarlo por un icono de Check si tienes
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                )

                Text(
                    text = "¡Instrucciones enviadas!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Revisa tu bandeja de entrada o carpeta de spam.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al inicio de sesión")
                }
            }
        }
    }
}