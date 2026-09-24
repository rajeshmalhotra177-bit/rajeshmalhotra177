package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HearClearApplication
import com.example.audio.BluetoothStatus
import com.example.data.model.DetectionLog
import com.example.data.model.TriggerKeyword
import com.example.data.model.UserSettings
import com.example.service.VoiceListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HearClearViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HearClearApplication
    private val repository = app.repository
    private val audioEngine = app.audioEngine
    private val bluetoothHelper = app.bluetoothHelper
    private val speechDetector = app.speechDetector

    // Room Database Observables
    val userSettings: StateFlow<UserSettings> = repository.settingsFlow
        .map { it ?: UserSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    val detectionLogs: StateFlow<List<DetectionLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val triggerKeywords: StateFlow<List<TriggerKeyword>> = repository.allKeywords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Audio Engine Real-time Observables
    val isListening: StateFlow<Boolean> = audioEngine.isListening
    val currentDb: StateFlow<Float> = audioEngine.currentDb
    val ambientFloorDb: StateFlow<Float> = audioEngine.ambientFloorDb
    val waveformPoints: StateFlow<List<Float>> = audioEngine.waveformPoints
    val isVoiceActive: StateFlow<Boolean> = audioEngine.isVoiceActive
    val isPassthroughActive: StateFlow<Boolean> = audioEngine.isPassthroughActive

    // Hardware & Sensor Observables
    val bluetoothStatus: StateFlow<BluetoothStatus> = bluetoothHelper.status
    val lastRecognizedText: StateFlow<String> = speechDetector.lastRecognizedText
    val matchedTriggerWord: StateFlow<String?> = speechDetector.matchedTriggerWord

    // UI Feedback state
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    init {
        bluetoothHelper.startListening()
    }

    fun toggleListening(enable: Boolean) {
        if (enable) {
            VoiceListenerService.start(app)
        } else {
            VoiceListenerService.stop(app)
        }
    }

    fun updateSensitivity(db: Float) {
        val current = userSettings.value
        val updated = current.copy(sensitivityDb = db)
        audioEngine.sensitivityDb = db
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun updateMicGain(gain: Float) {
        val current = userSettings.value
        val updated = current.copy(micGainMultiplier = gain)
        audioEngine.micGainMultiplier = gain
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun updateAutoDuckMedia(enabled: Boolean) {
        val current = userSettings.value
        val updated = current.copy(autoDuckMedia = enabled)
        audioEngine.autoDuckMedia = enabled
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun updatePlayAlertChime(enabled: Boolean) {
        val current = userSettings.value
        val updated = current.copy(playAlertChime = enabled)
        audioEngine.playAlertChime = enabled
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun updateVibrateOnAlert(enabled: Boolean) {
        val current = userSettings.value
        val updated = current.copy(vibrateOnAlert = enabled)
        audioEngine.vibrateOnAlert = enabled
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun updateContinuousPassthrough(enabled: Boolean) {
        val current = userSettings.value
        val updated = current.copy(continuousPassthrough = enabled)
        audioEngine.continuousPassthrough = enabled
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun updateSilenceHoldDuration(seconds: Int) {
        val current = userSettings.value
        val updated = current.copy(silenceHoldDurationSec = seconds)
        audioEngine.silenceHoldDurationSec = seconds
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun updateLanguage(lang: String) {
        val current = userSettings.value
        val updated = current.copy(appLanguage = lang)
        viewModelScope.launch {
            repository.updateSettings(updated)
        }
    }

    fun addTriggerKeyword(name: String, language: String = "both") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addKeyword(name.trim(), language)
            showFeedback("Keyword added: $name")
        }
    }

    fun toggleKeywordEnabled(keyword: TriggerKeyword) {
        viewModelScope.launch {
            repository.updateKeyword(keyword.copy(isEnabled = !keyword.isEnabled))
        }
    }

    fun deleteKeyword(keyword: TriggerKeyword) {
        viewModelScope.launch {
            repository.deleteKeyword(keyword)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            showFeedback("History cleared")
        }
    }

    fun simulateVoiceCall(testDb: Float = 70f, triggerName: String = "Suno") {
        audioEngine.simulateCallOut(testDb, triggerName)
        viewModelScope.launch {
            repository.addLog(
                DetectionLog(
                    triggerType = "MANUAL_TEST",
                    peakDecibels = testDb,
                    detailText = "Manual Simulation Trigger ('$triggerName' - ${testDb.toInt()} dB)"
                )
            )
        }
    }

    fun showFeedback(msg: String) {
        _userFeedbackMessage.value = msg
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }
}
