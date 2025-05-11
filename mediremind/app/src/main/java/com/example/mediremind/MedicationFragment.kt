package com.example.mediremind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.mediremind.databinding.FragmentMedicationBinding
import android.app.AlertDialog

// 📦 약 정보 데이터 클래스
data class Medication(var name: String, var description: String, var schedule: String)

class MedicationFragment : Fragment() {

    // Fragment 전용 ViewBinding
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
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_medication, null)

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

                if (name.isNotBlank()) {
                    if (position == null) {
                        medications.add(Medication(name, desc, schedule))
                    } else {
                        medications[position] = Medication(name, desc, schedule)
                    }
                    refreshList()
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
}