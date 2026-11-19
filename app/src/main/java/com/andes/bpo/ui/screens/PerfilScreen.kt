package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var claveActual by remember { mutableStateOf("") }
    var claveNueva by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }

    LaunchedEffect(estado.claveActualizada) {
        if (estado.claveActualizada) {
            claveActual = ""
            claveNueva = ""
            confirmarClave = ""
            viewModel.limpiarConfirmacionClave()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator()
            } else {
                Text("Datos personales", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = estado.rut,
                    onValueChange = {},
                    label = { Text("RUT (no editable)") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = viewModel::cambiarNombre,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorNombre != null,
                    supportingText = { estado.errorNombre?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = estado.apellido,
                    onValueChange = viewModel::cambiarApellido,
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorApellido != null,
                    supportingText = { estado.errorApellido?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = estado.correo,
                    onValueChange = viewModel::cambiarCorreo,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorCorreo != null,
                    supportingText = { estado.errorCorreo?.let { Text(it) } }
                )
                Button(
                    onClick = viewModel::guardarPerfil,
                    enabled = !estado.guardandoPerfil && !estado.cambiandoClave,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.guardandoPerfil) "Guardando..." else "Guardar cambios")
                }

                HorizontalDivider()
                Text("Cambiar contraseña", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = claveActual,
                    onValueChange = { claveActual = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorClaveActual != null,
                    supportingText = { estado.errorClaveActual?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = claveNueva,
                    onValueChange = { claveNueva = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorClaveNueva != null,
                    supportingText = { estado.errorClaveNueva?.let { Text(it) } }
                )
                OutlinedTextField(
                    value = confirmarClave,
                    onValueChange = { confirmarClave = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = estado.errorConfirmacion != null,
                    supportingText = { estado.errorConfirmacion?.let { Text(it) } }
                )
                Button(
                    onClick = {
                        viewModel.cambiarContrasena(claveActual, claveNueva, confirmarClave)
                    },
                    enabled = !estado.cambiandoClave && !estado.guardandoPerfil,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.cambiandoClave) "Actualizando..." else "Cambiar contraseña")
                }

                estado.errorGeneral?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
                estado.mensaje?.let { mensaje ->
                    Text(mensaje, color = MaterialTheme.colorScheme.primary)
                }

                OutlinedButton(
                    onClick = { navController.navigate("configurar_recuperacion") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configurar código de recuperación")
                }
            }
        }
    }
}
