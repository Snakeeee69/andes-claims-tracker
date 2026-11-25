package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.data.local.AppDatabase
import com.andes.bpo.ui.theme.BackgroundAndes
import com.andes.bpo.ui.theme.MintAndes
import com.andes.bpo.ui.theme.PrimaryAndes
import com.andes.bpo.viewmodel.HistorialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    navController: NavController,
    idSiniestro: Int,
    viewModel: HistorialViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context, scope) }
    val siniestroDetalle by db.siniestroDao().getSiniestroPorId(idSiniestro).collectAsState(initial = null)
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(idSiniestro) {
        viewModel.cargarHistorial(idSiniestro)
    }

    Scaffold(
        containerColor = BackgroundAndes,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryAndes,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = {
                    Column {
                        Text("Ficha y BitÃ¡cora del Caso", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (estado.numeroSiniestro.isNotEmpty()) {
                            Text(
                                estado.numeroSiniestro,
                                style = MaterialTheme.typography.bodySmall,
                                color = MintAndes
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                estado.cargando -> {
                    CircularProgressIndicator(
                        color = PrimaryAndes,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                estado.error != null -> {
                    Text(
                        text = estado.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Etapa actual: ${estado.etapaActual}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryAndes
                                    )
                                    siniestroDetalle?.let { sin ->
                                        Text(sin.tipoSiniestroNombre, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("${sin.companiaNombre} Â· PÃ³liza ${sin.numeroPoliza}", fontSize = 12.sp)
                                        Text("Comuna: ${sin.comunaNombre} Â· Ocurrencia: ${sin.fechaOcurrencia}", fontSize = 12.sp, color = Color.DarkGray)
                                        Text(sin.descripcion, fontSize = 12.sp, color = Color.DarkGray)
                                        Text(
                                            text = "Liquidador: ${sin.liquidadorNombre ?: "Sin asignar"}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryAndes
                                        )
                                        if (sin.etapaNombre != "Cerrado") {
                                            BarraSemaforoLegal(
                                                diasTranscurridos = calcularDiasDesdeReporte(sin.fechaReporte),
                                                totalDias = 45 + sin.diasProrroga
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { navController.navigate("evidencia/${sin.idSiniestro}") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryAndes)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ver Evidencias del Caso", color = PrimaryAndes)
                                        }
                                    }
                                }
                            }
                        }

                        if (estado.gestiones.isEmpty()) {
                            item {
                                Text(
                                    text = "AÃºn no hay gestiones registradas.",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        } else {
                            items(
                                items = estado.gestiones,
                                key = { it.idGestion }
                            ) { gestion ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = gestion.fechaHora,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.Gray
                                            )
                                            if (!gestion.visibleAsegurado) {
                                                Surface(
                                                    color = Color(0xFFFEF3C7),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Nota interna",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF92400E),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = gestion.descripcion,
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        HorizontalDivider()

                                        Text(
                                            text = "Responsable: ${gestion.responsable}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PrimaryAndes,
                                            fontWeight = FontWeight.SemiBold
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
