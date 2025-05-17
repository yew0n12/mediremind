package com.example.mediremind.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.mediremind.alarm.NotificationReceiver
import com.example.mediremind.tracker.Medication
import java.util.Calendar

object AlarmManagerHelper {
    fun scheduleAlarm(context: Context, medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        medication.days.forEach { day ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, medication.time.hour)
                set(Calendar.MINUTE, medication.time.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.DAY_OF_WEEK, convertDayToCalendarDay(day))
            }

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("id", medication.id)
                putExtra("name", medication.name)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.requestCode + day.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun convertDayToCalendarDay(day: String): Int {
        return when (day) {
            "월" -> Calendar.MONDAY
            "화" -> Calendar.TUESDAY
            "수" -> Calendar.WEDNESDAY
            "목" -> Calendar.THURSDAY
            "금" -> Calendar.FRIDAY
            "토" -> Calendar.SATURDAY
            "일" -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }
}
