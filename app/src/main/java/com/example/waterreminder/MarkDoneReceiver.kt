package com.example.waterreminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MarkDoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val drinkId = intent.getStringExtra("drink_id") ?: return
        val notificationId = intent.getIntExtra("notification_id", 0)

        val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(drinkId, true).apply()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)

        // Optional: Send broadcast to MainActivity to refresh UI if it's open
        context.sendBroadcast(Intent("REFRESH_UI"))
    }
}