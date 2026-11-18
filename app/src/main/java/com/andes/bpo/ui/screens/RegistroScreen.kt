
package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    navController: NavController,
    viewModel: RegistroViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Completa tus datos para registrarte como asegurado.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (estado.registrado) {
            Text(
                text = "Cuenta registrada correctamente en Room.",
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al inicio de sesión")
            }
        } else {
            CampoRegistro(
                "RUT",
                estado.rut,
                { viewModel.cambiarCampo("rut", it) },
                estado.errores["rut"]
            )

            CampoRegistro(
                "Nombre",
                estado.nombre,
                { viewModel.cambiarCampo("nombre", it) },
                estado.errores["nombre"]
            )

            CampoRegistro(
                "Apellido",
                estado.apellido,
                { viewModel.cambiarCampo("apellido", it) },
                estado.errores["apellido"]
            )

            CampoRegistro(
                "Correo electrónico",
                estado.correo,
                { viewModel.cambiarCampo("correo", it) },
                estado.errores["correo"],
                KeyboardType.Email
            )

            CampoRegistro(
                "Contraseña",
                estado.contrasena,
                { viewModel.cambiarCampo("contrasena", it) },
                estado.errores["contrasena"],
                KeyboardType.Password,
                true
            )

            CampoRegistro(
                "Confirmar contraseña",
                estado.confirmacion,
                { viewModel.cambiarCampo("confirmacion", it) },
                estado.errores["confirmacion"],
                KeyboardType.Password,
                true
            )

            if (estado.errorGeneral != null) {
                Text(
                    text = estado.errorGeneral!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.registrar() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !estado.guardando
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear cuenta")
                }
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ya tengo cuenta")
            }
        }
    }
}

@Composable
private fun CampoRegistro(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    error: String?,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    esPassword: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado
        ),
        visualTransformation = if (esPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        }
    )
}
