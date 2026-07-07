package com.duit.app.ui.ocr

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrUiState(
    val isScanning: Boolean = false,
    val parseResult: OcrParser.ParseResult? = null,
    val error: String? = null
)

@HiltViewModel
class OcrViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    // ponytail: atomic flag — stops analyzer after first result/error, only reset() clears it
    @Volatile private var isStopped = false

    // ponytail: lazy-init recognizer, reused across captures
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun analyzeImage(imageProxy: ImageProxy) {
        if (isStopped || _uiState.value.isScanning) {
            imageProxy.close()
            return
        }
        _uiState.value = _uiState.value.copy(isScanning = true, error = null)

        viewModelScope.launch {
            try {
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    _uiState.value = _uiState.value.copy(isScanning = false)
                    return@launch
                }
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                // ponytail: suspendCancellableCoroutine bridges ML Kit Task to coroutine — no extra dep needed
                val result = suspendCancellableCoroutine { cont ->
                    recognizer.process(image)
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                imageProxy.close()

                val rawText = result.text
                if (rawText.isBlank()) {
                    isStopped = true
                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        error = "Teks tidak terdeteksi. Coba arahkan kamera lebih dekat."
                    )
                    return@launch
                }

                isStopped = true
                val parsed = OcrParser.parse(rawText)
                _uiState.value = _uiState.value.copy(isScanning = false, parseResult = parsed)
            } catch (e: Exception) {
                imageProxy.close()
                isStopped = true
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = "Gagal memproses gambar: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        isStopped = false
        _uiState.value = OcrUiState()
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}
