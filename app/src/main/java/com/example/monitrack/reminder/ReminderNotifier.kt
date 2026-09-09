package com.example.monitrack.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.monitrack.MainActivity
import com.example.monitrack.R
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.entity.Session
import com.example.monitrack.formatDuration

/** Builds and posts the reminder notifications (overrun and daily nudge). */
object ReminderNotifier {
    const val CHANNEL_ID = "forgot_to_stop"
    private const val NUDGE_ID_BASE = 100_000

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun show(context: Context, session: Session, attempt: Int) {
        val elapsed = System.currentTimeMillis() - session.startTime
        val notificationId = session.id.toInt()

        val openApp = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title, session.type))
            .setContentText(context.getString(R.string.reminder_text, formatDuration(elapsed)))
            .setContentIntent(openApp)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(
                0,
                context.getString(R.string.reminder_action_stop),
                ReminderActionReceiver.pendingIntent(
                    context, ReminderActionReceiver.ACTION_STOP, session.id, attempt,
                ),
            )
            .addAction(
                0,
                context.getString(R.string.reminder_action_still_going),
                ReminderActionReceiver.pendingIntent(
                    context, ReminderActionReceiver.ACTION_SNOOZE, session.id, attempt,
                ),
            )
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDailyNudge(context: Context, config: ActivityConfig, totalMs: Long) {
        val openApp = PendingIntent.getActivity(
            context,
            NUDGE_ID_BASE + config.id,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.nudge_title, config.type))
            .setContentText(
                context.getString(
                    R.string.nudge_text,
                    formatDuration(totalMs),
                    formatDuration(config.targetMs),
                ),
            )
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NUDGE_ID_BASE + config.id, notification)
    }
}
