package com.andes.bpo.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun PanelAdminScreen(navController: NavController) {
    BandejaAnalistaScreen(
        navController = navController,
        esModoAdmin = true
    )
}
