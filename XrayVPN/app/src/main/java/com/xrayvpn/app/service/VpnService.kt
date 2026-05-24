package com.xrayvpn.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.xrayvpn.app.MainActivity
import com.xrayvpn.common.constants.AppConfig
import com.xrayvpn.core.xray.XrayManager
import com.xrayvpn.domain.model.ServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * VPN Service that handles the actual VPN connection using Xray core
 */
class VpnService : VpnService() {
    
    private val xrayManager: XrayManager by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var currentServer: ServerConfig? = null
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "xray_vpn_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_CONNECT = "com.xrayvpn.action.CONNECT"
        const val ACTION_DISCONNECT = "com.xrayvpn.action.DISCONNECT"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val serverId = intent.getStringExtra("server_id")
                serverId?.let { connectToServer(it) }
            }
            ACTION_DISCONNECT -> {
                disconnect()
            }
        }
        
        return START_STICKY
    }
    
    private fun connectToServer(serverId: String) {
        serviceScope.launch {
            try {
                // Get server config from repository
                val repository: com.xrayvpn.domain.repository.ServerRepository by inject()
                val server = repository.getServerById(serverId) ?: return@launch
                
                currentServer = server
                
                // Start VPN interface
                setupVpnInterface()
                
                // Start Xray core
                xrayManager.startConnection(server)
                
                // Show connected notification
                showConnectedNotification(server)
                
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorNotification(e.message ?: "Connection failed")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }
    
    private fun setupVpnInterface() {
        val builder = Builder()
            .setSessionName("XrayVPN")
            .addAddress(AppConfig.LOOPBACK, 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .addDnsServer("8.8.4.4")
            .setMtu(1500)
        
        // Add bypass for local addresses
        builder.addRoute(AppConfig.GEOIP_PRIVATE.replace("geoip:", ""), 0)
        
        // Set up SOCKS proxy routing
        builder.addAddress("10.0.0.1", 32)
        
        vpnInterface = builder.establish()
        
        if (vpnInterface == null) {
            throw IllegalStateException("Failed to establish VPN interface")
        }
    }
    
    private fun disconnect() {
        serviceScope.launch {
            try {
                xrayManager.stopConnection()
                vpnInterface?.close()
                vpnInterface = null
                currentServer = null
                
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onDestroy() {
        serviceScope.launch {
            xrayManager.stopConnection()
        }
        vpnInterface?.close()
        super.onDestroy()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "VPN Connection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN connection status"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showConnectedNotification(server: ServerConfig) {
        val disconnectIntent = Intent(this, VpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            0,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("XrayVPN Connected")
            .setContentText("${server.name} - ${server.address}:${server.port}")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Disconnect", disconnectPendingIntent)
            .setOngoing(true)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Connection Error")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
}
