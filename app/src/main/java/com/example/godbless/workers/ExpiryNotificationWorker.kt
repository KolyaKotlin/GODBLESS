package com.example.godbless.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.godbless.NeprosrochApp
import com.example.godbless.R
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date

class ExpiryNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as NeprosrochApp
        val productRepository = app.productRepository
        val preferencesRepository = app.preferencesRepository

        val preferences = preferencesRepository.userPreferences.first()
        val products = productRepository.getAllProducts().first()

        createNotificationChannel()

        val now = Date()
        val calendar = Calendar.getInstance()

        products.forEach { product ->
            val daysUntilExpiry = product.getDaysUntilExpiry()

            val shouldNotify = when (daysUntilExpiry) {
                7 -> preferences.notifySevenDays
                3 -> preferences.notifyThreeDays
                1 -> preferences.notifyOneDay
                else -> false
            }

            if (shouldNotify) {
                sendNotification(product.name, daysUntilExpiry, product.id.toInt())
            }
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Уведомления о сроке годности"
            val descriptionText = "Уведомления о продуктах, у которых истекает срок годности"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(productName: String, daysLeft: Int, notificationId: Int) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText("$productName - осталось $daysLeft дней")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext)
                .notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    companion object {
        const val CHANNEL_ID = "expiry_notifications"
        const val WORK_NAME = "expiry_notification_work"
    }
}
