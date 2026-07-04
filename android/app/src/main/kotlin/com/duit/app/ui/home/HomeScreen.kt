package com.duit.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duit.app.domain.model.Summary
import com.duit.app.domain.model.Transaction
import com.duit.app.ui.theme.Danger
import com.duit.app.ui.theme.Primary
import com.duit.app.ui.theme.TextMuted
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.load() }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Duit", style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold)
                Text("Bulan ini", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }

            item {
                uiState.summary?.let { SummaryCards(it) }
                    ?: if (!uiState.isLoading) {
                        Text("Gagal memuat ringkasan", color = Danger)
                    }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text("Transaksi Terbaru", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            if (uiState.recentTransactions.isEmpty() && !uiState.isLoading) {
                item { EmptyTransactions() }
            } else {
                items(uiState.recentTransactions) { tx ->
                    TransactionItem(tx)
                }
            }
        }
    }
}

@Composable
private fun SummaryCards(summary: Summary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard("Pemasukan", summary.income, Primary, Modifier.weight(1f))
        SummaryCard("Pengeluaran", summary.expense, Danger, Modifier.weight(1f))
        SummaryCard(
            "Saldo", summary.balance,
            if (summary.balance >= 0) Primary else Danger,
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(label: String, amount: Double, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                formatRupiah(amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun TransactionItem(tx: Transaction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${tx.category.name} · ${tx.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${if (tx.type == "income") "+" else "-"}${formatRupiah(tx.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (tx.type == "income") Primary else Danger
            )
        }
    }
}

@Composable
private fun EmptyTransactions() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Person, contentDescription = null,
            modifier = Modifier.size(48.dp), tint = TextMuted)
        Spacer(Modifier.height(8.dp))
        Text("Belum ada transaksi", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Tambah transaksi pertamamu", style = MaterialTheme.typography.bodyMedium,
            color = TextMuted)
    }
}

fun formatRupiah(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${fmt.format(amount)}"
}
