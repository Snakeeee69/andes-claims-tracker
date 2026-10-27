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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController)
        }

        composable("recuperar") {
            RecuperarContrasenaScreen(navController)
        }

        composable("tracking/{rut}") { backStackEntry ->
            val rutUsuario = backStackEntry.arguments?.getString("rut") ?: ""
            TrackingScreen(navController = navController, rut = rutUsuario)
        }

        composable("evidencia") {
            EvidenciaScreen(navController = navController)
        }

        composable("historial") {
            HistorialScreen(navController = navController)
        }

        composable("notificaciones") {
            NotificacionesScreen(navController = navController)
        }

        composable("ayuda") {
            CentroAyudaScreen(navController = navController)
        }

        composable("nuevo_siniestro") {
            NuevoSiniestroScreen(navController = navController)
        }
    }
}