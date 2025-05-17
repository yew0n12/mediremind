package com.example.mediremind.alarm  // 패키지 이름 네 프로젝트 구조에 맞게 바꿔!

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mediremind.R

class MedicationScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_schedule) // layout 파일 필요
    }
}
