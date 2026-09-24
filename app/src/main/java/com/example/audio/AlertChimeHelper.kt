package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class AlertChimeHelper {

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Synthesizes and plays a smooth, pleasant dual-tone chime (880 Hz -> 1320 Hz)
     * through AudioTrack so it plays directly into the currently connected headphones.
     */
    fun playChime() {
        scope.launch {
            try {
                val sampleRate = 44100
                val durationMs = 260
                val totalSamples = (sampleRate * durationMs) / 1000
                val pcmBuffer = ShortArray(totalSamples)

                val freq1 = 880.0  // A5
                val freq2 = 1320.0 // E6

                val splitIndex = totalSamples / 2

                for (i in 0 until totalSamples) {
                    val progress = i.toDouble() / totalSamples
                    // Amplitude envelope: quick attack, smooth exponential decay
                    val envelope = if (i < splitIndex) {
                        (1.0 - (i.toDouble() / splitIndex) * 0.4)
                    } else {
                        val subProgress = (i - splitIndex).toDouble() / (totalSamples - splitIndex)
                        (0.9 * (1.0 - subProgress))
                    }

                    val freq = if (i < splitIndex) freq1 else freq2
                    val angle = 2.0 * Math.PI * i * freq / sampleRate
                    val sampleVal = (sin(angle) * envelope * Short.MAX_VALUE * 0.65).toInt()
                    pcmBuffer[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val bufferSizeBytes = totalSamples * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcmBuffer, 0, totalSamples)
                track.play()

                // Release track after playback completes
                kotlinx.coroutines.delay(durationMs + 80L)
                track.stop()
                track.release()
            } catch (e: Exception) {
                // Non-fatal if audio track cannot be initialized
            }
        }
    }
}
