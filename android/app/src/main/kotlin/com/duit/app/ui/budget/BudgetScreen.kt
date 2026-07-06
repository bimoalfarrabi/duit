package com.duit.app.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duit.app.ui.home.formatRupiah
import com.duit.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel = hiltViewModel()) {
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
                Icon(Icons.Default.Add, contentDescription = "Tambah Budget")
            }
        }
    ) { padding ->
        if (uiState.budgets.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada budget bulan ini", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.budgets, key = { it.id }) { budget ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(budget.categoryName, style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { viewModel.deleteBudget(budget.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus")
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Terpakai: ${formatRupiah(budget.spent)}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text("Budget: ${formatRupiah(budget.amount)}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { budget.progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (budget.progress >= 1f) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
                            )
                            if (budget.remaining < 0) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Melebihi budget ${formatRupiah(-budget.remaining)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        AddBudgetSheet(
            categories = uiState.categories,
            onDismiss = { showSheet = false },
            onSave = { categoryId, amount ->
                viewModel.saveBudget(categoryId, amount)
                showSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetSheet(
    categories: List<com.duit.app.domain.model.Category>,
    onDismiss: () -> Unit,
    onSave: (Int, Double) -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var amount by remember { mutableStateOf("") }
    // ponytail: simple expand/collapse for category picker, skip full dropdown library
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tambah Budget", style = MaterialTheme.typography.headlineMedium)
            Text("Kategori", style = MaterialTheme.typography.labelMedium)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Pilih kategori",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.filter { it.type == "expense" }.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = { selectedCategoryId = cat.id; expanded = false }
                        )
                    }
                }
            }
            Text("Jumlah Budget", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: 500000") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val id = selectedCategoryId
                    val amt = amount.toDoubleOrNull()
                    if (id != null && amt != null && amt > 0) onSave(id, amt)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = selectedCategoryId != null && amount.toDoubleOrNull() != null
            ) { Text("Simpan") }
        }
    }
}
