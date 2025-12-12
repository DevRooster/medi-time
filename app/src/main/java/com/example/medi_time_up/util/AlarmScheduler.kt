import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.medi_time_up.data.ScheduledMedication
import com.example.medi_time_up.receivers.NotificationPublisher
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private const val TAG = "AlarmScheduler"

object AlarmScheduler {

    /**
     * Schedule alarms for the given schedule across the date range.
     * This generates one alarm per date and time in timesCsv.
     */
    fun scheduleForSchedule(context: Context, schedule: ScheduledMedication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Parse timesCsv -> List<LocalTime>
        val times: List<LocalTime> = schedule.timesCsv
            .split(",")
            .mapNotNull { hhmm ->
                val parts = hhmm.split(":")
                if (parts.size >= 2) {
                    val h = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()
                    if (h != null && m != null) LocalTime.of(h, m) else null
                } else null
            }

        val zone = ZoneId.systemDefault()

        // build list of epochDays (Long): if selectedDatesCsv provided use those, else generate from start..end
        val epochDays: List<Long> = schedule.selectedDatesCsv?.split(",")?.mapNotNull { it.toLongOrNull() }
            ?: run {
                val from = schedule.startEpochDay
                val to = schedule.endEpochDay
                val list = mutableListOf<Long>()
                var d = from
                while (d <= to) {
                    list.add(d)
                    d++
                }
                list
            }

        for (dayEpoch in epochDays) {
            val date = LocalDate.ofEpochDay(dayEpoch)
            for (time in times) {
                val zoned = date.atTime(time).atZone(zone)
                var triggerMillis = zoned.toInstant().toEpochMilli()

                // subtract remindBefore (minutes)
                if (schedule.remindBeforeMinutes > 0) {
                    triggerMillis -= schedule.remindBeforeMinutes * 60_000L
                }

                val intent = Intent(context, NotificationPublisher::class.java).apply {
                    putExtra(NotificationPublisher.EXTRA_TITLE, "Tomar ${schedule.nombre}")
                    putExtra(
                        NotificationPublisher.EXTRA_TEXT,
                        "${schedule.tipo} - ${schedule.dosis} (${time.toString()})"
                    )
                }

                // Build a stable Int requestCode from components (avoiding mixing Long/Int in bit ops)
                val requestCode = schedule.id.hashCode() xor dayEpoch.hashCode() xor time.toSecondOfDay()

                val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                val pi = PendingIntent.getBroadcast(context, requestCode, intent, piFlags)

                // Try to schedule exact alarm, but handle permission / SecurityException
                try {
                    // Optional: explicit check for exact alarm capability on API >= S
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            if (!alarmManager.canScheduleExactAlarms()) {
                                Log.w(TAG, "App cannot schedule exact alarms (canScheduleExactAlarms() = false). Alarm might be postponed by the system.")
                            }
                        } catch (e: NoSuchMethodError) {
                            // Defensive: older runtimes
                        }
                    }
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                    Log.d(TAG, "Scheduled alarm (id=${schedule.id}) at $triggerMillis (epochDay=$dayEpoch, time=$time, req=$requestCode)")
                } catch (sec: SecurityException) {
                    // In some devices / Android versions this may throw if app lacks special permission
                    Log.e(TAG, "SecurityException scheduling exact alarm, falling back to set().", sec)
                    try {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
                        Log.d(TAG, "Fallback scheduled (inexact) alarm at $triggerMillis for req=$requestCode")
                    } catch (t: Throwable) {
                        Log.e(TAG, "Failed to schedule fallback alarm", t)
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Failed to schedule alarm", t)
                }
            }
        }
    }

    fun cancelSchedule(context: Context, schedule: ScheduledMedication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val times: List<LocalTime> = schedule.timesCsv
            .split(",")
            .mapNotNull { hhmm ->
                val parts = hhmm.split(":")
                if (parts.size >= 2) {
                    val h = parts[0].toIntOrNull()
                    val m = parts[1].toIntOrNull()
                    if (h != null && m != null) LocalTime.of(h, m) else null
                } else null
            }

        val epochDays: List<Long> = schedule.selectedDatesCsv?.split(",")?.mapNotNull { it.toLongOrNull() }
            ?: run {
                val from = schedule.startEpochDay
                val to = schedule.endEpochDay
                val list = mutableListOf<Long>()
                var d = from
                while (d <= to) {
                    list.add(d)
                    d++
                }
                list
            }

        for (dayEpoch in epochDays) {
            for (time in times) {
                val intent = Intent(context, NotificationPublisher::class.java)
                val requestCode = schedule.id.hashCode() xor dayEpoch.hashCode() xor time.toSecondOfDay()
                val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                val pi = PendingIntent.getBroadcast(context, requestCode, intent, piFlags)
                try {
                    alarmManager.cancel(pi)
                    Log.d(TAG, "Cancelled alarm req=$requestCode for scheduleId=${schedule.id}")
                } catch (t: Throwable) {
                    Log.e(TAG, "Error cancelling alarm req=$requestCode", t)
                }
            }
        }
    }
}