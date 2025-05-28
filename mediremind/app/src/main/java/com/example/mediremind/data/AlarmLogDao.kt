package com.example.mediremind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlarmLogDao {

    @Insert
    fun insert(log: AlarmLog)

    @Query("SELECT * FROM alarm_logs ORDER BY timestamp DESC")
    fun getAll(): List<AlarmLog>

    @Query("UPDATE alarm_logs SET taken = 1 WHERE id = :id")
    suspend fun markAsTaken(id: Int)
//
//    @Query("""
//        SELECT * FROM alarm_logs
//        WHERE DATE(timestamp / 1000, 'unixepoch') = :date
//        ORDER BY timestamp ASC
//    """)
//    suspend fun getLogsByDate(date: String): List<AlarmLog>
@Query("SELECT * FROM alarm_logs WHERE timestamp = :epochDay ORDER BY id ASC")
suspend fun getLogsByDate(epochDay: Long): List<AlarmLog>

    @Query("UPDATE alarm_logs SET taken = 0 WHERE id = :id")
    suspend fun markAsNotTaken(id: Int)

}


