package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detection_logs")
data class DetectionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerType: String, // "NAME_CALLED", "VOICE_DETECTED", "SPEECH_BURST", "MANUAL_TEST"
    val peakDecibels: Float,
    val detailText: String,
    val durationMs: Long = 0
)
