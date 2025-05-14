package com.example.mediremind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_logs")
data class AlarmLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val desc: String,
    val timestamp: Long,
    val taken: Boolean = false // ✅ 복용 여부 기본값 false
)



