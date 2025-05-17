package com.example.mediremind.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medication_intake_logs")
data class MedicationIntakeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationId: Int,
    val date: LocalDate,
    val taken: Boolean
)
