package com.example.godbless.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val category: ProductCategory,
    val storageLocation: StorageLocation,
    val expiryDate: Date,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    fun getDaysUntilExpiry(): Int {
        // Обнуляем время для корректного подсчета полных дней
        val nowCal = java.util.Calendar.getInstance().apply {
            time = Date()
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val expiryCal = java.util.Calendar.getInstance().apply {
            time = expiryDate
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val diff = expiryCal.timeInMillis - nowCal.timeInMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun isExpired(): Boolean = getDaysUntilExpiry() < 0

    fun isExpiringSoon(): Boolean {
        val days = getDaysUntilExpiry()
        return days in 0..7
    }

    fun getStatusColor(): ProductStatus {
        return when {
            isExpired() -> ProductStatus.EXPIRED
            getDaysUntilExpiry() <= 3 -> ProductStatus.WARNING
            else -> ProductStatus.GOOD
        }
    }
}

enum class ProductCategory(val displayName: String) {
    DAIRY("Молочные продукты"),
    MEAT("Мясо и птица"),
    FISH("Рыба и морепродукты"),
    VEGETABLES("Овощи"),
    FRUITS("Фрукты"),
    BAKERY("Хлебобулочные изделия"),
    FROZEN("Замороженные продукты"),
    CANNED("Консервы"),
    BEVERAGES("Напитки"),
    OTHER("Другое")
}

enum class StorageLocation(val displayName: String) {
    FRIDGE("Холодильник"),
    FREEZER("Морозильник"),
    PANTRY("Кладовая"),
    COUNTER("На столе")
}

enum class ProductStatus {
    GOOD,      // Зеленый
    WARNING,   // Желтый
    EXPIRED    // Красный
}
