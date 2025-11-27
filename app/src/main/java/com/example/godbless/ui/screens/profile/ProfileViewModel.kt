package com.example.godbless.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.godbless.data.repository.AuthRepository
import com.example.godbless.data.repository.PreferencesRepository
import com.example.godbless.domain.model.UserPreferences
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferences.collect { preferences ->
                _userPreferences.value = preferences
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            if (!isValidEmail(email)) {
                _error.value = "Неверный формат email"
                _isLoading.value = false
                return@launch
            }

            if (password.length < 6) {
                _error.value = "Пароль должен содержать минимум 6 символов"
                _isLoading.value = false
                return@launch
            }

            val result = authRepository.signIn(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _successMessage.value = "Вы успешно вошли"
            }.onFailure { exception ->
                _error.value = exception.message ?: "Ошибка авторизации"
            }

            _isLoading.value = false
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            if (!isValidEmail(email)) {
                _error.value = "Неверный формат email"
                _isLoading.value = false
                return@launch
            }

            if (password.length < 6) {
                _error.value = "Пароль должен содержать минимум 6 символов"
                _isLoading.value = false
                return@launch
            }

            if (password != confirmPassword) {
                _error.value = "Пароли не совпадают"
                _isLoading.value = false
                return@launch
            }

            val result = authRepository.signUp(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                _successMessage.value = "Регистрация успешна"
            }.onFailure { exception ->
                _error.value = exception.message ?: "Ошибка регистрации"
            }

            _isLoading.value = false
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
    }

    fun updateNotificationPreference(
        notifySevenDays: Boolean? = null,
        notifyThreeDays: Boolean? = null,
        notifyOneDay: Boolean? = null
    ) {
        val current = _userPreferences.value
        val updated = UserPreferences(
            notifySevenDays = notifySevenDays ?: current.notifySevenDays,
            notifyThreeDays = notifyThreeDays ?: current.notifyThreeDays,
            notifyOneDay = notifyOneDay ?: current.notifyOneDay
        )
        preferencesRepository.savePreferences(updated)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
