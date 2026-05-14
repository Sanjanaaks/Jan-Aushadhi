package com.janaushadhi.finder.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.janaushadhi.finder.R

object NotificationHelper {
    const val CHANNEL_ID = "refill_reminders"

    fun createRefillChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Refill reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Medicine refill reminder notifications"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun showRefillNotification(context: Context, medicineName: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to refill $medicineName")
            .setContentText("Visit your nearest Jan-Aushadhi store.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Time to refill $medicineName! Visit your nearest Jan-Aushadhi store."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
