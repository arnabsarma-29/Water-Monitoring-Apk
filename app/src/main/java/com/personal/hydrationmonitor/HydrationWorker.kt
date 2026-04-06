package com.personal.hydrationmonitor

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class HydrationWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationHelper.CHANNEL_ID
        )
            .setContentTitle("💧 Drink Water")
            .setContentText("Stay hydrated!")
            .setSmallIcon(R.drawable.waterdrop)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "Mark as Done", DoneReceiver.getPendingIntent(applicationContext))
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }
}