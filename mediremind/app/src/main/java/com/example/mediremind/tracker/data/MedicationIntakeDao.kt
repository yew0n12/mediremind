package com.example.mediremind.tracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate

@Dao
interface MedicationIntakeDao {
    @Insert
    suspend fun insert(log: MedicationIntakeLog)

    @Query("SELECT taken FROM medication_intake_logs WHERE medicationId = :medicationId AND date = :date LIMIT 1")
    suspend fun getIntakeStatus(medicationId: Int, date: LocalDate): Boolean?
}
