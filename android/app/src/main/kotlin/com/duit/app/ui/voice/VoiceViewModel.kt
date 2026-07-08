package com.duit.app.ui.voice

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class VoiceUiState(
    val isListening: Boolean = false,
    val rawText: String = "",
    val parseResult: VoiceParser.ParseResult? = null,
    val error: String? = null
)

@HiltViewModel
class VoiceViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    // ponytail: SpeechRecognizer lifecycle tied to screen; created/destroyed in DisposableEffect
    private var recognizer: SpeechRecognizer? = null

    fun createRecognizer(activity: Activity): SpeechRecognizer {
        val r = SpeechRecognizer.createSpeechRecognizer(activity)
        r.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {
                _uiState.value = _uiState.value.copy(isListening = true, error = null)
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                _uiState.value = _uiState.value.copy(isListening = false)
            }
            override fun onError(errorCode: Int) {
                val msg = when (errorCode) {
                    SpeechRecognizer.ERROR_NO_MATCH       -> "Suara tidak dikenali. Coba lagi."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tidak ada suara terdeteksi."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Izin mikrofon diperlukan."
                    else -> "Gagal mengenali suara (kode $errorCode)."
                }
                _uiState.value = _uiState.value.copy(isListening = false, error = msg)
            }
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val best = matches?.firstOrNull() ?: return
                val parsed = VoiceParser.parse(best)
                _uiState.value = _uiState.value.copy(
                    rawText = best,
                    parseResult = parsed,
                    isListening = false
                )
            }
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })
        recognizer = r
        return r
    }

    fun startListening() {
        if (_uiState.value.isListening) return
        _uiState.value = _uiState.value.copy(isListening = true, error = null, parseResult = null, rawText = "")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer?.startListening(intent)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        recognizer?.stopListening()
        _uiState.value = VoiceUiState()
    }

    override fun onCleared() {
        super.onCleared()
        recognizer?.destroy()
        recognizer = null
    }
}
