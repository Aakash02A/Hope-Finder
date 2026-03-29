package com.hope_finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.hope_finder.navigation.NavGraph
import com.hope_finder.ui.theme.HopeFinderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HopeFinderTheme {
                val navController = rememberNavController()
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
