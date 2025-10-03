package com.kentonprojects.mytravelrouter.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kentonprojects.mytravelrouter.R
import com.kentonprojects.mytravelrouter.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vpnViewModel: VpnViewModel,
    onNavigateToSettings: () -> Unit
) {
    val vpnState by vpnViewModel.vpnState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Title at top left
        Text(
            text = "TravelRouter",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Centered phone animation
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PhoneAnimation()
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // VPN Status Card
        VpnStatusCard(
            isConnected = vpnState.isConnected,
            isConnecting = vpnState.isConnecting,
            onToggleConnection = {
                if (vpnState.isConnected) {
                    vpnViewModel.disconnectVpn()
                } else {
                    vpnState.currentConfig?.let { config ->
                        vpnViewModel.connectVpn(config.name)
                    }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Connection Info Card
        ConnectionInfoCard(
            endpoint = vpnState.currentConfig?.endpoint ?: "203.0.113.45:51820",
            bytesIn = vpnState.bytesIn,
            bytesOut = vpnState.bytesOut
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Enable Hotspot Button (text-only)
        TextButton(
            onClick = {
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Enable Hotspot",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF00BCD4)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error Message
        vpnState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PhoneAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("wifi_animation.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
        isPlaying = true
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pulsing Wi-Fi Animation
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Smartphone Image
        SmartphoneImage()
    }
}

@Composable
fun SmartphoneImage() {
    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C2C2C))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Notch
            Box(
                modifier = Modifier
                    .size(width = 20.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1A1A1A))
                    .padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A))
            ) {
                // Screen content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Status bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Time
                        Box(
                            modifier = Modifier
                                .size(20.dp, 4.dp)
                                .background(Color(0xFF00BCD4), RoundedCornerShape(2.dp))
                        )
                        
                        // Battery
                        Box(
                            modifier = Modifier
                                .size(16.dp, 4.dp)
                                .background(Color(0xFF00BCD4), RoundedCornerShape(2.dp))
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // App icon grid
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00BCD4), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00BCD4), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Home button
            Box(
                modifier = Modifier
                    .size(20.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1A1A1A))
            )
        }
    }
}

@Composable
fun VpnStatusCard(
    isConnected: Boolean,
    isConnecting: Boolean,
    onToggleConnection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WireGuard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Text(
                    text = if (isConnecting) "Connecting..." 
                           else if (isConnected) "Connected" 
                           else "Disconnected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isConnected) Color(0xFF00BCD4) else Color.Gray
                )
            }
            
            Switch(
                checked = isConnected,
                onCheckedChange = { onToggleConnection() },
                enabled = !isConnecting,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00BCD4),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF424242)
                )
            )
        }
    }
}

@Composable
fun ConnectionInfoCard(
    endpoint: String,
    bytesIn: Long,
    bytesOut: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Server Endpoint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Server Endpoint",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = endpoint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Data Usage - Received
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Data Usage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = formatBytes(bytesIn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = formatBytes(bytesOut),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DataUsageItem(
    label: String,
    bytes: Long,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatBytes(bytes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun EnableHotspotButton() {
    val context = LocalContext.current
    
    Button(
        onClick = {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF00BCD4)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "Enable Hotspot",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return String.format("%.1f %s", size, units[unitIndex])
}
