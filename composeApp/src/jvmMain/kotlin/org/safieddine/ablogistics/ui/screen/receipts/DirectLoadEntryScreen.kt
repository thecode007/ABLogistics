package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.Receipt
import io.github.composefluent.icons.regular.Add
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.LockClosed
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.ui.theme.ABLogisticsAccentButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFluentApi::class)
@Composable
fun DirectLoadEntryScreen(viewModel: DirectLoadViewModel = remember { DirectLoadViewModel() }) {
    val selectedWarehouse by SessionStore.selectedWarehouse.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val brvs by viewModel.brvs.collectAsState()
    val loads by viewModel.loads.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFinalizing by viewModel.isFinalizing.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var finalizingLoad by remember { mutableStateOf<ReceiptResponse?>(null) }

    LaunchedEffect(selectedWarehouse) {
        selectedWarehouse?.id?.let { viewModel.loadInitialData(it) }
    }

    Column(Modifier.fillMaxSize().background(FluentTheme.colors.background.mica.base)) {
        // Header Bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Load Management", style = FluentTheme.typography.titleLarge)
                Text(
                    "Monitor and finalize fuel delivery orders.",
                    style = FluentTheme.typography.caption,
                    color = FluentTheme.colors.text.accent.secondary
                )
            }
            
            ABLogisticsAccentButton(onClick = { viewModel.fetchLoads(selectedWarehouse?.id ?: 0L) }) {
                Icon(Icons.Default.ArrowCounterclockwise, contentDescription = "Refresh")
                Spacer(Modifier.width(8.dp))
                Text("Refresh")
            }
            
            Spacer(Modifier.width(12.dp))
            
            ABLogisticsAccentButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Load")
                Spacer(Modifier.width(8.dp))
                Text("New Load")
            }
        }

        // Notifications
        if (error != null || success != null) {
            Box(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                if (error != null) {
                    InfoBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { Text("Error") },
                        severity = InfoBarSeverity.Critical,
                        message = { Text(error!!) }
                    )
                }
                if (success != null) {
                    InfoBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { Text("Success") },
                        severity = InfoBarSeverity.Success,
                        message = { Text(success!!) }
                    )
                }
            }
        }

        // Table Content
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp)
                .background(FluentTheme.colors.background.mica.base)
        ) {
            if (isLoading && loads.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ProgressRing()
                }
            } else if (loads.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Receipt,
                            contentDescription = "No loads",
                            modifier = Modifier.size(64.dp),
                            tint = FluentTheme.colors.text.accent.secondary.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No load orders found for this warehouse.", color = FluentTheme.colors.text.accent.secondary)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().weight(1f)
                        .background(FluentTheme.colors.background.layer.default, shape = FluentTheme.shapes.control)
                ) {
                    stickyHeader {
                        Row(
                            Modifier.fillMaxWidth().background(FluentTheme.colors.background.layer.alt).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableHeader("Date", Modifier.weight(1.2f))
                            TableHeader("BRV", Modifier.weight(1f))
                            TableHeader("Customer", Modifier.weight(1.5f))
                            TableHeader("Loaded", Modifier.weight(1f))
                            TableHeader("Dispatched", Modifier.weight(1f))
                            TableHeader("Status", Modifier.weight(1f))
                            TableHeader("Actions", Modifier.weight(1f))
                        }
                        HorizontalDivider(thickness = 1.dp, color = FluentTheme.colors.stroke.control.secondary)
                    }

                    items(loads) { load ->
                        LoadRow(
                            load = load,
                            onFinalize = { finalizingLoad = it }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = FluentTheme.colors.stroke.control.secondary)
                    }
                }
            }
        }
    }

    // Add Load Dialog
    if (showAddDialog) {
        ContentDialog(
            title = "Process Direct Load",
            visible = true,
            size = DialogSize.Max,
            primaryButtonText = if (isLoading) "Processing..." else "Process Load",
            closeButtonText = "Cancel",
            onButtonClick = { btn ->
                if (btn == ContentDialogButton.Primary) {
                    if (!isLoading && viewModel.selectedCustomer != null && viewModel.selectedBrv != null) {
                        viewModel.processLoad(selectedWarehouse?.id ?: 0L)
                    }
                } else {
                    showAddDialog = false
                }
            },
            content = {
                LoadEntryForm(viewModel, customers, brvs)
            }
        )
    }
    
    // Auto-close dialog on success
    LaunchedEffect(success) {
        if (success != null) {
            showAddDialog = false
            finalizingLoad = null
        }
    }

    // Finalize Dialog
    finalizingLoad?.let { load ->
        DeliveryFinalizationDialog(
            receipt = load,
            onDismiss = { finalizingLoad = null },
            onConfirm = { qty ->
                viewModel.finalizeLoad(selectedWarehouse?.id ?: 0L, load.id, qty)
            },
            isLoading = isFinalizing
        )
    }
}

