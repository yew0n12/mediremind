package com.example.mediremind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediremind.data.AppDatabase
import com.example.mediremind.data.Medication
import com.example.mediremind.databinding.ActivityHomeFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

class HomeFragment : Fragment() {

    private var _binding: ActivityHomeFragmentBinding? = null
    private val binding get() = _binding!!
    private val adapter = MedAdapter()
    private fun generateDates(): List<LocalDate> {
        val today = LocalDate.now()
        return (0..30).map { today.plusDays(it.toLong()) }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val db = AppDatabase.getInstance(requireContext())
//        val medicationDao = db.medicationDao()
//
//        val today = LocalDate.now().toString()
//
//        lifecycleScope.launch {
//            val meds = withContext(Dispatchers.IO) {
//                medicationDao.getMedicationsForToday(today)
//            }
//
//            val summary = if (meds.isEmpty()) {
//                "오늘 복용할 약이 없습니다."
//            } else {
//                meds.joinToString("\n") { "- ${it.name} (${it.time})" }
//            }
//
//            binding.tvMedicationSummary.text = summary
//        }
//    }
fun generateDatesForMonth(year: Int, month: Int): List<LocalDate> {
    val yearMonth = YearMonth.of(year, month)
    val daysInMonth = yearMonth.lengthOfMonth()

    return (1..daysInMonth).map { day ->
        LocalDate.of(year, month, day)
    }
}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onViewCreated(view, savedInstanceState)

        val year = 2025
        val month = 5
        val dates = generateDatesForMonth(2025, 5)
        val calendarAdapter = CalendarAdapter(generateDatesForMonth(year, month)) { selectedDate ->
//            val selectedDateStr = selectedDate.toString() // "2025-05-23"
val dialog = RecordDetailBottomSheet.newInstance(selectedDate.toString())
    dialog.show(parentFragmentManager, "RecordDetailDialog")
//            // 코루틴으로 Room에서 기록 조회
//            lifecycleScope.launch {
//                val logs = withContext(Dispatchers.IO) {
//                    AppDatabase.getInstance(requireContext())
//                        .alarmLogDao()
//                        .getLogsByDate(selectedDateStr)
//                }
//
//                // 결과를 UI에 표시
//                if (logs.isEmpty()) {
//                    binding.tvHabitSummary.text = "기록 없음"
//                } else {
//                    binding.tvHabitSummary.text = logs.joinToString("\n") {
//                        "- ${it.name} (${if (it.taken) "복용함" else "미복용"})"
//                    }
//                }
 //           }
        }
        binding.rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvCalendar.adapter = calendarAdapter
        }

        binding.tvCalendarTitle.text = "${year}년 ${month}월"

        // 1) RecyclerView 기본 설정
        binding.rvMedications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
        // 2) 오늘 날짜 문자열 생성 (YYYY-MM-DD)
        val today = LocalDate.now().toString()

        // 3) 비동기로 오늘의 약 목록 조회 및 화면에 반영
        lifecycleScope.launch {
            // 백그라운드 쓰레드에서 DB 호출
            val meds: List<Medication> = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext())
                    .medicationDao()
                    .getMedicationsForToday(today)
            }
            // 조회된 리스트를 Adapter에 전달
            adapter.submitList(meds)
        }

        // 4) 오늘의 건강 습관 요약 불러와서 TextView에 표시
        val prefs = requireContext()
            .getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
        val summary = prefs.getString(
            "today_summary",
            "오늘 등록된 건강 습관이 없습니다."
        )
        binding.tvHabitSummary.text = summary


//        binding.rvMedications.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvMedications.adapter = adapter
//
//        val dao = AppDatabase.getInstance(requireContext()).medicationDao()
//        val today = LocalDate.now().toString()
//
//        lifecycleScope.launch {
//            val meds = withContext(Dispatchers.IO) { dao.getMedicationsForToday(today) }
//            adapter.submitList(meds)
//
//            val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
//            val summary = prefs.getString("today_summary", "오늘 등록된 건강 습관이 없습니다.")
//            binding.tvHabitSummary.text = summary
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
