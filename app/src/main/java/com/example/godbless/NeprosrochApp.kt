package com.example.godbless

import android.app.Application
import androidx.work.*
import com.example.godbless.data.local.AppDatabase
import com.example.godbless.data.remote.RetrofitClient
import com.example.godbless.data.repository.AuthRepository
import com.example.godbless.data.repository.OpenFoodFactsRepository
import com.example.godbless.data.repository.PreferencesRepository
import com.example.godbless.data.repository.ProductRepository
import com.example.godbless.data.repository.ShoppingRepository
import com.example.godbless.workers.ExpiryNotificationWorker
import java.util.concurrent.TimeUnit

class NeprosrochApp : Application() {

    lateinit var productRepository: ProductRepository
    lateinit var shoppingRepository: ShoppingRepository
    lateinit var authRepository: AuthRepository
    lateinit var preferencesRepository: PreferencesRepository
    lateinit var openFoodFactsRepository: OpenFoodFactsRepository

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize database
        val database = AppDatabase.getDatabase(applicationContext)

        // Initialize repositories
        productRepository = ProductRepository(
            productDao = database.productDao(),
            openFoodFactsApi = RetrofitClient.openFoodFactsApi
        )

        shoppingRepository = ShoppingRepository(
            shoppingItemDao = database.shoppingItemDao()
        )

        authRepository = AuthRepository(applicationContext)

        preferencesRepository = PreferencesRepository(applicationContext)

        openFoodFactsRepository = OpenFoodFactsRepository(
            api = RetrofitClient.openFoodFactsApi
        )

        // Setup WorkManager for notifications
        setupExpiryNotifications()
    }

    private fun setupExpiryNotifications() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val notificationWork = PeriodicWorkRequestBuilder<ExpiryNotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                ExpiryNotificationWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWork
            )
    }

    companion object {
        lateinit var instance: NeprosrochApp
            private set
    }
}
