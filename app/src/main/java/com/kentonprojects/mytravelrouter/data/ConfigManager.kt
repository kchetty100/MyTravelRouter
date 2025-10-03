package com.kentonprojects.mytravelrouter.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
// WireGuard imports commented out - using simplified config parsing
// import com.wireguard.config.Config
// import com.wireguard.config.Interface
// import com.wireguard.config.Peer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

data class WireGuardConfig(
    val name: String,
    val rawConfig: String,
    val interfaceConfig: InterfaceConfig,
    val peerConfig: PeerConfig,
    val endpoint: String = "",
    val peerCount: Int = 1
)

data class InterfaceConfig(
    val privateKey: String = "",
    val address: String = "",
    val dns: String = ""
)

data class PeerConfig(
    val publicKey: String = "",
    val endpoint: String = "",
    val allowedIPs: String = "",
    val persistentKeepalive: String = ""
)

class ConfigManager(private val context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "wireguard_configs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val CONFIG_PREFIX = "config_"
        private const val ACTIVE_CONFIG_KEY = "active_config"
    }
    
    suspend fun saveConfig(config: WireGuardConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val configKey = "$CONFIG_PREFIX${config.name}"
            sharedPreferences.edit()
                .putString(configKey, config.rawConfig)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loadConfig(name: String): Result<WireGuardConfig> = withContext(Dispatchers.IO) {
        try {
            val configKey = "$CONFIG_PREFIX$name"
            val rawConfig = sharedPreferences.getString(configKey, null)
                ?: return@withContext Result.failure(IllegalArgumentException("Config not found: $name"))
            
            val config = parseConfig(rawConfig, name)
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllConfigs(): Result<List<WireGuardConfig>> = withContext(Dispatchers.IO) {
        try {
            val configs = mutableListOf<WireGuardConfig>()
            val allKeys = sharedPreferences.all.keys
            
            for (key in allKeys) {
                if (key.startsWith(CONFIG_PREFIX)) {
                    val name = key.removePrefix(CONFIG_PREFIX)
                    val rawConfig = sharedPreferences.getString(key, null)
                    if (rawConfig != null) {
                        try {
                            val config = parseConfig(rawConfig, name)
                            configs.add(config)
                        } catch (e: Exception) {
                            // Skip invalid configs
                            continue
                        }
                    }
                }
            }
            
            Result.success(configs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteConfig(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val configKey = "$CONFIG_PREFIX$name"
            sharedPreferences.edit()
                .remove(configKey)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setActiveConfig(name: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit()
                .putString(ACTIVE_CONFIG_KEY, name)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getActiveConfig(): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val activeConfig = sharedPreferences.getString(ACTIVE_CONFIG_KEY, null)
            Result.success(activeConfig)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseConfig(rawConfig: String, name: String): WireGuardConfig {
        val lines = rawConfig.lines()
        var interfaceConfig = InterfaceConfig()
        var peerConfig = PeerConfig()
        var endpoint = ""
        var peerCount = 0
        var currentSection = ""
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine.startsWith("[Interface]") -> {
                    currentSection = "Interface"
                }
                trimmedLine.startsWith("[Peer]") -> {
                    currentSection = "Peer"
                    peerCount++
                }
                trimmedLine.startsWith("PrivateKey = ") && currentSection == "Interface" -> {
                    interfaceConfig = interfaceConfig.copy(
                        privateKey = trimmedLine.substringAfter("PrivateKey = ").trim()
                    )
                }
                trimmedLine.startsWith("Address = ") && currentSection == "Interface" -> {
                    interfaceConfig = interfaceConfig.copy(
                        address = trimmedLine.substringAfter("Address = ").trim()
                    )
                }
                trimmedLine.startsWith("DNS = ") && currentSection == "Interface" -> {
                    interfaceConfig = interfaceConfig.copy(
                        dns = trimmedLine.substringAfter("DNS = ").trim()
                    )
                }
                trimmedLine.startsWith("PublicKey = ") && currentSection == "Peer" -> {
                    peerConfig = peerConfig.copy(
                        publicKey = trimmedLine.substringAfter("PublicKey = ").trim()
                    )
                }
                trimmedLine.startsWith("Endpoint = ") && currentSection == "Peer" -> {
                    val endpointValue = trimmedLine.substringAfter("Endpoint = ").trim()
                    peerConfig = peerConfig.copy(endpoint = endpointValue)
                    endpoint = endpointValue
                }
                trimmedLine.startsWith("AllowedIPs = ") && currentSection == "Peer" -> {
                    peerConfig = peerConfig.copy(
                        allowedIPs = trimmedLine.substringAfter("AllowedIPs = ").trim()
                    )
                }
                trimmedLine.startsWith("PersistentKeepalive = ") && currentSection == "Peer" -> {
                    peerConfig = peerConfig.copy(
                        persistentKeepalive = trimmedLine.substringAfter("PersistentKeepalive = ").trim()
                    )
                }
            }
        }
        
        return WireGuardConfig(
            name = name,
            rawConfig = rawConfig,
            interfaceConfig = interfaceConfig,
            peerConfig = peerConfig,
            endpoint = endpoint,
            peerCount = peerCount
        )
    }
    
    fun parseConfigFromString(configString: String, name: String): Result<WireGuardConfig> {
        return try {
            val config = parseConfig(configString, name)
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
