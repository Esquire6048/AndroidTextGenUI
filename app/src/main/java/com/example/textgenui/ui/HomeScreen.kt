package com.example.textgenui.ui

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.textgenui.PreferencesViewModel
import com.example.textgenui.PreferencesViewModelFactory
import com.example.textgenui.backend.MsgItem
import com.example.textgenui.backend.PrefUtils
import com.example.textgenui.backend.TextgenAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModelFactory(LocalContext.current))
) {
    val TAG = "Homescreen"
    val serverAddress = "http://nsf-jst-chatbot.ist.osaka-u.ac.jp:8443"
//    val serverAddress = "http://gpu-server:5000"
//    val serverAddress = "http://v-hyperion:5000"

    // assignments won't be tracked unless using "by"
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    val cScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var userText by rememberSaveable { mutableStateOf("") }

    // viewmodel variables
    val history = viewModel.history
    val llmUserBio by viewModel.llmUserBio.collectAsState()
    val llmContext by viewModel.llmContext.collectAsState()
    val llmTemperature by viewModel.temperature.collectAsState()
    val locale by viewModel.locale.collectAsState()

    var generating by rememberSaveable { mutableStateOf(false) }
    var modelInfo by rememberSaveable { mutableStateOf("") }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = "key1") { // currently the model should not change, to no need to recalculate, therefore constant key
        withContext(Dispatchers.IO) {
            modelInfo = TextgenAPI(host = serverAddress).getModelInfoString()
        }
    }

    val ctx = LocalContext.current // the location of this matters

    // TTS init & dispose
    LaunchedEffect(key1 = locale) { // recalculate if "locale" changes
        textToSpeech?.shutdown()
        textToSpeech = TextToSpeech(ctx) { status ->
            if (status != TextToSpeech.ERROR) {
                Log.e("DEBUG", "Setting TTS locale: " + locale.toLanguageTag())
                textToSpeech?.language = locale
                textToSpeech?.setSpeechRate(1.1f)
            } else {
                Log.e(TAG, "Error initializing TextToSpeech")
            }
        }
    }
    DisposableEffect(Unit) { onDispose { textToSpeech?.shutdown() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chatbot client") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)  // This applies the correct padding)
        ) {
            MessageList(listState = listState, items = history, textToSpeech = textToSpeech)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SpeechRecognitionTextField(
                    text = userText,
                    onTextChange = { userText = it },
                    locale = locale,
                    //localeState = viewModel.locale.collectAsState(),
                    modifier = Modifier.weight(0.75f)
                )

                Button(
                    onClick = {
                        cScope.launch(Dispatchers.IO) {
                            generating = true
                            val textgenAPI = TextgenAPI(host = serverAddress)
                            // modify history through viewmodel, not directly. Separation of concerns.
                            viewModel.addMessage(MsgItem(role = "user", content = userText))
                            viewModel.addMessage(MsgItem(role = "assistant", content = "")) // add an empty message, and modify in real time
                            // Animate scroll to the end after adding new messages
                            withContext(Dispatchers.Main) { listState.animateScrollToItem(index = history.lastIndex) }

                            userText = "" // clear input after adding to chatlog
                            textgenAPI.sendChatMessageStreaming(
                                history = history,
                                userBio = llmUserBio,
                                context = llmContext,
                                temperature = llmTemperature,
                                onNewMessageChunk = {
                                    Log.i(TAG, "NEW CHUNK: <$it>")
                                    cScope.launch(Dispatchers.Main) {
                                        viewModel.appendToLastMessage(it) // viewmodel updates should be on main thread
                                        listState.animateScrollToItem(index = history.lastIndex) // Animate scroll to the last item
                                    }
                                })
                            textToSpeech?.speak(history.last().content, TextToSpeech.QUEUE_FLUSH, null, "utterance-id-1")
                            //println(">>> Model response (character: ${api.character}): $r1")
                            PrefUtils.saveHistory(ctx, history)
                            generating = false
                        }
                    },
                    enabled = !generating,
                    modifier = Modifier
                        .weight(0.25f)
                        .wrapContentWidth().height(80.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Send", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

            }

            Row {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        when (locale) {
                            Locale.US -> {
                                viewModel.addMessage(MsgItem(role = "system", "Answer in at most two sentences in English."))
                                viewModel.addMessage(MsgItem(role = "assistant", "Welcome. What brings you here?"))
                            }

                            Locale.JAPAN -> {
                                viewModel.addMessage(MsgItem(role = "system", "Answer in at most two sentences in Japanese."))
                                viewModel.addMessage(MsgItem(role = "assistant", "こんにちは、今日はどうしたの？"))
                            }

                            else -> {
                                error("unknown locale $locale")
                            }
                        }
                        cScope.launch(Dispatchers.IO) { PrefUtils.saveHistory(ctx, history) }
                    },
                    content = { Text(text = "Clear") }
                )
                Button(
                    onClick = {
                        userText = viewModel.removeLastTwoMessages()
                        cScope.launch(Dispatchers.IO) { PrefUtils.saveHistory(ctx, history) }
                    },
                    content = { Text(text = "-1") }
                )
                Button(
                    onClick = {
                        cScope.launch(Dispatchers.IO) {
                            generating = true
                            val tempText = viewModel.regenerateLastMessages()
                            val textgenAPI = TextgenAPI(host = serverAddress)
                            // modify history through viewmodel, not directly. Separation of concerns.
                            // viewModel.addMessage(MsgItem(role = "user", content = tempText))
                            viewModel.addMessage(MsgItem(role = "assistant", content = "")) // add an empty message, and modify in real time
                            // Animate scroll to the end after adding new messages
                            withContext(Dispatchers.Main) { listState.animateScrollToItem(index = history.lastIndex) }

                            userText = "" // clear input after adding to chatlog
                            textgenAPI.sendChatMessageStreaming(
                                history = history,
                                userBio = llmUserBio,
                                context = llmContext,
                                temperature = llmTemperature,
                                onNewMessageChunk = {
                                    Log.i(TAG, "NEW CHUNK: <$it>")
                                    cScope.launch(Dispatchers.Main) {
                                        viewModel.appendToLastMessage(it) // viewmodel updates should be on main thread
                                        listState.animateScrollToItem(index = history.lastIndex) // Animate scroll to the last item
                                    }
                                })
                            textToSpeech?.speak(history.last().content, TextToSpeech.QUEUE_FLUSH, null, "utterance-id-1")
                            //println(">>> Model response (character: ${api.character}): $r1")
                            PrefUtils.saveHistory(ctx, history)
                            generating = false
                        }
                    },
                    enabled = !generating,
                    content = { Text(text = "Regenerate") }
                )
                Button(
                    onClick = { textToSpeech?.stop() },
                    content = { Text(text = "Stop voice") }
                )
            } // row
        } // main column
    } // scaffold

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("LLM Information", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow("API endpoint", serverAddress)
                    InfoRow("Model", modelInfo)
                    InfoRow("User bio", llmUserBio)
                    InfoRow("Context (character info)", llmContext)
                    InfoRow("Temperature", llmTemperature.toString())
                }
            },
            confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text("Close") } },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}