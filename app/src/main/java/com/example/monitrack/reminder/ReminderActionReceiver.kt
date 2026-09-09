package com.example.monitrack.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.monitrack.repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Handles the reminder notification's "Stop now" and "Still going" actions. */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
        val attempt = intent.getIntExtra(EXTRA_ATTEMPT, 0)
        if (sessionId < 0) return

        val appContext = context.applicationContext
        NotificationManagerCompat.from(appContext).cancel(sessionId.toInt())

        when (intent.action) {
            ACTION_STOP -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        appContext.repository.stopSession(
                            sessionId,
                            endTime = System.currentTimeMillis(),
                            manuallyAdjusted = true,
                        )
                        ReminderScheduler.cancel(appContext, sessionId)
                    } finally {
                        pending.finish()
                    }
                }
            }

            ACTION_SNOOZE -> ReminderScheduler.scheduleBackoff(appContext, sessionId, attempt + 1)
        }
    }

    companion object {
        const val ACTION_STOP = "com.example.monitrack.action.STOP"
        const val ACTION_SNOOZE = "com.example.monitrack.action.SNOOZE"
        private const val EXTRA_SESSION_ID = "session_id"
        private const val EXTRA_ATTEMPT = "attempt"

        fun pendingIntent(
            context: Context,
            action: String,
            sessionId: Long,
            attempt: Int,
        ): PendingIntent {
            val intent = Intent(context, ReminderActionReceiver::class.java).apply {
                this.action = action
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_ATTEMPT, attempt)
            }
            // Distinct request code per (session, action) so the two actions don't collide.
            val requestCode = (sessionId.toInt() shl 1) or if (action == ACTION_STOP) 0 else 1
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }
    }
}
