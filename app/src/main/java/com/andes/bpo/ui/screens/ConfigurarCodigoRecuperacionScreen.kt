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
                title = { Text("Código de recuperación") },
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
                "Este código te permitirá recuperar la contraseña si la olvidas. " +
                        "Debes guardarlo en un lugar seguro: no se enviará por correo."
            )

            if (estado.codigoGenerado == null) {
                OutlinedTextField(
                    value = claveActual,
                    onValueChange = {
                        claveActual = it
                        viewModel.limpiarError()
                    },
                    label = { Text("Confirma tu contraseña actual") },
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
                    Text(if (estado.cargando) "Generando..." else "Generar código")
                }
            }

            estado.error?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }

            estado.codigoGenerado?.let { codigo ->
                HorizontalDivider()
                Text("Tu código de recuperación", style = MaterialTheme.typography.titleMedium)
                SelectionContainer {
                    Text(codigo, style = MaterialTheme.typography.titleLarge)
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(codigo)) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Copiar código") }
                Text(
                    "Guárdalo ahora. Al salir de esta pantalla no volveremos a mostrarlo. " +
                            "Si generas otro código, el anterior dejará de funcionar.",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Listo, guardé el código") }
            }
        }
    }
}
