package com.example.mediremind

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mediremind.databinding.FragmentMedicationBinding
import com.google.android.material.datepicker.DateValidatorPointBackward.before
import java.util.Calendar
// 패키지 데이터 클래스
data class Medication(var name: String, var description: String, var schedule: String)

class MedicationFragment : Fragment() {

    private var _binding: FragmentMedicationBinding? = null
    private val binding get() = _binding!!

    private val medications = mutableListOf<Medication>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, getDisplayList())
        binding.listMedications.adapter = adapter

        binding.btnAddMedication.setOnClickListener {
            showMedicationDialog()
        }

        binding.listMedications.setOnItemClickListener { _, _, position, _ ->
            showMedicationDialog(position)
        }

        binding.listMedications.setOnItemLongClickListener { _, _, position, _ ->
            val med = medications[position]
            AlertDialog.Builder(requireContext())
                .setTitle("삭제 확인")
                .setMessage("‘${med.name}’ 을(를) 삭제할까요?")
                .setPositiveButton("삭제") { _, _ ->
                    medications.removeAt(position)
                    cancelAlarm(med.name)
                    refreshList()
                }
                .setNegativeButton("취소", null)
                .show()
            true
        }
    }

    private fun getDisplayList(): List<String> {
        return medications.map { "${it.name} (${it.schedule})\n${it.description}" }
    }

    private fun refreshList() {
        adapter.clear()
        adapter.addAll(getDisplayList())
        adapter.notifyDataSetChanged()
    }

    private fun showMedicationDialog(position: Int? = null) {

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_medication, null)

        val editName = dialogView.findViewById<EditText>(R.id.edit_med_name)
        val editDesc = dialogView.findViewById<EditText>(R.id.edit_med_desc)
        val editSchedule = dialogView.findViewById<EditText>(R.id.edit_schedule)

        if (position != null) {
            val med = medications[position]
            editName.setText(med.name)
            editDesc.setText(med.description)
            editSchedule.setText(med.schedule)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (position == null) "약 추가하기" else "약 수정하기")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val name = editName.text.toString()
                val desc = editDesc.text.toString()
                val schedule = editSchedule.text.toString()
                Log.d("AlarmDebug", "setPositiveButton 클릭됨, schedule=$schedule")
                Log.d("AlarmDebug", "입력된 schedule = $schedule")

                if (name.isNotBlank()) {
                    if (position == null) {
                        medications.add(Medication(name, desc, schedule))
                    } else {
                        medications[position] = Medication(name, desc, schedule)
                    }
                    refreshList()

                    val time = parseHourMinute(schedule)
                    if (time != null) {
                        setAlarm(time.first, time.second, name, desc)
                        Log.d("AlarmDebug", "setAlarm() 호출됨: ${time.first}:${time.second}")
                        Toast.makeText(requireContext(), "알람이 설정되었습니다", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("AlarmDebug", "시간 파싱 실패로 알람 설정 안 됨: 입력=$schedule")
                        Toast.makeText(requireContext(), "시간 형식이 잘못됐습니다 (예: 08:30)", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(requireContext(), "약 이름은 필수입니다", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parseHourMinute(schedule: String): Pair<Int, Int>? {
        Log.d("AlarmDebug", "parseHourMinute() 호출됨, 입력값: $schedule")
        return try {
            val parts = schedule.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            Log.d("AlarmDebug", "시간 파싱 성공: $hour:$minute")
            hour to minute
        } catch (e: Exception) {
            Log.e("AlarmDebug", "시간 파싱 실패: $schedule", e)
            null
        }
    }

    private fun setAlarm(hour: Int, minute: Int, name: String, desc: String) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        Log.d("AlarmDebug", "알람 설정 시각: ${calendar.time}")

        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("medName", name)
            putExtra("medDesc", desc)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            name.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm(name: String) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            name.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.d("AlarmCancel", "알람 취소됨: $name (${name.hashCode()})")
        Toast.makeText(requireContext(), "알람이 취소되었습니다", Toast.LENGTH_SHORT).show()
    }

}