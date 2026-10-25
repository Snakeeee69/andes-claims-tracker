package com.andes.bpo.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andes.bpo.ui.theme.*

@Composable
fun StatusBadge(status: Int) {
    val (bgColor, textColor, label) = when (status) {
        0 -> Triple(StatusRecibidoBg, StatusRecibidoText, "Recibido")
        1 -> Triple(StatusEvaluacionBg, StatusEvaluacionText, "En evaluación")
        2 -> Triple(StatusLiquidacionBg, StatusLiquidacionText, "En liquidación")
        3 -> Triple(StatusCerradoBg, StatusCerradoText, "Cerrado")
        else -> Triple(StatusCerradoBg, StatusCerradoText, "En progreso")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}