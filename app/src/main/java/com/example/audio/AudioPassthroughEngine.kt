package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.math.tanh

class AudioPassthroughEngine(
    private val context: Context,
    private val audioFocusHelper: AudioFocusHelper,
    private val alertChimeHelper: AlertChimeHelper,
    private val hapticAlertHelper: HapticAlertHelper
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var recordingJob: Job? = null
    private var silenceCheckJob: Job? = null

    // State flows for UI and observers
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _currentDb = MutableStateFlow(0f)
    val currentDb: StateFlow<Float> = _currentDb.asStateFlow()

    private val _ambientFloorDb = MutableStateFlow(42f)
    val ambientFloorDb: StateFlow<Float> = _ambientFloorDb.asStateFlow()

    private val _waveformPoints = MutableStateFlow<List<Float>>(List(24) { 0.05f })
    val waveformPoints: StateFlow<List<Float>> = _waveformPoints.asStateFlow()

    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive: StateFlow<Boolean> = _isVoiceActive.asStateFlow()

    private val _isPassthroughActive = MutableStateFlow(false)
    val isPassthroughActive: StateFlow<Boolean> = _isPassthroughActive.asStateFlow()

    // Configurable runtime settings
    var sensitivityDb: Float = 58f
    var micGainMultiplier: Float = 2.0f
    var autoDuckMedia: Boolean = true
    var audioPassthroughEnabled: Boolean = true
    var playAlertChime: Boolean = true
    var vibrateOnAlert: Boolean = true
    var continuousPassthrough: Boolean = false
    var silenceHoldDurationSec: Int = 3

    // Callbacks for higher-level events (e.g. logging and SpeechRecognizer)
    var onVoiceTriggered: ((peakDb: Float) -> Unit)? = null
    var onVoiceEnded: (() -> Unit)? = null

    private var lastVoiceDetectedTime: Long = 0L
    private val sampleRate = 16000

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (_isListening.value) return

        recordingJob = scope.launch(Dispatchers.IO) {
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minRecordBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = max(minRecordBufferSize * 2, 2048)

            var audioRecord: AudioRecord? = null
            var audioTrack: AudioTrack? = null

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e("AudioPassthroughEngine", "AudioRecord initialization failed")
                    return@launch
                }

                // Prepare output AudioTrack for real-time passthrough to headphones
                val outChannelConfig = AudioFormat.CHANNEL_OUT_MONO
                val minTrackBufferSize = AudioTrack.getMinBufferSize(sampleRate, outChannelConfig, audioFormat)
                val trackBufferSize = max(minTrackBufferSize * 2, 2048)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(outChannelConfig)
                            .build()
                    )
                    .setBufferSizeInBytes(trackBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioRecord.startRecording()
                audioTrack.play()
                _isListening.value = true

                val shortBuffer = ShortArray(bufferSize / 4)
                var ambientBaseline = 44.0f

                while (isActive) {
                    val readSamples = audioRecord.read(shortBuffer, 0, shortBuffer.size)
                    if (readSamples <= 0) continue

                    // Calculate RMS and Decibels
                    var sumSquares = 0.0
                    for (i in 0 until readSamples) {
                        val sample = shortBuffer[i].toDouble()
                        sumSquares += sample * sample
                    }
                    val rms = sqrt(sumSquares / readSamples)
                    // Convert RMS to approximate SPL Decibels (normalized scale)
                    val db = (20.0 * log10(max(1.0, rms))).toFloat().coerceIn(0f, 100f)

                    // Exponential moving average for ambient noise floor
                    if (db < ambientBaseline + 8f) {
                        ambientBaseline = ambientBaseline * 0.98f + db * 0.02f
                    }
                    _currentDb.value = db
                    _ambientFloorDb.value = ambientBaseline

                    // Update UI Waveform visualization (downsample shortBuffer to 24 bars)
                    val step = max(1, readSamples / 24)
                    val points = mutableListOf<Float>()
                    for (i in 0 until 24) {
                        val idx = (i * step).coerceAtMost(readSamples - 1)
                        val amp = (Math.abs(shortBuffer[idx].toInt()) / 32768f).coerceIn(0.05f, 1f)
                        points.add(amp)
                    }
                    _waveformPoints.value = points

                    // Voice trigger criteria:
                    // 1. Decibel level exceeds user's configured sensitivity threshold
                    // 2. Decibel level is at least 6 dB above the current ambient room noise floor
                    val isVoiceDetected = (db >= sensitivityDb) && (db >= ambientBaseline + 6f)

                    if (isVoiceDetected) {
                        lastVoiceDetectedTime = System.currentTimeMillis()
                        handleVoiceDetected(db)
                    } else {
                        checkVoiceSilenceTimeout()
                    }

                    // Passthrough condition: active during detection or continuous ambient mode
                    val shouldPassthrough = audioPassthroughEnabled && (_isVoiceActive.value || continuousPassthrough)
                    _isPassthroughActive.value = shouldPassthrough

                    if (shouldPassthrough && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        // Apply digital gain and soft-clipping limiter
                        val outBuffer = ShortArray(readSamples)
                        val gain = micGainMultiplier
                        for (i in 0 until readSamples) {
                            val raw = shortBuffer[i] * gain
                            // Soft limiter using tanh
                            val normalized = raw / 32768.0
                            val compressed = tanh(normalized)
                            outBuffer[i] = (compressed * 32767.0).toInt().toShort()
                        }
                        audioTrack.write(outBuffer, 0, readSamples)
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioPassthroughEngine", "Error in audio loop: ${e.message}", e)
            } finally {
                _isListening.value = false
                _isPassthroughActive.value = false
                _isVoiceActive.value = false
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
                audioFocusHelper.restoreMedia()
            }
        }
    }

    private fun handleVoiceDetected(db: Float) {
        val wasActive = _isVoiceActive.value
        _isVoiceActive.value = true

        if (!wasActive) {
            Log.d("AudioPassthroughEngine", "Voice trigger started! Level: $db dB")
            // 1. Duck background media playing via Bluetooth
            if (autoDuckMedia) {
                audioFocusHelper.duckMedia()
            }
            // 2. Play subtle alert chime in headphones
            if (playAlertChime) {
                alertChimeHelper.playChime()
            }
            // 3. Vibrate device
            if (vibrateOnAlert) {
                hapticAlertHelper.triggerAlertVibration()
            }
            // 4. Notify listeners
            onVoiceTriggered?.invoke(db)
        }
    }

    private fun checkVoiceSilenceTimeout() {
        if (!_isVoiceActive.value) return

        val silenceDuration = System.currentTimeMillis() - lastVoiceDetectedTime
        val holdThresholdMs = silenceHoldDurationSec * 1000L

        if (silenceDuration > holdThresholdMs) {
            _isVoiceActive.value = false
            Log.d("AudioPassthroughEngine", "Voice silence timeout reached. Restoring normal audio.")
            if (autoDuckMedia) {
                audioFocusHelper.restoreMedia()
            }
            onVoiceEnded?.invoke()
        }
    }

    fun stopListening() {
        recordingJob?.cancel()
        recordingJob = null
        silenceCheckJob?.cancel()
        silenceCheckJob = null
        _isListening.value = false
        _isVoiceActive.value = false
        _isPassthroughActive.value = false
        _currentDb.value = 0f
        audioFocusHelper.restoreMedia()
    }

    /**
     * Manual simulation trigger for user to test the feature without waiting for someone to call.
     */
    fun simulateCallOut(testDb: Float = 68f, triggerName: String = "Suno / Rahul") {
        scope.launch {
            handleVoiceDetected(testDb)
            onVoiceTriggered?.invoke(testDb)
            delay((silenceHoldDurationSec * 1000L).coerceAtLeast(2500L))
            checkVoiceSilenceTimeout()
        }
    }
}
