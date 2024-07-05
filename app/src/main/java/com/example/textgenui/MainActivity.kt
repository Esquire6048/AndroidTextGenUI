package com.example.textgenui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.textgenui.ui.HomeScreen
import com.example.textgenui.ui.SettingsScreen
import com.example.textgenui.ui.theme.TextGenUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextGenUITheme {
                val navController = rememberNavController()
                val context = remember { this@MainActivity.applicationContext }
                val sharedViewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModelFactory(context))

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize() // without this, animation is weird
                ) {
                    // We've added popUpTo("home") { inclusive = true } when navigating from settings to home. This ensures
                    // that when you go back to the home screen, you're not just adding a new instance on top of the stack,
                    // but replacing the current stack with a single home screen instance.
                    composable(route = "settings") {
                        SettingsScreen(
                            viewModel = sharedViewModel,
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(route = "home") {
                        HomeScreen(
                            viewModel = sharedViewModel,
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }
                    // back button will basicallty pop one item from the back stack
                }
            }
        }
    }
}
