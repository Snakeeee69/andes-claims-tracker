package com.andes.bpo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.viewmodel.NuevoSiniestroViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.saveable.rememberSaveable

fun formatearFechaInput(input: String): String {
    val digitos = input.filter { it.isDigit() }.take(8)
    val sb = StringBuilder()
    for (i in digitos.indices) {
        if (i == 2 || i == 4) {
            sb.append('/')
        }
        sb.append(digitos[i])
    }
    return sb.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoSiniestroScreen(
    navController: NavController,
    rut: String,
    viewModel: NuevoSiniestroViewModel = viewModel()
) {
    val colorAndes = PrimaryAndes
    val context = LocalContext.current


    var compania by remember { mutableStateOf("") }
    var numeroPoliza by remember { mutableStateOf("") }
    var tipoSiniestro by remember { mutableStateOf("Colisión") }
    var modeloVehiculo by remember { mutableStateOf("Toyota Yaris 2023") }
    var fechaOcurrencia by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }

    val areas by viewModel.areas.collectAsState()

    var idAreaSeleccionada by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var menuAreasExpandido by remember {
        mutableStateOf(false)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Nuevo Siniestro", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorAndes
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(titleContentColor = colorAndes)
            )
        }
    ) { paddingValues ->
        AnimacionEntrada {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Completa los datos del evento para ingresar la solicitud al sistema.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = compania,
                    onValueChange = { compania = it },
                    label = { Text("Compañía de Seguros") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = numeroPoliza,
                    onValueChange = { if (it.length <= 12) numeroPoliza = it },
                    label = { Text("Número de Póliza (Solo números)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = menuAreasExpandido,
                    onExpandedChange = { expandido ->
                        menuAreasExpandido =
                            expandido && areas.isNotEmpty()
                    }
                ) {
                    OutlinedTextField(
                        value = areas.firstOrNull {
                            it.idArea == idAreaSeleccionada
                        }?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Área del siniestro")
                        },
                        placeholder = {
                            Text("Selecciona un área")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = menuAreasExpandido
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        isError = idAreaSeleccionada == null &&
                                errorMensaje != null
                    )

                    ExposedDropdownMenu(
                        expanded = menuAreasExpandido,
                        onDismissRequest = {
                            menuAreasExpandido = false
                        }
                    ) {
                        areas.forEach { area ->

                            DropdownMenuItem(
                                text = {
                                    Text(area.nombre)
                                },
                                onClick = {
                                    idAreaSeleccionada = area.idArea
                                    menuAreasExpandido = false
                                    errorMensaje = null
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tipoSiniestro,
                    onValueChange = { tipoSiniestro = it },
                    label = { Text("Tipo de Siniestro (ej: Colisión)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = modeloVehiculo,
                    onValueChange = { modeloVehiculo = it },
                    label = { Text("Modelo de Vehículo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fechaOcurrencia,
                    onValueChange = { fechaOcurrencia = formatearFechaInput(it) },
                    label = { Text("Fecha Ocurrencia (DD/MM/AAAA)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("15/05/2026") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = comuna,
                    onValueChange = { comuna = it },
                    label = { Text("Comuna") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción del Detalle") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                if (errorMensaje != null) {
                    Text(
                        text = errorMensaje!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (idAreaSeleccionada == null) {
                            errorMensaje = "Debes seleccionar el área del siniestro"
                        } else {


                            if (
                                compania.isBlank() ||
                                numeroPoliza.isBlank() ||
                                descripcion.isBlank()
                            ) {
                                errorMensaje = "Completa los datos obligatorios."

                            } else if (!viewModel.esFechaOcurrenciaValida(fechaOcurrencia)) {

                                errorMensaje =
                                    "La fecha no es válida. Utiliza el formato DD/MM/AAAA."

                            } else {
                            val fechaActualFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                            val nuevoSiniestro = SiniestroEntity(
                                numero = "SIN-26-${(100000..999999).random()}",
                                idAsegurado = 0,
                                companiaNombre = compania,
                                numeroPoliza = numeroPoliza,
                                tipoSiniestroNombre = tipoSiniestro,
                                modeloVehiculo = modeloVehiculo,
                                fechaOcurrencia = fechaOcurrencia,
                                fechaReporte = fechaActualFormateada,
                                comunaNombre = comuna,
                                descripcion = descripcion,
                                etapaNombre = "Recibido",
                                liquidadorNombre = "Por asignar",
                                proximoPasoTexto = "Revisión inicial de antecedentes",
                                idArea = idAreaSeleccionada
                            )

                            viewModel.guardarSiniestro(
                                siniestro = nuevoSiniestro,
                                onExito = {
                                    Toast.makeText(
                                        context,
                                        "Siniestro creado exitosamente",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.popBackStack()
                                },
                                onError = { mensaje ->
                                    errorMensaje = mensaje
                                }
                            )
                        }}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorAndes)
                ) {
                    Text("Ingresar Siniestro", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}