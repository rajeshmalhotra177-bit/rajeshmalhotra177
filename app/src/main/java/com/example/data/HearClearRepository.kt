package com.example.data

import com.example.data.dao.DetectionLogDao
import com.example.data.dao.TriggerKeywordDao
import com.example.data.dao.UserSettingsDao
import com.example.data.model.DetectionLog
import com.example.data.model.TriggerKeyword
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

class HearClearRepository(
    private val detectionLogDao: DetectionLogDao,
    private val triggerKeywordDao: TriggerKeywordDao,
    private val userSettingsDao: UserSettingsDao
) {
    val allLogs: Flow<List<DetectionLog>> = detectionLogDao.getAllLogs()
    val logCount: Flow<Int> = detectionLogDao.getLogCount()
    val allKeywords: Flow<List<TriggerKeyword>> = triggerKeywordDao.getAllKeywords()
    val enabledKeywords: Flow<List<TriggerKeyword>> = triggerKeywordDao.getEnabledKeywords()
    val settingsFlow: Flow<UserSettings?> = userSettingsDao.getSettings()

    suspend fun getSettingsDirect(): UserSettings {
        return userSettingsDao.getSettingsDirect() ?: UserSettings().also {
            userSettingsDao.insertOrUpdate(it)
        }
    }

    suspend fun updateSettings(settings: UserSettings) {
        userSettingsDao.insertOrUpdate(settings)
    }

    suspend fun addLog(log: DetectionLog): Long {
        return detectionLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        detectionLogDao.clearAllLogs()
    }

    suspend fun addKeyword(keyword: String, language: String = "both") {
        if (keyword.isNotBlank()) {
            triggerKeywordDao.insertKeyword(
                TriggerKeyword(
                    keyword = keyword.trim(),
                    isEnabled = true,
                    isPreset = false,
                    language = language
                )
            )
        }
    }

    suspend fun updateKeyword(keyword: TriggerKeyword) {
        triggerKeywordDao.updateKeyword(keyword)
    }

    suspend fun deleteKeyword(keyword: TriggerKeyword) {
        triggerKeywordDao.deleteKeyword(keyword)
    }
}
