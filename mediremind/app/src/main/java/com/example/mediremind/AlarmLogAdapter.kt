package com.example.mediremind

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mediremind.data.AlarmLog
import com.example.mediremind.data.AppDatabase
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date



class AlarmLogAdapter(private val logs: List<AlarmLog>) :
    RecyclerView.Adapter<AlarmLogAdapter.AlarmLogViewHolder>() {

    class AlarmLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle: TextView = itemView.findViewById(R.id.text_title)
        val txtTime: TextView = itemView.findViewById(R.id.text_time)
        val btnMarkTaken: Button = itemView.findViewById(R.id.btn_mark_taken)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm_log, parent, false)
        return AlarmLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmLogViewHolder, position: Int) {

        val log = logs[position]
        if (log.taken) {
            holder.txtTitle.text = "✅ ${log.name} - ${log.desc}"
            holder.btnMarkTaken.isEnabled = false
            holder.btnMarkTaken.text = "복용 완료"
        } else {
            holder.btnMarkTaken.isEnabled = true
            holder.btnMarkTaken.text = "복용 완료"
        }

        val timeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(log.timestamp))
        holder.btnMarkTaken.setOnClickListener {
            Thread {
                val db = AppDatabase.getInstance(holder.itemView.context)
                db.alarmLogDao().markAsTaken(log.id)
            }.start()

            Toast.makeText(holder.itemView.context, "복용 완료로 처리되었습니다", Toast.LENGTH_SHORT).show()
        }

        holder.txtTitle.text = "${log.name} - ${log.desc}"
        holder.txtTime.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(log.timestamp))
    }

    override fun getItemCount() = logs.size
}
