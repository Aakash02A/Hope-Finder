package com.tryout.hopefinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tryout.hopefinder.navigation.NavGraph
import com.tryout.hopefinder.ui.theme.RescueRadarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RescueRadarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NavContent(modifier: Modifier = Modifier) {
    NavGraph()
}
