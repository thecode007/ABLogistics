package org.safieddine.ablogistics.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.service.ReceiptService
import java.util.Locale

@Composable
fun DashboardScreen() {
    var totalProfit by remember { mutableStateOf(0.0) }
    var expectedRevenue by remember { mutableStateOf(0.0) }
    var totalQuantityLoaded by remember { mutableStateOf(0.0) }
    var customerDebt by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            try {
                // Fetch KPIs & Profit
                val profitRes = ReceiptService.getProfitAnalysis()
                if (profitRes.isSuccess) {
                    val data = profitRes.getOrNull()?.data
                    totalProfit = data?.totalProfit?.toDouble() ?: 0.0
                    expectedRevenue = data?.expectedRevenue?.toDouble() ?: 0.0
                    totalQuantityLoaded = data?.totalQuantityLoaded?.toDouble() ?: 0.0
                } else {
                    errorMessage = profitRes.exceptionOrNull()?.message ?: "Failed to fetch profit analysis"
                }

                // Fetch Debt Summary
                val debtRes = ReceiptService.getDebtSummary()
                if (debtRes.isSuccess) {
                    val data = debtRes.getOrNull()?.data
                    customerDebt = data?.customerDebt?.toDouble() ?: 0.0
                } else {
                    if (errorMessage == null) {
                        errorMessage = debtRes.exceptionOrNull()?.message ?: "Failed to fetch debt summary"
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DashboardCard("Total Profit", totalProfit)
                DashboardCard("Expected Revenue", expectedRevenue)
                DashboardCard("Total Quantity Loaded", totalQuantityLoaded)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                DashboardCard("Customer Debt", kotlin.math.abs(customerDebt))
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: Double) {
    Card(
        modifier = Modifier.padding(8.dp).width(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.2f", value),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}