package com.example.mediremind

//import android.content.Context
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.mediremind.data.AppDatabase
//import com.example.mediremind.data.Medication
//import com.example.mediremind.databinding.ActivityHomeFragmentBinding
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.time.LocalDate
//
//class HomeFragment : Fragment() {
//    private var _binding: ActivityHomeFragmentBinding? = null
//    private val binding get() = _binding!!
//    private val adapter = MedAdapter()
//
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = ActivityHomeFragmentBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // 1) RecyclerView 기본 설정
//        binding.rvMedications.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = this@HomeFragment.adapter
//        }
//
//        // 2) 오늘 날짜 문자열 생성 (YYYY-MM-DD)
//        val today = LocalDate.now().toString()
//
//
//        // 3) 비동기로 오늘의 약 목록 조회 및 화면에 반영
//        lifecycleScope.launch {
//            // 백그라운드 쓰레드에서 DB 호출
//            val meds: List<Medication> = withContext(Dispatchers.IO) {
//                AppDatabase.getInstance(requireContext())
//                    .medicationDao()
//                    .getMedicationsForToday(today)
//            }
//            // 조회된 리스트를 Adapter에 전달
//            adapter.submitList(meds)
//        }
//
//        // 4) 오늘의 건강 습관 요약 불러와서 TextView에 표시
//        val prefs = requireContext()
//            .getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
//        val summary = prefs.getString(
//            "summary_$today",
//            "오늘 등록된 건강 습관이 없습니다."
//        )
//        binding.tvHabitSummary.text = summary
//    }
//    override fun onResume() {
//        super.onResume()
//
//        val today = LocalDate.now().toString()
//        val prefs = requireContext()
//            .getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
//        val summary = prefs.getString(
//            "today_summary",
//            "오늘 등록된 건강 습관이 없습니다."
//        )
//        binding.tvHabitSummary.text = summary
//    }
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: ActivityHomeFragmentBinding? = null
    private val binding get() = _binding!!
    private val adapter = MedAdapter()

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

        binding.rvMedications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }

        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        updateUIForDate(selectedDate)
    }

    private fun showDatePicker() {
        val now = LocalDate.now()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateUIForDate(selectedDate)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
        datePicker.show()
    }

//    private fun updateUIForDate(date: LocalDate) {
//        val dateString = date.format(DateTimeFormatter.ISO_DATE)
//        binding.tvSelectedDate.text = "선택한 날짜: $dateString"
//
//        // 1. 약 리스트 조회
//        lifecycleScope.launch {
//            val meds: List<Medication> = withContext(Dispatchers.IO) {
//                AppDatabase.getInstance(requireContext())
//                    .medicationDao()
//                    .getMedicationsForToday(dateString)
//            }
//            adapter.submitList(meds)
//        }
//
//        // 2. 건강 습관 요약 불러오기
//        val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
////        val summary = prefs.getString("summary_$dateString", "이 날 등록된 건강 습관이 없습니다.")
////        binding.tvHabitSummary.text = summary
//        // 오늘 날짜 문자열 변수 생성 (yyyy-MM-dd)
//        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//
//// 저장된 오늘 날짜 키로 데이터 가져오기
//        val summary = prefs.getString(todayKey, "이 날 등록된 건강 습관이 없습니다.")
//        binding.tvHabitSummary.text = summary
//
//    }
private fun updateUIForDate(date: LocalDate) {
    val dateString = date.format(DateTimeFormatter.ISO_DATE) // yyyy-MM-dd
    binding.tvSelectedDate.text = "선택한 날짜: $dateString"

    // 1. 약 리스트 조회
    lifecycleScope.launch {
        val meds: List<Medication> = withContext(Dispatchers.IO) {
            AppDatabase.getInstance(requireContext())
                .medicationDao()
                .getMedicationsForToday(dateString)
        }
        adapter.submitList(meds)
    }

    // 2. 건강 습관 요약 불러오기 (선택한 날짜 기반)
    val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
    val summary = prefs.getString(dateString, "이 날 등록된 건강 습관이 없습니다.")
    binding.tvHabitSummary.text = summary
}


    override fun onResume() {
        super.onResume()
        updateUIForDate(selectedDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
