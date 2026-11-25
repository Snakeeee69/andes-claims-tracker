package com.andes.bpo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.ui.theme.BackgroundAndes
import com.andes.bpo.ui.theme.MintAndes
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.utils.SesionUsuario
import com.andes.bpo.viewmodel.BandejaLiquidadorViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val MOTIVOS_RECHAZO = listOf(
    "Sobrecarga operativa",
    "Conflicto de interÃ©s",
    "Falta de especialidad en el ramo",
    "Otro"
)

@Composable
fun BandejaLiquidadorScreen(
    navController: NavController,
    viewModel: BandejaLiquidadorViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var tabInferior by remember { mutableStateOf("casos") }
    var filtroCasos by remember { mutableStateOf("todos_activos") }

    var casoParaRechazar by remember { mutableStateOf<SiniestroEntity?>(null) }
    var motivoRechazo by remember { mutableStateOf(MOTIVOS_RECHAZO.first()) }
    var detalleRechazo by remember { mutableStateOf("") }

    var idCasoGestion by remember { mutableStateOf<Int?>(null) }
    var descripcionGestion by remember(idCasoGestion) { mutableStateOf("") }
    var visibleAsegurado by remember(idCasoGestion) { mutableStateOf(true) }

    var casoParaAvanzar by remember { mutableStateOf<Int?>(null) }
    var casoParaCerrar by remember { mutableStateOf<SiniestroEntity?>(null) }
    var resultadoCierre by remember { mutableStateOf("Aprobado") }
    var mensajeCierre by remember { mutableStateOf("") }
    var casoParaSolicitar by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarCasosAsignados()
    }

    LaunchedEffect(estado.solicitudRegistrada) {
        if (estado.solicitudRegistrada) {
            casoParaSolicitar = null
            viewModel.limpiarEstadoSolicitud()
        }
    }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            idCasoGestion = null
            snackbar.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.limpiarMensaje()
        }
    }

    val pendientesAceptacion = estado.casos.filter {
        it.etapaNombre != "Cerrado" && it.aceptadoPorLiquidador != true && it.etapaNombre != "En liquidaciÃ³n"
    }
    val enLiquidacion = estado.casos.filter {
        it.etapaNombre != "Cerrado" && (it.aceptadoPorLiquidador == true || it.etapaNombre == "En liquidaciÃ³n")
    }
    val cerrados = estado.casos.filter { it.etapaNombre == "Cerrado" }
    val activosCount = pendientesAceptacion.size + enLiquidacion.size
    val avisosNoLeidos = estado.avisos.count { !it.leido }

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = PrimaryAndes,
        selectedTextColor = PrimaryAndes,
        indicatorColor = MintAndes,
        unselectedIconColor = Color(0xFF5C6F68),
        unselectedTextColor = Color(0xFF5C6F68)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = BackgroundAndes,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = tabInferior == "casos",
                    onClick = { tabInferior = "casos" },
                    colors = navItemColors,
                    icon = {
                        BadgedBox(badge = { if (activosCount > 0) Badge { Text("$activosCount") } }) {
                            Icon(Icons.Default.Inbox, contentDescription = null)
                        }
                    },
                    label = { Text("Mis Casos", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = tabInferior == "avisos",
                    onClick = { tabInferior = "avisos" },
                    colors = navItemColors,
                    icon = {
                        BadgedBox(badge = { if (avisosNoLeidos > 0) Badge { Text("$avisosNoLeidos") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }
                    },
                    label = { Text("Avisos", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("perfil") },
                    colors = navItemColors,
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil", fontSize = 11.sp) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                color = PrimaryAndes,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LIQUIDADOR EXTERNO Â· ${estado.especialidad}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintAndes,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Portal de LiquidaciÃ³n â€” D.S. NÂ° 1.055",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = estado.nombreLiquidador,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(onClick = {
                            SesionUsuario.cerrarSesion()
                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiHeaderCard("$activosCount", "Casos activos", Modifier.weight(1f))
                        KpiHeaderCard("${pendientesAceptacion.size}", "Por aceptar", Modifier.weight(1f))
                        KpiHeaderCard("45 d", "Plazo legal", Modifier.weight(1f))
                    }
                }
            }

            when (tabInferior) {
                "casos" -> {
                    when {
                        estado.cargando -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryAndes)
                            }
                        }
                        estado.error != null -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(estado.error!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        else -> {
                            val chipColors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryAndes,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = PrimaryAndes
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = filtroCasos == "todos_activos",
                                    onClick = { filtroCasos = "todos_activos" },
                                    colors = chipColors,
                                    label = { Text("Activos ($activosCount)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = filtroCasos == "por_aceptar",
                                    onClick = { filtroCasos = "por_aceptar" },
                                    colors = chipColors,
                                    label = { Text("Por aceptar (${pendientesAceptacion.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = filtroCasos == "cerrados",
                                    onClick = { filtroCasos = "cerrados" },
                                    colors = chipColors,
                                    label = { Text("Cerrados (${cerrados.size})", fontSize = 11.sp) }
                                )
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (estado.casos.isEmpty()) {
                                    item {
                                        ElevatedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Bandeja de especialidad ${estado.especialidad} al dÃ­a",
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryAndes,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = "AÃºn no tienes siniestros asignados por el Analista. Cuando se derive un caso del ramo ${estado.especialidad}, aparecerÃ¡ aquÃ­ con un plazo de 2 dÃ­as hÃ¡biles para aceptar o rechazar.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.DarkGray
                                                )
                                                OutlinedButton(
                                                    onClick = { tabInferior = "avisos" },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryAndes)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Revisar mis avisos (${estado.avisos.size})", color = PrimaryAndes)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (filtroCasos == "todos_activos" || filtroCasos == "por_aceptar") {
                                        item {
                                            Text(
                                                text = "Pendientes de aceptaciÃ³n (${pendientesAceptacion.size})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryAndes
                                            )
                                        }

                                        if (pendientesAceptacion.isEmpty()) {
                                            item {
                                                Surface(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Sin casos pendientes de aceptaciÃ³n en este momento.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(14.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            items(pendientesAceptacion, key = { "pend_${it.idSiniestro}" }) { siniestro ->
                                                CardPendienteAceptacion(
                                                    siniestro = siniestro,
                                                    onAceptar = { viewModel.aceptarCaso(siniestro.idSiniestro) },
                                                    onRechazar = {
                                                        motivoRechazo = MOTIVOS_RECHAZO.first()
                                                        detalleRechazo = ""
                                                        casoParaRechazar = siniestro
                                                    },
                                                    onVerDocs = {
                                                        navController.navigate("documentos_liquidador/${siniestro.idSiniestro}")
                                                    },
                                                    onVerHistorial = {
                                                        navController.navigate("historial_siniestro/${siniestro.idSiniestro}")
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (filtroCasos == "todos_activos") {
                                        item {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "En evaluaciÃ³n y liquidaciÃ³n (${enLiquidacion.size})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryAndes
                                            )
                                        }

                                        if (enLiquidacion.isEmpty()) {
                                            item {
                                                Surface(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "No tienes casos aceptados en proceso de liquidaciÃ³n.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(14.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            items(enLiquidacion, key = { "liq_${it.idSiniestro}" }) { siniestro ->
                                                CardEnLiquidacion(
                                                    siniestro = siniestro,
                                                    onSolicitarDoc = {
                                                        viewModel.limpiarEstadoSolicitud()
                                                        casoParaSolicitar = siniestro.idSiniestro
                                                    },
                                                    onVerEvidencias = {
                                                        navController.navigate("documentos_liquidador/${siniestro.idSiniestro}")
                                                    },
                                                    onVerHistorial = {
                                                        navController.navigate("historial_siniestro/${siniestro.idSiniestro}")
                                                    },
                                                    onEmitirInformeCerrar = {
                                                        resultadoCierre = if (siniestro.etapaNombre == "En evaluaciÃ³n") "Rechazado" else "Aprobado"
                                                        mensajeCierre = ""
                                                        casoParaCerrar = siniestro
                                                    },
                                                    onRegistrarGestion = {
                                                        viewModel.limpiarErrorGestion()
                                                        idCasoGestion = siniestro.idSiniestro
                                                    },
                                                    onAvanzarLiquidacion = {
                                                        casoParaAvanzar = siniestro.idSiniestro
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (filtroCasos == "cerrados") {
                                        item {
                                            Text(
                                                text = "Casos cerrados (${cerrados.size})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryAndes
                                            )
                                        }
                                        if (cerrados.isEmpty()) {
                                            item {
                                                Surface(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "AÃºn no tienes expedientes cerrados.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(14.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            items(cerrados, key = { "cer_${it.idSiniestro}" }) { siniestro ->
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                                                ) {
                                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(siniestro.numero, fontWeight = FontWeight.Bold, color = PrimaryAndes)
                                                            AssistChip(onClick = {}, label = { Text(siniestro.resultadoCierre ?: "Cerrado") })
                                                        }
                                                        Text(siniestro.tipoSiniestroNombre, style = MaterialTheme.typography.bodyMedium)
                                                        siniestro.mensajeCierre?.let {
                                                            Text(it, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                                        }
                                                        OutlinedButton(
                                                            onClick = { navController.navigate("historial_siniestro/${siniestro.idSiniestro}") },
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryAndes)
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text("Ver bitÃ¡cora del cierre", color = PrimaryAndes, fontSize = 12.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "avisos" -> {
                    if (estado.avisos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes notificaciones registradas.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    "Notificaciones de asignaciÃ³n y evidencias (${estado.avisos.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryAndes,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            items(estado.avisos, key = { "aviso_liq_${it.idAviso}" }) { aviso ->
                                val matchNumero = Regex("SIN-[A-Z0-9-]+").find(aviso.mensaje)?.value
                                val casoVinculado = matchNumero?.let { num ->
                                    estado.casos.firstOrNull { it.numero == num }
                                }

                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!aviso.leido) viewModel.marcarAvisoLeido(aviso.idAviso)
                                            if (casoVinculado != null) {
                                                filtroCasos = "todos_activos"
                                                tabInferior = "casos"
                                            }
                                        },
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = if (aviso.leido) Color.White else MintAndes
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(if (aviso.leido) Color.LightGray else PrimaryAndes),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(aviso.fechaHora, fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                aviso.mensaje,
                                                fontSize = 13.sp,
                                                fontWeight = if (aviso.leido) FontWeight.Normal else FontWeight.SemiBold,
                                                color = Color(0xFF263C35)
                                            )
                                            if (casoVinculado != null) {
                                                Text(
                                                    text = "Toca para gestionar ${casoVinculado.numero} â†’",
                                                    fontSize = 11.sp,
                                                    color = PrimaryAndes,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    casoParaRechazar?.let { siniestro ->
        var expandidoMotivo by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { casoParaRechazar = null },
            containerColor = Color.White,
            title = { Text("Rechazar y derivar caso", fontWeight = FontWeight.Bold, color = PrimaryAndes) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${siniestro.numero}: El expediente se derivarÃ¡ al Liquidador 2 (Respaldo) o quedarÃ¡ marcado para reasignaciÃ³n y registrado en el historial.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = motivoRechazo,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Motivo del rechazo") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { expandidoMotivo = true }
                        )
                        DropdownMenu(
                            expanded = expandidoMotivo,
                            onDismissRequest = { expandidoMotivo = false }
                        ) {
                            MOTIVOS_RECHAZO.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        motivoRechazo = opcion
                                        expandidoMotivo = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = detalleRechazo,
                        onValueChange = { detalleRechazo = it.take(300) },
                        label = { Text("Detalle del motivo") },
                        minLines = 3,
                        isError = detalleRechazo.isNotEmpty() && detalleRechazo.trim().length < 10,
                        supportingText = { Text("Explica el motivo (mÃ­n. 10 caracteres)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = detalleRechazo.trim().length >= 10,
                    onClick = {
                        viewModel.rechazarYDerivar(siniestro.idSiniestro, motivoRechazo, detalleRechazo)
                        casoParaRechazar = null
                    }
                ) {
                    Text("Confirmar rechazo y derivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { casoParaRechazar = null }) { Text("Cancelar", color = PrimaryAndes) }
            }
        )
    }

    if (idCasoGestion != null) {
        AlertDialog(
            onDismissRequest = { if (!estado.guardandoGestion) idCasoGestion = null },
            containerColor = Color.White,
            title = { Text("Registrar gestiÃ³n", color = PrimaryAndes, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = descripcionGestion,
                        onValueChange = { descripcionGestion = it.take(500) },
                        label = { Text("DescripciÃ³n de la gestiÃ³n") },
                        minLines = 3,
                        isError = estado.errorGestion != null,
                        supportingText = { Text(estado.errorGestion ?: "${descripcionGestion.length}/500") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Visible para el asegurado", modifier = Modifier.weight(1f))
                        Switch(checked = visibleAsegurado, onCheckedChange = { visibleAsegurado = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes),
                    enabled = descripcionGestion.trim().length >= 5 && !estado.guardandoGestion,
                    onClick = {
                        idCasoGestion?.let { viewModel.registrarGestion(it, descripcionGestion, visibleAsegurado) }
                    }
                ) {
                    Text(if (estado.guardandoGestion) "Guardando..." else "Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { idCasoGestion = null }) { Text("Cancelar", color = PrimaryAndes) }
            }
        )
    }

    if (casoParaAvanzar != null) {
        AlertDialog(
            onDismissRequest = { casoParaAvanzar = null },
            containerColor = Color.White,
            title = { Text("Avanzar a liquidaciÃ³n", color = PrimaryAndes, fontWeight = FontWeight.Bold) },
            text = { Text("Â¿Confirmas que todos los documentos estÃ¡n completos para pasar a En liquidaciÃ³n?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes),
                    onClick = {
                        casoParaAvanzar?.let { viewModel.avanzarALiquidacion(it) }
                        casoParaAvanzar = null
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { casoParaAvanzar = null }) { Text("Cancelar", color = PrimaryAndes) }
            }
        )
    }

    casoParaCerrar?.let { siniestro ->
        val enEvaluacion = siniestro.etapaNombre == "En evaluaciÃ³n"
        AlertDialog(
            onDismissRequest = { casoParaCerrar = null },
            containerColor = Color.White,
            title = { Text("Emitir informe y cerrar siniestro", color = PrimaryAndes, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ResoluciÃ³n para ${siniestro.numero}:")
                    if (!enEvaluacion) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = resultadoCierre == "Aprobado",
                                onClick = { resultadoCierre = "Aprobado" },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryAndes)
                            )
                            Text("Aprobado (IndemnizaciÃ³n procedente)")
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = resultadoCierre == "Rechazado",
                            onClick = { resultadoCierre = "Rechazado" },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryAndes)
                        )
                        Text("Rechazado")
                    }
                    OutlinedTextField(
                        value = mensajeCierre,
                        onValueChange = { mensajeCierre = it.take(500) },
                        label = { Text("Resumen del informe de liquidaciÃ³n") },
                        minLines = 3,
                        supportingText = { Text("MÃ­nimo 10 caracteres") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes),
                    enabled = mensajeCierre.trim().length >= 10 && !estado.cerrandoSiniestro,
                    onClick = {
                        viewModel.cerrarSiniestro(siniestro.idSiniestro, resultadoCierre, mensajeCierre)
                        casoParaCerrar = null
                    }
                ) { Text("Emitir y cerrar") }
            },
            dismissButton = {
                TextButton(onClick = { casoParaCerrar = null }) { Text("Cancelar", color = PrimaryAndes) }
            }
        )
    }

    if (casoParaSolicitar != null) {
        SolicitarDocumentoDialog(
            guardando = estado.solicitandoDocumento,
            error = estado.errorSolicitud,
            onDismiss = {
                casoParaSolicitar = null
                viewModel.limpiarEstadoSolicitud()
            },
            onSolicitar = { tipo, comentario ->
                casoParaSolicitar?.let { viewModel.solicitarDocumento(it, tipo, comentario) }
            }
        )
    }
}

@Composable
private fun KpiHeaderCard(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(etiqueta, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun CardPendienteAceptacion(
    siniestro: SiniestroEntity,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onVerDocs: () -> Unit,
    onVerHistorial: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(siniestro.numero, fontWeight = FontWeight.Bold, color = PrimaryAndes)
                    Text(siniestro.tipoSiniestroNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "1 d 18 h para responder",
                        color = Color(0xFF92400E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "${siniestro.companiaNombre} Â· PÃ³liza ${siniestro.numeroPoliza}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Text(
                text = siniestro.descripcion,
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onVerDocs,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryAndes)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Evidencias", fontSize = 12.sp, color = PrimaryAndes)
                }
                OutlinedButton(
                    onClick = onVerHistorial,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryAndes)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BitÃ¡cora", fontSize = 12.sp, color = PrimaryAndes)
                }
            }

            Text(
                text = "Silencio positivo: si vence el plazo de 2 dÃ­as hÃ¡biles, el caso se entiende aceptado.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAceptar,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar Caso", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onRechazar,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rechazar y Derivar", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CardEnLiquidacion(
    siniestro: SiniestroEntity,
    onSolicitarDoc: () -> Unit,
    onVerEvidencias: () -> Unit,
    onVerHistorial: () -> Unit,
    onEmitirInformeCerrar: () -> Unit,
    onRegistrarGestion: () -> Unit,
    onAvanzarLiquidacion: () -> Unit
) {
    val diasTranscurridos = calcularDiasDesdeReporte(siniestro.fechaReporte)
    val totalPlazo = 45 + siniestro.diasProrroga

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(siniestro.numero, fontWeight = FontWeight.Bold, color = PrimaryAndes)
                    Text(siniestro.tipoSiniestroNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${siniestro.companiaNombre} Â· ${siniestro.comunaNombre}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
                Surface(color = MintAndes, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = siniestro.etapaNombre,
                        color = PrimaryAndes,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            BarraSemaforoLegal(diasTranscurridos = diasTranscurridos, totalDias = totalPlazo)

            OutlinedButton(
                onClick = onSolicitarDoc,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryAndes)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Solicitar Documento Adicional al Asegurado", color = PrimaryAndes)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onVerEvidencias,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryAndes)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Evidencias", fontSize = 12.sp, color = PrimaryAndes)
                }
                OutlinedButton(
                    onClick = onVerHistorial,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryAndes)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BitÃ¡cora", fontSize = 12.sp, color = PrimaryAndes)
                }
            }

            Button(
                onClick = onEmitirInformeCerrar,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Emitir Informe y Cerrar Siniestro", fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onRegistrarGestion, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryAndes)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Registrar gestiÃ³n", fontSize = 12.sp, color = PrimaryAndes)
                }
                if (siniestro.etapaNombre == "En evaluaciÃ³n") {
                    TextButton(onClick = onAvanzarLiquidacion, modifier = Modifier.weight(1f)) {
                        Text("Pasar a liquidaciÃ³n", fontSize = 12.sp, color = PrimaryAndes)
                    }
                }
            }
        }
    }
}

@Composable
fun BarraSemaforoLegal(diasTranscurridos: Int, totalDias: Int) {
    val porcentaje = (diasTranscurridos.toFloat() / totalDias.coerceAtLeast(1)).coerceIn(0f, 1f)
    val restantes = (totalDias - diasTranscurridos).coerceAtLeast(0)
    val colorBarra = when {
        restantes <= 7 -> Color(0xFFDC2626)
        restantes <= 15 -> Color(0xFFD97706)
        else -> Color(0xFF16A34A)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Plazo D.S. NÂ° 1.055: DÃ­a $diasTranscurridos de $totalDias", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("Quedan $restantes d", fontSize = 11.sp, color = colorBarra, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { porcentaje },
            color = colorBarra,
            trackColor = Color.LightGray.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

fun calcularDiasDesdeReporte(fechaReporte: String): Int {
    return runCatching {
        val fechaStr = fechaReporte.trim().take(10)
        val fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ChronoUnit.DAYS.between(fecha, LocalDate.now()).toInt().coerceAtLeast(1)
    }.getOrDefault(1)
}
