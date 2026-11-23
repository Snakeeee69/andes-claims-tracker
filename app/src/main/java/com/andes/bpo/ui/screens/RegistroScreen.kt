package com.andes.bpo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    viewModel: RegistroViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val esEmpresa = estado.tipoAsegurado == "ASEGURADO_EMPRESA"
    var mostrarDialogoTerminos by remember { mutableStateOf(false) }

    if (mostrarDialogoTerminos) {
        DialogoTerminosYCondiciones(
            onDismiss = { mostrarDialogoTerminos = false },
            onAceptar = {
                viewModel.cambiarAceptacionTerminos(true)
                mostrarDialogoTerminos = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Completa tus datos para registrarte como asegurado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (estado.registrado) {
                Text(
                    text = "Cuenta registrada correctamente.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al inicio de sesión")
                }
            } else {
                // 1. IDENTIFICACIÓN
                TituloSeccion("1. Identificación")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !esEmpresa,
                        onClick = { viewModel.cambiarTipoAsegurado("ASEGURADO_NATURAL") },
                        label = { Text("Persona Natural") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = esEmpresa,
                        onClick = { viewModel.cambiarTipoAsegurado("ASEGURADO_EMPRESA") },
                        label = { Text("Empresa") },
                        modifier = Modifier.weight(1f)
                    )
                }

                CampoRegistro(
                    etiqueta = if (esEmpresa) "RUT Empresa" else "RUT",
                    valor = estado.rut,
                    onCambio = { viewModel.cambiarCampo("rut", it) },
                    error = estado.errores["rut"],
                    capitalizacion = KeyboardCapitalization.Characters,
                    transformacionVisual = RutVisualTransformation()
                )

                CampoRegistro(
                    etiqueta = if (esEmpresa) "Razón Social" else "Nombre",
                    valor = estado.nombre,
                    onCambio = { viewModel.cambiarCampo("nombre", it) },
                    error = estado.errores["nombre"]
                )

                CampoRegistro(
                    etiqueta = if (esEmpresa) "Tipo de sociedad (ej. SpA, Ltda.)" else "Apellido",
                    valor = estado.apellido,
                    onCambio = { viewModel.cambiarCampo("apellido", it) },
                    error = estado.errores["apellido"]
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // 2. CONTACTO
                TituloSeccion("2. Contacto")

                CampoRegistro(
                    etiqueta = "Correo electrónico",
                    valor = estado.correo,
                    onCambio = { viewModel.cambiarCampo("correo", it) },
                    error = estado.errores["correo"],
                    tipoTeclado = KeyboardType.Email
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // 3. DATOS DEL SEGURO Y UBICACIÓN
                TituloSeccion("3. Seguro y Ubicación")

                SelectorDesplegable(
                    etiqueta = "Aseguradora",
                    seleccionado = estado.aseguradora,
                    opciones = RegistroViewModel.ASEGURADORAS,
                    onSeleccionar = { viewModel.cambiarCampo("aseguradora", it) },
                    error = estado.errores["aseguradora"]
                )

                SelectorDesplegable(
                    etiqueta = "Región",
                    seleccionado = estado.region,
                    opciones = RegistroViewModel.REGIONES_CHILE,
                    onSeleccionar = { viewModel.cambiarCampo("region", it) },
                    error = estado.errores["region"]
                )

                CampoRegistro(
                    etiqueta = "Dirección (Calle y número - opcional)",
                    valor = estado.direccion,
                    onCambio = { viewModel.cambiarCampo("direccion", it) },
                    error = estado.errores["direccion"]
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // 4. SEGURIDAD
                TituloSeccion("4. Seguridad")

                CampoRegistro(
                    etiqueta = "Contraseña",
                    valor = estado.contrasena,
                    onCambio = { viewModel.cambiarCampo("contrasena", it) },
                    error = estado.errores["contrasena"],
                    tipoTeclado = KeyboardType.Password,
                    esPassword = true
                )

                CampoRegistro(
                    etiqueta = "Confirmar contraseña",
                    valor = estado.confirmacion,
                    onCambio = { viewModel.cambiarCampo("confirmacion", it) },
                    error = estado.errores["confirmacion"],
                    tipoTeclado = KeyboardType.Password,
                    esPassword = true
                )

                // 5. TÉRMINOS Y CONDICIONES
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = estado.aceptaTerminos,
                            onCheckedChange = { viewModel.cambiarAceptacionTerminos(it) }
                        )
                        val textoTerminos = buildAnnotatedString {
                            append("He leído y acepto los ")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Términos y Condiciones")
                            }
                            append(" de Servicios Corporativos Andes SpA.")
                        }
                        Text(
                            text = textoTerminos,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { mostrarDialogoTerminos = true }
                        )
                    }

                    estado.errores["terminos"]?.let { errorTerminos ->
                        Text(
                            text = errorTerminos,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                        )
                    }
                }

                if (estado.errorGeneral != null) {
                    Text(
                        text = estado.errorGeneral!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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
}

@Composable
private fun DialogoTerminosYCondiciones(
    onDismiss: () -> Unit,
    onAceptar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Términos y Condiciones de Uso",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SERVICIOS CORPORATIVOS ANDES SPA (ANDES BPO)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                ClausulaTerminos(
                    titulo = "1. Objeto de la Plataforma",
                    cuerpo = "Esta aplicación permite a asegurados (personas naturales y empresas) denunciar siniestros, adjuntar respaldos documentales y consultar en línea el estado de sus procesos de liquidación en las áreas de Vehículos, Hogar/Incendio y Salud/Vida."
                )

                ClausulaTerminos(
                    titulo = "2. Veracidad de los Antecedentes",
                    cuerpo = "El usuario declara bajo su responsabilidad que los datos proporcionados, pólizas y documentos adjuntos (fotografías, declaraciones, presupuestos, certificados o boletas) son auténticos y fidedignos. La entrega de información falsa o adulterada facultará el rechazo del siniestro conforme al Código de Comercio."
                )

                ClausulaTerminos(
                    titulo = "3. Plazos Legales de Liquidación (D.S. N° 1.055)",
                    cuerpo = "La tramitación y liquidación de siniestros se rige por el Decreto Supremo N° 1.055 del Ministerio de Hacienda. El plazo general de resolución es de 45 días corridos desde la denuncia, pudiendo prorrogarse fundadamente por 90 o 180 días en los casos calificados que autoriza la normativa vigente."
                )

                ClausulaTerminos(
                    titulo = "4. Privacidad y Protección de Datos (Ley N° 19.628)",
                    cuerpo = "Los datos personales, patrimoniales y médicos ingresados serán tratados con estricta confidencialidad de acuerdo con la Ley N° 19.628 sobre Protección de la Vida Privada, y se utilizarán exclusivamente para gestionar la evaluación, liquidación y resolución del siniestro ante la compañía aseguradora y los liquidadores designados."
                )

                ClausulaTerminos(
                    titulo = "5. Notificaciones Electrónicas y Seguridad",
                    cuerpo = "El asegurado acepta recibir notificaciones sobre solicitudes de documentos, asignaciones y resoluciones a través de la aplicación y al correo electrónico registrado. Es deber del usuario custodiar la confidencialidad de su clave de acceso."
                )
            }
        },
        confirmButton = {
            Button(onClick = onAceptar) {
                Text("Aceptar y continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun ClausulaTerminos(titulo: String, cuerpo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = titulo,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = cuerpo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorDesplegable(
    etiqueta: String,
    seleccionado: String,
    opciones: List<String>,
    onSeleccionar: (String) -> Unit,
    error: String?
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
            },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else {
                null
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    }
                )
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
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
    esPassword: Boolean = false,
    transformacionVisual: VisualTransformation = VisualTransformation.None
) {
    var passwordVisible by remember { mutableStateOf(false) }

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
        trailingIcon = if (esPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            capitalization = capitalizacion
        ),
        visualTransformation = when {
            esPassword && !passwordVisible -> PasswordVisualTransformation()
            esPassword && passwordVisible -> VisualTransformation.None
            else -> transformacionVisual
        }
    )
}