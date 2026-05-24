package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SparkViewModel
import com.example.ui.screens.*
import com.example.ui.theme.SparkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SparkTheme {
                val viewModel: SparkViewModel = viewModel()
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

                // State tracker for onboarding flow
                val sharedPrefs = remember { getSharedPreferences("spark_general_prefs", Context.MODE_PRIVATE) }
                var hasSeenOnboarding by remember { mutableStateOf(sharedPrefs.getBoolean("seen_onboarding", false)) }
                var currentScreen by remember { mutableStateOf(if (!hasSeenOnboarding) "onboarding" else if (!isUserLoggedIn) "login" else "main") }

                LaunchedEffect(isUserLoggedIn, hasSeenOnboarding) {
                    currentScreen = when {
                        !hasSeenOnboarding -> "onboarding"
                        !isUserLoggedIn -> "login"
                        else -> "main"
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "onboarding" -> OnboardingScreen(
                            onNavigateToLogin = {
                                sharedPrefs.edit().putBoolean("seen_onboarding", true).apply()
                                hasSeenOnboarding = true
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                        "login" -> LoginRegisterScreen(
                            viewModel = viewModel,
                            onLoginSuccessful = {
                                // VM updates isUserLoggedIn which automatically triggers Main transition
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                        else -> MainLayoutScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
