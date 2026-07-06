package com.duit.app

import android.app.Application
import androidx.work.*
import com.duit.app.work.BudgetCheckWorker
import com.duit.app.work.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class DuitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        scheduleBudgetCheck()
    }

    private fun scheduleBudgetCheck() {
        val request = PeriodicWorkRequestBuilder<BudgetCheckWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints(requiresNetwork = NetworkType.CONNECTED))
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "budget_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}