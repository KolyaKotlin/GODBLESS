package com.example.godbless.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.delay

// Mock User для симуляции FirebaseUser
data class MockUser(val email: String, val uid: String)

class AuthRepository(context: Context? = null) {

    private val sharedPrefs: SharedPreferences? = context?.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private var _currentUser: MockUser? = null

    init {
        // Восстанавливаем пользователя из SharedPreferences при инициализации
        sharedPrefs?.let {
            val savedEmail = it.getString("user_email", null)
            val savedUid = it.getString("user_uid", null)
            if (savedEmail != null && savedUid != null) {
                _currentUser = MockUser(savedEmail, savedUid)
            }
        }
    }

    val currentUser: MockUser?
        get() = _currentUser

    val isUserLoggedIn: Boolean
        get() = _currentUser != null

    suspend fun signIn(email: String, password: String): Result<MockUser> {
        return try {
            delay(500) // Симуляция сетевого запроса

            // Простая валидация
            if (password.length < 6) {
                throw Exception("Пароль должен содержать минимум 6 символов")
            }

            // Создаем mock пользователя
            val user = MockUser(email = email, uid = email.hashCode().toString())
            _currentUser = user

            // Сохраняем в SharedPreferences
            sharedPrefs?.edit()?.apply {
                putString("user_email", email)
                putString("user_uid", user.uid)
                putString("user_password", password) // В реальном приложении НЕ ХРАНИТЕ пароли так!
                apply()
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<MockUser> {
        return try {
            delay(500) // Симуляция сетевого запроса

            // Простая валидация
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                throw Exception("Неверный формат email")
            }

            if (password.length < 6) {
                throw Exception("Пароль должен содержать минимум 6 символов")
            }

            // Проверяем, не зарегистрирован ли уже такой email
            val existingEmail = sharedPrefs?.getString("user_email", null)
            if (existingEmail == email) {
                throw Exception("Пользователь с таким email уже существует")
            }

            // Создаем нового пользователя
            val user = MockUser(email = email, uid = email.hashCode().toString())
            _currentUser = user

            // Сохраняем в SharedPreferences
            sharedPrefs?.edit()?.apply {
                putString("user_email", email)
                putString("user_uid", user.uid)
                putString("user_password", password)
                apply()
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            delay(500)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        _currentUser = null
        sharedPrefs?.edit()?.clear()?.apply()
    }
}
