package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.QuranTrackerViewModel
import com.example.ui.viewmodel.QuranTrackerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        // Manual DI through Custom Application context
        val app = application as QuranTrackerApplication
        val factory = QuranTrackerViewModelFactory(app.repository)
        val viewModel: QuranTrackerViewModel by viewModels { factory }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Main layout is internally handled safely with status navigation margins
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
