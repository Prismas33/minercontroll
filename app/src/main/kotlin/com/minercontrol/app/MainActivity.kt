package com.minercontrol.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        
        // Configure window for maximum screen usage including notch/cutout
        setupFullScreenWithNotchSupport()
        
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
    
    private fun setupFullScreenWithNotchSupport() {
        // Make app use full screen including notch/cutout area
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9+ specific cutout support
            window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ improved cutout support
            window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        
        // Configure system bars to be transparent and allow content behind them
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = 
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        // Make status bar and navigation bar dark/transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        Log.d("MainActivity", "📱 Configured full screen with notch/cutout support")
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
