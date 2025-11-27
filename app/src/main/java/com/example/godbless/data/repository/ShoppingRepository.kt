package com.example.godbless.data.repository

import com.example.godbless.data.local.ShoppingItemDao
import com.example.godbless.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(
    private val shoppingItemDao: ShoppingItemDao
) {

    fun getAllShoppingItems(): Flow<List<ShoppingItem>> = shoppingItemDao.getAllShoppingItems()

    suspend fun getShoppingItemById(id: Long): ShoppingItem? =
        shoppingItemDao.getShoppingItemById(id)

    suspend fun insertShoppingItem(item: ShoppingItem): Long =
        shoppingItemDao.insertShoppingItem(item)

    suspend fun updateShoppingItem(item: ShoppingItem) =
        shoppingItemDao.updateShoppingItem(item)

    suspend fun deleteShoppingItem(item: ShoppingItem) =
        shoppingItemDao.deleteShoppingItem(item)

    suspend fun deleteShoppingItemById(id: Long) =
        shoppingItemDao.deleteShoppingItemById(id)

    suspend fun deletePurchasedItems() =
        shoppingItemDao.deletePurchasedItems()
}
