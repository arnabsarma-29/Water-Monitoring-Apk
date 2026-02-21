package com.example.waterreminder

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {

    // Final-like references (assigned once in onCreate)
    private lateinit var prefs: SharedPreferences
    private lateinit var progressText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var morningCard: CardView
    private lateinit var afternoonCard: CardView
    private lateinit var eveningCard: CardView
    private lateinit var morningCheck: ImageView
    private lateinit var afternoonCheck: ImageView
    private lateinit var eveningCheck: ImageView
    private lateinit var todayText: TextView
    private lateinit var goalText: TextView
    private lateinit var percentText: TextView

    companion object {
        const val CHANNEL_ID = "water_reminder"
        const val PREFS_NAME = "WaterReminder"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initViews()
        checkNotificationPermission()
        checkAlarmPermission()
        createNotificationChannel()
        checkDailyReset()
        scheduleNotifications()
        updateUI()
        setupClickListeners()
    }

    private fun initViews() {
        progressText = findViewById(R.id.progressText)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        morningCard = findViewById(R.id.morningCard)
        afternoonCard = findViewById(R.id.afternoonCard)
        eveningCard = findViewById(R.id.eveningCard)
        morningCheck = findViewById(R.id.morningCheck)
        afternoonCheck = findViewById(R.id.afternoonCheck)
        eveningCheck = findViewById(R.id.eveningCheck)
        todayText = findViewById(R.id.todayText)
        goalText = findViewById(R.id.goalText)
        percentText = findViewById(R.id.percentText)

        findViewById<ImageButton>(R.id.resetButton).setOnClickListener {
            resetProgress()
        }
    }

    private fun updateUI() {
        val m = prefs.getBoolean("morning", false)
        val a = prefs.getBoolean("afternoon", false)
        val e = prefs.getBoolean("evening", false)

        updateCard(morningCheck, morningCard, m)
        updateCard(afternoonCheck, afternoonCard, a)
        updateCard(eveningCheck, eveningCard, e)

        val completed = listOf(m, a, e).count { it }
        val progress = (completed * 100) / 3

        progressText.text = "$completed/3"
        progressBar.progress = progress
        todayText.text = "${completed}L"
        percentText.text = "$progress%"
        statusText.text = if (completed == 3) "🎉 Daily goal achieved!" else "${3 - completed} more to go today!"
    }

    private fun updateCard(checkView: ImageView, cardView: CardView, done: Boolean) {
        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        cardView.setCardBackgroundColor(typedValue.data)

        if (done) {
            checkView.setImageDrawable(null)
            checkView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            cardView.alpha = 1f
        } else {
            checkView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            checkView.setImageResource(android.R.drawable.checkbox_off_background)
            cardView.alpha = 0.8f
        }
    }

    private fun setupClickListeners() {
        morningCard.setOnClickListener { toggleDrink("morning") }
        afternoonCard.setOnClickListener { toggleDrink("afternoon") }
        eveningCard.setOnClickListener { toggleDrink("evening") }
    }

    private fun toggleDrink(id: String) {
        prefs.edit().putBoolean(id, !prefs.getBoolean(id, false)).apply()
        updateUI()
    }

    private fun scheduleNotifications() {
        scheduleNotification(7, 0, "morning", "Morning reminder - Drink 1L!")
        scheduleNotification(12, 0, "afternoon", "Afternoon reminder - Drink 1L!")
        scheduleNotification(19, 0, "evening", "Evening reminder - Drink 1L!")
    }

    private fun scheduleNotification(hour: Int, minute: Int, id: String, message: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("message", message)
            putExtra("id", id.hashCode())
            putExtra("drink_id", id)
        }

        // Use FLAG_MUTABLE to ensure Extras (message) are attached to the Intent
        val pendingIntent = PendingIntent.getBroadcast(
            this, id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Water Reminders", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }

    private fun checkDailyReset() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        if (prefs.getInt("lastReset", -1) != today) resetProgress()
    }

    private fun resetProgress() {
        prefs.edit()
            .putBoolean("morning", false)
            .putBoolean("afternoon", false)
            .putBoolean("evening", false)
            .putInt("lastReset", Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
            .apply()
        updateUI()
    }
}