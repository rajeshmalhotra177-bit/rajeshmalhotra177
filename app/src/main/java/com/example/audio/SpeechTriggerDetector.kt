package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechTriggerDetector(
    private val context: Context,
    private val onKeywordMatched: (matchedKeyword: String, fullText: String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningForSpeech = false

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _matchedTriggerWord = MutableStateFlow<String?>(null)
    val matchedTriggerWord: StateFlow<String?> = _matchedTriggerWord.asStateFlow()

    var targetKeywords: List<String> = listOf("Suno", "सुनो", "Excuse me", "Bhaiya", "भैया", "Hello", "Rajesh")
    var isEnabled: Boolean = true

    fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("SpeechTriggerDetector", "Speech recognition not available on this device")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        Log.d("SpeechTriggerDetector", "Recognition error code: $error")
                        // If continuous, restart speech recognition after brief pause
                        if (isListeningForSpeech && isEnabled) {
                            restartListeningDelayed()
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognizedText = matches[0]
                            _lastRecognizedText.value = recognizedText
                            Log.d("SpeechTriggerDetector", "Speech recognized: $recognizedText")
                            checkMatches(recognizedText)
                        }
                        if (isListeningForSpeech && isEnabled) {
                            restartListeningDelayed()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val partialText = matches[0]
                            _lastRecognizedText.value = partialText
                            checkMatches(partialText)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.e("SpeechTriggerDetector", "Failed to init speech recognizer: ${e.message}")
        }
    }

    private fun checkMatches(text: String) {
        val lowerText = text.lowercase(Locale.ROOT)
        for (rawKeyword in targetKeywords) {
            val cleanKw = rawKeyword.split("/")[0].trim().lowercase(Locale.ROOT)
            if (cleanKw.isNotBlank() && lowerText.contains(cleanKw)) {
                _matchedTriggerWord.value = rawKeyword
                Log.d("SpeechTriggerDetector", "Matched target keyword: $rawKeyword in '$text'")
                onKeywordMatched(rawKeyword, text)
                break
            }
        }
    }

    fun startListening() {
        if (!isEnabled || isListeningForSpeech) return
        if (speechRecognizer == null) initialize()

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            }
            speechRecognizer?.startListening(intent)
            isListeningForSpeech = true
        } catch (e: Exception) {
            Log.e("SpeechTriggerDetector", "Error starting speech recognition: ${e.message}")
        }
    }

    private fun restartListeningDelayed() {
        try {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isListeningForSpeech && isEnabled) {
                    try {
                        startListening()
                    } catch (_: Exception) {}
                }
            }, 600)
        } catch (_: Exception) {}
    }

    fun stopListening() {
        isListeningForSpeech = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
    }

    fun destroy() {
        stopListening()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }
}
