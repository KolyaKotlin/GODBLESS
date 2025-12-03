package com.example.godbless.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.example.godbless.domain.model.Store
import java.net.URLEncoder

/**
 * Утилита для открытия приложений магазинов или их веб-сайтов
 */
object StoreIntentHelper {

    /**
     * Открывает магазин с поиском по названию товара.
     * Сначала пытается открыть приложение (если установлено),
     * если не получается - открывает веб-сайт.
     *
     * @param context Контекст приложения
     * @param store Магазин для открытия
     * @param productName Название товара для поиска
     */
    fun openStore(context: Context, store: Store, productName: String) {
        // Сначала пытаемся открыть приложение
        if (store.packageName != null && isAppInstalled(context, store.packageName)) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(store.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                // Если не получилось открыть приложение, откроем сайт
            }
        }

        // Если приложение не установлено или не удалось открыть, открываем сайт
        openWebsite(context, store, productName)
    }

    /**
     * Открывает веб-сайт магазина с поиском по названию товара
     */
    private fun openWebsite(context: Context, store: Store, productName: String) {
        try {
            val encodedProductName = URLEncoder.encode(productName, "UTF-8")
            val url = store.webUrlTemplate.replace("%s", encodedProductName)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Проверяет, установлено ли приложение
     */
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Проверяет, какие магазины установлены на устройстве
     */
    fun getInstalledStores(context: Context, stores: List<Store>): List<Store> {
        return stores.filter { store ->
            store.packageName != null && isAppInstalled(context, store.packageName)
        }
    }
}
