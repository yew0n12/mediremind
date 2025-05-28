package com.example.mediremind

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

        // 1) RecyclerView 기본 설정
        binding.rvMedications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }

        // 2) 오늘 날짜 설정
        val today = LocalDate.now().toString()
        binding.tvDate.text = today

        // 3) 오늘의 약 목록 불러오기
        loadMedicationsForToday(today)

        // 4) 체크박스 상태 변경 시 DB 업데이트 + 퍼센트 갱신
        adapter.onTakenChecked = { med, isChecked ->
            val updated = med.copy(taken = isChecked)
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                AppDatabase.getInstance(requireContext())
                                    .medicationDao()
                                    .update(updated)
                            }

                            val newList = adapter.currentList.map {
                                if (it.id == med.id) updated else it
                }
                adapter.submitList(newList)

                updateProgressPercent(newList)
            }
        }

        // 5) 건강 습관 요약 불러오기
        val prefs = requireContext()
            .getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
        val summary = prefs.getString(
            "today_summary",
            "오늘 등록된 건강 습관이 없습니다."
        )
        binding.tvHabitSummary.text = summary
    }

    private fun loadMedicationsForToday(today: String) {
        lifecycleScope.launch {
            val meds: List<Medication> = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext())
                    .medicationDao()
                    .getMedicationsForToday(today)
            }
            adapter.submitList(meds)
            updateProgressPercent(meds)
        }
    }

    private fun updateProgressPercent(meds: List<Medication>) {
        val total = meds.size
        val takenCount = meds.count { it.taken }
        val percent = if (total == 0) 0 else (takenCount * 100) / total

        binding.tvProgressPercent.text = "$percent%"

        // 동적 상태 메시지 설정
        val statusText = when {
            total == 0 -> "No Medication\nToday!"
            percent == 100 -> "All Done!\nPerfect!"
            percent >= 70 -> "Great Job!\nAlmost Done!"
            percent >= 30 -> "Keep Going!\nYou're Getting There!"
            else -> "Your Plan\nJust Started!"
        }
        binding.tvProgressTitle.text = statusText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
