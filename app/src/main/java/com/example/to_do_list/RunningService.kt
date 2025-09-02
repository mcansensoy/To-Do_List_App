package com.example.to_do_list

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RunningService: Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> {
                val taskText = intent.getStringExtra("TASK_TEXT") ?: "No task"
                start(taskText)
            }
            Actions.STOP.name -> stopSelf()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun start(taskText: String) {
        val notification = NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Yapılacak Görev")
            .setContentText(taskText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)
    }

    enum class Actions { START, STOP }
}

