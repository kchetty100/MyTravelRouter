package com.kentonprojects.mytravelrouter.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kentonprojects.mytravelrouter.data.ConfigManager
import com.kentonprojects.mytravelrouter.data.WireGuardConfig
import com.kentonprojects.mytravelrouter.service.TravelRouterVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VpnState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val currentConfig: WireGuardConfig? = null,
    val error: String? = null,
    val bytesIn: Long = 0,
    val bytesOut: Long = 0
)

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    
    private val configManager = ConfigManager(application)
    
    private val _vpnState = MutableStateFlow(VpnState())
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()
    
    private val _configs = MutableStateFlow<List<WireGuardConfig>>(emptyList())
    val configs: StateFlow<List<WireGuardConfig>> = _configs.asStateFlow()
    
    init {
        loadConfigs()
        loadActiveConfig()
    }
    
    fun loadConfigs() {
        viewModelScope.launch {
            configManager.getAllConfigs()
                .onSuccess { configs ->
                    _configs.value = configs
                }
                .onFailure { error ->
                    _vpnState.value = _vpnState.value.copy(error = error.message)
                }
        }
    }
    
    fun loadActiveConfig() {
        viewModelScope.launch {
            configManager.getActiveConfig()
                .onSuccess { activeConfigName ->
                    if (activeConfigName != null) {
                        configManager.loadConfig(activeConfigName)
                            .onSuccess { config ->
                                _vpnState.value = _vpnState.value.copy(currentConfig = config)
                            }
                    }
                }
        }
    }
    
    fun connectVpn(configName: String) {
        viewModelScope.launch {
            _vpnState.value = _vpnState.value.copy(isConnecting = true, error = null)
            
            configManager.loadConfig(configName)
                .onSuccess { config ->
                    _vpnState.value = _vpnState.value.copy(currentConfig = config)
                    
                    val intent = Intent(getApplication(), TravelRouterVpnService::class.java).apply {
                        action = TravelRouterVpnService.ACTION_START_VPN
                        putExtra(TravelRouterVpnService.EXTRA_CONFIG_NAME, configName)
                    }
                    getApplication<Application>().startService(intent)
                    
                    _vpnState.value = _vpnState.value.copy(
                        isConnected = true,
                        isConnecting = false
                    )
                    
                    configManager.setActiveConfig(configName)
                }
                .onFailure { error ->
                    _vpnState.value = _vpnState.value.copy(
                        isConnecting = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun disconnectVpn() {
        viewModelScope.launch {
            val intent = Intent(getApplication(), TravelRouterVpnService::class.java).apply {
                action = TravelRouterVpnService.ACTION_STOP_VPN
            }
            getApplication<Application>().startService(intent)
            
            _vpnState.value = _vpnState.value.copy(
                isConnected = false,
                isConnecting = false,
                currentConfig = null
            )
            
            configManager.setActiveConfig(null)
        }
    }
    
    fun saveConfig(config: WireGuardConfig) {
        viewModelScope.launch {
            configManager.saveConfig(config)
                .onSuccess {
                    loadConfigs()
                }
                .onFailure { error ->
                    _vpnState.value = _vpnState.value.copy(error = error.message)
                }
        }
    }
    
    fun deleteConfig(configName: String) {
        viewModelScope.launch {
            configManager.deleteConfig(configName)
                .onSuccess {
                    loadConfigs()
                    if (_vpnState.value.currentConfig?.name == configName) {
                        disconnectVpn()
                    }
                }
                .onFailure { error ->
                    _vpnState.value = _vpnState.value.copy(error = error.message)
                }
        }
    }
    
    fun clearError() {
        _vpnState.value = _vpnState.value.copy(error = null)
    }
}
