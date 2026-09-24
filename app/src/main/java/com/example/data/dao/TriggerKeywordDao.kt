package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TriggerKeyword
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerKeywordDao {
    @Query("SELECT * FROM trigger_keywords ORDER BY isPreset DESC, id ASC")
    fun getAllKeywords(): Flow<List<TriggerKeyword>>

    @Query("SELECT * FROM trigger_keywords WHERE isEnabled = 1")
    fun getEnabledKeywords(): Flow<List<TriggerKeyword>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: TriggerKeyword): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeywords(keywords: List<TriggerKeyword>)

    @Update
    suspend fun updateKeyword(keyword: TriggerKeyword)

    @Delete
    suspend fun deleteKeyword(keyword: TriggerKeyword)

    @Query("SELECT COUNT(*) FROM trigger_keywords")
    suspend fun getCount(): Int
}
