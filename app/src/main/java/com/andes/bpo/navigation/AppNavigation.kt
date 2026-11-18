package com.andes.bpo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andes.bpo.ui.screens.CentroAyudaScreen
import com.andes.bpo.ui.screens.EvidenciaScreen
import com.andes.bpo.ui.screens.HistorialScreen
import com.andes.bpo.ui.screens.LoginScreen
import com.andes.bpo.ui.screens.NotificacionesScreen
import com.andes.bpo.ui.screens.NuevoSiniestroScreen
import com.andes.bpo.ui.screens.RecuperarContrasenaScreen
import com.andes.bpo.ui.screens.TrackingScreen
import com.andes.bpo.ui.screens.RegistroScreen
import com.andes.bpo.ui.screens.MisSiniestrosScreen
import com.andes.bpo.ui.screens.PanelInternoScreen
import com.andes.bpo.ui.screens.BandejaAnalistaScreen
import com.andes.bpo.ui.screens.BandejaLiquidadorScreen
import com.andes.bpo.ui.screens.DocumentosLiquidadorScreen
import com.andes.bpo.ui.screens.ConfigurarCodigoRecuperacionScreen
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController)
        }

        composable("registro") {
            RegistroScreen(navController)
        }

        composable("recuperar") {
            RecuperarContrasenaScreen(navController)
        }

        composable("configurar_recuperacion") {
            ConfigurarCodigoRecuperacionScreen(
                navController = navController
            )
        }

        composable("tracking/{rut}") { backStackEntry ->
            val rutUsuario = backStackEntry.arguments?.getString("rut") ?: ""
            TrackingScreen(navController = navController, rut = rutUsuario)
        }

        composable("evidencia/{idSiniestro}") { backStackEntry ->

            val idSiniestro = backStackEntry.arguments
                ?.getString("idSiniestro")
                ?.toIntOrNull() ?: -1

            EvidenciaScreen(
                navController = navController,
                idSiniestro = idSiniestro
            )
        }


        composable("historial_siniestro/{idSiniestro}") { backStackEntry ->

            val idSiniestro = backStackEntry.arguments
                ?.getString("idSiniestro")
                ?.toIntOrNull() ?: -1

            HistorialScreen(
                navController = navController,
                idSiniestro = idSiniestro
            )
        }

        composable("notificaciones/{rut}") { backStackEntry ->
            val rutUsuario = backStackEntry.arguments?.getString("rut") ?: ""
            NotificacionesScreen(navController = navController, rut = rutUsuario)
        }

        composable("ayuda") {
            CentroAyudaScreen(navController = navController)
        }

        composable("nuevo_siniestro/{rut}") { backStackEntry ->
            val rutUsuario = backStackEntry.arguments?.getString("rut") ?: ""
            NuevoSiniestroScreen(navController = navController, rut = rutUsuario)
        }

        composable("panel_liquidador") {
            PanelInternoScreen(
                navController = navController,
                rolRequerido = "LIQUIDADOR"
            )
        }

        composable("panel_analista") {
            PanelInternoScreen(
                navController = navController,
                rolRequerido = "ANALISTA"
            )
        }

        composable("bandeja_liquidador") {
            BandejaLiquidadorScreen(
                navController = navController
            )
        }

        composable("bandeja_analista") {
            BandejaAnalistaScreen(
                navController = navController
            )
        }

        composable("documentos_liquidador/{idSiniestro}") { backStackEntry ->

            val idSiniestro = backStackEntry.arguments
                ?.getString("idSiniestro")
                ?.toIntOrNull() ?: -1

            DocumentosLiquidadorScreen(
                navController = navController,
                idSiniestro = idSiniestro
            )
        }

        composable("mis_siniestros/{rut}") { backStackEntry ->
            val rutUsuario =
                backStackEntry.arguments?.getString("rut") ?: ""

            MisSiniestrosScreen(
                navController = navController,
                rut = rutUsuario
            )
        }
    }
}