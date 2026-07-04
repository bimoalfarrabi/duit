package com.duit.app.ui.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duit.app.ui.home.formatRupiah
import com.duit.app.ui.theme.Primary
import com.duit.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(viewModel: WalletViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Dompet")
            }
        }
    ) { padding ->
        if (uiState.wallets.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada dompet", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.wallets, key = { it.id }) { wallet ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(wallet.name, style = MaterialTheme.typography.titleMedium)
                                Text(wallet.type.uppercase(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(formatRupiah(wallet.balance),
                                style = MaterialTheme.typography.titleMedium,
                                color = if (wallet.balance >= 0) Primary else MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        AddWalletSheet(
            hasCashWallet = uiState.hasCashWallet,
            onDismiss = { showSheet = false },
            onSave = { name, type, color -> viewModel.createWallet(name, type, color); showSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWalletSheet(
    hasCashWallet: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("bank") }
    val walletTypes = listOf("bank", "ewallet") + if (!hasCashWallet) listOf("cash") else emptyList()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tambah Dompet", style = MaterialTheme.typography.headlineMedium)
            Text("Nama", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(value = name, onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                placeholder = { Text("Contoh: BCA Tabungan") })
            Text("Tipe", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                walletTypes.forEach { t ->
                    FilterChip(selected = type == t, onClick = { type = t },
                        label = { Text(t.uppercase()) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, type, "#10b981") },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Simpan") }
        }
    }
}
