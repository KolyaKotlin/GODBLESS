package com.example.godbless.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.godbless.data.SettingsManager
import kotlin.random.Random

object NorthKoreanNotificationManager {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isActive = false

    // Список сообщений на корейском для уведомлений
    private val messages = listOf(
        "🇰🇵 위대한 수령님께 영광을!",
        "🇰🇵 조선민주주의인민공화국 만세!",
        "🇰🇵 주체사상이 승리한다!",
        "🇰🇵 김일성 동지 만세!",
        "🇰🇵 김정일 동지 만세!",
        "🇰🇵 김정은 동지 만세!",
        "🇰🇵 조선로동당 만세!",
        "🇰🇵 사회주의 조국 수호!",
        "🇰🇵 백두산 정기 만세!",
        "🇰🇵 천리마 정신으로!",
        "🇰🇵 붉은기 정신 만세!",
        "🇰🇵 인민의 영원한 태양!",
        "⭐ 영광스러운 조선!",
        "⭐ 자주독립국가 만세!",
        "🚀 우주강국 조선!",
        "💪 강성대국 건설!",
        "🌟 주체조선의 힘!",
        "🏔️ 백두의 혈통!"
    )

    private val emojis = listOf(
        "🇰🇵", "⭐", "🚀", "💪", "🌟", "🏔️", "🔥", "✨",
        "🎖️", "🎗️", "🏆", "👑", "⚡", "💥", "🌅", "🎆"
    )

    fun startNotifications(context: Context, onNotification: (String) -> Unit) {
        if (isActive) return

        val settingsManager = SettingsManager(context)
        if (settingsManager.getLanguage() != SettingsManager.LANG_NORTH_KOREAN) {
            return
        }

        isActive = true
        handler = Handler(Looper.getMainLooper())

        scheduleNextNotification(onNotification)
    }

    private fun scheduleNextNotification(onNotification: (String) -> Unit) {
        if (!isActive) return

        val delay = Random.nextLong(5000, 15000) // 5-15 секунд

        runnable = Runnable {
            if (isActive) {
                val emoji = emojis.random()
                val message = messages.random()
                onNotification("$emoji $message")

                scheduleNextNotification(onNotification)
            }
        }

        handler?.postDelayed(runnable!!, delay)
    }

    fun stopNotifications() {
        isActive = false
        runnable?.let { handler?.removeCallbacks(it) }
        handler = null
        runnable = null
    }

    fun isNorthKoreanLanguageSelected(context: Context): Boolean {
        val settingsManager = SettingsManager(context)
        return settingsManager.getLanguage() == SettingsManager.LANG_NORTH_KOREAN
    }
}

// Composable для отображения уведомления
@Composable
fun NorthKoreanNotificationOverlay(
    context: Context,
    message: String?,
    onDismiss: () -> Unit
) {
    message?.let {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(32.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
