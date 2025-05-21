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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
