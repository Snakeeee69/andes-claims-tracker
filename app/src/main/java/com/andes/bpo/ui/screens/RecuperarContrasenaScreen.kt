package com.andes.bpo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var rut by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var claveNueva by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }

    var verClaveNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }

    LaunchedEffect(estado.claveRestablecida) {
        if (estado.claveRestablecida) {
            Toast.makeText(
                context,
                "ContraseÃ±a actualizada correctamente",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseÃ±a") },
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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (estado.claveRestablecida) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Â¡ContraseÃ±a actualizada!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Ya puedes iniciar sesiÃ³n con tu nueva contraseÃ±a. " +
                                    "El cÃ³digo de verificaciÃ³n utilizado ha quedado invalidado."
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver al inicio de sesiÃ³n")
                        }
                    }
                }
            } else {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "1. Solicitar cÃ³digo al correo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Ingresa tu RUT y correo registrado para recibir un cÃ³digo de 6 dÃ­gitos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = rut,
                            onValueChange = { input ->
                                rut = input
                                    .replace(Regex("[^0-9kK]"), "")
                                    .uppercase()
                                    .take(9)
                                viewModel.limpiarError()
                            },
                            label = { Text("RUT") },
                            placeholder = { Text("19.876.544-4") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                capitalization = KeyboardCapitalization.Characters
                            ),
                            visualTransformation = RutVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !estado.cargando
                        )
                        OutlinedTextField(
                            value = correo,
                            onValueChange = {
                                correo = it.trim()
                                viewModel.limpiarError()
                            },
                            label = { Text("Correo electrÃ³nico registrado") },
                            placeholder = { Text("usuario@correo.cl") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !estado.cargando
                        )
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.solicitarCodigoCorreo(rut, correo)
                            },
                            enabled = !estado.cargando && rut.isNotBlank() && correo.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (estado.cargando) "Enviando..." else "Enviar cÃ³digo al correo")
                        }

                        if (estado.codigoEnviadoCorreo) {
                            Text(
                                text = estado.mensajeEnvio
                                    ?: "Revisa tu bandeja de entrada (o spam) e ingresa el cÃ³digo de 6 dÃ­gitos abajo.",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "2. Restablecer contraseÃ±a",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = codigo,
                            onValueChange = { input ->
                                codigo = input.filter { it.isDigit() }.take(6)
                                viewModel.limpiarError()
                            },
                            label = { Text("CÃ³digo de verificaciÃ³n (6 dÃ­gitos)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            label = { Text("Nueva contraseÃ±a") },
                            supportingText = { Text("8 a 72 caracteres, con letras y nÃºmeros") },
                            visualTransformation = if (verClaveNueva) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { verClaveNueva = !verClaveNueva }) {
                                    Icon(
                                        imageVector = if (verClaveNueva) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = null
                                    )
                                }
                            },
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
                            label = { Text("Confirmar nueva contraseÃ±a") },
                            visualTransformation = if (verConfirmar) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { verConfirmar = !verConfirmar }) {
                                    Icon(
                                        imageVector = if (verConfirmar) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = null
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !estado.cargando
                        )

                        estado.error?.let { mensaje ->
                            Text(
                                text = mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
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
                            Text(if (estado.cargando) "Verificando..." else "Cambiar contraseÃ±a")
                        }
                    }
                }
            }
        }
    }
}
