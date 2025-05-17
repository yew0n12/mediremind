package com.example.mediremind.tracker

import java.time.LocalTime

data class Medication(
    val id: Int,
    val name: String,
    val days: List<String>,        // 예: ["월", "수", "금"]
    val time: LocalTime,           // 예: LocalTime.of(9, 30)
    val requestCode: Int
)
