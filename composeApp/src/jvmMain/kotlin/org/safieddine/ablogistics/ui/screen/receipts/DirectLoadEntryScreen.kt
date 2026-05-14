package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.Receipt
import io.github.composefluent.icons.regular.Add
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.DocumentTableTruck
import io.github.composefluent.icons.regular.LockClosed
import io.github.composefluent.icons.filled.Pen
import org.safieddine.ablogistics.ui.utils.NumberCommaTransformation
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.ui.screen.adminScreen.UserTableShimmerRow
import org.safieddine.ablogistics.ui.theme.ABLogisticsAccentButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsSubtleButton
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
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var finalizingLoad by remember { mutableStateOf<ReceiptResponse?>(null) }

    LaunchedEffect(selectedWarehouse) {
        selectedWarehouse?.id?.let { viewModel.loadInitialData(it) }
    }

    Column(Modifier.fillMaxSize()) {
        // Header Bar
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Load Management",
                style = FluentTheme.typography.title,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(Modifier.width(16.dp))
            
            ABLogisticsTextField(
                modifier = Modifier.width(300.dp),
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                singleLine = true,
                placeholder = { Text("Search by Receipt ID") }
            )

            Spacer(Modifier.weight(1f))
            
            ABLogisticsSubtleButton(iconOnly = true, onClick = { viewModel.fetchLoads(selectedWarehouse?.id ?: 0L) }) {
                Icon(Icons.Default.ArrowCounterclockwise, contentDescription = "Refresh")
            }
            
            Spacer(Modifier.width(6.dp))
            
            ABLogisticsSubtleButton(iconOnly = true, onClick = { showAddDialog = true }) {
                Icon(Icons.Regular.DocumentTableTruck, contentDescription = "Add Load")
            }
        }

        // Notifications
        if (error != null || success != null) {
            Box(Modifier.padding(horizontal = 8.dp)) {
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
        Spacer(Modifier.height(16.dp))

        if (!isLoading && loads.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = "No loads",
                    tint = FluentTheme.colors.background.smoke.default,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "No load orders found for this warehouse.",
                    style = FluentTheme.typography.bodyLarge,
                    color = FluentTheme.colors.background.smoke.default
                )
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                stickyHeader {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(FluentTheme.colors.background.mica.base.copy(alpha = 0.9f))
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                            TableHeader("Date", Modifier.weight(1.2f))
                            TableHeader("Receipt ID", Modifier.weight(1f))
                            TableHeader("Material", Modifier.weight(0.8f))
                            TableHeader("BRV", Modifier.weight(1f))
                            TableHeader("Customer", Modifier.weight(1.5f))
                            TableHeader("Loaded", Modifier.weight(1f))
                            TableHeader("Dispatched", Modifier.weight(1f))
                            TableHeader("Status", Modifier.weight(1f))
                            TableHeader("Actions", Modifier.weight(1f))
                        }
                        Divider(
                            color = FluentTheme.colors.background.mica.base.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }

                    if (isLoading) {
                        items(5) {
                            LoadTableShimmerRow()
                            Divider(
                                color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                                thickness = 0.5.dp
                            )
                        }
                    } else {
                        items(loads) { load ->
                            LoadRow(
                                load = load,
                                onFinalize = { finalizingLoad = it },
                                onEdit = { 
                                    viewModel.prepareEdit(it)
                                    showAddDialog = true
                                }
                            )
                            Divider(
                                color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }

    // Add Load Dialog
    if (showAddDialog) {
        ContentDialog(
            title = if (viewModel.isEditMode) "Update Direct Load" else "Process Direct Load",
            visible = true,
            size = DialogSize.Max,
            primaryButtonText = if (isLoading) "Processing..." else (if (viewModel.isEditMode) "Update Load" else "Process Load"),
            closeButtonText = "Cancel",
            onButtonClick = { btn ->
                if (btn == ContentDialogButton.Primary) {
                    if (!isLoading && viewModel.selectedCustomer != null && viewModel.selectedBrv != null) {
                        if (viewModel.isEditMode) {
                            viewModel.updateLoad(selectedWarehouse?.id ?: 0L)
                        } else {
                            viewModel.processLoad(selectedWarehouse?.id ?: 0L)
                        }
                    }
                } else {
                    showAddDialog = false
                    viewModel.isEditMode = false // Reset mode on cancel
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
        style = FluentTheme.typography.title.copy(fontSize = 14.sp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun LoadRow(load: ReceiptResponse, onFinalize: (ReceiptResponse) -> Unit, onEdit: (ReceiptResponse) -> Unit) {
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
            load.receiptId ?: "-",
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.bodyStrong.copy(fontSize = 12.sp)
        )
        Text(
            load.materialType?.name ?: "-",
            Modifier.weight(0.8f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.bodyStrong.copy(fontSize = 12.sp)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isFinalized) {
                    ABLogisticsAccentButton(onClick = { onFinalize(load) }, modifier = Modifier.height(28.dp)) {
                        Text("Finalize", style = FluentTheme.typography.caption)
                    }
                    Spacer(Modifier.width(6.dp))
                    ABLogisticsSubtleButton(
                        onClick = { onEdit(load) },
                        modifier = Modifier.height(28.dp),
                        iconOnly = true
                    ) {
                        Icon(Icons.Filled.Pen, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                } else {
                    Icon(Icons.Default.LockClosed, contentDescription = "Finalized", tint = FluentTheme.colors.text.accent.secondary, modifier = Modifier.size(16.dp))
                }
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
        BRVSearch(
            brvs = brvs,
            onSelected = { viewModel.selectedBrv = it },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Material Type", style = FluentTheme.typography.bodyStrong)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            MaterialType.values().forEach { type ->
                if (viewModel.selectedMaterial == type) {
                    ABLogisticsAccentButton(
                        onClick = { viewModel.onMaterialChanged(type) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(type.name)
                    }
                } else {
                    ABLogisticsSubtleButton(
                        onClick = { viewModel.onMaterialChanged(type) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(type.name)
                    }
                }
                if (type != MaterialType.values().last()) Spacer(Modifier.width(8.dp))
            }
        }
        
        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth()) {
            ABLogisticsTextField(
                value = viewModel.loadedQuantity,
                onValueChange = { 
                    viewModel.loadedQuantity = it.filter { ch -> ch.isDigit() || ch == '.' }
                    viewModel.updateCalculations()
                },
                header = { Text("Loaded Quantity (L)") },
                visualTransformation = NumberCommaTransformation(),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(16.dp))
            ABLogisticsTextField(
                value = viewModel.costPrice,
                onValueChange = { 
                    viewModel.costPrice = it.filter { ch -> ch.isDigit() || ch == '.' }
                    viewModel.updateCalculations()
                },
                header = { Text("Cost Price / L") },
                visualTransformation = NumberCommaTransformation(),
                modifier = Modifier.weight(1f),
                singleLine = true
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
                visualTransformation = NumberCommaTransformation(),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(16.dp))
            ABLogisticsTextField(
                value = viewModel.brvCost,
                onValueChange = { 
                    viewModel.brvCost = it.filter { ch -> ch.isDigit() || ch == '.' }
                    viewModel.updateCalculations()
                },
                header = { Text("Delivery Cost") },
                visualTransformation = NumberCommaTransformation(),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        ABLogisticsTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            header = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(16.dp))

        ABLogisticsTextField(
            value = viewModel.receiptId,
            onValueChange = { viewModel.receiptId = it },
            header = { Text("Receipt ID (Optional)") },
            placeholder = { Text("e.g. REC-12345") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                Switcher(
                    checked = viewModel.supplierPaid,
                    onCheckStateChange = { viewModel.supplierPaid = it },
                    text = "Supplier Paid"
                )
            }
            Spacer(Modifier.width(16.dp))
            Box(Modifier.weight(1f)) {
                Switcher(
                    checked = viewModel.customerPaid,
                    onCheckStateChange = { viewModel.customerPaid = it },
                    text = "Customer Paid"
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Quick Summary in Dialog
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FluentTheme.colors.background.mica.base, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Projected Profit", style = FluentTheme.typography.caption)
                    Text(
                        "${viewModel.projectedProfit.setScale(2, java.math.RoundingMode.HALF_UP)}", 
                        color = if (viewModel.projectedProfit >= BigDecimal.ZERO) Color(0xFF13A10E) else Color(0xFFD32F2F)
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

@Composable
fun LoadTableShimmerRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1.2f).height(12.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(12.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(0.8f).height(12.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(12.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1.5f).height(12.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(12.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(12.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(12.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(12.dp).shimmerEffect())
    }
}

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "shimmerAnim"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value, 0f),
        end = Offset(translateAnim.value + 200f, 0f)
    )

    return this.background(brush)
}


