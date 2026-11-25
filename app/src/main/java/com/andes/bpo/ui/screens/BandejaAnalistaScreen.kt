package com.andes.bpo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import com.andes.bpo.data.local.entity.UsuarioEntity
import com.andes.bpo.ui.theme.BackgroundAndes
import com.andes.bpo.ui.theme.MintAndes
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.utils.SesionUsuario
import com.andes.bpo.viewmodel.BandejaAnalistaViewModel

@Composable
fun BandejaAnalistaScreen(
    navController: NavController,
    esModoAdmin: Boolean = false,
    viewModel: BandejaAnalistaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val sesionActual = remember { SesionUsuario.obtenerSesion() }

    var tabInferior by remember { mutableStateOf("bandeja") }
    var filtroCasos by remember { mutableStateOf("activos") }
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroRolAdmin by remember { mutableStateOf("TODOS") }
    var usuarioAEliminar by remember { mutableStateOf<UsuarioEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarCasos()
    }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.limpiarMensaje()
        }
    }

    val nuevos = estado.todosLosCasos.filter {
        it.etapaNombre == "Recibido" && it.idLiquidador == null
    }
    val rechazados = estado.todosLosCasos.filter {
        it.motivoRechazoLiquidador != null && it.etapaNombre != "Cerrado"
    }
    val enSeguimientoActivos = estado.todosLosCasos.filter {
        it.etapaNombre == "En evaluaciÃ³n" || it.etapaNombre == "En liquidaciÃ³n"
    }
    val cerrados = estado.todosLosCasos.filter {
        it.etapaNombre == "Cerrado"
    }
    val pendientesAccion = nuevos.size + rechazados.size
    val avisosNoLeidos = estado.avisosAnalista.count { !it.leido }

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
                    selected = tabInferior == "bandeja",
                    onClick = { tabInferior = "bandeja" },
                    colors = navItemColors,
                    icon = {
                        BadgedBox(badge = { if (pendientesAccion > 0) Badge { Text("$pendientesAccion") } }) {
                            Icon(Icons.Default.Inbox, contentDescription = null)
                        }
                    },
                    label = { Text(if (esModoAdmin) "SupervisiÃ³n" else "Bandeja", fontSize = 11.sp) }
                )

                if (esModoAdmin) {
                    NavigationBarItem(
                        selected = tabInferior == "usuarios",
                        onClick = { tabInferior = "usuarios" },
                        colors = navItemColors,
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Usuarios", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = tabInferior == "catalogo",
                        onClick = { tabInferior = "catalogo" },
                        colors = navItemColors,
                        icon = { Icon(Icons.Default.GridView, contentDescription = null) },
                        label = { Text("Ãreas", fontSize = 11.sp) }
                    )
                } else {
                    NavigationBarItem(
                        selected = tabInferior == "equipo",
                        onClick = { tabInferior = "equipo" },
                        colors = navItemColors,
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Equipo", fontSize = 11.sp) }
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
                }

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
                                text = if (esModoAdmin) "ADMINISTRADOR Â· GOBERNANZA" else "ANALISTA OPERATIVO Â· SINIESTROS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintAndes,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (esModoAdmin) "Control Total Andes BPO" else "Centro de AsignaciÃ³n y Plazos",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            if (estado.nombreUsuario.isNotBlank()) {
                                Text(
                                    text = estado.nombreUsuario,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (esModoAdmin) {
                                IconButton(onClick = { navController.navigate("notificaciones") }) {
                                    BadgedBox(badge = { if (avisosNoLeidos > 0) Badge { Text("$avisosNoLeidos") } }) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Avisos", tint = Color.White)
                                    }
                                }
                            }
                            IconButton(onClick = {
                                SesionUsuario.cerrarSesion()
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (esModoAdmin) {
                            KpiHeaderOperativo("${estado.todosLosCasos.size}", "Casos totales", Modifier.weight(1f))
                            KpiHeaderOperativo("$pendientesAccion", "Por atender", Modifier.weight(1f))
                            KpiHeaderOperativo("${estado.todosLosUsuarios.size}", "Usuarios", Modifier.weight(1f))
                        } else {
                            KpiHeaderOperativo("${nuevos.size}", "Sin asignar", Modifier.weight(1f))
                            KpiHeaderOperativo("${enSeguimientoActivos.size}", "En curso", Modifier.weight(1f))
                            KpiHeaderOperativo("${rechazados.size}", "Rechazados", Modifier.weight(1f))
                        }
                    }
                }
            }

            when (tabInferior) {
                "bandeja" -> {
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

                            if (esModoAdmin) {
                                OutlinedTextField(
                                    value = textoBusqueda,
                                    onValueChange = { textoBusqueda = it },
                                    placeholder = { Text("Buscar por NÂ° siniestro, pÃ³liza o ramo...", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryAndes) },
                                    trailingIcon = {
                                        if (textoBusqueda.isNotEmpty()) {
                                            IconButton(onClick = { textoBusqueda = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = PrimaryAndes
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, top = 10.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = filtroCasos == "activos",
                                    onClick = { filtroCasos = "activos" },
                                    colors = chipColors,
                                    label = { Text("Activos (${nuevos.size + enSeguimientoActivos.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = filtroCasos == "por_asignar",
                                    onClick = { filtroCasos = "por_asignar" },
                                    colors = chipColors,
                                    label = { Text("Nuevos (${nuevos.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = filtroCasos == "rechazados",
                                    onClick = { filtroCasos = "rechazados" },
                                    colors = chipColors,
                                    label = { Text("Alertas (${rechazados.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = filtroCasos == "cerrados",
                                    onClick = { filtroCasos = "cerrados" },
                                    colors = chipColors,
                                    label = { Text("Cerrados (${cerrados.size})", fontSize = 11.sp) }
                                )
                            }

                            val query = textoBusqueda.trim()
                            fun coincideBusqueda(s: SiniestroEntity): Boolean {
                                if (!esModoAdmin || query.isEmpty()) return true
                                return s.numero.contains(query, ignoreCase = true) ||
                                        s.numeroPoliza.contains(query, ignoreCase = true) ||
                                        s.tipoSiniestroNombre.contains(query, ignoreCase = true) ||
                                        (s.liquidadorNombre?.contains(query, ignoreCase = true) == true)
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (filtroCasos == "activos" || filtroCasos == "por_asignar" || filtroCasos == "rechazados") {
                                    val prioritarios = when (filtroCasos) {
                                        "por_asignar" -> nuevos
                                        "rechazados" -> rechazados
                                        else -> (rechazados + nuevos).distinctBy { it.idSiniestro }
                                    }.filter(::coincideBusqueda)

                                    item {
                                        Text(
                                            text = if (filtroCasos == "rechazados") {
                                                "Casos rechazados por liquidador (${prioritarios.size})"
                                            } else {
                                                "Requieren asignaciÃ³n o reasignaciÃ³n (${prioritarios.size})"
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryAndes
                                        )
                                    }

                                    if (prioritarios.isEmpty()) {
                                        item {
                                            Surface(
                                                color = Color.White,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Sin casos pendientes de asignaciÃ³n en esta vista.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray,
                                                    modifier = Modifier.padding(14.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        items(prioritarios, key = { "prio_${it.idSiniestro}" }) { siniestro ->
                                            CardCasoAnalistaCompleto(
                                                siniestro = siniestro,
                                                onVerEvidencias = { navController.navigate("evidencia/${siniestro.idSiniestro}") },
                                                onVerHistorial = { navController.navigate("historial_siniestro/${siniestro.idSiniestro}") },
                                                onGestionar = { viewModel.prepararAsignacion(siniestro) }
                                            )
                                        }
                                    }
                                }

                                if (filtroCasos == "activos") {
                                    val enCurso = enSeguimientoActivos
                                        .filter { it.motivoRechazoLiquidador == null }
                                        .filter(::coincideBusqueda)

                                    item {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "En evaluaciÃ³n y liquidaciÃ³n (${enCurso.size})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryAndes
                                        )
                                    }

                                    if (enCurso.isEmpty()) {
                                        item {
                                            Surface(
                                                color = Color.White,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "No hay casos asignados en evaluaciÃ³n o liquidaciÃ³n.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray,
                                                    modifier = Modifier.padding(14.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        items(enCurso, key = { "curso_${it.idSiniestro}" }) { siniestro ->
                                            CardCasoAnalistaCompleto(
                                                siniestro = siniestro,
                                                onVerEvidencias = { navController.navigate("evidencia/${siniestro.idSiniestro}") },
                                                onVerHistorial = { navController.navigate("historial_siniestro/${siniestro.idSiniestro}") },
                                                onGestionar = { viewModel.prepararAsignacion(siniestro) }
                                            )
                                        }
                                    }
                                }

                                if (filtroCasos == "cerrados") {
                                    val listaCerrados = cerrados.filter(::coincideBusqueda)
                                    item {
                                        Text(
                                            text = "Expedientes cerrados (${listaCerrados.size})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryAndes
                                        )
                                    }
                                    if (listaCerrados.isEmpty()) {
                                        item {
                                            Surface(
                                                color = Color.White,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "No hay expedientes cerrados registrados.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray,
                                                    modifier = Modifier.padding(14.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        items(listaCerrados, key = { "cer_${it.idSiniestro}" }) { siniestro ->
                                            CardCasoAnalistaCompleto(
                                                siniestro = siniestro,
                                                onVerEvidencias = { navController.navigate("evidencia/${siniestro.idSiniestro}") },
                                                onVerHistorial = { navController.navigate("historial_siniestro/${siniestro.idSiniestro}") },
                                                onGestionar = { viewModel.prepararAsignacion(siniestro) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "equipo" -> {
                    val liquidadores = estado.todosLosUsuarios.filter { it.rol.startsWith("LIQUIDADOR") }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Carga operativa por liquidador (${liquidadores.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryAndes
                            )
                        }
                        items(liquidadores, key = { "liq_eq_${it.idUsuario}" }) { liq ->
                            val casosLiq = estado.todosLosCasos.filter {
                                it.idLiquidador == liq.idUsuario && it.etapaNombre != "Cerrado"
                            }
                            val cerradosLiq = estado.todosLosCasos.count {
                                it.idLiquidador == liq.idUsuario && it.etapaNombre == "Cerrado"
                            }
                            val etiquetaEquipo = when (liq.equipo) {
                                "VEHICULOS" -> "VehÃ­culos"
                                "HOGAR" -> "Hogar e Incendio"
                                "SALUD" -> "Salud y Vida"
                                else -> liq.equipo ?: "General"
                            }

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
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(MintAndes),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${liq.nombre.firstOrNull() ?: ""}${liq.apellido.firstOrNull() ?: ""}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryAndes
                                                )
                                            }
                                            Column {
                                                Text("${liq.nombre} ${liq.apellido}", fontWeight = FontWeight.Bold, color = PrimaryAndes)
                                                Text("Especialidad: $etiquetaEquipo", fontSize = 12.sp, color = Color.DarkGray)
                                            }
                                        }
                                        Surface(
                                            color = if (casosLiq.isEmpty()) MintAndes else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (casosLiq.isEmpty()) "Disponible" else "${casosLiq.size} activos",
                                                color = if (casosLiq.isEmpty()) PrimaryAndes else Color(0xFF92400E),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "RUT ${liq.rut} Â· ${liq.correo} Â· $cerradosLiq cerrados",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    if (casosLiq.isNotEmpty()) {
                                        HorizontalDivider()
                                        casosLiq.forEach { c ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { navController.navigate("historial_siniestro/${c.idSiniestro}") }
                                                    .padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("${c.numero} Â· ${c.tipoSiniestroNombre}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Text(c.etapaNombre, fontSize = 11.sp, color = PrimaryAndes)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "avisos" -> {
                    if (estado.avisosAnalista.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sin notificaciones registradas", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    "Notificaciones operativas (${estado.avisosAnalista.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryAndes,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            items(estado.avisosAnalista, key = { "aviso_${it.idAviso}" }) { aviso ->
                                val matchNumero = Regex("SIN-[A-Z0-9-]+").find(aviso.mensaje)?.value
                                val casoVinculado = matchNumero?.let { num ->
                                    estado.todosLosCasos.firstOrNull { it.numero == num }
                                }

                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!aviso.leido) viewModel.marcarAvisoLeido(aviso.idAviso)
                                            if (casoVinculado != null) {
                                                if (casoVinculado.etapaNombre != "Cerrado") {
                                                    viewModel.prepararAsignacion(casoVinculado)
                                                } else {
                                                    navController.navigate("historial_siniestro/${casoVinculado.idSiniestro}")
                                                }
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
                                                    text = "Toca para revisar ${casoVinculado.numero} â†’",
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

                "usuarios" -> {
                    val chipAdminColors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryAndes,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = PrimaryAndes
                    )
                    val usuariosFiltrados = estado.todosLosUsuarios.filter { u ->
                        when (filtroRolAdmin) {
                            "LIQUIDADORES" -> u.rol.startsWith("LIQUIDADOR")
                            "INTERNOS" -> u.rol == "ANALISTA" || u.rol == "ADMINISTRADOR"
                            "ASEGURADOS" -> u.rol.startsWith("ASEGURADO")
                            else -> true
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = filtroRolAdmin == "TODOS",
                                onClick = { filtroRolAdmin = "TODOS" },
                                colors = chipAdminColors,
                                label = { Text("Todos (${estado.todosLosUsuarios.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = filtroRolAdmin == "LIQUIDADORES",
                                onClick = { filtroRolAdmin = "LIQUIDADORES" },
                                colors = chipAdminColors,
                                label = { Text("Liquidadores", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = filtroRolAdmin == "INTERNOS",
                                onClick = { filtroRolAdmin = "INTERNOS" },
                                colors = chipAdminColors,
                                label = { Text("Staff", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = filtroRolAdmin == "ASEGURADOS",
                                onClick = { filtroRolAdmin = "ASEGURADOS" },
                                colors = chipAdminColors,
                                label = { Text("Clientes", fontSize = 11.sp) }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(usuariosFiltrados, key = { "usr_${it.idUsuario}" }) { user ->
                                CardUsuarioAdmin(
                                    nombre = "${user.nombre} ${user.apellido}",
                                    rut = user.rut,
                                    correo = user.correo,
                                    rolActual = if (user.rol.startsWith("LIQUIDADOR")) "LIQUIDADOR_${user.equipo ?: "VEHICULOS"}" else user.rol,
                                    esUsuarioActual = sesionActual?.idUsuario == user.idUsuario,
                                    onCambiarRol = { nuevoRol ->
                                        viewModel.cambiarRolUsuarioAdmin(user.idUsuario, nuevoRol)
                                    },
                                    onEliminarUsuario = {
                                        usuarioAEliminar = user
                                    }
                                )
                            }
                        }
                    }
                }

                "catalogo" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "CatÃ¡logo de Ã¡reas y ramos asegurados (${estado.areasCatalogo.size})",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryAndes,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        items(estado.areasCatalogo, key = { "area_${it.idArea}" }) { area ->
                            val casosArea = estado.todosLosCasos.count { it.idArea == area.idArea }
                            val activosArea = estado.todosLosCasos.count { it.idArea == area.idArea && it.etapaNombre != "Cerrado" }
                            val polizas = area.polizasDisponibles.split("|").filter { it.isNotBlank() }
                            val esp = when (area.idArea) {
                                1 -> "Javier Paredes Â· Equipo VehÃ­culos"
                                2 -> "Nacho Vargas Â· Equipo Hogar"
                                3 -> "Elena Vargas Â· Equipo Salud"
                                else -> "Sin especialista titular"
                            }

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
                                        Text(area.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = PrimaryAndes)
                                        Surface(color = MintAndes, shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                text = "$activosArea activos / $casosArea total",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryAndes,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "PÃ³lizas habilitadas: ${polizas.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                    HorizontalDivider()
                                    Text("Titular del ramo: $esp", fontSize = 12.sp, color = PrimaryAndes, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    usuarioAEliminar?.let { usr ->
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Eliminar usuario",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Â¿Seguro que deseas eliminar permanentemente esta cuenta del sistema?",
                        fontSize = 13.sp
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${usr.nombre} ${usr.apellido}".trim(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text("RUT: ${usr.rut}", fontSize = 12.sp)
                            Text(usr.correo, fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.eliminarUsuarioAdmin(usr.idUsuario)
                        usuarioAEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }) {
                    Text("Cancelar", color = PrimaryAndes)
                }
            }
        )
    }

    val casoSel = estado.todosLosCasos.firstOrNull { it.idSiniestro == estado.idCasoSeleccionado }
    if (casoSel != null) {
        var modoSheet by remember(casoSel.idSiniestro) { mutableStateOf("liq") }
        var liqSeleccionado by remember(casoSel.idSiniestro) { mutableStateOf<Int?>(null) }
        var diasProrroga by remember(casoSel.idSiniestro) { mutableStateOf(90) }
        var justificacionProrroga by remember(casoSel.idSiniestro) { mutableStateOf("") }

        LaunchedEffect(estado.liquidadores) {
            if (liqSeleccionado == null && estado.liquidadores.isNotEmpty()) {
                liqSeleccionado = estado.liquidadores.first().idUsuario
            }
        }

        val chipDialogColors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryAndes,
            selectedLabelColor = Color.White
        )

        AlertDialog(
            onDismissRequest = { viewModel.cerrarSeleccion() },
            containerColor = Color.White,
            title = { Text("GestiÃ³n ${casoSel.numero}", fontWeight = FontWeight.Bold, color = PrimaryAndes) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    estado.errorAsignacion?.let { msgError ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msgError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Surface(
                        color = MintAndes.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(casoSel.tipoSiniestroNombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryAndes)
                            Text("${casoSel.companiaNombre} Â· PÃ³liza ${casoSel.numeroPoliza}", fontSize = 12.sp)
                            Text("Etapa: ${casoSel.etapaNombre} Â· ${casoSel.comunaNombre}", fontSize = 11.sp, color = Color.DarkGray)
                            Text(casoSel.descripcion, fontSize = 11.sp, color = Color.DarkGray)
                            if (casoSel.liquidadorNombre != null) {
                                Text("Liquidador: ${casoSel.liquidadorNombre}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PrimaryAndes)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val id = casoSel.idSiniestro
                                viewModel.cerrarSeleccion()
                                navController.navigate("evidencia/$id")
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryAndes)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Evidencias (${estado.documentosCasoSeleccionado.size})", fontSize = 11.sp, color = PrimaryAndes)
                        }
                        OutlinedButton(
                            onClick = {
                                val id = casoSel.idSiniestro
                                viewModel.cerrarSeleccion()
                                navController.navigate("historial_siniestro/$id")
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryAndes)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BitÃ¡cora", fontSize = 11.sp, color = PrimaryAndes)
                        }
                    }

                    if (estado.documentosCasoSeleccionado.isNotEmpty()) {
                        estado.documentosCasoSeleccionado.forEach { doc ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(15.dp), tint = PrimaryAndes)
                                Text(doc.nombreArchivo ?: doc.nombreTipoDocumento, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF16A34A))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = modoSheet == "liq",
                            onClick = { modoSheet = "liq" },
                            colors = chipDialogColors,
                            label = { Text("Asignar liquidador", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = modoSheet == "prorroga",
                            onClick = { modoSheet = "prorroga" },
                            colors = chipDialogColors,
                            label = { Text("PrÃ³rroga legal", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (modoSheet == "liq") {
                        Text(
                            text = "Especialidad requerida: ${estado.equipoSeleccionado}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryAndes
                        )

                        if (estado.cargandoLiquidadores) {
                            CircularProgressIndicator(color = PrimaryAndes)
                        } else if (estado.liquidadores.isEmpty()) {
                            Text("No hay liquidadores disponibles en ${estado.equipoSeleccionado}.", fontSize = 12.sp)
                        } else {
                            estado.liquidadores.forEach { liq ->
                                val cargaLiq = estado.todosLosCasos.count {
                                    it.idLiquidador == liq.idUsuario && it.etapaNombre != "Cerrado"
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (liqSeleccionado == liq.idUsuario) MintAndes.copy(alpha = 0.5f) else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (liqSeleccionado == liq.idUsuario) PrimaryAndes else Color.LightGray
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { liqSeleccionado = liq.idUsuario }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = liqSeleccionado == liq.idUsuario,
                                            onClick = { liqSeleccionado = liq.idUsuario },
                                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryAndes)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${liq.nombre} ${liq.apellido}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text("Especialidad: ${liq.equipo ?: "General"} Â· $cargaLiq casos activos", fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text("PrÃ³rroga fundada D.S. NÂ° 1.055:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = diasProrroga == 90,
                                onClick = { diasProrroga = 90 },
                                colors = chipDialogColors,
                                label = { Text("90 dÃ­as") }
                            )
                            FilterChip(
                                selected = diasProrroga == 180,
                                onClick = { diasProrroga = 180 },
                                colors = chipDialogColors,
                                label = { Text("180 dÃ­as") }
                            )
                        }
                        val largoActual = justificacionProrroga.trim().length
                        OutlinedTextField(
                            value = justificacionProrroga,
                            onValueChange = { justificacionProrroga = it.take(300) },
                            label = { Text("JustificaciÃ³n fundada") },
                            minLines = 3,
                            supportingText = {
                                Text(
                                    text = if (largoActual < 5) "Faltan ${5 - largoActual} caracteres (mÃ­nimo 5)" else "$largoActual/300 caracteres",
                                    color = if (largoActual < 5) MaterialTheme.colorScheme.error else PrimaryAndes
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAndes),
                    enabled = if (modoSheet == "liq") {
                        liqSeleccionado != null && !estado.asignando
                    } else {
                        justificacionProrroga.trim().length >= 5
                    },
                    onClick = {
                        if (modoSheet == "liq") {
                            liqSeleccionado?.let { viewModel.asignarLiquidador(it) }
                        } else {
                            viewModel.aplicarProrroga(casoSel.idSiniestro, diasProrroga, justificacionProrroga)
                        }
                    }
                ) {
                    Text(
                        if (modoSheet == "liq") {
                            if (estado.asignando) "Asignando..." else "Confirmar asignaciÃ³n"
                        } else {
                            "Aplicar prÃ³rroga"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cerrarSeleccion() }) {
                    Text("Cancelar", color = PrimaryAndes)
                }
            }
        )
    }
}

@Composable
private fun KpiHeaderOperativo(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
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
private fun CardCasoAnalistaCompleto(
    siniestro: SiniestroEntity,
    onVerEvidencias: () -> Unit,
    onVerHistorial: () -> Unit,
    onGestionar: () -> Unit
) {
    val dias = calcularDiasDesdeReporte(siniestro.fechaReporte)
    val totalPlazo = 45 + siniestro.diasProrroga
    val esRechazado = siniestro.motivoRechazoLiquidador != null && siniestro.etapaNombre != "Cerrado"

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
                Surface(
                    color = when {
                        esRechazado -> MaterialTheme.colorScheme.errorContainer
                        siniestro.etapaNombre == "Recibido" -> Color(0xFFFEF3C7)
                        siniestro.etapaNombre == "Cerrado" -> Color(0xFFE2E8F0)
                        else -> MintAndes
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (esRechazado) "Reasignar" else siniestro.etapaNombre,
                        color = when {
                            esRechazado -> MaterialTheme.colorScheme.onErrorContainer
                            siniestro.etapaNombre == "Recibido" -> Color(0xFF92400E)
                            siniestro.etapaNombre == "Cerrado" -> Color(0xFF334155)
                            else -> PrimaryAndes
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (siniestro.modeloVehiculo.isNotBlank()) {
                Text(siniestro.modeloVehiculo, fontSize = 12.sp, color = Color.DarkGray)
            }
            Text("${siniestro.companiaNombre} Â· PÃ³liza ${siniestro.numeroPoliza}", fontSize = 12.sp, color = Color.DarkGray)
            Text("Ingreso: ${siniestro.fechaReporte} Â· ${siniestro.comunaNombre}", fontSize = 11.sp, color = Color.Gray)

            if (siniestro.idLiquidador != null) {
                val estadoAceptacion = when (siniestro.aceptadoPorLiquidador) {
                    true -> "Aceptado"
                    false -> "Rechazado"
                    null -> "Pendiente de aceptaciÃ³n (2d hÃ¡b.)"
                }
                Text(
                    text = "Liquidador: ${siniestro.liquidadorNombre} Â· $estadoAceptacion",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryAndes
                )
            }

            siniestro.motivoRechazoLiquidador?.let { motivo ->
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Rechazado â€” $motivo",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (siniestro.etapaNombre != "Cerrado") {
                BarraSemaforoLegal(diasTranscurridos = dias, totalDias = totalPlazo)
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

            if (siniestro.etapaNombre != "Cerrado") {
                Button(
                    onClick = onGestionar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esRechazado) MaterialTheme.colorScheme.error else PrimaryAndes
                    )
                ) {
                    Text(
                        text = when {
                            esRechazado -> "Reasignar Liquidador"
                            siniestro.idLiquidador == null -> "Asignar Liquidador"
                            else -> "PrÃ³rroga Legal / Reasignar"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CardUsuarioAdmin(
    nombre: String,
    rut: String,
    correo: String,
    rolActual: String,
    esUsuarioActual: Boolean,
    onCambiarRol: (String) -> Unit,
    onEliminarUsuario: () -> Unit
) {
    val opcionesRoles = listOf(
        "ASEGURADO_NATURAL" to "Asegurado 1 (Persona Natural)",
        "ASEGURADO_EMPRESA" to "Asegurado 2 (Empresa / Corredor)",
        "ANALISTA" to "Analista Operativo",
        "ADMINISTRADOR" to "Administrador",
        "LIQUIDADOR_VEHICULOS" to "Liquidador Â· VehÃ­culos",
        "LIQUIDADOR_HOGAR" to "Liquidador Â· Hogar",
        "LIQUIDADOR_SALUD" to "Liquidador Â· Salud"
    )
    var expandido by remember { mutableStateOf(false) }
    val etiquetaActual = opcionesRoles.firstOrNull { it.first == rolActual }?.second ?: rolActual
    val iniciales = nombre.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MintAndes),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iniciales, fontWeight = FontWeight.Bold, color = PrimaryAndes, fontSize = 13.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(nombre, fontWeight = FontWeight.Bold, color = PrimaryAndes)
                    Text("RUT $rut Â· $correo", fontSize = 12.sp, color = Color.DarkGray)
                }
                if (esUsuarioActual) {
                    Surface(color = MintAndes, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "Tu cuenta",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAndes,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    IconButton(onClick = onEliminarUsuario) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar usuario",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = etiquetaActual,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol y Especialidad") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expandido = true }
                )
                DropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    opcionesRoles.forEach { (codigo, etiqueta) ->
                        DropdownMenuItem(
                            text = { Text(etiqueta) },
                            onClick = {
                                onCambiarRol(codigo)
                                expandido = false
                            }
                        )
                    }
                }
            }
        }
    }
}