@Composable
fun TableHeader(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = FluentTheme.typography.bodyStrong.copy(fontSize = 13.sp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun LoadRow(load: ReceiptResponse, onFinalize: (ReceiptResponse) -> Unit) {
    val isFinalized = load.dispatchedQuantity != null && load.dispatchedQuantity != BigDecimal.ZERO
    
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            formatDate(load.createdAt),
            Modifier.weight(1.2f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.caption
        )
        Text(
            "BRV #${load.brvId}",
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.bodyStrong
        )
        Text(
            load.description?.substringBefore(":") ?: "N/A",
            Modifier.weight(1.5f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.body
        )
        Text(
            "${load.loadedQuantity?.setScale(2, java.math.RoundingMode.HALF_UP) ?: 0} L",
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.body
        )
        Text(
            if (isFinalized) "${load.dispatchedQuantity?.setScale(2, java.math.RoundingMode.HALF_UP)} L" else "-",
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.body
        )
        
        // Status Tag
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val bgColor = if (isFinalized) FluentTheme.colors.system.success.copy(alpha = 0.1f) 
                          else FluentTheme.colors.system.caution.copy(alpha = 0.1f)
            val txtColor = if (isFinalized) FluentTheme.colors.system.success 
                           else FluentTheme.colors.system.caution
            
            Text(
                text = if (isFinalized) "FINALIZED" else "LOADED",
                modifier = Modifier.background(bgColor, shape = FluentTheme.shapes.control).padding(horizontal = 8.dp, vertical = 2.dp),
                style = FluentTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = txtColor
            )
        }

        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (!isFinalized) {
                ABLogisticsAccentButton(onClick = { onFinalize(load) }, modifier = Modifier.height(28.dp)) {
                    Text("Finalize", style = FluentTheme.typography.caption)
                }
            } else {
                Icon(Icons.Default.LockClosed, contentDescription = "Finalized", tint = FluentTheme.colors.text.accent.secondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun LoadEntryForm(
    viewModel: DirectLoadViewModel,
    customers: List<CustomerResponse>,
    brvs: List<BRVDTO>
) {
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Text("Customer (Outward Destination)", style = FluentTheme.typography.bodyStrong)
                Spacer(Modifier.height(8.dp))
                CustomerSearch(
                    customers = customers,
                    onSelected = { viewModel.selectedCustomer = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))

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

        Row(Modifier.fillMaxWidth()) {
            ABLogisticsTextField(
                value = viewModel.loadedQuantity,
                onValueChange = { 
                    viewModel.loadedQuantity = it.filter { ch -> ch.isDigit() || ch == '.' }
                    viewModel.updateCalculations()
                },
                header = { Text("Loaded Quantity (L)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            ABLogisticsTextField(
                value = viewModel.costPrice,
                onValueChange = { 
                    viewModel.costPrice = it.filter { ch -> ch.isDigit() || ch == '.' }
                    viewModel.updateCalculations()
                },
                header = { Text("Cost Price / L") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth()) {
            ABLogisticsTextField(
                value = viewModel.sellingPrice,
                onValueChange = { 
                    viewModel.sellingPrice = it.filter { ch -> ch.isDigit() || ch == '.' }
                    viewModel.updateCalculations()
                },
                header = { Text("Selling Price / L") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            ABLogisticsTextField(
                value = viewModel.brvCost,
                onValueChange = { 
                    viewModel.brvCost = it.filter { ch -> ch.isDigit() || ch == '.' }
                    viewModel.updateCalculations()
                },
                header = { Text("Delivery Cost") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        ABLogisticsTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            header = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(24.dp))
        
        // Quick Summary in Dialog
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Projected Profit", style = FluentTheme.typography.caption)
                    Text(
                        "${viewModel.projectedProfit.setScale(2, java.math.RoundingMode.HALF_UP)}", 
                        color = if (viewModel.projectedProfit >= BigDecimal.ZERO) FluentTheme.colors.system.success else FluentTheme.colors.system.critical
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Revenue", style = FluentTheme.typography.caption)
                    Text("${viewModel.projectedRevenue.setScale(2, java.math.RoundingMode.HALF_UP)}", style = FluentTheme.typography.bodyStrong)
                }
            }
        }
    }
}


