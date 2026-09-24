package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log

class AudioFocusHelper(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isDuckingActive = false

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        Log.d("AudioFocusHelper", "Focus changed: $focusChange")
    }

    /**
     * Request audio focus with ducking. This tells currently playing media apps (YouTube,
     * Spotify, Music Players) to lower their volume (duck) or pause so ambient voice is clear.
     */
    fun duckMedia(): Boolean {
        if (isDuckingActive) return true

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()

            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_NOTIFICATION,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }

        isDuckingActive = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        Log.d("AudioFocusHelper", "duckMedia result granted: $isDuckingActive")
        return isDuckingActive
    }

    /**
     * Restores media playback volume when the voice call-out finishes.
     */
    fun restoreMedia() {
        if (!isDuckingActive) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }

        isDuckingActive = false
        Log.d("AudioFocusHelper", "restoreMedia completed")
    }

    val isDucking: Boolean
        get() = isDuckingActive
}
