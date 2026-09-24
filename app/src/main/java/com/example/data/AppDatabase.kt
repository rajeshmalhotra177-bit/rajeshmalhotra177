package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DetectionLogDao
import com.example.data.dao.TriggerKeywordDao
import com.example.data.dao.UserSettingsDao
import com.example.data.model.DetectionLog
import com.example.data.model.TriggerKeyword
import com.example.data.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DetectionLog::class,
        TriggerKeyword::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun detectionLogDao(): DetectionLogDao
    abstract fun triggerKeywordDao(): TriggerKeywordDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hearclear_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default presets and initial settings in background
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.userSettingsDao().insertOrUpdate(UserSettings())
                                database.triggerKeywordDao().insertKeywords(
                                    listOf(
                                        TriggerKeyword(keyword = "Suno / सुनो", isPreset = true, language = "hi"),
                                        TriggerKeyword(keyword = "Excuse Me", isPreset = true, language = "en"),
                                        TriggerKeyword(keyword = "Bhaiya / भैया", isPreset = true, language = "hi"),
                                        TriggerKeyword(keyword = "Hello / हेलो", isPreset = true, language = "both"),
                                        TriggerKeyword(keyword = "Listen", isPreset = true, language = "en"),
                                        TriggerKeyword(keyword = "Rajesh", isPreset = false, language = "both")
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
