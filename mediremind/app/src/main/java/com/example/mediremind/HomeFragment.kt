package com.example.mediremind

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediremind.data.AlarmLog
import com.example.mediremind.data.AppDatabase
import com.example.mediremind.data.Medication
import com.example.mediremind.databinding.ActivityHomeFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: ActivityHomeFragmentBinding? = null
    private val binding get() = _binding!!
    private val adapter = MedAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val year = 2025
        val month = 5

        val calendarAdapter = CalendarAdapter(generateDatesForMonth(year, month)) { selectedDate ->
            val dialog = RecordDetailBottomSheet.newInstance(selectedDate.toString())
            dialog.show(parentFragmentManager, "RecordDetailDialog")
        }

        binding.rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = calendarAdapter
        }
        val today = LocalDate.now().toString()
//        binding.tvCalendarTitle.text = "${year}년 ${month}월"
//
//
//
//        // 오늘의 약 RecyclerView
//        binding.rvMedications.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = this@HomeFragment.adapter
//        }

        // 오늘 복용할 약 리스트
        lifecycleScope.launch {
            val meds: List<Medication> = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext())
                    .medicationDao()
                    .getMedicationsForToday(today)
            }
            adapter.submitList(meds)
        }

        // ✅ 오늘 복용한 약의 CheckBox 리스트 추가
        lifecycleScope.launch {
            val logDao = AppDatabase.getInstance(requireContext()).alarmLogDao()
            val logs = withContext(Dispatchers.IO) {
                logDao.getLogsByDate(today).filter { !it.taken }
            }


            val container = binding.containerTodayMeds
            container.removeAllViews()

            logs.forEach { log ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = "${log.name} (${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))})"
                    isChecked = log.taken
                    setTextColor(if (log.taken) Color.GRAY else Color.BLACK)
                }

                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (isChecked) {
                            logDao.markAsTaken(log.id)
                        } else {
                            // 만약 체크 해제 기능도 추가하고 싶다면 아래 메서드를 AlarmLogDao에 만들어야 함
                            logDao.markAsNotTaken(log.id)
                        }
                    }
                }

                container.addView(checkBox)
            }
        }

        // 건강 습관 요약
        val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
        val summary = prefs.getString("today_summary", "오늘 등록된 건강 습관이 없습니다.")
        binding.tvHabitSummary.text = summary
    }

    private fun generateDatesForMonth(year: Int, month: Int): List<LocalDate> {
        val yearMonth = YearMonth.of(year, month)
        return (1..yearMonth.lengthOfMonth()).map { day -> LocalDate.of(year, month, day) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
