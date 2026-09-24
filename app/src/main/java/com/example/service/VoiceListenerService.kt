package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.HearClearApplication
import com.example.MainActivity
import com.example.data.model.DetectionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VoiceListenerService : Service() {

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        const val CHANNEL_ID = "hearclear_voice_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.hearclear.ACTION_START"
        const val ACTION_STOP = "com.example.hearclear.ACTION_STOP"
        const val ACTION_SIMULATE = "com.example.hearclear.ACTION_SIMULATE"

        fun start(context: Context) {
            val intent = Intent(context, VoiceListenerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VoiceListenerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_SIMULATE -> {
                val app = application as? HearClearApplication
                app?.audioEngine?.simulateCallOut(68f, "Manual Test")
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification("Listening for voice & name call-outs..."))
                startAudioMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startAudioMonitoring() {
        val app = application as? HearClearApplication ?: return
        val engine = app.audioEngine
        val repo = app.repository
        val btHelper = app.bluetoothHelper
        val speechDetector = app.speechDetector

        // Sync repository settings to engine
        scope.launch {
            repo.settingsFlow.collectLatest { settings ->
                if (settings != null) {
                    engine.sensitivityDb = settings.sensitivityDb
                    engine.micGainMultiplier = settings.micGainMultiplier
                    engine.autoDuckMedia = settings.autoDuckMedia
                    engine.audioPassthroughEnabled = settings.audioPassthroughEnabled
                    engine.playAlertChime = settings.playAlertChime
                    engine.vibrateOnAlert = settings.vibrateOnAlert
                    engine.continuousPassthrough = settings.continuousPassthrough
                    engine.silenceHoldDurationSec = settings.silenceHoldDurationSec
                    speechDetector.isEnabled = settings.speechRecognitionEnabled
                }
            }
        }

        // Sync keyword list to speech recognizer
        scope.launch {
            repo.enabledKeywords.collectLatest { keywords ->
                speechDetector.targetKeywords = keywords.map { it.keyword }
            }
        }

        // Listen for Bluetooth status
        btHelper.startListening()

        // Hook engine voice trigger event
        engine.onVoiceTriggered = { peakDb ->
            scope.launch {
                val btDevice = btHelper.checkCurrentStatus()
                val detail = if (btDevice.isConnected) {
                    "Voice call-out detected (${peakDb.toInt()} dB) - Ducked ${btDevice.deviceName}"
                } else {
                    "Voice call-out detected (${peakDb.toInt()} dB) - Passthrough activated"
                }

                repo.addLog(
                    DetectionLog(
                        triggerType = "VOICE_DETECTED",
                        peakDecibels = peakDb,
                        detailText = detail
                    )
                )

                updateNotification("🎙️ Voice detected (${peakDb.toInt()} dB)! Passthrough active.")
            }
        }

        engine.onVoiceEnded = {
            updateNotification("👂 HearClear Active: Listening for your name / speech...")
        }

        // Hook speech detector keyword match
        speechDetector.initialize()

        // Start listening in AudioEngine
        engine.startListening()
    }

    private fun stopForegroundService() {
        val app = application as? HearClearApplication
        app?.audioEngine?.stopListening()
        app?.bluetoothHelper?.stopListening()
        app?.speechDetector?.stopListening()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(statusText: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingOpenIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, VoiceListenerService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HearClear Active")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingOpenIntent)
            .addAction(android.R.drawable.ic_media_pause, "Turn Off", pendingStopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HearClear Ambient Voice Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification while HearClear listens for ambient calls and voice"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
