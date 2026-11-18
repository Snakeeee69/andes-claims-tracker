package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.RecuperacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContrasenaScreen(
    navController: NavController,
    viewModel: RecuperacionViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var rut by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var claveNueva by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (estado.claveRestablecida) {
                Text("¡Contraseña actualizada!", style = MaterialTheme.typography.headlineSmall)
                Text("Ya puedes iniciar sesión con tu nueva contraseña. " +
                        "El código de recuperación utilizado ha quedado invalidado.")
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Volver al inicio de sesión") }
            } else {
                Text(
                    "Ingresa el RUT y el código que generaste desde tu cuenta. " +
                            "No se enviará ningún correo en esta versión local."
                )
                OutlinedTextField(
                    value = rut,
                    onValueChange = {
                        rut = it
                        viewModel.limpiarError()
                    },
                    label = { Text("RUT") },
                    placeholder = { Text("12345678-9") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !estado.cargando
                )
                OutlinedTextField(
                    value = codigo,
                    onValueChange = {
                        codigo = it
                        viewModel.limpiarError()
                    },
                    label = { Text("Código de recuperación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !estado.cargando
                )
                OutlinedTextField(
                    value = claveNueva,
                    onValueChange = {
                        claveNueva = it
                        viewModel.limpiarError()
                    },
                    label = { Text("Nueva contraseña") },
                    supportingText = { Text("8 a 72 caracteres, con letras y números") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !estado.cargando
                )
                OutlinedTextField(
                    value = confirmarClave,
                    onValueChange = {
                        confirmarClave = it
                        viewModel.limpiarError()
                    },
                    label = { Text("Confirmar nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !estado.cargando
                )

                estado.error?.let { mensaje ->
                    Text(mensaje, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        viewModel.restablecerClave(
                            rutIngresado = rut,
                            codigoIngresado = codigo,
                            nuevaClave = claveNueva,
                            confirmarClave = confirmarClave
                        )
                    },
                    enabled = !estado.cargando && rut.isNotBlank() &&
                            codigo.isNotBlank() && claveNueva.isNotBlank() && confirmarClave.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.cargando) "Verificando..." else "Cambiar contraseña")
                }
            }
        }
    }
}
