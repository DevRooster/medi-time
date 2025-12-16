package com.example.medi_time_up.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.medi_time_up.data.ScheduledMedication
import com.example.medi_time_up.receivers.NotificationReceiver
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {

    private const val CHANNEL_ID = "meditime_channel"

    fun scheduleForSchedule(context: Context, schedule: ScheduledMedication) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val zoneId = ZoneId.systemDefault()

        val times = schedule.timesCsv.split(",")
            .mapNotNull {
                val p = it.split(":")
                if (p.size == 2) LocalTime.of(p[0].toInt(), p[1].toInt()) else null
            }

        for (day in schedule.startEpochDay..schedule.endEpochDay) {
            val date = LocalDate.ofEpochDay(day)

            times.forEachIndexed { index, time ->
                val dateTime = date.atTime(time)
                val triggerAtMillis =
                    dateTime.atZone(zoneId).toInstant().toEpochMilli() -
                            schedule.remindBeforeMinutes * 60_000L

                if (triggerAtMillis <= System.currentTimeMillis()) return@forEachIndexed

                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("title", "Medicamento")
                    putExtra("message", "${schedule.nombre} - ${schedule.dosis}")
                }

                val requestCode =
                    (schedule.id * 1000 + day + index).toInt()

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    fun cancelSchedule(context: Context, schedule: ScheduledMedication) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (day in schedule.startEpochDay..schedule.endEpochDay) {
            for (i in schedule.timesCsv.split(",").indices) {
                val intent = Intent(context, NotificationReceiver::class.java)

                val requestCode =
                    (schedule.id * 1000 + day + i).toInt()

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.cancel(pendingIntent)
            }
        }
    }
}