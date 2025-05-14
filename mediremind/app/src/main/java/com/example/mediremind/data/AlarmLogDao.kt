package com.example.mediremind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlarmLogDao {
    @Insert
    fun insert(log: AlarmLog)

    @Query("SELECT * FROM alarm_logs ORDER BY timestamp DESC")
    fun getAll(): List<AlarmLog>

    @Query("UPDATE alarm_logs SET taken = 1 WHERE id = :logId")
    fun markAsTaken(logId: Int)
}
