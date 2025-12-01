package com.example.godbless.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.godbless.data.SettingsManager
import kotlin.random.Random

object NorthKoreanNotificationManager {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isActive = false

    // МЕГА список сообщений
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
        "🏔️ 백두의 혈통!",
        "🇵🇸 팔레스타인 해방!",
        "🇮🇱 제국주의 반대!",
        "🇵🇸🇮🇱 세계평화!",
        "☭ 사회주의 만세!",
        "🔥 혁명정신으로!",
        "⚡ 조선의 힘!",
        "💥 반제국주의!",
        "🌍 세계혁명!",
        "✊ 인민의 승리!",
        "🎖️ 영웅조선!",
        "🏆 승리의 조선!",
        "👑 위대한 조선!",
        "🌅 밝은 미래!"
    )

    val flagCombinations = listOf(
        "🇰🇵",
        "🇰🇵🇰🇵",
        "🇰🇵🇵🇸",
        "🇰🇵🇮🇱",
        "🇵🇸🇮🇱",
        "🇰🇵🇵🇸🇮🇱",
        "🇰🇵🇰🇵🇰🇵",
        "🇵🇸🇵🇸",
        "🇮🇱🇮🇱",
        "🇰🇵☭",
        "☭🇰🇵☭",
        "🇰🇵🔥",
        "⭐🇰🇵⭐"
    )

    fun startNotifications(context: Context, onNotification: (String, String) -> Unit) {
        if (isActive) return

        val settingsManager = SettingsManager(context)
        if (settingsManager.getLanguage() != SettingsManager.LANG_NORTH_KOREAN) {
            return
        }

        isActive = true
        handler = Handler(Looper.getMainLooper())

        scheduleNextNotification(onNotification)
    }

    private fun scheduleNextNotification(onNotification: (String, String) -> Unit) {
        if (!isActive) return

        val delay = Random.nextLong(5000, 15000) // 5-15 секунд

        runnable = Runnable {
            if (isActive) {
                val flags = flagCombinations.random()
                val message = messages.random()
                onNotification(flags, message)

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

// БЕЗУМНОЕ МИГАЮЩЕЕ уведомление с флагами и анимациями!!!
@Composable
fun NorthKoreanNotificationOverlay(
    context: Context,
    flags: String?,
    message: String?,
    onDismiss: () -> Unit
) {
    if (flags == null || message == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "nk_notification")

    // МИГАНИЕ
    val blink by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    // ВРАЩЕНИЕ
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // ПУЛЬСАЦИЯ
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // ЦВЕТНАЯ МИГАЛКА
    val colorBlink by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_blink"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ОГРОМНЫЕ ФЛАГИ СВЕРХУ
            Text(
                text = flags,
                fontSize = 120.sp,
                modifier = Modifier
                    .scale(scale)
                    .rotate(rotation)
                    .alpha(blink),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // МИГАЮЩАЯ КАРТОЧКА С СООБЩЕНИЕМ
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .scale(scale)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(
                        width = 6.dp,
                        brush = Brush.linearGradient(
                            colors = if (colorBlink > 0.5f) {
                                listOf(Color.Red, Color.Yellow, Color.Red)
                            } else {
                                listOf(Color.Yellow, Color.Red, Color.Yellow)
                            }
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (colorBlink > 0.5f) Color.Red else Color(0xFFFF0000)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Red,
                                    Color(0xFFCC0000),
                                    Color.Red
                                )
                            )
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .alpha(blink)
                            .rotate(rotation * 0.5f),
                        lineHeight = 48.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ОГРОМНЫЕ ФЛАГИ СНИЗУ (ДРУГИЕ)
            Text(
                text = NorthKoreanNotificationManager.flagCombinations.random(),
                fontSize = 100.sp,
                modifier = Modifier
                    .scale(scale * 1.1f)
                    .rotate(-rotation)
                    .alpha(blink),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // МИГАЮЩИЕ ЗВЕЗДЫ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(5) { index ->
                    Text(
                        text = "⭐",
                        fontSize = 60.sp,
                        modifier = Modifier
                            .alpha(if (index % 2 == 0) blink else 1f - blink)
                            .scale(if (index == 2) scale * 1.2f else scale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ДОПОЛНИТЕЛЬНЫЕ ФЛАГИ В РЯД
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🇰🇵",
                    fontSize = 80.sp,
                    modifier = Modifier
                        .alpha(blink)
                        .scale(scale)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "🇵🇸",
                    fontSize = 80.sp,
                    modifier = Modifier
                        .alpha(1f - blink)
                        .scale(scale * 1.1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "🇮🇱",
                    fontSize = 80.sp,
                    modifier = Modifier
                        .alpha(blink)
                        .scale(scale)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // КНОПКА ЗАКРЫТИЯ (МИГАЕТ)
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .scale(scale)
                    .alpha(blink),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colorBlink > 0.5f) Color.Yellow else Color.White,
                    contentColor = Color.Red
                )
            ) {
                Text(
                    text = "[ × ]",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
