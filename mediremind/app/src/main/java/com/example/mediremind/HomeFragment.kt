package com.example.mediremind

//import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.time.format.DateTimeFormatter
//import java.util.*

class HomeFragment : Fragment() {
    private var _binding: ActivityHomeFragmentBinding? = null
    private val binding get() = _binding!!
    private val adapter = MedAdapter()

    // 현재 화면에 표시 중인 날짜
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 수평 달력 설정 (월별 날짜)
        val today = LocalDate.now()
        setupMonthCalendar(today.year, today.monthValue)

        // 2) 리사이클러뷰 설정
        binding.rvMedications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }

        adapter.onTakenChecked = { medication, isChecked ->
            medication.taken = isChecked
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(requireContext())
                        .medicationDao()
                        .update(medication)
                }

                val updatedList = withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(requireContext())
                        .medicationDao()
                        .getMedicationsForToday(selectedDate.format(DateTimeFormatter.ISO_DATE))
                }
                adapter.submitList(updatedList)
                updateProgressPercent(updatedList)
            }
        }


        // 3) 날짜 선택 버튼
//        binding.btnSelectDate.setOnClickListener {
//            showDatePicker()
//        }

        // 첫 화면에 오늘 날짜 기준 UI 업데이트
        updateUIForDate(selectedDate)
    }

    // 달력을 위한 어댑터 설정
    private fun setupMonthCalendar(year: Int, month: Int) {
        val dates = generateDatesForMonth(year, month)
        val calendarAdapter = CalendarAdapter(dates) { clickedDate ->
            // 달력에서 날짜 클릭 시 해당 날짜로 UI 업데이트
            selectedDate = clickedDate
            updateUIForDate(clickedDate)
        }
        binding.rvCalendar.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCalendar.adapter = calendarAdapter
        binding.tvCalendarTitle.text = "${year}년 ${month}월"
    }


    // 화면에 날짜별 데이터를 로딩
    private fun updateUIForDate(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ISO_DATE) // yyyy-MM-dd

        // 선택한 날짜 표시
//        binding.tvSelectedDate.text = "선택한 날짜: $dateString"

        // 1) 약 목록 조회
        lifecycleScope.launch {
            val meds: List<Medication> = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext())
                    .medicationDao()
                    .getMedicationsForToday(dateString)
            }
            adapter.submitList(meds)
            updateProgressPercent(meds)
        }

        // 2) 건강 습관 요약 불러오기
        val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
        val summary = prefs.getString(dateString, "이 날 등록된 건강 습관이 없습니다.")
        binding.tvHabitSummary.text = summary
    }

    // 복용 퍼센트 계산 및 상태 메시지 업데이트
    private fun updateProgressPercent(meds: List<Medication>) {
        val total = meds.size
        val takenCount = meds.count { it.taken }
        val percent = if (total == 0) 0 else (takenCount * 100) / total

        binding.tvProgressPercent.text = "$percent%"

        val statusText = when {
            total == 0 -> "No Medication\nToday!"
            percent == 100 -> "All Done!\nPerfect!"
            percent >= 70 -> "Great Job!\nAlmost Done!"
            percent >= 30 -> "Keep Going!\nYou're Getting There!"
            else -> "Your Plan\nJust Started!"
        }
        binding.tvProgressTitle.text = statusText
    }

    // 해당 월의 날짜 리스트 생성
    private fun generateDatesForMonth(year: Int, month: Int): List<LocalDate> {
        val ym = YearMonth.of(year, month)
        return (1..ym.lengthOfMonth()).map { day ->
            LocalDate.of(year, month, day)
        }
    }

    override fun onResume() {
        super.onResume()
        // 복귀 시에도 선택된 날짜 기준 데이터 갱신
        updateUIForDate(selectedDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
