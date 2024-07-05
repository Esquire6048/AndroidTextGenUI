package com.example.textgenui.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.textgenui.R
import java.util.Locale

@Composable
fun SpeechRecognitionTextField(
    text: String,
    onTextChange: (String) -> Unit,
    //localeState: State<Locale>,
    locale: Locale,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    //val locale = localeState.value
    var isListening by remember { mutableStateOf(false) }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
        Log.e("DEBUG", "SpeechRecognizer locale: " + locale.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Granted
        } else {
            // Permission Denied
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() {
                onTextChange("")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onTextChange(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partialMatches.isNullOrEmpty()) {
                    onTextChange(partialMatches[0])
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        onDispose {
            speechRecognizer.destroy()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            minLines = 3,
            maxLines = 3,
            modifier = Modifier.weight(1f),
            placeholder = { Text(if (isListening) "Listening..." else "Speech will appear here") }
        )

        MicIconButton(
            isListening = isListening,
            onMicToggle = {
                if (isListening) {
                    speechRecognizer.stopListening()
                } else {
                    speechRecognizer.startListening(speechRecognizerIntent)
                }
            },
            modifier = Modifier.padding(16.dp)
        )

        /*IconButton(
            onClick = {
                if (isListening) {
                    speechRecognizer.stopListening()
                } else {
                    speechRecognizer.startListening(speechRecognizerIntent)
                }
            }
        ) {
            Icon(
                painter = painterResource(id = if (isListening) R.drawable.ic_mic_off else R.drawable.ic_mic),
                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                modifier = Modifier.fillMaxSize()
            )
        }*/
    }
}

@Composable
fun MicIconButton(
    isListening: Boolean,
    onMicToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isListening) Color.Red else MaterialTheme.colorScheme.primary
    val iconColor = Color.White
    Box(
        modifier = modifier
            .size(56.dp)
            .background(backgroundColor, shape = CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), shape = CircleShape)
    ) {
        IconButton(
            onClick = onMicToggle,
            modifier = Modifier.matchParentSize()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mic),
                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}