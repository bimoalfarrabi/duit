package com.duit.app.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duit.app.domain.model.Transaction
import com.duit.app.ui.home.formatRupiah
import com.duit.app.ui.theme.Danger
import com.duit.app.ui.theme.Primary
import com.duit.app.ui.theme.TextMuted
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(viewModel: TransactionListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Int?>(null) }
    var monthExpanded by remember { mutableStateOf(false) }
    var walletExpanded by remember { mutableStateOf(false) }

    deleteTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Hapus Transaksi") },
            text = { Text("Hapus transaksi ini? Aksi ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTransaction(id); deleteTarget = null }) {
                    Text("Hapus", color = Danger)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Batal") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Month picker
            ExposedDropdownMenuBox(expanded = monthExpanded, onExpandedChange = { monthExpanded = it },
                modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = Month.of(uiState.selectedMonth).getDisplayName(TextStyle.SHORT, Locale("id")),
                    onValueChange = {}, readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(monthExpanded) }
                )
                ExposedDropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                    (1..12).forEach { m ->
                        DropdownMenuItem(
                            text = { Text(Month.of(m).getDisplayName(TextStyle.SHORT, Locale("id"))) },
                            onClick = { viewModel.setMonth(m, uiState.selectedYear); monthExpanded = false }
                        )
                    }
                }
            }
            // Wallet filter
            ExposedDropdownMenuBox(expanded = walletExpanded, onExpandedChange = { walletExpanded = it },
                modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = uiState.wallets.firstOrNull { it.id == uiState.selectedWalletId }?.name ?: "Semua",
                    onValueChange = {}, readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(walletExpanded) }
                )
                ExposedDropdownMenu(expanded = walletExpanded, onDismissRequest = { walletExpanded = false }) {
                    DropdownMenuItem(text = { Text("Semua") }, onClick = { viewModel.setWallet(null); walletExpanded = false })
                    uiState.wallets.forEach { w ->
                        DropdownMenuItem(text = { Text(w.name) }, onClick = { viewModel.setWallet(w.id); walletExpanded = false })
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada transaksi", color = TextMuted)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.transactions, key = { it.id }) { tx ->
                    TransactionCard(tx, onDelete = { deleteTarget = tx.id })
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(tx: Transaction, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.titleMedium)
                Text("${tx.category.name} · ${tx.wallet.name} · ${tx.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "${if (tx.type == "income") "+" else "-"}${formatRupiah(tx.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (tx.type == "income") Primary else Danger
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TextMuted)
            }
        }
    }
}
