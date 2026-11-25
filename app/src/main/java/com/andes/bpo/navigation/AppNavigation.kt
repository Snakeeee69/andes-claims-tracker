package com.andes.bpo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andes.bpo.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("cambio_clave_obligatorio") { CambioClaveObligatorioScreen(navController) }
        composable("registro") { RegistroScreen(navController) }
        composable("recuperar") { RecuperarContrasenaScreen(navController) }
        composable("configurar_recuperacion") { ConfigurarCodigoRecuperacionScreen(navController) }

        composable("tracking/{rut}") { backStackEntry ->
            val rutUsuario = backStackEntry.arguments?.getString("rut") ?: ""
            TrackingScreen(navController = navController, rut = rutUsuario)
        }

        composable("evidencia/{idSiniestro}") { backStackEntry ->
            val idSiniestro = backStackEntry.arguments?.getString("idSiniestro")?.toIntOrNull() ?: -1
            EvidenciaScreen(navController = navController, idSiniestro = idSiniestro)
        }

        composable("historial_siniestro/{idSiniestro}") { backStackEntry ->
            val idSiniestro = backStackEntry.arguments?.getString("idSiniestro")?.toIntOrNull() ?: -1
            HistorialScreen(navController = navController, idSiniestro = idSiniestro)
        }

        composable("notificaciones/{rut}") { backStackEntry ->
            val rutUsuario = backStackEntry.arguments?.getString("rut") ?: ""
            NotificacionesScreen(navController = navController, rut = rutUsuario)
        }

        composable("ayuda") { CentroAyudaScreen(navController = navController) }
        composable("perfil") { PerfilScreen(navController = navController) }

        composable("nuevo_siniestro/{rut}") { backStackEntry ->
            val rutUsuario = backStackEntry.arguments?.getString("rut") ?: ""
            NuevoSiniestroScreen(navController = navController, rut = rutUsuario)
        }

        composable("panel_liquidador") {
            PanelInternoScreen(navController = navController, rolRequerido = "LIQUIDADOR")
        }

        composable("panel_analista") {
            PanelInternoScreen(navController = navController, rolRequerido = "ANALISTA")
        }

        composable("panel_admin") {
            PanelAdminScreen(navController = navController)
        }

        composable("bandeja_liquidador") { BandejaLiquidadorScreen(navController = navController) }
        composable("bandeja_analista") { BandejaAnalistaScreen(navController = navController) }

        composable("documentos_liquidador/{idSiniestro}") { backStackEntry ->
            val idSiniestro = backStackEntry.arguments?.getString("idSiniestro")?.toIntOrNull() ?: -1
            DocumentosLiquidadorScreen(navController = navController, idSiniestro = idSiniestro)
        }

        composable("mis_siniestros/{rut}") { backStackEntry ->
            val rutUsuario = backStackEntry.arguments?.getString("rut") ?: ""
            MisSiniestrosScreen(navController = navController, rut = rutUsuario)
        }
    }
}
