package com.duit.app.ui.savings

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
fun SavingsScreen(viewModel: SavingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var topupGoalId by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Target")
            }
        }
    ) { padding ->
        if (uiState.goals.isEmpty() && !uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada target tabungan", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.goals, key = { it.id }) { goal ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(goal.name, style = MaterialTheme.typography.titleMedium)
                                    if (goal.deadline != null) {
                                        Text("Target: ${goal.deadline}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                }
                                if (goal.isCompleted) {
                                    AssistChip(onClick = {}, label = { Text("✓ Tercapai") })
                                }
                                IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus")
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formatRupiah(goal.currentAmount), style = MaterialTheme.typography.bodyMedium)
                                Text(formatRupiah(goal.targetAmount), style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { goal.progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (goal.isCompleted) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.primary
                            )
                            if (!goal.isCompleted) {
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { topupGoalId = goal.id },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Topup") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddSavingsSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, target, deadline ->
                viewModel.createGoal(name, target, deadline)
                showAddSheet = false
            }
        )
    }

    topupGoalId?.let { goalId ->
        val goal = uiState.goals.firstOrNull { it.id == goalId }
        if (goal != null) {
            TopupSheet(
                goal = goal,
                onDismiss = { topupGoalId = null },
                onSave = { newAmount ->
                    viewModel.topup(goalId, newAmount)
                    topupGoalId = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsSheet(
    onDismiss: () -> Unit,
    onSave: (String, Double, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Target Tabungan Baru", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(value = name, onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Nama target") }, singleLine = true)
            OutlinedTextField(
                value = target, onValueChange = { target = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Jumlah target (Rp)") }, singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            OutlinedTextField(value = deadline, onValueChange = { deadline = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Deadline (opsional, YYYY-MM-DD)") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val amt = target.toDoubleOrNull()
                    if (name.isNotBlank() && amt != null && amt > 0)
                        onSave(name, amt, deadline.takeIf { it.isNotBlank() })
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = name.isNotBlank() && target.toDoubleOrNull() != null
            ) { Text("Simpan") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopupSheet(
    goal: com.duit.app.domain.model.SavingsGoal,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var topupAmount by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Topup: ${goal.name}", style = MaterialTheme.typography.headlineMedium)
            Text("Terkumpul: ${formatRupiah(goal.currentAmount)} / ${formatRupiah(goal.targetAmount)}", color = TextMuted)
            OutlinedTextField(
                value = topupAmount, onValueChange = { topupAmount = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Jumlah topup (Rp)") }, singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val add = topupAmount.toDoubleOrNull()
                    if (add != null && add > 0) onSave(goal.currentAmount + add)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = topupAmount.toDoubleOrNull() != null
            ) { Text("Tambah") }
        }
    }
}
