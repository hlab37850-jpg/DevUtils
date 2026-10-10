package com.devtools.utils.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentHistory(): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE toolName = :tool ORDER BY timestamp DESC LIMIT 20")
    suspend fun getHistoryByTool(tool: String): List<HistoryEntity>

    @Insert
    suspend fun insert(history: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun deleteAll()
}
