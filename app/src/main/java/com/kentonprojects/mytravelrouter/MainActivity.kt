package com.kentonprojects.mytravelrouter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kentonprojects.mytravelrouter.navigation.TravelRouterNavigation
import com.kentonprojects.mytravelrouter.ui.theme.MyTravelRouterTheme
import com.kentonprojects.mytravelrouter.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyTravelRouterTheme {
                val navController = rememberNavController()
                val vpnViewModel: VpnViewModel = viewModel()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TravelRouterNavigation(
                        navController = navController,
                        vpnViewModel = vpnViewModel
                    )
                }
            }
        }
    }
}