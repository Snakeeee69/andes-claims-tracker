package com.andes.bpo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SolicitarDocumentoDialog(
    guardando: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSolicitar: (String, String) -> Unit
) {
    val tipos = listOf(
        "Fotografía del daño",
        "Presupuesto de reparación",
        "Denuncia policial",
        "Otro documento"
    )

    var tipoSeleccionado by remember {
        mutableStateOf(tipos.first())
    }

    var comentario by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = {
            if (!guardando) onDismiss()
        },
        title = {
            Text("Solicitar documento")
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Selecciona el documento que necesitas."
                )

                tipos.forEach { tipo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !guardando) {
                                tipoSeleccionado = tipo
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoSeleccionado == tipo,
                            onClick = {
                                tipoSeleccionado = tipo
                            },
                            enabled = !guardando
                        )

                        Text(tipo)
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = comentario,
                    onValueChange = {
                        comentario = it.take(300)
                    },
                    label = {
                        Text("Comentario")
                    },
                    placeholder = {
                        Text(
                            "Ej.: Adjunta el presupuesto del taller."
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    enabled = !guardando,
                    isError = error != null,
                    supportingText = {
                        Text(
                            error ?: "${comentario.length}/300 caracteres"
                        )
                    }
                )

                Text(
                    "El asegurado verá esta solicitud " +
                            "como pendiente.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando &&
                        comentario.trim().length in 5..300,
                onClick = {
                    onSolicitar(
                        tipoSeleccionado,
                        comentario.trim()
                    )
                }
            ) {
                Text(
                    if (guardando) {
                        "Guardando..."
                    } else {
                        "Solicitar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !guardando,
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}
