package com.example.godbless.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: String? = null,
    val isPurchased: Boolean = false,
    val category: ProductCategory? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
