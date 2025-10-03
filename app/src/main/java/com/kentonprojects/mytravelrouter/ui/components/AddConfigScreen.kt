package com.kentonprojects.mytravelrouter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kentonprojects.mytravelrouter.data.ConfigManager
import com.kentonprojects.mytravelrouter.data.WireGuardConfig
import com.kentonprojects.mytravelrouter.utils.rememberFilePicker
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
    var selectedFile by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val configManager = ConfigManager(context)
    
    val filePicker = rememberFilePicker { content ->
        configText = content
        selectedFile = "File imported successfully"
    }
    
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
                .weight(1f)
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // File Upload Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2C)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Import Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { filePicker() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload File",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload .conf File")
                }
                
                selectedFile?.let { fileName ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00BCD4)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Divider
        Text(
            text = "OR",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Manual Configuration Text
        OutlinedTextField(
            value = configText,
            onValueChange = { configText = it },
            label = { Text("WireGuard Configuration (Manual Entry)") },
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
PrivateKey = UNUrpuFuq9uabhhOoDmk2UFZ7hKNkaUVG8+KDwY+FFE=
Address = 10.0.0.2/32
DNS = 1.1.1.1

[Peer]
PublicKey = TQ4r309K+4srXdn8+KV7P0bHOAYLrKu8PRRA+xl3SzA=
Endpoint = kentonhome.duckdns.org:51820
AllowedIPs = 0.0.0.0/0
PersistentKeepalive = 25
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
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Save Button - Always visible at bottom
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
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving...")
            } else {
                Text("Save Configuration")
            }
        }
    }
}
