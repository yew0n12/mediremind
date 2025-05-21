package com.example.mediremind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,           // 약 이름
    val description: String,    // 설명
    val startDate: String,       // ex) "2025-05-21"
    val endDate: String?,        // ex) "2025-06-01" or null (null이면 무기한)
    val time: String,            // ex) "08:00"
)
