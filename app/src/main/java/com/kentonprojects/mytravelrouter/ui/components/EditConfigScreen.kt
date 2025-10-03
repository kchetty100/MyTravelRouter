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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kentonprojects.mytravelrouter.data.ConfigManager
import com.kentonprojects.mytravelrouter.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditConfigScreen(
    vpnViewModel: VpnViewModel,
    configName: String,
    onNavigateBack: () -> Unit
) {
    var configText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoaded by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val configManager = ConfigManager(context)
    
    // Load existing config
    LaunchedEffect(configName) {
        if (!isLoaded) {
            configManager.loadConfig(configName)
                .onSuccess { config ->
                    configText = config.rawConfig
                    isLoaded = true
                }
                .onFailure { exception ->
                    error = "Failed to load configuration: ${exception.message}"
                }
        }
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
                text = "Edit Configuration",
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
            // Configuration Name (Read-only)
            OutlinedTextField(
                value = configName,
                onValueChange = { },
                label = { Text("Configuration Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
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
                    if (configText.isBlank()) {
                        error = "Configuration text cannot be empty"
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
                enabled = !isLoading && configText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}
