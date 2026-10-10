package com.devtools.utils.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val toolName: String,
    val input: String,
    val output: String,
    val timestamp: Long
)
