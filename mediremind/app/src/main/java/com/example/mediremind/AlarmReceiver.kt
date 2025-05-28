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
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import java.util.Date


import com.example.mediremind.R
import com.example.mediremind.data.AlarmLog
import com.example.mediremind.data.AppDatabase
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "알람 발동 시간: ${Date(System.currentTimeMillis())}")


        if (context == null) return


        // 알림 채널 ID (MainActivity 또는 Application에서 미리 생성해 둬야 함)
        val channelId = "mediremind"
        val medName = intent?.getStringExtra("medName") ?: "약 복용"
        val medDesc = intent?.getStringExtra("medDesc") ?: "시간입니다"
        val hour = intent?.getIntExtra("hour",8)  ?: 8    // [추가됨]
        val minute = intent?.getIntExtra("minute",0) ?: 0 // 기본시간값 설정

        // [추가됨] 고유 알림 ID 생성: 약 이름 + 시각 기준
        val uniqueId = (System.currentTimeMillis().toString() + medName).hashCode()

        val tapIntent = Intent(context, MainActivity::class.java) // [추가됨] 알림 클릭 시 어디로 이동할 지 설정

        // [추가됨] 시스템에 넘겨서 알림 보관
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // 아이콘 없으면 기본 앱 아이콘 사용
            .setContentTitle(medName) //약 복용 시간니다.
            .setContentText(medDesc) //설정한 시간에 약을 복용하세요
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // [추가됨] 알림 클릭하면 앱을 열어주기

        // [추가됨] 다음날 알림 재예약입
        val nextCalendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val notificationManager = NotificationManagerCompat.from(context)

        // Android 13 이상에서는 POST_NOTIFICATIONS 권한 체크 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                // [수정됨] notify 호출 시 고유 ID 사용
                notificationManager.notify(uniqueId, builder.build())
            } else {
                // 권한 없음: 알림 안 띄움 or 로그 기록
                Toast.makeText(context, "알림 권한이 없어 알림이 표시되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Android 13 미만은 그냥 실행
            // [수정됨] notify 호출 시 고유 ID 사용
            notificationManager.notify(uniqueId, builder.build())
        }
        val log = AlarmLog(name = medName, desc = medDesc, timestamp = System.currentTimeMillis(), taken = false )
        Thread {
            val db = AppDatabase.getInstance(context)
            db.alarmLogDao().insert(log)
        }.start()

        // ---아래 모두 [추가됨] 매일 알람 반복 처리
        val nextIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medName", medName)
            putExtra("medDesc", medDesc)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val nextRequestCode = (medName + "%02d:%02d".format(hour, minute)).hashCode()

        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            nextRequestCode,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextCalendar.timeInMillis,
            nextPendingIntent
        )
    }
}