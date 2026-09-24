package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val sensitivityDb: Float = 58f,             // Trigger threshold in dB (40 to 85)
    val micGainMultiplier: Float = 2.0f,         // Boost factor for voice passthrough (1.0x to 4.0x)
    val autoDuckMedia: Boolean = true,           // Ducks Spotify / YouTube / media when speech detected
    val audioPassthroughEnabled: Boolean = true, // Live mic passthrough to bluetooth headset
    val playAlertChime: Boolean = true,          // Pleasant alert chime in ears when voice detected
    val vibrateOnAlert: Boolean = true,          // Haptic double vibration alert
    val continuousPassthrough: Boolean = false,  // Constant ambient listening vs On-callout only
    val silenceHoldDurationSec: Int = 3,         // Seconds of silence before resuming media volume
    val speechRecognitionEnabled: Boolean = true,// Keyword spotting for names ("Rajesh", "Suno", etc.)
    val appLanguage: String = "hi"               // "hi" for Hindi, "en" for English
)
