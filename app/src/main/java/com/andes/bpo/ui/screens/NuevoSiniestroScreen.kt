package com.andes.bpo.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.viewmodel.AdjuntoBorradorItem
import com.andes.bpo.viewmodel.NuevoSiniestroViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoSiniestroScreen(
    navController: NavController,
    rut: String,
    viewModel: NuevoSiniestroViewModel = viewModel()
) {
    val colorAndes = PrimaryAndes
    val context = LocalContext.current

    val areas by viewModel.areas.collectAsState()
    val requisitos by viewModel.requisitos.collectAsState()

    var idAreaSeleccionada by rememberSaveable { mutableStateOf<Int?>(null) }
    var menuAreasExpandido by remember { mutableStateOf(false) }

    var polizaSeleccionada by rememberSaveable { mutableStateOf("") }
    var menuPolizasExpandido by remember { mutableStateOf(false) }
    var numeroPoliza by rememberSaveable { mutableStateOf("") }
    var compania by rememberSaveable { mutableStateOf("") }
    var tipoSiniestro by rememberSaveable { mutableStateOf("") }
    var detalleBien by rememberSaveable { mutableStateOf("") }
    var fechaOcurrencia by rememberSaveable { mutableStateOf("") }
    var comuna by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }

    var adjuntos by remember { mutableStateOf<List<AdjuntoBorradorItem>>(emptyList()) }
    var tipoDocumentoAAdjuntar by remember { mutableStateOf("Póliza de Seguro") }
    var adjuntoAEliminar by remember { mutableStateOf<AdjuntoBorradorItem?>(null) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    // Errores individuales por campo (isError + supportingText)
    var errArea by remember { mutableStateOf<String?>(null) }
    var errPoliza by remember { mutableStateOf<String?>(null) }
    var errNumPoliza by remember { mutableStateOf<String?>(null) }
    var errCompania by remember { mutableStateOf<String?>(null) }
    var errTipo by remember { mutableStateOf<String?>(null) }
    var errFecha by remember { mutableStateOf<String?>(null) }
    var errComuna by remember { mutableStateOf<String?>(null) }
    var errDescripcion by remember { mutableStateOf<String?>(null) }
    var errDocs by remember { mutableStateOf<String?>(null) }

    val areaActual = areas.firstOrNull { it.idArea == idAreaSeleccionada }
    val listaPolizasArea = remember(areaActual) {
        areaActual?.polizasDisponibles?.split("|")?.filter { it.isNotBlank() }.orEmpty()
    }

    val selectorImagen = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.copiarArchivoLocal(it, tipoDocumentoAAdjuntar)
                .onSuccess { item ->
                    adjuntos = adjuntos + item
                    errDocs = null
                }
                .onFailure { e -> errDocs = e.message }
        }
    }

    val selectorPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.copiarArchivoLocal(it, tipoDocumentoAAdjuntar)
                .onSuccess { item ->
                    adjuntos = adjuntos + item
                    errDocs = null
                }
                .onFailure { e -> errDocs = e.message }
        }
    }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        fechaOcurrencia = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        errFecha = null
                    }
                    mostrarDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    adjuntoAEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { adjuntoAEliminar = null },
            title = { Text("Eliminar archivo adjunto") },
            text = { Text("¿Seguro que deseas quitar '${item.nombreArchivo}'?") },
            confirmButton = {
                TextButton(onClick = {
                    File(item.rutaLocal).delete()
                    adjuntos = adjuntos.filterNot { it.idTemp == item.idTemp }
                    adjuntoAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { adjuntoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Nuevo Siniestro", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorAndes)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Ingresa los antecedentes del siniestro. El plazo legal de resolución es de 45 días corridos (D.S. N° 1.055).",
                style = MaterialTheme.typography.bodySmall
            )

            // 1. Área desde BD
            ExposedDropdownMenuBox(
                expanded = menuAreasExpandido,
                onExpandedChange = { menuAreasExpandido = it && areas.isNotEmpty() }
            ) {
                OutlinedTextField(
                    value = areaActual?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Área del siniestro *") },
                    placeholder = { Text("Selecciona Vehículos, Hogar o Salud") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAreasExpandido) },
                    isError = errArea != null,
                    supportingText = { errArea?.let { Text(it) } },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuAreasExpandido,
                    onDismissRequest = { menuAreasExpandido = false }
                ) {
                    areas.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area.nombre) },
                            onClick = {
                                idAreaSeleccionada = area.idArea
                                polizaSeleccionada = ""
                                menuAreasExpandido = false
                                errArea = null
                                viewModel.cargarRequisitosDeArea(area.idArea)
                            }
                        )
                    }
                }
            }

            // 2. Póliza desde BD según Área
            ExposedDropdownMenuBox(
                expanded = menuPolizasExpandido,
                onExpandedChange = { menuPolizasExpandido = it && listaPolizasArea.isNotEmpty() }
            ) {
                OutlinedTextField(
                    value = polizaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Póliza *") },
                    placeholder = { Text("Selecciona tu póliza") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuPolizasExpandido) },
                    isError = errPoliza != null,
                    supportingText = { errPoliza?.let { Text(it) } },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuPolizasExpandido,
                    onDismissRequest = { menuPolizasExpandido = false }
                ) {
                    listaPolizasArea.forEach { poliza ->
                        DropdownMenuItem(
                            text = { Text(poliza) },
                            onClick = {
                                polizaSeleccionada = poliza
                                menuPolizasExpandido = false
                                errPoliza = null
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = numeroPoliza,
                onValueChange = {
                    numeroPoliza = it.filter { c -> c.isDigit() }.take(12)
                    errNumPoliza = null
                },
                label = { Text("Número de Póliza *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = errNumPoliza != null,
                supportingText = { errNumPoliza?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = compania,
                onValueChange = { compania = it; errCompania = null },
                label = { Text("Compañía Aseguradora *") },
                isError = errCompania != null,
                supportingText = { errCompania?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = tipoSiniestro,
                onValueChange = { tipoSiniestro = it; errTipo = null },
                label = { Text("Evento / Tipo de Siniestro *") },
                placeholder = { Text("Ej: Colisión, Incendio, Reembolso médico") },
                isError = errTipo != null,
                supportingText = { errTipo?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = detalleBien,
                onValueChange = { detalleBien = it },
                label = { Text("Bien asegurado (Patente/Modelo, Dirección o Paciente)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Fecha con Calendario DatePickerDialog
            OutlinedTextField(
                value = fechaOcurrencia,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de Ocurrencia *") },
                placeholder = { Text("Toca el calendario para elegir") },
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
                    }
                },
                isError = errFecha != null,
                supportingText = { errFecha?.let { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePicker = true }
            )

            OutlinedTextField(
                value = comuna,
                onValueChange = { comuna = it; errComuna = null },
                label = { Text("Comuna *") },
                isError = errComuna != null,
                supportingText = { errComuna?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it; errDescripcion = null },
                label = { Text("Relato detallado de los hechos *") },
                isError = errDescripcion != null,
                supportingText = { errDescripcion?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider()

            // Checklist documental dinámico desde BD
            Text("Documentación de respaldo y Póliza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (requisitos.isNotEmpty()) {
                requisitos.forEach { req ->
                    val cumple = adjuntos.any { it.tipoDocumento == req.nombreDocumento }
                    Text(
                        text = "${if (cumple) "✅" else if (req.esObligatorio) "🔴" else "⚪"} ${req.nombreDocumento} (${if (req.esObligatorio) "Obligatorio" else "Opcional"})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        tipoDocumentoAAdjuntar = "Póliza de Seguro"
                        selectorPdf.launch("application/pdf")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Adjuntar Póliza (PDF)", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        val pendiente = requisitos.firstOrNull { r -> adjuntos.none { it.tipoDocumento == r.nombreDocumento } }
                        tipoDocumentoAAdjuntar = pendiente?.nombreDocumento ?: "Evidencia del siniestro"
                        selectorImagen.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Adjuntar Foto", fontSize = 12.sp)
                }
            }

            errDocs?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            adjuntos.forEach { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.esImagen) {
                            AsyncImage(
                                model = File(item.rutaLocal),
                                contentDescription = item.nombreArchivo,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.tipoDocumento, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(item.nombreArchivo, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { adjuntoAEliminar = item }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    errArea = if (idAreaSeleccionada == null) "Selecciona el área" else null
                    errPoliza = if (polizaSeleccionada.isBlank()) "Selecciona el tipo de póliza" else null
                    errNumPoliza = if (numeroPoliza.length < 4) "Ingresa un número de póliza válido" else null
                    errCompania = if (compania.isBlank()) "Ingresa la compañía aseguradora" else null
                    errTipo = if (tipoSiniestro.isBlank()) "Describe el tipo de evento" else null
                    errFecha = if (!viewModel.esFechaOcurrenciaValida(fechaOcurrencia)) "Selecciona una fecha válida no futura" else null
                    errComuna = if (comuna.isBlank()) "Ingresa la comuna" else null
                    errDescripcion = if (descripcion.trim().length < 10) "El relato debe tener al menos 10 caracteres" else null

                    val obligatoriosFaltantes = requisitos.filter { it.esObligatorio }
                        .filter { req -> adjuntos.none { it.tipoDocumento == req.nombreDocumento } }
                    errDocs = if (obligatoriosFaltantes.isNotEmpty()) {
                        "Faltan documentos obligatorios: ${obligatoriosFaltantes.joinToString { it.nombreDocumento }}"
                    } else null

                    if (listOf(errArea, errPoliza, errNumPoliza, errCompania, errTipo, errFecha, errComuna, errDescripcion, errDocs).all { it == null }) {
                        val fechaAhora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        val prefijoArea = when (idAreaSeleccionada) {
                            1 -> "Vehículo - "
                            2 -> "Hogar - "
                            else -> "Salud - "
                        }
                        val nuevoSiniestro = SiniestroEntity(
                            numero = "SIN-26-${(100000..999999).random()}",
                            idAsegurado = 0,
                            companiaNombre = compania.trim(),
                            numeroPoliza = "$polizaSeleccionada #$numeroPoliza",
                            tipoSiniestroNombre = "$prefijoArea${tipoSiniestro.trim()}",
                            modeloVehiculo = detalleBien.trim(),
                            fechaOcurrencia = fechaOcurrencia,
                            fechaReporte = fechaAhora,
                            comunaNombre = comuna.trim(),
                            descripcion = descripcion.trim(),
                            etapaNombre = "Recibido",
                            liquidadorNombre = "Por asignar",
                            proximoPasoTexto = "Revisión inicial de antecedentes (Plazo legal 45 días)",
                            idArea = idAreaSeleccionada
                        )
                        viewModel.guardarSiniestroCompleto(
                            siniestro = nuevoSiniestro,
                            adjuntos = adjuntos,
                            onExito = {
                                Toast.makeText(context, "Siniestro ingresado con éxito", Toast.LENGTH_LONG).show()
                                navController.popBackStack()
                            },
                            onError = { errDescripcion = it }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAndes)
            ) {
                Text("Ingresar Siniestro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}