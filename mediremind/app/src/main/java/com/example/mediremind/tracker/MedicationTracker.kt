package com.example.mediremind.tracker
import java.time.LocalDate                      // ✅ LocalDate를 위해 필요
import com.example.mediremind.tracker.data.MedicationIntakeDao   // ✅ DAO가 같은 패키지에 있으면 생략 가능하지만, 명시하는 게 좋음
import com.example.mediremind.tracker.data.MedicationIntakeLog   // ✅ 로그 데이터 클래스도 필요

class MedicationTracker(private val dao: MedicationIntakeDao) {

    suspend fun markAsTaken(medicationId: Int, date: LocalDate) {
        val log =MedicationIntakeLog(medicationId = medicationId, date = date, taken = true)
        dao.insert(log)
    }

    suspend fun isTaken(medicationId: Int, date: LocalDate): Boolean {
        return dao.getIntakeStatus(medicationId, date) ?: false
    }
}
