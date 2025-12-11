package com.example.godbless.data.local
import androidx.room.*
import com.example.godbless.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow
@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, createdAt DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>
    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getShoppingItemById(id: Long): ShoppingItem?
    @Query("SELECT * FROM shopping_items WHERE LOWER(name) = LOWER(:name) AND isPurchased = 0 LIMIT 1")
    suspend fun getShoppingItemByName(name: String): ShoppingItem?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem): Long
    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)
    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)
    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteShoppingItemById(id: Long)
    @Query("DELETE FROM shopping_items WHERE isPurchased = 1")
    suspend fun deletePurchasedItems()
}
