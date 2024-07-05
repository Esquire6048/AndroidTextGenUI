package com.example.textgenui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textgenui.PreferencesViewModel
import com.example.textgenui.PreferencesViewModelFactory
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHome: () -> Unit,
    viewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModelFactory(LocalContext.current))
) {
    val locale by viewModel.locale.collectAsState()
    val llmUserBio by viewModel.llmUserBio.collectAsState()
    val llmContext by viewModel.llmContext.collectAsState()
    val temperature by viewModel.temperature.collectAsState()

    val defaultUserBio = "{{user}} is a computer hacker from an anime."
    val defaultCharBio = "{{char}} is a comedian who talks in very informal, familiar language says unexpected things."

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LocaleSelector(viewModel)

            OutlinedTextField(
                value = llmUserBio,
                onValueChange = { viewModel.updateLLMUserBio(it) },
                label = { Text("User Bio") },
                minLines = 3,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.updateLLMUserBio(defaultUserBio) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Reset to default")
            }

            OutlinedTextField(
                value = llmContext,
                onValueChange = { viewModel.updateLLMContext(it) },
                label = { Text("Character Context") },
                minLines = 3,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.updateLLMContext(defaultCharBio) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Reset to default")
            }

            Text(
                "Temperature: ${String.format("%.2f", temperature)}",
                fontWeight = FontWeight.Medium
            )
            Slider(
                value = temperature,
                onValueChange = { viewModel.updateTemperature(it) },
                valueRange = 0f..5f,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Settings")
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}