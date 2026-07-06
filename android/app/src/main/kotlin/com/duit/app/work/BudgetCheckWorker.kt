package com.duit.app.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.duit.app.data.repository.BudgetRepository
import com.duit.app.data.repository.SavingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar

@HiltWorker
class BudgetCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val budgetRepository: BudgetRepository,
    private val savingsRepository: SavingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        // ponytail: skip notification if token missing (user logged out)
        budgetRepository.getBudgets(month, year).onSuccess { budgets ->
            budgets.filter { it.progress >= 0.8f && it.amount > 0 }.forEach { budget ->
                val pct = (budget.progress * 100).toInt()
                NotificationHelper.send(
                    applicationContext,
                    "Budget ${budget.categoryName} hampir habis",
                    "Sudah terpakai $pct% (${formatRupiah(budget.spent)} / ${formatRupiah(budget.amount)})"
                )
            }
        }

        savingsRepository.getSavingsGoals().onSuccess { goals ->
            goals.filter { it.isCompleted }.forEach { goal ->
                NotificationHelper.send(
                    applicationContext,
                    "Target tabungan tercapai! 🎉",
                    "${goal.name}: ${formatRupiah(goal.currentAmount)} terkumpul"
                )
            }
        }

        return Result.success()
    }

    private fun formatRupiah(amount: Double): String =
        "Rp ${"%,.0f".format(amount).replace(",", ".")}"
}
