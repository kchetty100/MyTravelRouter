package com.kentonprojects.mytravelrouter.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kentonprojects.mytravelrouter.R
import com.kentonprojects.mytravelrouter.ui.theme.SuccessGreen
import com.kentonprojects.mytravelrouter.ui.theme.ErrorRed
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TravelRouter",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // VPN Status Card
        VpnStatusCard(
            isConnected = vpnState.isConnected,
            isConnecting = vpnState.isConnecting,
            configName = vpnState.currentConfig?.name,
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Data Usage Card
        DataUsageCard(
            bytesIn = vpnState.bytesIn,
            bytesOut = vpnState.bytesOut
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Hotspot Button
        HotspotButton()
        
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
fun VpnStatusCard(
    isConnected: Boolean,
    isConnecting: Boolean,
    configName: String?,
    onToggleConnection: () -> Unit
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("wifi_animation.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (isConnected) Int.MAX_VALUE else 1,
        isPlaying = isConnected || isConnecting
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) SuccessGreen.copy(alpha = 0.1f) 
                           else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lottie Animation
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Text
            Text(
                text = when {
                    isConnecting -> "Connecting..."
                    isConnected -> "Connected"
                    else -> "Disconnected"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    isConnecting -> MaterialTheme.colorScheme.primary
                    isConnected -> SuccessGreen
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            // Config Name
            configName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Toggle Button
            Button(
                onClick = onToggleConnection,
                enabled = !isConnecting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) ErrorRed else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isConnected) "Disconnect" else "Connect",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun DataUsageCard(
    bytesIn: Long,
    bytesOut: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Data Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DataUsageItem(
                    label = "Downloaded",
                    bytes = bytesIn,
                    color = MaterialTheme.colorScheme.primary
                )
                
                DataUsageItem(
                    label = "Uploaded",
                    bytes = bytesOut,
                    color = MaterialTheme.colorScheme.secondary
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HotspotButton() {
    val context = LocalContext.current
    
    Button(
        onClick = {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "Open Hotspot Settings",
            style = MaterialTheme.typography.titleMedium
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
