package com.andes.bpo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.viewmodel.MisSiniestrosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSiniestrosScreen(
    navController: NavController,
    rut: String,
    viewModel: MisSiniestrosViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    LaunchedEffect(rut) { viewModel.cargarSiniestros(rut) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis siniestros") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("nuevo_siniestro/$rut") }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo siniestro")
            }
        }
    ) { paddingValues ->
        when {
            estado.cargando -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            estado.error != null -> Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { Text(estado.error!!, color = MaterialTheme.colorScheme.error) }

            estado.siniestros.isEmpty() -> Column(
                Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aún no tienes siniestros registrados", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Presiona el botón + para reportar uno.")
            }

            else -> LazyColumn(
                Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Hola, ${estado.nombreUsuario}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("Tienes ${estado.siniestros.size} siniestro(s) registrado(s).")
                }
                items(estado.siniestros, key = { it.idSiniestro }) { siniestro ->
                    TarjetaSiniestro(
                        siniestro = siniestro,
                        onVerHistorial = {
                            navController.navigate("historial_siniestro/${siniestro.idSiniestro}")
                        },
                        onVerEvidencias = {
                            navController.navigate("evidencia/${siniestro.idSiniestro}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaSiniestro(
    siniestro: SiniestroEntity,
    onVerHistorial: () -> Unit,
    onVerEvidencias: () -> Unit
) {
    var expandido by remember(siniestro.idSiniestro) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expandido = !expandido },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                siniestro.numero,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(siniestro.tipoSiniestroNombre)
            Text(siniestro.companiaNombre, style = MaterialTheme.typography.bodySmall)
            Text(
                "Estado: ${siniestro.etapaNombre}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text("Reporte: ${siniestro.fechaReporte}", style = MaterialTheme.typography.bodySmall)
            if (expandido) {
                HorizontalDivider()
                Text("Póliza: ${siniestro.numeroPoliza}")
                Text("Ocurrencia: ${siniestro.fechaOcurrencia}")
                Text("Comuna: ${siniestro.comunaNombre}")
                Text("Descripción: ${siniestro.descripcion}")
                Text("Liquidador: ${siniestro.liquidadorNombre ?: "Sin asignar"}")
                Text("Próximo paso: ${siniestro.proximoPasoTexto}")
                if (siniestro.etapaNombre == "Cerrado") {
                    HorizontalDivider()
                    Text("Resultado del siniestro", fontWeight = FontWeight.Bold)
                    Text(siniestro.resultadoCierre ?: "Resultado no informado")
                    Text("Mensaje de cierre", fontWeight = FontWeight.Bold)
                    Text(siniestro.mensajeCierre ?: "No hay un mensaje de cierre registrado.")
                }
            }
            Text(
                if (expandido) "Toca para ocultar detalles" else "Toca para ver detalles",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()
            TextButton(onClick = onVerHistorial, modifier = Modifier.fillMaxWidth()) {
                Text("Ver historial")
            }
            OutlinedButton(onClick = onVerEvidencias, modifier = Modifier.fillMaxWidth()) {
                Text("Ver evidencias")
            }
        }
    }
}
