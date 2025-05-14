package com.example.mediremind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import android.Manifest
import android.util.Log
import java.util.Date

import com.example.mediremind.R
import com.example.mediremind.data.AlarmLog
import com.example.mediremind.data.AppDatabase


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "알람 발동 시간: ${Date(System.currentTimeMillis())}")


        if (context == null) return


        // 알림 채널 ID (MainActivity 또는 Application에서 미리 생성해 둬야 함)
        val channelId = "mediremind"
        val medName = intent?.getStringExtra("medName") ?: "약 복용"
        val medDesc = intent?.getStringExtra("medDesc") ?: "시간입니다"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // 아이콘 없으면 기본 앱 아이콘 사용
            .setContentTitle("약 복용 시간입니다")
            .setContentText("설정한 시간에 약을 복용하세요.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        // ✅ Android 13 이상에서는 POST_NOTIFICATIONS 권한 체크 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                notificationManager.notify(1001, builder.build())
            } else {
                // 권한 없음: 알림 안 띄움 or 로그 기록
                Toast.makeText(context, "알림 권한이 없어 알림이 표시되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Android 13 미만은 그냥 실행
            notificationManager.notify(1001, builder.build())
        }
        val log = AlarmLog(name = medName, desc = medDesc, timestamp = System.currentTimeMillis(), taken = false )
        Thread {
            val db = AppDatabase.getInstance(context)
            db.alarmLogDao().insert(log)
        }.start()
    }

}

