package com.andes.bpo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.viewmodel.AdjuntoBorradorItem
import com.andes.bpo.viewmodel.NuevoSiniestroViewModel
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private data class SolicitudAdjuntoActivo(
    val tipoDocumento: String,
    val esPoliza: Boolean
)

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
    val uiState by viewModel.uiState.collectAsState()

    var menuAreasExpandido by remember { mutableStateOf(false) }
    var menuTipoExpandido by remember { mutableStateOf(false) }
    var menuPolizasExpandido by remember { mutableStateOf(false) }
    var menuCompaniaExpandido by remember { mutableStateOf(false) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var solicitudAdjunto by remember { mutableStateOf<SolicitudAdjuntoActivo?>(null) }
    var adjuntoAEliminar by remember { mutableStateOf<AdjuntoBorradorItem?>(null) }
    var fotoPantallaCompleta by remember { mutableStateOf<AdjuntoBorradorItem?>(null) }

    val areaActual = areas.firstOrNull { it.idArea == uiState.idArea }
    val listaPolizasArea = remember(areaActual) { viewModel.obtenerPolizasPorArea(areaActual) }
    val listaTiposProblema = remember(uiState.idArea) { viewModel.obtenerTiposProblemaPorArea(uiState.idArea) }

    // Launchers de archivos, galería y cámara
    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        val destino = solicitudAdjunto ?: return@rememberLauncherForActivityResult
        if (bitmap != null) {
            viewModel.procesarBitmapCamara(bitmap, destino.tipoDocumento, destino.esPoliza)
                .onFailure { e -> viewModel.setErrorDocumento(destino.esPoliza, e.message) }
        }
    }

    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val destino = solicitudAdjunto ?: return@rememberLauncherForActivityResult
        uri?.let {
            viewModel.procesarUriAdjunto(it, destino.tipoDocumento, destino.esPoliza)
                .onFailure { e -> viewModel.setErrorDocumento(destino.esPoliza, e.message) }
        }
    }

    val launcherArchivos = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val destino = solicitudAdjunto ?: return@rememberLauncherForActivityResult
        uri?.let {
            viewModel.procesarUriAdjunto(it, destino.tipoDocumento, destino.esPoliza)
                .onFailure { e -> viewModel.setErrorDocumento(destino.esPoliza, e.message) }
        }
    }

    // Launchers de permisos en tiempo de ejecución
    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            launcherCamara.launch(null)
        } else {
            Toast.makeText(context, "Se requiere permiso de Cámara para tomar fotografías", Toast.LENGTH_SHORT).show()
        }
    }

    val permisoGaleriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            launcherGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            Toast.makeText(context, "Se requiere permiso de Galería para seleccionar fotos", Toast.LENGTH_SHORT).show()
        }
    }

    fun abrirCamaraConPermiso() {
        val permiso = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
            launcherCamara.launch(null)
        } else {
            permisoCamaraLauncher.launch(permiso)
        }
    }

    fun abrirGaleriaConPermiso() {
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
            launcherGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            permisoGaleriaLauncher.launch(permiso)
        }
    }

    // Diálogo selector de origen (Cámara / Galería / Carpetas)
    solicitudAdjunto?.let { destino ->
        AlertDialog(
            onDismissRequest = { solicitudAdjunto = null },
            title = { Text("Adjuntar ${destino.tipoDocumento}", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { abrirCamaraConPermiso() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar Foto con Cámara")
                    }
                    OutlinedButton(
                        onClick = { abrirGaleriaConPermiso() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir Foto de Galería")
                    }
                    OutlinedButton(
                        onClick = {
                            launcherArchivos.launch(arrayOf("application/pdf", "image/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Explorar Carpetas (PDF o Imagen)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { solicitudAdjunto = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Selector de fecha Material 3 (DatePickerDialog)
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
                        viewModel.onFechaOcurrenciaSelected(
                            localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        )
                    }
                    mostrarDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Confirmación para eliminar archivo adjunto
    adjuntoAEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { adjuntoAEliminar = null },
            title = { Text("Eliminar archivo adjunto") },
            text = { Text("¿Deseas eliminar '${item.nombreArchivo}' de este siniestro?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarAdjunto(item)
                    adjuntoAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { adjuntoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // Visor de foto en pantalla completa
    fotoPantallaCompleta?.let { foto ->
        Dialog(
            onDismissRequest = { fotoPantallaCompleta = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = File(foto.rutaLocal),
                    contentDescription = foto.nombreArchivo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(foto.tipoDocumento, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(foto.nombreArchivo, color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = { fotoPantallaCompleta = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar visor", tint = Color.White)
                    }
                }
            }
        }
    }

    val polizaAdjunta = uiState.adjuntos.firstOrNull { it.esPoliza }
    val evidenciasAdjuntas = uiState.adjuntos.filterNot { it.esPoliza }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingresar Siniestro", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SECCIÓN 1: DATOS DEL SINIESTRO
            Text("1. Antecedentes del Siniestro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorAndes)

            // Área desde BD
            ExposedDropdownMenuBox(
                expanded = menuAreasExpandido,
                onExpandedChange = { menuAreasExpandido = it && areas.isNotEmpty() }
            ) {
                OutlinedTextField(
                    value = areaActual?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Área del Siniestro *") },
                    placeholder = { Text("Vehículos, Hogar o Salud") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAreasExpandido) },
                    isError = uiState.errArea != null,
                    supportingText = { uiState.errArea?.let { Text(it) } },
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
                                viewModel.onAreaSelected(area.idArea)
                                menuAreasExpandido = false
                            }
                        )
                    }
                }
            }

            // Tipo de Siniestro / Problema (Lista desplegable)
            ExposedDropdownMenuBox(
                expanded = menuTipoExpandido,
                onExpandedChange = { menuTipoExpandido = it && listaTiposProblema.isNotEmpty() }
            ) {
                OutlinedTextField(
                    value = uiState.tipoSiniestro,
                    onValueChange = {},
                    readOnly = true,
                    enabled = uiState.idArea != null,
                    label = { Text("Tipo de Siniestro / Problema *") },
                    placeholder = { Text(if (uiState.idArea == null) "Primero selecciona un área" else "Selecciona el tipo de problema") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTipoExpandido) },
                    isError = uiState.errTipo != null,
                    supportingText = { uiState.errTipo?.let { Text(it) } },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuTipoExpandido,
                    onDismissRequest = { menuTipoExpandido = false }
                ) {
                    listaTiposProblema.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                viewModel.onTipoSiniestroSelected(tipo)
                                menuTipoExpandido = false
                            }
                        )
                    }
                }
            }

            // Fecha con Calendario Material 3 (detecta toque en todo el campo)
            val fechaInteractionSource = remember { MutableInteractionSource() }
            LaunchedEffect(fechaInteractionSource) {
                fechaInteractionSource.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        mostrarDatePicker = true
                    }
                }
            }

            OutlinedTextField(
                value = uiState.fechaOcurrencia,
                onValueChange = {},
                readOnly = true,
                interactionSource = fechaInteractionSource,
                label = { Text("Fecha de Ocurrencia *") },
                placeholder = { Text("DD/MM/AAAA") },
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Abrir calendario", tint = colorAndes)
                    }
                },
                isError = uiState.errFecha != null,
                supportingText = { uiState.errFecha?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.comuna,
                onValueChange = { viewModel.onComunaChange(it) },
                label = { Text("Comuna *") },
                isError = uiState.errComuna != null,
                supportingText = { uiState.errComuna?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.detalleBien,
                onValueChange = { viewModel.onDetalleBienChange(it) },
                label = {
                    Text(
                        when (uiState.idArea) {
                            1 -> "Patente y Modelo del Vehículo"
                            2 -> "Dirección del Inmueble"
                            3 -> "Nombre del Paciente / Beneficiario"
                            else -> "Bien o Persona Asegurada"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = { viewModel.onDescripcionChange(it) },
                label = { Text("Descripción de los hechos *") },
                isError = uiState.errDescripcion != null,
                supportingText = { uiState.errDescripcion?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider()

            // SECCIÓN 2: PÓLIZA Y DOCUMENTO DE RESPALDO
            Text("2. Póliza de Respaldo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorAndes)

            ExposedDropdownMenuBox(
                expanded = menuPolizasExpandido,
                onExpandedChange = { menuPolizasExpandido = it && listaPolizasArea.isNotEmpty() }
            ) {
                OutlinedTextField(
                    value = uiState.polizaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    enabled = uiState.idArea != null,
                    label = { Text("Tipo de Póliza *") },
                    placeholder = { Text(if (uiState.idArea == null) "Primero selecciona un área" else "Selecciona la póliza contratada") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuPolizasExpandido) },
                    isError = uiState.errPoliza != null,
                    supportingText = { uiState.errPoliza?.let { Text(it) } },
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
                                viewModel.onPolizaSelected(poliza)
                                menuPolizasExpandido = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.numeroPoliza,
                onValueChange = { viewModel.onNumeroPolizaChange(it) },
                label = { Text("Número de Póliza *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.errNumPoliza != null,
                supportingText = { uiState.errNumPoliza?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = menuCompaniaExpandido,
                onExpandedChange = { menuCompaniaExpandido = it }
            ) {
                OutlinedTextField(
                    value = uiState.compania,
                    onValueChange = { viewModel.onCompaniaChange(it) },
                    label = { Text("Compañía Aseguradora *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCompaniaExpandido) },
                    isError = uiState.errCompania != null,
                    supportingText = { uiState.errCompania?.let { Text(it) } },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = menuCompaniaExpandido,
                    onDismissRequest = { menuCompaniaExpandido = false }
                ) {
                    viewModel.companiasDisponibles.forEach { comp ->
                        DropdownMenuItem(
                            text = { Text(comp) },
                            onClick = {
                                viewModel.onCompaniaChange(comp)
                                menuCompaniaExpandido = false
                            }
                        )
                    }
                }
            }

            // Botón para adjuntar documento de Póliza (PDF o Foto)
            OutlinedButton(
                onClick = {
                    solicitudAdjunto = SolicitudAdjuntoActivo("Póliza de Seguro", esPoliza = true)
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(
                    1.dp,
                    if (uiState.errPolizaDoc != null) MaterialTheme.colorScheme.error else colorAndes
                )
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (polizaAdjunta == null) "Adjuntar Documento de Póliza (PDF o Foto) *" else "Cambiar Documento de Póliza")
            }

            uiState.errPolizaDoc?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            polizaAdjunta?.let { item ->
                TarjetaArchivoAdjunto(
                    item = item,
                    onVerFoto = { fotoPantallaCompleta = item },
                    onEliminar = { adjuntoAEliminar = item }
                )
            }

            HorizontalDivider()

            // SECCIÓN 3: CHECKLIST DOCUMENTAL DINÁMICO SEGÚN ÁREA
            Text("3. Documentos del Siniestro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colorAndes)

            if (requisitos.isEmpty()) {
                Text(
                    "Selecciona un área para cargar los documentos requeridos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                requisitos.forEach { req ->
                    val adjuntoDeEsteTipo = evidenciasAdjuntas.firstOrNull { it.tipoDocumento == req.nombreDocumento }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (adjuntoDeEsteTipo != null) Icons.Default.CheckCircle else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (adjuntoDeEsteTipo != null) Color(0xFF2E7D32) else if (req.esObligatorio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.nombreDocumento, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    text = if (req.esObligatorio) "Obligatorio" else "Adicional / Opcional",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (req.esObligatorio) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = {
                                    solicitudAdjunto = SolicitudAdjuntoActivo(req.nombreDocumento, esPoliza = false)
                                }
                            ) {
                                Text(if (adjuntoDeEsteTipo == null) "Adjuntar" else "Agregar otro")
                            }
                        }
                    }
                }
            }

            uiState.errDocs?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            evidenciasAdjuntas.forEach { item ->
                TarjetaArchivoAdjunto(
                    item = item,
                    onVerFoto = { fotoPantallaCompleta = item },
                    onEliminar = { adjuntoAEliminar = item }
                )
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = {
                    viewModel.guardarSiniestroCompleto(
                        onExito = {
                            Toast.makeText(context, "Siniestro ingresado con éxito", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAndes)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Ingresar Siniestro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TarjetaArchivoAdjunto(
    item: AdjuntoBorradorItem,
    onVerFoto: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.esImagen) {
                AsyncImage(
                    model = File(item.rutaLocal),
                    contentDescription = item.nombreArchivo,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onVerFoto() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PDF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.tipoDocumento, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    item.nombreArchivo,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.esImagen) {
                    Text(
                        "Toca la miniatura para ampliar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar archivo", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}