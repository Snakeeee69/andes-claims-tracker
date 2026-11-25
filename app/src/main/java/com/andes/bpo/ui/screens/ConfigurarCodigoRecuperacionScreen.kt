package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.RecuperacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarCodigoRecuperacionScreen(
    navController: NavController,
    viewModel: RecuperacionViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    var claveActual by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CÃ³digo de recuperaciÃ³n") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Este cÃ³digo te permitirÃ¡ recuperar la contraseÃ±a si la olvidas. " +
                        "Debes guardarlo en un lugar seguro: no se enviarÃ¡ por correo."
            )

            if (estado.codigoGenerado == null) {
                OutlinedTextField(
                    value = claveActual,
                    onValueChange = {
                        claveActual = it
                        viewModel.limpiarError()
                    },
                    label = { Text("Confirma tu contraseÃ±a actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !estado.cargando
                )
                Button(
                    onClick = {
                        viewModel.generarCodigo(claveActual)
                        claveActual = ""
                    },
                    enabled = claveActual.isNotBlank() && !estado.cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.cargando) "Generando..." else "Generar cÃ³digo")
                }
            }

            estado.error?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            estado.codigoGenerado?.let { codigo ->
                HorizontalDivider()
                Text("Tu cÃ³digo de recuperaciÃ³n", style = MaterialTheme.typography.titleMedium)
                SelectionContainer {
                    Text(codigo, style = MaterialTheme.typography.titleLarge)
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(codigo)) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Copiar cÃ³digo") }
                Text(
                    "GuÃ¡rdalo ahora. Al salir de esta pantalla no volveremos a mostrarlo. " +
                            "Si generas otro cÃ³digo, el anterior dejarÃ¡ de funcionar.",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Listo, guardÃ© el cÃ³digo") }
            }
        }
    }
}
