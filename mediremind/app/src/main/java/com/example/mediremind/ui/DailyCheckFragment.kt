package com.example.mediremind.ui

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mediremind.R
import com.example.mediremind.tracker.MedicationTracker
import com.example.mediremind.tracker.data.AppDatabase
import java.time.LocalDate
import kotlinx.coroutines.launch

class DailyCheckFragment : Fragment(R.layout.fragment_daily_check) {

    private lateinit var tracker: MedicationTracker
    private val today = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // 꼭 호출하기!

        val checkbox = view.findViewById<CheckBox>(R.id.check_taken)

        lifecycleScope.launch {
            val taken = tracker.isTaken(medicationId = 1, date = today)
            checkbox.isChecked = taken
        }

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                if (isChecked) tracker.markAsTaken(1, today)
            }
        }
    }
}
