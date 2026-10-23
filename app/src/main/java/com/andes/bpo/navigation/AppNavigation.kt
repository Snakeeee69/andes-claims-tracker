package com.andes.bpo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andes.bpo.ui.screens.LoginScreen
import com.andes.bpo.ui.screens.RecuperarContrasenaScreen
import com.andes.bpo.ui.screens.TrackingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // NavHost es el contenedor que cambia las pantallas
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(navController)
        }

        composable("recuperar") {
            RecuperarContrasenaScreen(navController)
        }

        composable("tracking") {
            TrackingScreen(navController)
        }
    }
}