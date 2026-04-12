package com.example.expensetracker.ui.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.local.dao.CategoryTotal
import com.example.expensetracker.ui.theme.CategoryColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val monthSelection by viewModel.monthSelection.collectAsStateWithLifecycle()
    val monthlyCategoryTotals by viewModel.monthlyCategoryTotals.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val weekSelection by viewModel.weekSelection.collectAsStateWithLifecycle()
    val weeklyCategoryTotals by viewModel.weeklyCategoryTotals.collectAsStateWithLifecycle()
    val weeklyTotal by viewModel.weeklyTotal.collectAsStateWithLifecycle()
    val daySelection by viewModel.daySelection.collectAsStateWithLifecycle()
    val dailyCategoryTotals by viewModel.dailyCategoryTotals.collectAsStateWithLifecycle()
    val dailyTotal by viewModel.dailyTotal.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Analysis") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == AnalysisTab.MONTHLY,
                onClick = { viewModel.selectTab(AnalysisTab.MONTHLY) },
                text = { Text("Monthly") }
            )
            Tab(
                selected = selectedTab == AnalysisTab.WEEKLY,
                onClick = { viewModel.selectTab(AnalysisTab.WEEKLY) },
                text = { Text("Weekly") }
            )
            Tab(
                selected = selectedTab == AnalysisTab.DAILY,
                onClick = { viewModel.selectTab(AnalysisTab.DAILY) },
                text = { Text("Daily") }
            )
        }

        when (selectedTab) {
            AnalysisTab.MONTHLY -> {
                val label = remember(monthSelection) {
                    val cal = Calendar.getInstance().apply {
                        set(monthSelection.year, monthSelection.month, 1)
                    }
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                }
                PeriodAnalysisContent(
                    periodLabel = label,
                    categoryTotals = monthlyCategoryTotals,
                    total = monthlyTotal,
                    onPrevious = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() }
                )
            }
            AnalysisTab.WEEKLY -> {
                val label = remember(weekSelection) {
                    val startCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, weekSelection.year)
                        set(Calendar.WEEK_OF_YEAR, weekSelection.week)
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    }
                    val endCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, weekSelection.year)
                        set(Calendar.WEEK_OF_YEAR, weekSelection.week)
                        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        add(Calendar.DAY_OF_WEEK, 6)
                    }
                    val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                    "${fmt.format(startCal.time)} – ${fmt.format(endCal.time)}"
                }
                PeriodAnalysisContent(
                    periodLabel = label,
                    categoryTotals = weeklyCategoryTotals,
                    total = weeklyTotal,
                    onPrevious = { viewModel.previousWeek() },
                    onNext = { viewModel.nextWeek() }
                )
            }
            AnalysisTab.DAILY -> {
                val label = remember(daySelection) {
                    val cal = Calendar.getInstance().apply {
                        set(daySelection.year, daySelection.month, daySelection.day)
                    }
                    SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(cal.time)
                }
                PeriodAnalysisContent(
                    periodLabel = label,
                    categoryTotals = dailyCategoryTotals,
                    total = dailyTotal,
                    onPrevious = { viewModel.previousDay() },
                    onNext = { viewModel.nextDay() }
                )
            }
        }
    }
}

@Composable
private fun PeriodAnalysisContent(
    periodLabel: String,
    categoryTotals: List<CategoryTotal>,
    total: Double,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Period selector (fixed, not scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
            }
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Spent",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formatAmount(total),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (categoryTotals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No categorised expenses for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    Text(
                        text = "By Category",
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(categoryTotals.size) { index ->
                    CategoryRow(item = categoryTotals[index], total = total, colorIndex = categoryTotals[index].colorIndex)
                }
                item { Spacer(modifier = Modifier.size(16.dp)) }
            }
        }
    }
}


@Composable
private fun CategoryRow(item: CategoryTotal, total: Double, colorIndex: Int) {
    val fraction = if (total > 0) (item.total / total).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = (fraction * 100).toInt()
    val sliceColor = CategoryColors[colorIndex % CategoryColors.size]

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color = sliceColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.categoryName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatAmount(item.total),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "  $percentage%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = sliceColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun remember(key: Any, calculation: () -> String): String {
    return androidx.compose.runtime.remember(key) { calculation() }
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
