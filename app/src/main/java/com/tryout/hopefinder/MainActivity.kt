package com.tryout.hopefinder

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.tryout.hopefinder.data.DevicePreferences
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

        val deviceIp = DevicePreferences.getSavedDeviceIp(this)
            ?: DevicePreferences.resolveDefaultDeviceIp().also {
                DevicePreferences.saveDeviceIp(this, it)
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
}