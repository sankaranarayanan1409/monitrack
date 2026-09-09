package com.example.monitrack.reminder

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.monitrack.repository

/** Posts the reminder if the session is still running when the worker fires. */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val sessionId = inputData.getLong(KEY_SESSION_ID, -1L)
        val attempt = inputData.getInt(KEY_ATTEMPT, 0)
        if (sessionId < 0) return Result.failure()

        val session = applicationContext.repository.getSession(sessionId)
            ?: return Result.success()
        if (!session.isRunning) return Result.success()

        ReminderNotifier.show(applicationContext, session, attempt)
        return Result.success()
    }
}
