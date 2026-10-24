package com.andes.bpo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.andes.bpo.navigation.AppNavigation
import com.andes.bpo.ui.theme.SiniestrosBPOTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SiniestrosBPOTheme {
                AppNavigation()
            }
        }
    }
}