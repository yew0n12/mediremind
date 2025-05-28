package com.example.mediremind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mediremind.data.AlarmLog
import com.example.mediremind.data.AppDatabase
import com.example.mediremind.data.Medication
import com.example.mediremind.databinding.DialogRecordDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class RecordDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogRecordDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDate = requireArguments().getString(ARG_DATE)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDateTitle.text = "$selectedDate 약 복용 기록"
        val db = AppDatabase.getInstance(requireContext())
        val medDao = db.medicationDao()
        val logDao = db.alarmLogDao()

        lifecycleScope.launch {
            val date = LocalDate.parse(selectedDate)
            val epochDay = date.toEpochDay()

            val meds: List<Medication> = withContext(Dispatchers.IO) {
                medDao.getAll().filter {
                    val start = LocalDate.parse(it.startDate)
                    val end = it.endDate?.let { LocalDate.parse(it) } ?: LocalDate.MAX
                    date in start..end
                }
            }

            val logs = withContext(Dispatchers.IO) {
                logDao.getLogsByDate(epochDay)
            }.toMutableList()

            if (logs.isEmpty()) {
                withContext(Dispatchers.IO) {
                    meds.forEach { med ->
                        val log = AlarmLog(
                            name = med.name,
                            desc = med.description,
                            timestamp = epochDay,
                            taken = false
                        )
                        logDao.insert(log)
                        logs.add(log)
                    }
                }
            }

            if (logs.all { it.taken }) {
                binding.tvAllTaken.text = "✅ 모두 복용 완료!"
            } else {
                binding.tvAllTaken.text = "⚠️ 아직 복용하지 않은 약이 있습니다."
            }

            // 기존 체크박스 대신 텍스트뷰로 대체
            binding.containerMeds.removeAllViews()
            logs.forEach { log ->
                val textView = TextView(requireContext()).apply {
                    val statusIcon = if (log.taken) "✅" else "❌"
                    text = "$statusIcon ${log.name}"
                    textSize = 16f
                    setPadding(8, 8, 8, 8)
                }
                binding.containerMeds.addView(textView)
            }

            binding.btnConfirmAll.setOnClickListener {
                Toast.makeText(requireContext(), "복용 정보가 저장되었습니다", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_DATE = "arg_date"

        fun newInstance(date: String): RecordDetailBottomSheet {
            val fragment = RecordDetailBottomSheet()
            val args = Bundle().apply {
                putString(ARG_DATE, date)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
