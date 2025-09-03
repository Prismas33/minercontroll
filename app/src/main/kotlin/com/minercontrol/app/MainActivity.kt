package com.minercontrol.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.minercontrol.app.model.SettingsRepository
import com.minercontrol.app.navigation.MinerControlNavigation
import com.minercontrol.app.network.UdpListener
import com.minercontrol.app.ui.theme.MinerControlTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var udpListener: UdpListener
    private lateinit var settingsRepository: SettingsRepository
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "✅ Permission granted, starting UDP listener")
            startUdpListener()
        } else {
            Log.e("MainActivity", "❌ Permission denied, cannot start UDP listener")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "🚀 Starting MinerControl...")
        
        settingsRepository = SettingsRepository.getInstance(this)
        udpListener = UdpListener(this)
        Log.d("MainActivity", "📱 UdpListener created with context")
        
        setContent {
            MinerControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MinerControlNavigation(
                        udpListener = udpListener,
                        settingsRepository = settingsRepository
                    )
                }
            }
        }
        
        checkPermissionsAndStart()
    }
    
    private fun checkPermissionsAndStart() {
        Log.d("MainActivity", "🔐 Checking permissions...")
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("MainActivity", "✅ Internet permission already granted")
                startUdpListener()
            }
            else -> {
                Log.d("MainActivity", "❓ Requesting internet permission...")
                requestPermissionLauncher.launch(Manifest.permission.INTERNET)
            }
        }
    }
    
    private fun startUdpListener() {
        Log.d("MainActivity", "🔊 Starting UDP listener on port 12345...")
        udpListener.start(12345)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "🛑 Stopping UDP listener...")
        udpListener.stop()
    }
}
