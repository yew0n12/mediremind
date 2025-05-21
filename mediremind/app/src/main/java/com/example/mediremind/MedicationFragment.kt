package com.example.mediremind

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mediremind.data.AppDatabase
import com.example.mediremind.data.Medication
import com.example.mediremind.databinding.FragmentMedicationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


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

        loadMedications()
        adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, getDisplayList())
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

                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getInstance(requireContext()).medicationDao().delete(med)
                    }
                    cancelAlarm(med.name)
                    refreshList()

//                    Thread {
//                        val db = AppDatabase.getInstance(requireContext())
//                        db.medicationDao().delete(med)
//                    }.start()
//
//                    cancelAlarm(med.name)
//                    refreshList()
                }
                .setNegativeButton("취소", null)
                .show()
            true
        }
    }

    //사라지는 거 방지
    override fun onResume() {
        super.onResume()
        loadMedications()
    }

    private fun getDisplayList(): List<String> {
        return medications.map { "${it.name} (${it.time})\n${it.startDate} ~ ${it.endDate ?: "무기한"}\n${it.description}" }
    }

    private fun refreshList() {
        adapter.clear()
        adapter.addAll(getDisplayList())
        adapter.notifyDataSetChanged()
    }

    //    private fun loadMedications() {
//        Thread {
//            val db = AppDatabase.getInstance(requireContext())
//            val data = db.medicationDao().getAll()
//            medications.clear()
//            medications.addAll(data)
//            requireActivity().runOnUiThread {
//                refreshList()
//            }
//        }.start()
//    }
    private fun loadMedications() {
        lifecycleScope.launch(Dispatchers.IO) {
            val data = AppDatabase.getInstance(requireContext()).medicationDao().getAll()
            medications.clear()
            medications.addAll(data)
            withContext(Dispatchers.Main) { refreshList() }
        }
    }

    private fun showMedicationDialog(position: Int? = null) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_medication, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_med_name)
        val editDesc = dialogView.findViewById<EditText>(R.id.edit_med_desc)
        val editStartDate = dialogView.findViewById<EditText>(R.id.edit_start_date)
        val editEndDate = dialogView.findViewById<EditText>(R.id.edit_end_date)
        val editTime = dialogView.findViewById<EditText>(R.id.edit_time)


        // DatePicker와 TimePicker 이용해서 시작 날짜, 종료 날짜, 시간 형식 강제
        editStartDate.setOnClickListener {
            showDatePicker { dateStr -> editStartDate.setText(dateStr) }
        }
        editEndDate.setOnClickListener {
            showDatePicker { dateStr -> editEndDate.setText(dateStr) }
        }
        editTime.setOnClickListener {
            showTimePicker { timeStr -> editTime.setText(timeStr) }
        }



        if (position != null) {
            val med = medications[position]
            editName.setText(med.name)
            editDesc.setText(med.description)
            editStartDate.setText(med.startDate)
            med.endDate?.let { editEndDate.setText(it) }    //nullable인 endDate
            editTime.setText(med.time)
        }


        AlertDialog.Builder(requireContext())
            .setTitle(if (position == null) "약 추가하기" else "약 수정하기")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val name = editName.text.toString().trim()
                val desc = editDesc.text.toString().trim()
                val startDate = editStartDate.text.toString().trim()
                val endDate = editEndDate.text.toString().trim().takeIf { it.isNotBlank() }
                val time = editTime.text.toString().trim()


                if (name.isBlank() || startDate.isBlank() || time.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "약 이름, 시작일, 복용 시간이 모두 필요합니다", Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val db = AppDatabase.getInstance(requireContext())

                lifecycleScope.launch(Dispatchers.IO) {
                    if (position == null) {
                        // --- ADD MODE: 새 약 추가 ---
                        val newMed = Medication(
                            name = name,
                            description = desc,
                            startDate = startDate,
                            endDate = endDate,
                            time = time
                        )
                        db.medicationDao().insert(newMed)
                        medications.add(newMed)

                        withContext(Dispatchers.Main) {
                            refreshList()
                            setAlarmByMedication(name, desc, time)
                            Toast.makeText(
                                requireContext(),
                                "알람이 설정되었습니다", Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        // --- UPDATE MODE: 기존 약 수정 ---
                        val oldMed = medications[position]
                        val updatedMed = oldMed.copy(
                            name = name,
                            description = desc,
                            startDate = startDate,
                            endDate = endDate,
                            time = time
                        )
                        db.medicationDao().update(updatedMed)
                        medications[position] = updatedMed

                        withContext(Dispatchers.Main) {
                            refreshList()
                            // 기존 알람 취소 후 새 알람 설정
                            cancelAlarm(oldMed.name)
                            setAlarmByMedication(name, desc, time)
                            Toast.makeText(
                                requireContext(),
                                "수정된 알람이 설정되었습니다", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }


//                if (name.isNotBlank()) {
//                    if (position == null) {
//                        val newMed = Medication(name = name, description = desc, schedule = schedule)
//                        medications.add(newMed)
//                        Thread {
//                            val db = AppDatabase.getInstance(requireContext())
//                            db.medicationDao().insert(newMed)
//                        }.start()
//                    } else {
//                        val updated = medications[position].copy(name = name, description = desc, schedule = schedule)
//                        medications[position] = updated
//                        Thread {
//                            val db = AppDatabase.getInstance(requireContext())
//                            db.medicationDao().update(updated)
//                        }.start()
//                    }
//
//                    refreshList()
//
//                    val time = parseHourMinute(schedule)
//                    if (time != null) {
//                        setAlarm(time.first, time.second, name, desc)
//                        Toast.makeText(requireContext(), "알람이 설정되었습니다", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(requireContext(), "시간 형식이 잘못됐습니다 (예: 08:30)", Toast.LENGTH_SHORT).show()
//                    }
//
//                } else {
//                    Toast.makeText(requireContext(), "약 이름은 필수입니다", Toast.LENGTH_SHORT).show()
//                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // DatePicker 구현
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val date = String.format("%04d-%02d-%02d", y, m + 1, d)
            onDateSelected(date)
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    // TimePicker 구현
    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val now = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, min ->
            val time = String.format("%02d:%02d", h, min)
            onTimeSelected(time)
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }


    // 알람 설정 헬퍼
    private fun setAlarmByMedication(name: String, desc: String, time: String) {
        parseHourMinute(time)?.let { (hour, minute) ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
            }
            val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                putExtra("medName", name); putExtra("medDesc", desc)
            }
            val pi = PendingIntent.getBroadcast(
                requireContext(), name.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE
            )
            val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setRepeating(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, pi
            )
        }
    }


//    private fun parseHourMinute(schedule: String): Pair<Int, Int>? {
//        return try {
//            val parts = schedule.split(":")
//            val hour = parts[0].toInt()
//            val minute = parts[1].toInt()
//            hour to minute
//        } catch (e: Exception) {
//            null
//        }
//    }
private fun parseHourMinute(schedule: String): Pair<Int, Int>? {
    return try {
        val (h, m) = schedule.split(":").map { it.toInt() }
        h to m
    } catch (e: Exception) {
        null
    }
}


//    private fun setAlarm(hour: Int, minute: Int, name: String, desc: String) {
//        val calendar = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, hour)
//            set(Calendar.MINUTE, minute)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//            if (before(Calendar.getInstance())) {
//                add(Calendar.DAY_OF_MONTH, 1)
//            }
//        }
//
//        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
//            putExtra("medName", name)
//            putExtra("medDesc", desc)
//        }
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            requireContext(),
//            name.hashCode(),
//            intent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            pendingIntent
//        )
//    }

//    private fun cancelAlarm(name: String) {
//        val intent = Intent(requireContext(), AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            requireContext(),
//            name.hashCode(),
//            intent,
//            PendingIntent.FLAG_IMMUTABLE
//        )
//        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmManager.cancel(pendingIntent)
//        Toast.makeText(requireContext(), "알람이 취소되었습니다", Toast.LENGTH_SHORT).show()
//    }


    private fun cancelAlarm(name: String) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            requireContext(),
            name.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        (requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
        Toast.makeText(requireContext(), "알람이 취소되었습니다", Toast.LENGTH_SHORT).show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}