package com.example.waterreminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val message = intent.getStringExtra("message") ?: "Time to drink water!"
        val notificationId = intent.getIntExtra("id", System.currentTimeMillis().toInt())
        val drinkId = intent.getStringExtra("drink_id") ?: ""

        val doneIntent = Intent(context, MarkDoneReceiver::class.java).apply {
            putExtra("drink_id", drinkId)
            putExtra("notification_id", notificationId)
        }

        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("💧 Water Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            // ❌ REMOVE setContentIntent()
            .addAction(
                android.R.drawable.ic_menu_add,
                "Mark as Done",
                donePendingIntent
            )
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
