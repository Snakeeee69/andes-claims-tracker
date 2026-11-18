package com.andes.bpo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.BandejaAnalistaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaAnalistaScreen(
    navController: NavController,
    viewModel: BandejaAnalistaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.cargarCasos()
    }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { mensaje ->
            snackbar.showSnackbar(mensaje)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Casos sin asignar") },
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
        },
        snackbarHost = {
            SnackbarHost(snackbar)
        }
    ) { padding ->

        when {
            estado.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            estado.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        estado.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            estado.casos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay casos pendientes de asignación")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Pendientes: ${estado.casos.size}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(
                        items = estado.casos,
                        key = { it.idSiniestro }
                    ) { siniestro ->

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    siniestro.numero,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(siniestro.tipoSiniestroNombre)

                                Text(siniestro.companiaNombre)

                                Text(
                                    "Comuna: ${siniestro.comunaNombre}"
                                )

                                Text(
                                    "Estado: ${siniestro.etapaNombre}",
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Button(
                                    onClick = {
                                        viewModel.prepararAsignacion(siniestro)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Asignar liquidador")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Ventana para seleccionar el liquidador
    if (estado.idCasoSeleccionado != null) {

        var liquidadorSeleccionado by remember(
            estado.idCasoSeleccionado
        ) {
            mutableStateOf<Int?>(null)
        }

        AlertDialog(
            onDismissRequest = {
                viewModel.cerrarSeleccion()
            },
            title = {
                Text("Asignar liquidador")
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Equipo requerido: " +
                                estado.equipoSeleccionado
                    )

                    HorizontalDivider()

                    when {
                        estado.cargandoLiquidadores -> {
                            CircularProgressIndicator()
                        }

                        estado.liquidadores.isEmpty() -> {
                            Text(
                                "No hay liquidadores registrados " +
                                        "para este equipo."
                            )
                        }

                        else -> {
                            estado.liquidadores.forEach { liquidador ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            liquidadorSeleccionado =
                                                liquidador.idUsuario
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected =
                                            liquidadorSeleccionado ==
                                                    liquidador.idUsuario,
                                        onClick = {
                                            liquidadorSeleccionado =
                                                liquidador.idUsuario
                                        }
                                    )

                                    Column {
                                        Text(
                                            "${liquidador.nombre} " +
                                                    liquidador.apellido
                                        )

                                        Text(
                                            liquidador.equipo ?: "",
                                            style = MaterialTheme
                                                .typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (estado.errorAsignacion != null) {
                        Text(
                            estado.errorAsignacion!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled =
                        liquidadorSeleccionado != null &&
                                !estado.asignando,
                    onClick = {
                        liquidadorSeleccionado?.let { id ->
                            viewModel.asignarLiquidador(id)
                        }
                    }
                ) {
                    Text(
                        if (estado.asignando) {
                            "Asignando..."
                        } else {
                            "Confirmar"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.cerrarSeleccion()
                    },
                    enabled = !estado.asignando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
