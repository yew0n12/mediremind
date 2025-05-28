package com.example.mediremind

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.mediremind.databinding.ActivityHabitFragmentBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitFragment : Fragment() {

    private var _binding: ActivityHabitFragmentBinding? = null
    private val binding get() = _binding!!


    // 🔹 선택된 날짜 저장용 (기본값: 오늘)
    private var selectedDate: Calendar = Calendar.getInstance()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityHabitFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ 기록 저장 버튼 클릭
//        binding.btnSave.setOnClickListener {
//            val exerciseTotalMinutes = binding.editExerciseMinutes.text.toString().toIntOrNull() ?: 0
//            val sleep = binding.editSleepHours.text.toString().toFloatOrNull() ?: 0f
//            val waterCups = binding.editWaterCups.text.toString().toIntOrNull() ?: 0
//
//            val goalExerciseMinutes = binding.editGoalExerciseMinutes.text.toString().toIntOrNull() ?: 0
//            val goalSleepHours = binding.editGoalSleepHours.text.toString().toFloatOrNull() ?: 0f
//            val goalWaterCups = binding.editGoalWaterCups.text.toString().toIntOrNull() ?: 0
//
//            val exerciseGoalMet = exerciseTotalMinutes >= goalExerciseMinutes
//            val sleepGoalMet = sleep >= goalSleepHours
//            val waterGoalMet = waterCups >= goalWaterCups
//
//            val exerciseHours = exerciseTotalMinutes / 60
//            val exerciseMinutes = exerciseTotalMinutes % 60
//            val waterMl = waterCups * 200
//            val waterLiters = waterMl / 1000.0
//            val waterFormatted = "${waterCups}잔 (${String.format("%.1f", waterLiters)}L)"
//
//            val summary = "운동: ${exerciseHours}시간 ${exerciseMinutes}분 (${if (exerciseGoalMet) "목표 달성🔥" else "목표 미달성🥲"})\n" +
//                    "수면: ${sleep}시간 (${if (sleepGoalMet) "목표 달성🔥" else "목표 미달성🥲"})\n" +
//                    "물: $waterFormatted (${if (waterGoalMet) "목표 달성🔥" else "목표 미달성🥲"})"
//
//            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//
//            val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
//            prefs.edit().putString(todayKey, summary).apply()
//
//            Toast.makeText(requireContext(), "기록이 저장되었습니다. ($todayKey)", Toast.LENGTH_SHORT).show()
//
//            // 입력 초기화
//            binding.editExerciseMinutes.text.clear()
//            binding.editSleepHours.text.clear()
//            binding.editWaterCups.text.clear()
//        }
        binding.btnSave.setOnClickListener {
            val exerciseTotalMinutes = binding.editExerciseMinutes.text.toString().toIntOrNull() ?: 0
            val sleep = binding.editSleepHours.text.toString().toFloatOrNull() ?: 0f
            val waterCups = binding.editWaterCups.text.toString().toIntOrNull() ?: 0

            // 🔸 목표값 SharedPreferences에서 불러오기
            val goalPrefs = requireContext().getSharedPreferences("habit_goals", Context.MODE_PRIVATE)
            val goalExerciseMinutes = goalPrefs.getString("goal_exercise", "0")?.toIntOrNull() ?: 0
            val goalSleepHours = goalPrefs.getString("goal_sleep", "0")?.toFloatOrNull() ?: 0f
            val goalWaterCups = goalPrefs.getString("goal_water", "0")?.toIntOrNull() ?: 0

            val exerciseGoalMet = exerciseTotalMinutes >= goalExerciseMinutes
            val sleepGoalMet = sleep >= goalSleepHours
            val waterGoalMet = waterCups >= goalWaterCups

            val exerciseHours = exerciseTotalMinutes / 60
            val exerciseMinutes = exerciseTotalMinutes % 60
            val waterMl = waterCups * 200
            val waterLiters = waterMl / 1000.0
            val waterFormatted = "${waterCups}잔 (${String.format("%.1f", waterLiters)}L)"

            val summary = "운동: ${exerciseHours}시간 ${exerciseMinutes}분 (${if (exerciseGoalMet) "목표 달성🔥" else "목표 미달성🥲"})\n" +
                    "수면: ${sleep}시간 (${if (sleepGoalMet) "목표 달성🔥" else "목표 미달성🥲"})\n" +
                    "물: $waterFormatted (${if (waterGoalMet) "목표 달성🔥" else "목표 미달성🥲"})"

            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString(todayKey, summary).apply()

            Toast.makeText(requireContext(), "기록이 저장되었습니다. ($todayKey)", Toast.LENGTH_SHORT).show()

            binding.editExerciseMinutes.text.clear()
            binding.editSleepHours.text.clear()
            binding.editWaterCups.text.clear()
        }



        // ✅ 목표 저장 버튼 클릭
        binding.btnSaveGoal.setOnClickListener {
            val goalExercise = binding.editGoalExerciseMinutes.text.toString()
            val goalSleep = binding.editGoalSleepHours.text.toString()
            val goalWater = binding.editGoalWaterCups.text.toString()

            // 비어있으면 안내 후 리턴
            if (goalExercise.isBlank() || goalSleep.isBlank() || goalWater.isBlank()) {
                Toast.makeText(requireContext(), "모든 목표를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 목표 저장
            val goalPrefs = requireContext().getSharedPreferences("habit_goals", Context.MODE_PRIVATE)
            with(goalPrefs.edit()) {
                putString("goal_exercise", goalExercise)
                putString("goal_sleep", goalSleep)
                putString("goal_water", goalWater)
                apply()
            }

            // 입력 초기화
            binding.editGoalExerciseMinutes.text.clear()
            binding.editGoalSleepHours.text.clear()
            binding.editGoalWaterCups.text.clear()

            Toast.makeText(requireContext(), "목표가 저장되었습니다", Toast.LENGTH_SHORT).show()
        }
    }



//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.btnSave.setOnClickListener {
//            // 사용자 입력값
//            val exerciseTotalMinutes = binding.editExerciseMinutes.text.toString().toIntOrNull() ?: 0
//            val sleep = binding.editSleepHours.text.toString().toFloatOrNull() ?: 0f
//            val waterCups = binding.editWaterCups.text.toString().toIntOrNull() ?: 0
//
//            // 목표값
//            val goalExerciseMinutes = binding.editGoalExerciseMinutes.text.toString().toIntOrNull() ?: 0
//            val goalSleepHours = binding.editGoalSleepHours.text.toString().toFloatOrNull() ?: 0f
//            val goalWaterCups = binding.editGoalWaterCups.text.toString().toIntOrNull() ?: 0
//
//
//            // 목표 달성 여부
//            val exerciseGoalMet = exerciseTotalMinutes >= goalExerciseMinutes
//            val sleepGoalMet = sleep >= goalSleepHours
//            val waterGoalMet = waterCups >= goalWaterCups
//
//            val exerciseHours = exerciseTotalMinutes / 60
//            val exerciseMinutes = exerciseTotalMinutes % 60
//            val waterMl = waterCups * 200
//            val waterLiters = waterMl / 1000.0
//            val waterFormatted = "${waterCups}잔 (${String.format("%.1f", waterLiters)}L)"
//
//            val summary = "운동: ${exerciseHours}시간 ${exerciseMinutes}분 (${if (exerciseGoalMet) "목표 달성🔥" else "목표 미달성🥲"})\n" +
//                    "수면: ${sleep}시간 (${if (sleepGoalMet) "목표 달성🔥" else "목표 미달성🥲"})\n" +
//                    "물: $waterFormatted (${if (waterGoalMet) "목표 달성🔥" else "목표 미달성🥲"})"
//
//            // 날짜 키 생성
//            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//
//            // SharedPreferences 저장
//            val prefs = requireContext().getSharedPreferences("habit_prefs", Context.MODE_PRIVATE)
//            prefs.edit().putString(todayKey, summary).apply()
//
//            Toast.makeText(requireContext(), "기록이 저장되었습니다. ($todayKey)", Toast.LENGTH_SHORT).show()
//
//
//
//            // 입력 필드 초기화
//            binding.editExerciseMinutes.text.clear()
//            binding.editSleepHours.text.clear()
//            binding.editWaterCups.text.clear()
//        }
//    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


