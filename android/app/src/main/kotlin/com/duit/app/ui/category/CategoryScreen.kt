package com.duit.app.ui.category

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
import com.duit.app.ui.theme.Danger
import com.duit.app.ui.theme.Primary
import com.duit.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(viewModel: CategoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    val income = uiState.categories.filter { it.type == "income" }
    val expense = uiState.categories.filter { it.type == "expense" }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kategori")
            }
        }
    ) { padding ->
        if (uiState.categories.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada kategori", color = TextMuted)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (income.isNotEmpty()) {
                    item { Text("Pemasukan", style = MaterialTheme.typography.labelMedium, color = Primary,
                        modifier = Modifier.padding(vertical = 8.dp)) }
                    items(income, key = { it.id }) { cat ->
                        ListItem(headlineContent = { Text(cat.name) },
                            supportingContent = { Text(cat.icon) })
                        HorizontalDivider()
                    }
                }
                if (expense.isNotEmpty()) {
                    item { Text("Pengeluaran", style = MaterialTheme.typography.labelMedium, color = Danger,
                        modifier = Modifier.padding(vertical = 8.dp)) }
                    items(expense, key = { it.id }) { cat ->
                        ListItem(headlineContent = { Text(cat.name) },
                            supportingContent = { Text(cat.icon) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showSheet) {
        AddCategorySheet(
            onDismiss = { showSheet = false },
            onSave = { name, type, icon -> viewModel.createCategory(name, type, "#10b981", icon); showSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategorySheet(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var icon by remember { mutableStateOf("💰") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tambah Kategori", style = MaterialTheme.typography.headlineMedium)
            Text("Nama", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(value = name, onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Text("Tipe", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = type == "expense", onClick = { type = "expense" },
                    label = { Text("Pengeluaran") })
                FilterChip(selected = type == "income", onClick = { type = "income" },
                    label = { Text("Pemasukan") })
            }
            Text("Icon (emoji)", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(value = icon, onValueChange = { icon = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, type, icon) },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Simpan") }
        }
    }
}
