package com.andes.bpo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.andes.bpo.viewmodel.BandejaLiquidadorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaLiquidadorScreen(
    navController: NavController,
    viewModel: BandejaLiquidadorViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    var idCasoSeleccionado by remember {
        mutableStateOf<Int?>(null)
    }

    var casoParaAvanzar by remember {
        mutableStateOf<Int?>(null)
    }
    var casoParaCerrar by remember {
        mutableStateOf<Int?>(null)
    }

    var casoParaSolicitar by remember {
        mutableStateOf<Int?>(null)
    }

    var resultadoCierre by remember {
        mutableStateOf("Aprobado")
    }

    var mensajeCierre by remember {
        mutableStateOf("")
    }
    var descripcion by remember(idCasoSeleccionado) {
        mutableStateOf("")
    }

    var visibleAsegurado by remember(idCasoSeleccionado) {
        mutableStateOf(true)
    }

    val snackbar = remember { SnackbarHostState() }

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
        estado.mensaje?.let { mensaje ->

            idCasoSeleccionado = null

            snackbar.showSnackbar(
                message = mensaje,
                duration = SnackbarDuration.Long
            )

            viewModel.limpiarMensaje()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis casos asignados") },
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
                        text = estado.error!!,
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
                    Text("No tienes siniestros asignados")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Hola, ${estado.nombreLiquidador}",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "Casos asignados: ${estado.casos.size}"
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
                                    "Estado: ${siniestro.etapaNombre}",
                                    color = MaterialTheme.colorScheme.primary
                                )

                                HorizontalDivider()

                                Text(
                                    "Comuna: ${siniestro.comunaNombre}"
                                )

                                Text(
                                    "Fecha: ${siniestro.fechaReporte}"
                                )

                                Text(
                                    "Descripción: ${siniestro.descripcion}"
                                )

                                OutlinedButton(
                                    onClick = {
                                        navController.navigate(
                                            "documentos_liquidador/${siniestro.idSiniestro}"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ver documentos")
                                }

                                if (siniestro.etapaNombre != "Cerrado") {

                                    // BOTÓN 1: Registrar gestión
                                    Button(
                                        onClick = {
                                            viewModel.limpiarErrorGestion()
                                            idCasoSeleccionado = siniestro.idSiniestro
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Registrar gestión")
                                    }

                                    // BOTÓN 2: Solicitar documento
                                    if (siniestro.etapaNombre == "En evaluación") {

                                        OutlinedButton(
                                            onClick = {
                                                viewModel.limpiarEstadoSolicitud()
                                                casoParaSolicitar = siniestro.idSiniestro
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !estado.solicitandoDocumento
                                        ) {
                                            Text("Solicitar documento")
                                        }

                                        // BOTÓN 3: Avanzar a liquidación
                                        OutlinedButton(
                                            onClick = {
                                                casoParaAvanzar = siniestro.idSiniestro
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !estado.avanzandoEtapa
                                        ) {
                                            Text("Avanzar a liquidación")
                                        }
                                    }

                                    // BOTÓN 4: Cerrar siniestro
                                    // En evaluación: Rechazado
                                    // En liquidación: Aprobado o Rechazado
                                    if (
                                        siniestro.etapaNombre == "En evaluación" ||
                                        siniestro.etapaNombre == "En liquidación"
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                casoParaCerrar = siniestro.idSiniestro

                                                resultadoCierre =
                                                    if (siniestro.etapaNombre == "En evaluación") {
                                                        "Rechazado"
                                                    } else {
                                                        "Aprobado"
                                                    }

                                                mensajeCierre = ""
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Cerrar siniestro")
                                        }
                                    }

                                } else {
                                    Text(
                                        text = "Caso cerrado",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }


                            }
                        }
                    }
                }
            }
        }
    }

    if (idCasoSeleccionado != null) {

        AlertDialog(
            onDismissRequest = {
                if (!estado.guardandoGestion) {
                    idCasoSeleccionado = null
                    viewModel.limpiarErrorGestion()
                }
            },
            title = {
                Text("Registrar gestión")
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Describe la gestión realizada en este siniestro."
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {
                            descripcion = it.take(500)
                            viewModel.limpiarErrorGestion()
                        },
                        label = {
                            Text("Descripción")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        isError = estado.errorGestion != null,
                        supportingText = {
                            Text(
                                estado.errorGestion
                                    ?: "${descripcion.length}/500 caracteres"
                            )
                        }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Visible para el asegurado",
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = visibleAsegurado,
                            onCheckedChange = {
                                visibleAsegurado = it
                            }
                        )
                    }

                    Text(
                        text = if (visibleAsegurado) {
                            "El asegurado podrá ver esta gestión en su historial."
                        } else {
                            "Esta gestión será de uso interno."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled =
                        descripcion.trim().length >= 5 &&
                                !estado.guardandoGestion,
                    onClick = {
                        idCasoSeleccionado?.let { id ->
                            viewModel.registrarGestion(
                                idSiniestro = id,
                                descripcion = descripcion,
                                visibleAsegurado = visibleAsegurado
                            )
                        }
                    }
                ) {
                    Text(
                        if (estado.guardandoGestion) {
                            "Guardando..."
                        } else {
                            "Guardar"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !estado.guardandoGestion,
                    onClick = {
                        idCasoSeleccionado = null
                        viewModel.limpiarErrorGestion()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (casoParaAvanzar != null) {

        AlertDialog(
            onDismissRequest = {
                if (!estado.avanzandoEtapa) {
                    casoParaAvanzar = null
                }
            },

            title = {
                Text("Avanzar siniestro")
            },

            text = {
                Text(
                    "¿Confirmas que este siniestro debe pasar " +
                            "de En evaluación a En liquidación? " +
                            "El cambio quedará registrado en el historial."
                )
            },

            confirmButton = {
                TextButton(
                    enabled = !estado.avanzandoEtapa,
                    onClick = {
                        casoParaAvanzar?.let { id ->
                            viewModel.avanzarALiquidacion(id)
                        }

                        casoParaAvanzar = null
                    }
                ) {
                    Text("Confirmar")
                }
            },

            dismissButton = {
                TextButton(
                    enabled = !estado.avanzandoEtapa,
                    onClick = {
                        casoParaAvanzar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (casoParaCerrar != null) {

        val siniestroSeleccionado = estado.casos.find {
            it.idSiniestro == casoParaCerrar
        }

        val enEvaluacion =
            siniestroSeleccionado?.etapaNombre == "En evaluación"

        AlertDialog(
            onDismissRequest = {
                if (!estado.cerrandoSiniestro) {
                    casoParaCerrar = null
                }
            },
            title = {
                Text("Cerrar siniestro")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Selecciona el resultado del siniestro.")

                    if (!enEvaluacion) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = resultadoCierre == "Aprobado",
                                onClick = {
                                    resultadoCierre = "Aprobado"
                                }
                            )

                            Text("Aprobado")
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = resultadoCierre == "Rechazado",
                            onClick = {
                                resultadoCierre = "Rechazado"
                            }
                        )

                        Text("Rechazado")
                    }

                    OutlinedTextField(
                        value = mensajeCierre,
                        onValueChange = {
                            mensajeCierre = it.take(500)
                        },
                        label = {
                            Text("Mensaje para el asegurado")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        isError = mensajeCierre.isNotEmpty() &&
                                mensajeCierre.trim().length < 10,
                        supportingText = {
                            Text("Mínimo 10 caracteres")
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = mensajeCierre.trim().length >= 10 &&
                            !estado.cerrandoSiniestro,
                    onClick = {
                        casoParaCerrar?.let { id ->
                            viewModel.cerrarSiniestro(
                                idSiniestro = id,
                                resultado = resultadoCierre,
                                mensajeCierre = mensajeCierre
                            )
                        }

                        casoParaCerrar = null
                    }
                ) {
                    Text("Confirmar cierre")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        casoParaCerrar = null
                    },
                    enabled = !estado.cerrandoSiniestro
                ) {
                    Text("Cancelar")
                }
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

                casoParaSolicitar?.let { id ->
                    viewModel.solicitarDocumento(
                        idSiniestro = id,
                        tipoDocumento = tipo,
                        comentario = comentario
                    )
                }
            }
        )
    }

}
