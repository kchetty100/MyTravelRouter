package com.kentonprojects.mytravelrouter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.kentonprojects.mytravelrouter.MainActivity
import com.kentonprojects.mytravelrouter.R
import com.kentonprojects.mytravelrouter.data.ConfigManager
import com.kentonprojects.mytravelrouter.data.WireGuardConfig
// WireGuard imports commented out - using simplified VPN implementation
// import com.wireguard.android.backend.Backend
// import com.wireguard.android.backend.GoBackend
// import com.wireguard.android.backend.Tunnel
// import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
// Removed unused imports

class TravelRouterVpnService : VpnService() {
    
    // Simplified VPN implementation without WireGuard library
    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    private val configManager by lazy { ConfigManager(this) }
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "vpn_service_channel"
        private const val CHANNEL_NAME = "VPN Service"
        
        const val ACTION_START_VPN = "com.kentonprojects.mytravelrouter.START_VPN"
        const val ACTION_STOP_VPN = "com.kentonprojects.mytravelrouter.STOP_VPN"
        const val EXTRA_CONFIG_NAME = "config_name"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_VPN -> {
                val configName = intent.getStringExtra(EXTRA_CONFIG_NAME)
                if (configName != null) {
                    startVpn(configName)
                }
            }
            ACTION_STOP_VPN -> {
                stopVpn()
            }
        }
        return START_STICKY
    }
    
    private fun startVpn(configName: String) {
        serviceJob = serviceScope.launch {
            try {
                val configResult = configManager.loadConfig(configName)
                if (configResult.isFailure) {
                    stopSelf()
                    return@launch
                }
                
                val config = configResult.getOrThrow()
                startVpnWithConfig(config)
            } catch (e: Exception) {
                stopSelf()
            }
        }
    }
    
    private suspend fun startVpnWithConfig(config: WireGuardConfig) {
        try {
            // Request VPN permission
            val intent = VpnService.prepare(this)
            if (intent != null) {
                // Permission not granted, stop service
                stopSelf()
                return
            }
            
            // Create VPN interface with basic configuration
            val builder = Builder()
                .setSession("TravelRouter VPN")
                .addAddress("10.0.0.2", 24) // Default client IP
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")
                .setMtu(1420)
                .setBlocking(false)
            
            // Add allowed applications (all traffic)
            builder.addRoute("0.0.0.0", 0)
            builder.addRoute("::", 0)
            
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                stopSelf()
                return
            }
            
            // Start foreground service
            startForeground(NOTIFICATION_ID, createNotification("Connected to ${config.name}"))
            
        } catch (e: Exception) {
            stopSelf()
        }
    }
    
    private fun stopVpn() {
        serviceJob?.cancel()
        
        vpnInterface?.close()
        vpnInterface = null
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN connection status"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(status: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TravelRouter VPN")
            .setContentText(status)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
