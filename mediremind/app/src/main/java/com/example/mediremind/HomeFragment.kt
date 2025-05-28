package com.example.mediremind

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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
import java.time.LocalDate
import java.time.YearMonth

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

        val db = AppDatabase.getInstance(requireContext())
        val logDao = db.alarmLogDao()
        val medDao = db.medicationDao()
        val today = LocalDate.now()
        val epochDay = today.toEpochDay()

        // 가로 달력 설정
        val year = today.year
        val month = today.monthValue
        val calendarAdapter = CalendarAdapter(generateDatesForMonth(year, month)) { selectedDate ->
            val dialog = RecordDetailBottomSheet.newInstance(selectedDate.toString())
            dialog.show(parentFragmentManager, "RecordDetailDialog")
        }
        binding.rvCalendar.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCalendar.adapter = calendarAdapter
        binding.tvCalendarTitle.text = "${year}년 ${month}월"

        // 오늘의 약 복용 로그 없으면 추가
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val meds = medDao.getAll().filter {
                    val start = LocalDate.parse(it.startDate)
                    val end = it.endDate?.let { LocalDate.parse(it) } ?: LocalDate.MAX
                    today in start..end
                }
                meds.forEach { med ->
                    val exists = logDao.exists(epochDay, med.name)
                    if (!exists) {
                        logDao.insert(
                            AlarmLog(
                                name = med.name,
                                desc = med.description,
                                timestamp = epochDay,
                                taken = false
                            )
                        )
                    }
                }
            }

            // 오늘의 로그 가져와 UI 업데이트
            val logs = withContext(Dispatchers.IO) {
                logDao.getLogsByDate(epochDay)
            }

            binding.tvStatus.text = if (logs.all { it.taken }) {
                "✅ 오늘의 약을 모두 복용했습니다!"
            } else {
                "⚠️ 복용해야 할 약이 있습니다"
            }

            // 체크박스로 표시
            val container = binding.containerTodayMeds
            container.removeAllViews()

            logs.forEach { log ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = log.name
                    isChecked = log.taken
                    setTextColor(if (log.taken) Color.GRAY else Color.BLACK)
                }
                checkBox.setOnCheckedChangeListener { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (checkBox.isChecked) {
                            logDao.markAsTaken(log.id)
                        } else {
                            logDao.markAsNotTaken(log.id)
                        }

                        val updatedLogs = logDao.getLogsByDate(epochDay)
                        withContext(Dispatchers.Main) {
                            binding.tvStatus.text = if (updatedLogs.all { it.taken }) {
                                "✅ 오늘의 약을 모두 복용했습니다!"
                            } else {
                                "⚠️ 복용해야 할 약이 있습니다"
                            }
                        }
                    }
                }
                container.addView(checkBox)
            }
        }

        // 습관 요약
        val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
        binding.tvHabitSummary.text =
            prefs.getString("today_summary", "오늘 등록된 건강 습관이 없습니다.")
    }

    private fun generateDatesForMonth(year: Int, month: Int): List<LocalDate> {
        val yearMonth = YearMonth.of(year, month)
        return (1..yearMonth.lengthOfMonth()).map { day ->
            LocalDate.of(year, month, day)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
