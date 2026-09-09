package com.example.monitrack

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.monitrack.data.MonitrackDatabase
import com.example.monitrack.data.MonitrackRepository
import com.example.monitrack.data.datastore.ProfileStore
import com.example.monitrack.data.entity.Activity
import com.example.monitrack.reminder.DailyNudgeScheduler
import com.example.monitrack.reminder.ReminderNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MonitrackApplication : Application() {
    val repository: MonitrackRepository by lazy {
        val db = MonitrackDatabase.get(this)
        MonitrackRepository(
            db.sessionDao(),
            db.measurementDao(),
            db.activityConfigDao(),
            db.activityDao(),
            ProfileStore(this),
        )
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            ReminderNotifier.CHANNEL_ID,
            getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = getString(R.string.reminder_channel_description) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        CoroutineScope(Dispatchers.IO).launch {
            // Seed the Activity scaffold once (unique name index also guards against dupes).
            val activityDao = MonitrackDatabase.get(this@MonitrackApplication).activityDao()
            if (activityDao.count() == 0) activityDao.upsert(Activity.defaults())
            // Re-arm daily nudges after a reboot/app restart.
            DailyNudgeScheduler.scheduleAll(this@MonitrackApplication, repository.getAllConfigs())
        }
    }
}

/** Convenience accessor for the app-wide repository singleton. */
val Context.repository: MonitrackRepository
    get() = (applicationContext as MonitrackApplication).repository
