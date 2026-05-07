package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.surface.Card
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.ui.theme.ABLogisticsAccentButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import java.math.BigDecimal

@OptIn(ExperimentalFluentApi::class)
@Composable
fun DirectLoadEntryScreen(viewModel: DirectLoadViewModel = remember { DirectLoadViewModel() }) {
    val selectedWarehouse by SessionStore.selectedWarehouse.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val brvs by viewModel.brvs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    LaunchedEffect(selectedWarehouse) {
        selectedWarehouse?.id?.let { viewModel.loadInitialData(it) }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Direct Drop-Ship Load", style = FluentTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Create linked Inward/Outward receipts for fuel delivery.", style = FluentTheme.typography.body, color = FluentTheme.colors.text.accent.secondary)
        
        Spacer(Modifier.height(24.dp))

        if (error != null) {
            InfoBar(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                title = { Text("Error") },
                severity = InfoBarSeverity.Critical,
                message = { Text(error!!) }
            )
        }
        if (success != null) {
            InfoBar(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                title = { Text("Success") },
                severity = InfoBarSeverity.Success,
                message = { Text(success!!) }
            )
        }

        Row(Modifier.fillMaxSize()) {
            // Form Column
            LazyColumn(Modifier.weight(1.5f).padding(end = 32.dp)) {
                item {
                    // Supplier Selection
                    Text("Supplier (Inward Source)", style = FluentTheme.typography.bodyStrong)
                    Spacer(Modifier.height(8.dp))
                    ComboBox(
                        placeholder = "Select Supplier",
                        items = suppliers.map { it.name },
                        selected = suppliers.indexOf(viewModel.selectedSupplier).takeIf { it >= 0 },
                        onSelectionChange = { i, _ -> viewModel.selectedSupplier = suppliers[i] },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    // Customer Selection
                    Text("Customer (Outward Destination)", style = FluentTheme.typography.bodyStrong)
                    Spacer(Modifier.height(8.dp))
                    CustomerSearch(
                        customers = customers,
                        onSelected = { viewModel.selectedCustomer = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    viewModel.selectedCustomer?.let {
                        Text("Selected: ${it.name} (${it.phoneNumber})", style = FluentTheme.typography.caption, color = FluentTheme.colors.text.accent.secondary)
                    }
                    Spacer(Modifier.height(16.dp))

                    // BRV Selection
                    Text("Tanker (BRV)", style = FluentTheme.typography.bodyStrong)
                    Spacer(Modifier.height(8.dp))
                    ComboBox(
                        placeholder = "Select Tanker",
                        items = brvs.map { "${it.plateNumber} - ${it.driverName ?: "N/A"}" },
                        selected = brvs.indexOf(viewModel.selectedBrv).takeIf { it >= 0 },
                        onSelectionChange = { i, _ -> viewModel.selectedBrv = brvs[i] },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))

                    // Input Fields
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            ABLogisticsTextField(
                                value = viewModel.loadedQuantity,
                                onValueChange = { 
                                    viewModel.loadedQuantity = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    viewModel.updateCalculations()
                                },
                                header = { Text("Loaded Quantity (Liters)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            ABLogisticsTextField(
                                value = viewModel.costPrice,
                                onValueChange = { 
                                    viewModel.costPrice = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    viewModel.updateCalculations()
                                },
                                header = { Text("Cost Price (per Liter)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            ABLogisticsTextField(
                                value = viewModel.sellingPrice,
                                onValueChange = { 
                                    viewModel.sellingPrice = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    viewModel.updateCalculations()
                                },
                                header = { Text("Selling Price (per Liter)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            ABLogisticsTextField(
                                value = viewModel.brvCost,
                                onValueChange = { 
                                    viewModel.brvCost = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    viewModel.updateCalculations()
                                },
                                header = { Text("BRV Delivery Cost") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    ABLogisticsTextField(
                        value = viewModel.description,
                        onValueChange = { viewModel.description = it },
                        header = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }

            // Preview Column
            Column(Modifier.weight(1f).padding(top = 8.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Live Preview", style = FluentTheme.typography.bodyStrong)
                        Spacer(Modifier.height(16.dp))
                        
                        PreviewItem("Supplier Debt", viewModel.loadedQuantity.toBigDecimalOrNull()?.multiply(viewModel.costPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO) ?: BigDecimal.ZERO)
                        PreviewItem("Customer Total", viewModel.projectedRevenue)
                        Divider(Modifier.padding(vertical = 12.dp))
                        Text("Projected Profit", style = FluentTheme.typography.caption)
                        Text("${viewModel.projectedProfit.setScale(2, java.math.RoundingMode.HALF_UP)}", 
                            style = FluentTheme.typography.titleLarge, 
                            color = if (viewModel.projectedProfit >= BigDecimal.ZERO) FluentTheme.colors.system.success else FluentTheme.colors.system.critical
                        )
                        
                        Spacer(Modifier.height(32.dp))
                        
                        ABLogisticsAccentButton(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            disabled = isLoading || viewModel.selectedCustomer == null || viewModel.selectedBrv == null || viewModel.loadedQuantity.isBlank(),
                            onClick = { viewModel.processLoad(selectedWarehouse?.id ?: 0L) }
                        ) {
                            if (isLoading) {
                                ProgressRing(Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Processing...")
                            } else {
                                Text("Process Load")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewItem(label: String, value: BigDecimal) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = FluentTheme.typography.body, color = FluentTheme.colors.text.accent.primary)
        Text("${value.setScale(2, java.math.RoundingMode.HALF_UP)}", style = FluentTheme.typography.body)
    }
}
