package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.utils.SesionUsuario
import com.andes.bpo.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambioClaveObligatorioScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    var nuevaClave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Actualización obligatoria de clave") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Ingresaste con una clave provisoria",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Por seguridad, debes crear una contraseña personal antes de operar en Andes BPO.")

            OutlinedTextField(
                value = nuevaClave,
                onValueChange = { nuevaClave = it; error = null },
                label = { Text("Nueva contraseña") },
                supportingText = { Text("Mínimo 8 caracteres, letras y números") },
                visualTransformation = PasswordVisualTransformation(),
                isError = error != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = confirmarClave,
                onValueChange = { confirmarClave = it; error = null },
                label = { Text("Confirmar nueva contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                isError = error != null,
                supportingText = { error?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    viewModel.completarCambioClaveObligatorio(
                        nuevaClave = nuevaClave,
                        confirmarClave = confirmarClave,
                        onExito = {
                            val usuario = SesionUsuario.obtenerSesion()
                            val destino = when {
                                usuario?.rol?.startsWith("ASEGURADO") == true -> "tracking/${usuario.rut}"
                                usuario?.rol?.startsWith("LIQUIDADOR") == true -> "panel_liquidador"
                                usuario?.rol == "ANALISTA" -> "panel_analista"
                                usuario?.rol == "ADMINISTRADOR" -> "panel_admin"
                                else -> "login"
                            }
                            navController.navigate(destino) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onError = { error = it }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar contraseña y continuar")
            }
        }
    }
}