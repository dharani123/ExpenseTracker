package com.example.expensetracker.ui.expenses

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.local.dao.ExpenseWithCategory
import com.example.expensetracker.data.local.entity.CategoryEntity
import com.example.expensetracker.ui.components.AddCategoryDialog
import com.example.expensetracker.ui.components.CategoryDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val groupedExpenses by viewModel.groupedExpenses.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Stable lambdas — created once per ViewModel instance, not on every recomposition
    val onCategorySelected = remember(viewModel) {
        { expenseId: Long, categoryId: Long -> viewModel.updateCategory(expenseId, categoryId) }
    }
    val onAmountEdited = remember(viewModel) {
        { expenseId: Long, newAmount: Double -> viewModel.updateAmount(expenseId, newAmount) }
    }
    val onDeleteCategory = remember(viewModel) {
        { category: com.example.expensetracker.data.local.entity.CategoryEntity -> viewModel.deleteCategory(category) }
    }
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val showOnlyUncategorized by viewModel.showOnlyUncategorized.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.syncSms()
        else permissionDenied = true
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.syncSms()
        else permissionLauncher.launch(Manifest.permission.READ_SMS)
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onConfirm = { viewModel.addCategory(it) },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Expenses") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            actions = {
                IconButton(onClick = { viewModel.toggleUncategorizedFilter() }) {
                    Icon(
                        imageVector = if (showOnlyUncategorized)
                            Icons.Default.FilterAlt
                        else
                            Icons.Default.FilterAltOff,
                        contentDescription = "Filter uncategorized",
                        tint = if (showOnlyUncategorized)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.READ_SMS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) viewModel.syncSms()
                        else permissionLauncher.launch(Manifest.permission.READ_SMS)
                    },
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sync SMS")
                }
            }
        )

        when {
            permissionDenied -> PermissionDeniedMessage(
                onRetry = {
                    permissionDenied = false
                    permissionLauncher.launch(Manifest.permission.READ_SMS)
                }
            )

            isLoading && groupedExpenses.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = "Reading your SMS...",
                            modifier = Modifier.padding(top = 16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Looking for transaction messages",
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            groupedExpenses.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap ↻ to sync SMS",
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Syncing new transactions...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Column header — shown once above the list
                TableHeader()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = groupedExpenses,
                        key = { item ->
                            when (item) {
                                is ExpenseListItem.MonthHeader -> "month_${item.label}"
                                is ExpenseListItem.DateHeader  -> "date_${item.label}"
                                is ExpenseListItem.ExpenseItem -> "expense_${item.expense.id}"
                            }
                        },
                        contentType = { item ->
                            when (item) {
                                is ExpenseListItem.MonthHeader -> 0
                                is ExpenseListItem.DateHeader  -> 1
                                is ExpenseListItem.ExpenseItem -> 2
                            }
                        }
                    ) { item ->
                        when (item) {
                            is ExpenseListItem.MonthHeader -> MonthHeaderItem(item.label)
                            is ExpenseListItem.DateHeader  -> DateHeaderItem(item.label, item.totalAmount)
                            is ExpenseListItem.ExpenseItem -> ExpenseRow(
                                expense = item.expense,
                                categories = categories,
                                onCategorySelected = onCategorySelected,
                                onAddCategoryClick = { showAddCategoryDialog = true },
                                onAmountEdited = onAmountEdited,
                                onDeleteCategory = onDeleteCategory
                            )
                        }
                    }
                }
            }
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
    }
}

@Composable
private fun MonthHeaderItem(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun DateHeaderItem(label: String, totalAmount: Double) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "(${formatAmount(totalAmount)})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Amount",   modifier = Modifier.weight(1f),   fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("To",       modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Category", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SmsBodyDialog(body: String, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Original SMS") },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

@Composable
private fun EditAmountDialog(
    currentAmount: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("%.2f".format(currentAmount)) }
    val isValid = text.toDoubleOrNull()?.let { it > 0 } == true

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Amount") },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Amount (Rs.)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                singleLine = true,
                isError = !isValid
            )
        },
        confirmButton = {
            TextButton(
                onClick = { text.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = isValid
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ExpenseRow(
    expense: ExpenseWithCategory,
    categories: List<CategoryEntity>,
    onCategorySelected: (expenseId: Long, categoryId: Long) -> Unit,
    onAddCategoryClick: () -> Unit,
    onAmountEdited: (expenseId: Long, newAmount: Double) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    var showSmsDialog by remember { mutableStateOf(false) }
    var showEditAmount by remember { mutableStateOf(false) }

    if (showSmsDialog) {
        SmsBodyDialog(
            body = expense.smsBody,
            onDismiss = { showSmsDialog = false }
        )
    }

    if (showEditAmount) {
        EditAmountDialog(
            currentAmount = expense.amount,
            onConfirm = { newAmount ->
                onAmountEdited(expense.id, newAmount)
                showEditAmount = false
            },
            onDismiss = { showEditAmount = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatAmount(expense.amount),
                modifier = Modifier
                    .weight(1f)
                    .clickable { showEditAmount = true },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = expense.merchant,
                modifier = Modifier
                    .weight(1.2f)
                    .padding(horizontal = 4.dp)
                    .clickable { showSmsDialog = true },
                fontSize = 12.sp,
                maxLines = 2
            )
            CategoryDropdown(
                categories = categories,
                selectedCategoryId = expense.categoryId,
                onCategorySelected = { categoryId -> onCategorySelected(expense.id, categoryId) },
                onAddCategoryClick = onAddCategoryClick,
                onDeleteCategory = onDeleteCategory,
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

@Composable
private fun PermissionDeniedMessage(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "SMS permission is required to\nread transaction messages.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onRetry) { Text("Grant Permission") }
        }
    }
}

private fun formatAmount(amount: Double): String {
    val formatted = "%.2f".format(amount)
    val parts = formatted.split(".")
    val intPart = parts[0]
    val decPart = parts[1]
    val result = StringBuilder()
    val reversed = intPart.reversed()
    reversed.forEachIndexed { index, c ->
        if (index == 3 || (index > 3 && (index - 3) % 2 == 0)) result.append(',')
        result.append(c)
    }
    return "Rs. ${result.reverse()}.$decPart"
}
