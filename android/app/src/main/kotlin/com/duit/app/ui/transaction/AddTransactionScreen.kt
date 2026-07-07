package com.duit.app.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.duit.app.ui.theme.Danger
import com.duit.app.ui.theme.Primary
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onNavigateToOcr: () -> Unit = {},
    // ponytail: prefill from OCR passed as nullable triple, no extra wrapper class needed
    ocrPrefill: Triple<String, String, String>? = null,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var selectedCategoryId by remember { mutableIntStateOf(0) }
    var selectedWalletId by remember { mutableIntStateOf(0) }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var note by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Apply OCR prefill once when non-null
    LaunchedEffect(ocrPrefill) {
        ocrPrefill?.let { (ocrTitle, ocrAmount, ocrDate) ->
            if (ocrTitle.isNotBlank()) title = ocrTitle
            if (ocrAmount.isNotBlank()) amount = ocrAmount
            if (ocrDate.isNotBlank()) date = ocrDate
        }
    }

    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onBack() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    // Auto-select first wallet/category when loaded
    LaunchedEffect(uiState.wallets) {
        if (selectedWalletId == 0) uiState.wallets.firstOrNull()?.let { selectedWalletId = it.id }
    }
    LaunchedEffect(uiState.categories) {
        if (selectedCategoryId == 0) uiState.categories.firstOrNull()?.let { selectedCategoryId = it.id }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tambah Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToOcr) {
                        Text("Scan Struk")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Judul", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(value = title, onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Contoh: Makan siang") })
            }
            item {
                Text("Nominal", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            item {
                Text("Tipe", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "expense", onClick = { type = "expense" },
                        label = { Text("Pengeluaran") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Danger.copy(alpha = 0.12f),
                            selectedLabelColor = Danger))
                    FilterChip(selected = type == "income", onClick = { type = "income" },
                        label = { Text("Pemasukan") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.12f),
                            selectedLabelColor = Primary))
                }
            }
            item {
                DropdownField(
                    label = "Kategori",
                    options = uiState.categories.map { it.id to it.name },
                    selectedId = selectedCategoryId,
                    onSelect = { selectedCategoryId = it }
                )
            }
            item {
                DropdownField(
                    label = "Dompet",
                    options = uiState.wallets.map { it.id to it.name },
                    selectedId = selectedWalletId,
                    onSelect = { selectedWalletId = it }
                )
            }
            item {
                Text("Tanggal (yyyy-MM-dd)", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(value = date, onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
            item {
                Text("Catatan (opsional)", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(value = note, onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.submit(title, amount, type, selectedCategoryId, selectedWalletId, date, note) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Simpan")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<Pair<Int, String>>,
    selectedId: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.firstOrNull { it.first == selectedId }?.second ?: "Pilih..."
    Text(label, style = MaterialTheme.typography.labelMedium)
    Spacer(Modifier.height(4.dp))
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }
}
