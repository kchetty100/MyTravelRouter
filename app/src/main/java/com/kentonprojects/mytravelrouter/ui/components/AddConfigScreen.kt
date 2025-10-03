package com.kentonprojects.mytravelrouter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kentonprojects.mytravelrouter.data.ConfigManager
import com.kentonprojects.mytravelrouter.data.WireGuardConfig
import com.kentonprojects.mytravelrouter.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConfigScreen(
    vpnViewModel: VpnViewModel,
    onNavigateBack: () -> Unit
) {
    var configName by remember { mutableStateOf("") }
    var configText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val configManager = ConfigManager(context)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Add Configuration",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuration Name
            OutlinedTextField(
                value = configName,
                onValueChange = { configName = it },
                label = { Text("Configuration Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Configuration Text
            OutlinedTextField(
                value = configText,
                onValueChange = { configText = it },
                label = { Text("WireGuard Configuration") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                minLines = 8,
                maxLines = 12
            )
            
            // Example Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Example Configuration:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = """
[Interface]
PrivateKey = your_private_key_here
Address = 10.0.0.2/24
DNS = 8.8.8.8

[Peer]
PublicKey = server_public_key_here
Endpoint = server.example.com:51820
AllowedIPs = 0.0.0.0/0
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Error Message
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    if (configName.isBlank() || configText.isBlank()) {
                        error = "Please fill in all fields"
                        return@Button
                    }
                    
                    isLoading = true
                    error = null
                    
                    val result = configManager.parseConfigFromString(configText, configName)
                    result.onSuccess { config ->
                        vpnViewModel.saveConfig(config)
                        onNavigateBack()
                    }.onFailure { exception ->
                        error = "Invalid configuration: ${exception.message}"
                        isLoading = false
                    }
                },
                enabled = !isLoading && configName.isNotBlank() && configText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Configuration")
                }
            }
        }
    }
}
