package com.example.textgenui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.textgenui.backend.MsgItem
import com.example.textgenui.backend.PrefUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class PreferencesViewModel(private val context: Context) : ViewModel() {
    private val TAG = this::class.simpleName

    private val _llmContext = MutableStateFlow("")
    val llmContext: StateFlow<String> = _llmContext

    private val _temperature = MutableStateFlow(0.5f)
    val temperature: StateFlow<Float> = _temperature

    private val _llmUserBio = MutableStateFlow("")
    val llmUserBio: StateFlow<String> = _llmUserBio

    private val _locale = MutableStateFlow(Locale.FRANCE)
    val locale: StateFlow<Locale> = _locale

    private val _history = mutableStateListOf<MsgItem>()
    val history: List<MsgItem> = _history

    init {
        viewModelScope.launch {
            _llmContext.value = PrefUtils.loadLLMContext(context)
            _temperature.value = PrefUtils.loadTemperature(context)
            _llmUserBio.value = PrefUtils.loadLLMUserBio(context)
            _locale.value = PrefUtils.loadLocale(context)
            _history.addAll(PrefUtils.loadHistory(context))
        }
    }

    fun updateLLMContext(value: String) {
        _llmContext.value = value
    }

    fun updateTemperature(value: Float) {
        _temperature.value = value
    }

    fun updateLLMUserBio(value: String) {
        _llmUserBio.value = value
    }

    fun updateLocale(value: Locale) {
        _locale.value = value
    }

    fun addMessage(message: MsgItem) {
        _history.add(message)
        viewModelScope.launch {
            PrefUtils.saveHistory(context, _history)
        }
    }

//    fun removeLastTwoMessages() {
//        repeat(2) { _history.removeLast() }
//    }

    fun removeLastTwoMessages(): String {
        _history.removeLast()
        val temp = _history.last().content
        _history.removeLast()
        return temp
    }

    fun appendToLastMessage(newChunk: String) {
        synchronized(_history) { // thread safe just in case
            val newMsgItem = _history.last().copy(content = _history.last().content + newChunk)
            if (_history.isNotEmpty())
                _history[_history.lastIndex] = newMsgItem
            else
                _history.add(newMsgItem)
        }
    }

    fun clearHistory() {
        _history.clear()
        viewModelScope.launch {
            PrefUtils.saveHistory(context, emptyList())
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            PrefUtils.saveLLMContext(context, llmContext.value)
            PrefUtils.saveTemperature(context, temperature.value)
            PrefUtils.saveLLMUserBio(context, llmUserBio.value)
            PrefUtils.saveLocale(context, locale.value)
        }
    }
}

class PreferencesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PreferencesViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}