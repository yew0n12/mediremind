package com.example.mediremind

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediremind.data.AppDatabase

class AlarmHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AlarmLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_alarm_history, container, false)
        recyclerView = view.findViewById(R.id.recycler_alarm_logs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadLogs()

        return view
    }

    private fun loadLogs() {
        Thread {
            val db = AppDatabase.getInstance(requireContext())
            val logs = db.alarmLogDao().getAll()

            requireActivity().runOnUiThread {
                adapter = AlarmLogAdapter(logs)
                recyclerView.adapter = adapter
            }
        }.start()
    }
}
