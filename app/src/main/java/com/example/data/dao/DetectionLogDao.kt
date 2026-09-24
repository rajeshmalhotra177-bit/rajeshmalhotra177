package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DetectionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionLogDao {
    @Query("SELECT * FROM detection_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<DetectionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DetectionLog): Long

    @Query("DELETE FROM detection_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM detection_logs")
    fun getLogCount(): Flow<Int>
}
