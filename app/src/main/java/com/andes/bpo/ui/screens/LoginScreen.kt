package com.andes.bpo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.R
import com.andes.bpo.utils.SesionUsuario
import com.andes.bpo.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var contrasenaVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            val usuario = SesionUsuario.obtenerSesion()

            val destino = if (uiState.requiereCambioClave) {
                "cambio_clave_obligatorio"
            } else {
                when (usuario?.rol) {
                    "ASEGURADO_NATURAL", "ASEGURADO_EMPRESA", "ASEGURADO" -> "tracking/${usuario.rut}"
                    "LIQUIDADOR_EXTERNO", "LIQUIDADOR_RESPALDO", "LIQUIDADOR" -> "panel_liquidador"
                    "ANALISTA" -> "panel_analista"
                    "ADMINISTRADOR" -> "panel_admin"
                    else -> null
                }
            }

            if (destino != null) {
                navController.navigate(destino) {
                    popUpTo("login") {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "Logo Andes BPO",
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Andes BPO",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    text = "Seguimiento de Siniestros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Portal Asegurados",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(bottom = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = uiState.rut,
                            onValueChange = { viewModel.onRutChange(it) },
                            label = { Text("RUT") },
                            placeholder = { Text("19.876.544-4") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                capitalization = KeyboardCapitalization.Characters
                            ),
                            visualTransformation = RutVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = uiState.error != null,
                            trailingIcon = {
                                if (uiState.error != null) {
                                    Icon(
                                        Icons.Filled.Error,
                                        contentDescription = "Error de validación",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.contrasena,
                            onValueChange = { viewModel.onContrasenaChange(it) },
                            label = { Text("Contraseña") },
                            visualTransformation = if (contrasenaVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = uiState.error != null,
                            trailingIcon = {
                                IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                                    Icon(
                                        imageVector = if (contrasenaVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = if (contrasenaVisible) {
                                            "Ocultar contraseña"
                                        } else {
                                            "Mostrar contraseña"
                                        }
                                    )
                                }
                            },
                            singleLine = true
                        )

                        if (uiState.error != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.login() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Iniciar Sesión",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { navController.navigate("recuperar") }
                        ) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextButton(
                            onClick = { navController.navigate("registro") }
                        ) {
                            Text("¿No tienes cuenta? Crear cuenta")
                        }
                    }
                }
            }
        }
    }
}

class RutVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.length <= 1) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val cuerpo = raw.dropLast(1)
        val dv = raw.last()
        val cuerpoFormateado = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        val formatted = "$cuerpoFormateado-$dv"

        val origToTrans = IntArray(raw.length + 1)
        var transIdx = 0
        for (i in raw.indices) {
            origToTrans[i] = transIdx
            transIdx++
            while (transIdx < formatted.length && (formatted[transIdx] == '.' || formatted[transIdx] == '-')) {
                transIdx++
            }
        }
        origToTrans[raw.length] = formatted.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return origToTrans[offset.coerceIn(0, raw.length)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, formatted.length)
                var rawCount = 0
                for (i in 0 until safeOffset) {
                    if (formatted[i] != '.' && formatted[i] != '-') {
                        rawCount++
                    }
                }
                return rawCount.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}