package com.example.godbless.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
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
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Создаем отдельные каналы для разных периодов с разными звуками
            createChannelWithSound(
                notificationManager,
                CHANNEL_ID_7_DAYS,
                "Уведомления за 7 дней",
                R.raw.notification_7days
            )

            createChannelWithSound(
                notificationManager,
                CHANNEL_ID_3_DAYS,
                "Уведомления за 3 дня",
                R.raw.notification_3days
            )

            createChannelWithSound(
                notificationManager,
                CHANNEL_ID_1_DAY,
                "Уведомления за 1 день",
                R.raw.notification_1day
            )
        }
    }

    private fun createChannelWithSound(
        notificationManager: NotificationManager,
        channelId: String,
        channelName: String,
        soundResId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse("android.resource://${applicationContext.packageName}/$soundResId")
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о продуктах, у которых истекает срок годности"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(productName: String, daysLeft: Int, notificationId: Int) {
        // Выбираем канал и звук в зависимости от количества дней
        val channelId = when (daysLeft) {
            7 -> CHANNEL_ID_7_DAYS
            3 -> CHANNEL_ID_3_DAYS
            1 -> CHANNEL_ID_1_DAY
            else -> CHANNEL_ID_7_DAYS
        }

        val soundResId = when (daysLeft) {
            7 -> R.raw.notification_7days
            3 -> R.raw.notification_3days
            1 -> R.raw.notification_1day
            else -> R.raw.notification_7days
        }

        val soundUri = Uri.parse("android.resource://${applicationContext.packageName}/$soundResId")

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText("$productName - осталось $daysLeft дней")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Для версий ниже Android O устанавливаем звук напрямую
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(soundUri)
        }

        val notification = builder.build()

        try {
            NotificationManagerCompat.from(applicationContext)
                .notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    companion object {
        const val CHANNEL_ID_7_DAYS = "expiry_7_days"
        const val CHANNEL_ID_3_DAYS = "expiry_3_days"
        const val CHANNEL_ID_1_DAY = "expiry_1_day"
        const val WORK_NAME = "expiry_notification_work"
    }
}
