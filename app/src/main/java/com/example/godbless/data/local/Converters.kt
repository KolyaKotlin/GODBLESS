package com.example.godbless.data.local

import androidx.room.TypeConverter
import com.example.godbless.domain.model.ProductCategory
import com.example.godbless.domain.model.StorageLocation
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromProductCategory(value: ProductCategory): String {
        return value.name
    }

    @TypeConverter
    fun toProductCategory(value: String): ProductCategory {
        return ProductCategory.valueOf(value)
    }

    @TypeConverter
    fun fromStorageLocation(value: StorageLocation): String {
        return value.name
    }

    @TypeConverter
    fun toStorageLocation(value: String): StorageLocation {
        return StorageLocation.valueOf(value)
    }
}
