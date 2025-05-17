package com.example.mediremind.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mediremind.R


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("name") ?: "약 복용 시간이에요!"
        val id = intent.getIntExtra("id", 0)

        val notificationIntent = Intent(context, MedicationScheduleActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("id", id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, id, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "med_channel")
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle("약 복용 알림")
            .setContentText("$name 복용할 시간이에요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("med_channel", "약 알림", NotificationManager.IMPORTANCE_HIGH)
            manager?.createNotificationChannel(channel)
        }

        manager?.notify(id, builder.build())
    }

}
