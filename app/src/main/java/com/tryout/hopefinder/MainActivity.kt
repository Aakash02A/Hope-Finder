package com.tryout.hopefinder

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.tryout.hopefinder.ui.screens.MainNavigation
import com.tryout.hopefinder.ui.theme.HopeFinderTheme
import com.tryout.hopefinder.viewmodel.MainViewModel
import com.tryout.hopefinder.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Detect if running in emulator and use appropriate IP
        val deviceIp = if (isRunningInEmulator()) {
            "10.0.2.2"  // Special alias for host machine in emulator
        } else {
            "192.168.1.100"  // Use this when on physical device
        }
        mainViewModel.initializeDevice(deviceIp)
        
        setContent {
            HopeFinderTheme {
                MainNavigation(context = this@MainActivity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.stopPolling()
    }

    private fun isRunningInEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") ||
                Build.FINGERPRINT.contains("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.DEVICE.contains("emulator") ||
                (Build.BRAND == "generic" && Build.DEVICE == "generic")
    }
}