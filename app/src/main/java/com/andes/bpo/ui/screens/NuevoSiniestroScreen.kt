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
import androidx.navigation.NavController
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.ui.theme.PrimaryAndes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoSiniestroScreen(
    navController: NavController,
    onGuardarSiniestro: (SiniestroEntity) -> Unit = {}
) {
    val colorAndes = PrimaryAndes
    val context = LocalContext.current

    var compania by remember { mutableStateOf("") }
    var numeroPoliza by remember { mutableStateOf("") }
    var tipoSiniestro by remember { mutableStateOf("Colisión") }
    var fechaOcurrencia by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var errorMensaje by remember { mutableStateOf<String?>(null) }

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

                OutlinedTextField(
                    value = tipoSiniestro,
                    onValueChange = { tipoSiniestro = it },
                    label = { Text("Tipo de Siniestro (ej: Colisión)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fechaOcurrencia,
                    onValueChange = {

                        if (it.length <= 10) fechaOcurrencia = it
                    },
                    label = { Text("Fecha (DD/MM/AAAA)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Ej: 15/05/2024") },
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
                        if (compania.isBlank() || numeroPoliza.isBlank() || descripcion.isBlank() || fechaOcurrencia.isBlank()) {
                            errorMensaje = "Por favor completa todos los campos principales."
                        } else {
                            val nuevoSiniestro = SiniestroEntity(
                                numero = "SIN-26-${(100000..999999).random()}",
                                idAsegurado = 1,
                                companiaNombre = compania,
                                numeroPoliza = numeroPoliza,
                                tipoSiniestroNombre = tipoSiniestro,
                                fechaOcurrencia = fechaOcurrencia,
                                fechaReporte = "Hoy",
                                comunaNombre = comuna,
                                descripcion = descripcion,
                                etapaNombre = "Recibido",
                                liquidadorNombre = "Por asignar",
                                proximoPasoTexto = "Revisión inicial"
                            )
                            onGuardarSiniestro(nuevoSiniestro)


                            Toast.makeText(context, "Siniestro creado exitosamente", Toast.LENGTH_LONG).show()

                            navController.popBackStack()
                        }
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