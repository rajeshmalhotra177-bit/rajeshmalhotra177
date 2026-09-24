package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_keywords")
data class TriggerKeyword(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyword: String,
    val isEnabled: Boolean = true,
    val isPreset: Boolean = false,
    val language: String = "both" // "hi", "en", "both"
)
