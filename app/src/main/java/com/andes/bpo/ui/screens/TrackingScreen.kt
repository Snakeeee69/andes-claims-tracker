package com.andes.bpo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrackingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- TARJETA PRINCIPAL (AZUL CORPORATIVO ANDES) ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E40AF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MI SINIESTRO ACTIVO",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    BadgeEstado("En evaluación")
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "SIN-2026-08471",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grilla de Datos Rápidos
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoBoxItem(titulo = "Tipo", valor = "Accidente vehicular")
                    InfoBoxItem(titulo = "Fecha ocurrencia", valor = "15 ago 2026")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoBoxItem(titulo = "Vehículo", valor = "BJKR-94")
                    InfoBoxItem(titulo = "Reporte", valor = "16 ago 2026")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stepper de 4 Pasos (Recibido -> En evaluación -> En liquidación -> Cerrado)
                StepperSiniestro(pasoActual = 2)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- TARJETA DE LIQUIDADOR ASIGNADO ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF818CF8),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("MG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Liquidador/a asignado/a", color = Color.Gray, fontSize = 12.sp)
                    Text("María González", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                    Text("Liquidaciones Andes · Andes SpA", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DETALLES ADICIONALES ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Detalles del siniestro", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                Spacer(modifier = Modifier.height(12.dp))
                DetalleFila("Subtipo", "Colisión con daños materiales")
                DetalleFila("Modelo", "Toyota Yaris 2022")
                DetalleFila("Póliza", "POL-88231-VH")
                DetalleFila("Aseguradora", "Aseguradora del Sur S.A.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BANNER DE AVISO / NOTIFICACIÓN ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEFF6FF),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("ℹ️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Te notificaremos de inmediato ante cualquier cambio de estado. No necesitas llamar ni visitar una oficina.",
                    color = Color(0xFF1E40AF),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun InfoBoxItem(titulo: String, valor: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.width(150.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(titulo, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            Text(valor, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun BadgeEstado(texto: String) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color(0xFFFEF3C7)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = Color(0xFFD97706), modifier = Modifier.size(6.dp)) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(texto, color = Color(0xFF92400E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StepperSiniestro(pasoActual: Int) {
    val pasos = listOf("Recibido", "En evaluación", "En liquidación", "Cerrado")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        pasos.forEachIndexed { index, paso ->
            val numero = index + 1
            val completado = numero <= pasoActual
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = if (completado) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (completado && numero < pasoActual) "✓" else "$numero",
                            color = if (completado) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = paso,
                    color = if (completado) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = if (completado) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DetalleFila(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, color = Color.Gray, fontSize = 13.sp)
        Text(valor, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF374151))
    }
}
