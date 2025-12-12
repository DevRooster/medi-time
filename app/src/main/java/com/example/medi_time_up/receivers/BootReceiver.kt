package com.example.medi_time_up.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.medi_time_up.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BootReceiver"

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "onReceive action=$action")
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Reprogramar alarms en background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val dao = db.scheduledMedicationDao()
                    val schedules = dao.getActiveSchedules()
                    Log.d(TAG, "Found ${schedules.size} active schedules to re-schedule")
                    schedules.forEach { schedule ->
                        try {
                            AlarmScheduler.scheduleForSchedule(context.applicationContext, schedule)
                        } catch (t: Throwable) {
                            Log.e(TAG, "Failed scheduling for schedule id=${schedule.id}", t)
                        }
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Error reprogramming schedules on boot", t)
                }
            }
        } else {
            Log.d(TAG, "Ignoring action=$action")
        }
    }
}