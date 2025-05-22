import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mediremind.data.AlarmLog
import com.example.mediremind.data.AppDatabase
import com.example.mediremind.data.Medication
import com.example.mediremind.data.MedicationDao
import com.example.mediremind.databinding.DialogRecordDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset


class RecordDetailBottomSheet : BottomSheetDialogFragment() {



    private var _binding: DialogRecordDetailBinding? = null
    private val binding get() = _binding!!

    // 선택한 날짜를 담을 변수
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
        val db = AppDatabase.getInstance(requireContext())
        val medDao = db.medicationDao()
        val logDao = db.alarmLogDao()
        // 날짜 제목 설정
        binding.tvDateTitle.text = "$selectedDate 약 복용 기록"

        // 코루틴으로 Room에서 데이터 조회
        CoroutineScope(Dispatchers.Main).launch {
            val date = LocalDate.parse(selectedDate)

            val meds: List<Medication> = withContext(Dispatchers.IO) {
                medDao.getMedicationsForToday(selectedDate)
            }
            val logs = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext())
                    .alarmLogDao()
                    .getLogsByDate(selectedDate)
            }

            if (logs.isEmpty()) {
                binding.tvDetail.text = "기록 없음"
                binding.tvAllTaken.text = ""
            } else {
                // 복용 기록을 텍스트로 정리
                val summary = logs.joinToString("\n") {
                    "- ${it.name} (${if (it.taken) "복용함" else "미복용"})"
                }
                binding.tvDetail.text = summary

                // 모두 복용했는지 체크해서 메시지 표시
                val allTaken = logs.all { it.taken }
                binding.tvAllTaken.text =
                    if (allTaken) "✅ 모두 복용 완료!" else "⚠️ 아직 복용하지 않은 약이 있습니다."
                withContext(Dispatchers.IO) {
                    meds.forEach { med ->
                        val timestamp = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
                        val existingLogs = logDao.getLogsByDate(selectedDate)
                        val alreadyLogged = existingLogs.any { it.name == med.name }

                        if (!alreadyLogged) {
                            logDao.insert(
                                AlarmLog(
                                    name = med.name,
                                    desc = med.description,
                                    timestamp = timestamp,
                                    taken = false
                                )
                            )
                        }
                    }
                }

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
