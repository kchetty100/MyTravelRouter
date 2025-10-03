package com.kentonprojects.mytravelrouter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kentonprojects.mytravelrouter.ui.components.DashboardScreen
import com.kentonprojects.mytravelrouter.ui.components.SettingsScreen
import com.kentonprojects.mytravelrouter.ui.components.AddConfigScreen
import com.kentonprojects.mytravelrouter.ui.components.ConfigScreen
import com.kentonprojects.mytravelrouter.ui.theme.MyTravelRouterTheme
import com.kentonprojects.mytravelrouter.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyTravelRouterTheme {
                val vpnViewModel: VpnViewModel = viewModel()
                
                TravelRouterApp(vpnViewModel = vpnViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelRouterApp(vpnViewModel: VpnViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF121212), // Dark background
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00BCD4),
                        unselectedIconColor = Color.Gray
                    )
                )
                
                NavigationBarItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00BCD4),
                        unselectedIconColor = Color.Gray
                    )
                )
                
                NavigationBarItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00BCD4),
                        unselectedIconColor = Color.Gray
                    )
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> DashboardScreen(
                vpnViewModel = vpnViewModel,
                onNavigateToSettings = { selectedTab = 1 }
            )
            1 -> {
                // Settings screen with configuration management
                SettingsScreen(
                    vpnViewModel = vpnViewModel,
                    onNavigateToAddConfig = { selectedTab = 3 },
                    onNavigateToConfigs = { selectedTab = 4 }
                )
            }
            2 -> {
                // Placeholder for Profile screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Profile\n(Coming Soon)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            3 -> {
                // Add Configuration screen
                AddConfigScreen(
                    vpnViewModel = vpnViewModel,
                    onNavigateBack = { selectedTab = 1 }
                )
            }
            4 -> {
                // Configurations list screen
                ConfigScreen(
                    vpnViewModel = vpnViewModel,
                    onNavigateToAddConfig = { selectedTab = 3 },
                    onNavigateToEditConfig = { configName ->
                        // For now, just go back to settings
                        selectedTab = 1
                    }
                )
            }
        }
    }
}