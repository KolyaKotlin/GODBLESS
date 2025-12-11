package com.example.godbless.data.repository
import android.content.Context
import android.content.SharedPreferences
import com.example.godbless.domain.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
class PreferencesRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    private val _userPreferences = MutableStateFlow(loadPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences
    private fun loadPreferences(): UserPreferences {
        return UserPreferences(
            notifySevenDays = sharedPreferences.getBoolean(KEY_NOTIFY_7_DAYS, true),
            notifyThreeDays = sharedPreferences.getBoolean(KEY_NOTIFY_3_DAYS, true),
            notifyOneDay = sharedPreferences.getBoolean(KEY_NOTIFY_1_DAY, true)
        )
    }
    fun savePreferences(preferences: UserPreferences) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_NOTIFY_7_DAYS, preferences.notifySevenDays)
            putBoolean(KEY_NOTIFY_3_DAYS, preferences.notifyThreeDays)
            putBoolean(KEY_NOTIFY_1_DAY, preferences.notifyOneDay)
            apply()
        }
        _userPreferences.value = preferences
    }
    companion object {
        private const val KEY_NOTIFY_7_DAYS = "notify_7_days"
        private const val KEY_NOTIFY_3_DAYS = "notify_3_days"
        private const val KEY_NOTIFY_1_DAY = "notify_1_day"
    }
}
