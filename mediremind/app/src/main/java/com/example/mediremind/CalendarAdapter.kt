package com.example.mediremind

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.YearMonth

class CalendarAdapter(
    private val dates: List<LocalDate>,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tv_day)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_date, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = dates[position]
        holder.tvDay.text = date.dayOfWeek.name.take(3) // ex: MON, TUE
        holder.tvDate.text = date.dayOfMonth.toString()

        holder.itemView.setOnClickListener {
            onDateClick(date)
        }
    }

    override fun getItemCount(): Int = dates.size
}
