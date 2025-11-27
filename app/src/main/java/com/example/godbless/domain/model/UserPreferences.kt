package com.example.godbless.domain.model

data class UserPreferences(
    val notifySevenDays: Boolean = true,
    val notifyThreeDays: Boolean = true,
    val notifyOneDay: Boolean = true
)
