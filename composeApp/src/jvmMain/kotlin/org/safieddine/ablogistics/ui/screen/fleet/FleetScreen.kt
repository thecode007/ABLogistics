package org.safieddine.ablogistics.ui.screen.fleet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.IconButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import org.safieddine.ablogistics.ui.theme.ABLogisticsSubtleButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsAccentButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Apps
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.History
import io.github.composefluent.surface.Card
import org.safieddine.ablogistics.data.BRVDTO
import org.safieddine.ablogistics.data.ReceiptResponse
import org.safieddine.ablogistics.ui.theme.DeleteDialog
import io.github.composefluent.icons.regular.Edit
import io.github.composefluent.icons.regular.Delete
import io.github.composefluent.icons.regular.Add
import io.github.composefluent.icons.regular.CheckmarkCircle
import org.safieddine.ablogistics.ui.screen.receipts.DeliveryFinalizationDialog
import org.safieddine.ablogistics.data.EntityType
import org.safieddine.ablogistics.data.ReceiptType


@Composable
fun FleetScreen(
    viewModel: FleetViewModel = viewModel()
) {
    val brvs by viewModel.brvs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val history by viewModel.selectedBrvHistory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedBrv by remember { mutableStateOf<BRVDTO?>(null) }
    var brvToEdit by remember { mutableStateOf<BRVDTO?>(null) }
    var brvToDelete by remember { mutableStateOf<BRVDTO?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var brvToFinalize by remember { mutableStateOf<BRVDTO?>(null) }
    var receiptToFinalize by remember { mutableStateOf<ReceiptResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFleetStatus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        var query by remember { mutableStateOf("") }
        val visibleBrvs by remember(brvs, query) {
            mutableStateOf(
                if (query.isBlank()) brvs else brvs.filter {
                    it.plateNumber.contains(query, ignoreCase = true) ||
                            (it.driverName?.contains(query, ignoreCase = true) ?: false) ||
                            (it.vendor?.contains(query, ignoreCase = true) ?: false)
                }
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ABLogisticsTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text("Search by Plate, Driver or Vendor")
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))
            ABLogisticsSubtleButton(iconOnly = true, onClick = { viewModel.loadFleetStatus() }) {
                Icon(Icons.Regular.ArrowCounterclockwise, contentDescription = null)
            }
            Spacer(Modifier.width(6.dp))
            ABLogisticsSubtleButton(iconOnly = true, onClick = { showAddDialog = true }) {
                Icon(Icons.Regular.Add, contentDescription = null)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (visibleBrvs.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Regular.Apps,
                        contentDescription = "",
                        modifier = Modifier.size(64.dp),
                        tint = FluentTheme.colors.background.smoke.default
                    )
                    Text(
                        if (query.isBlank()) "No tankers found in the fleet." else "No matching tankers found.",
                        style = FluentTheme.typography.bodyLarge,
                        color = FluentTheme.colors.background.smoke.default
                    )
                }

            } else {
                FleetTable(
                    brvs = visibleBrvs,
                    isLoading = isLoading,
                    onViewHistory = {
                        selectedBrv = it
                        viewModel.loadBRVHistory(it.id)
                        showHistoryDialog = true
                    },
                    onEdit = { brvToEdit = it },
                    onDelete = { brvToDelete = it },
                    onFinalize = { 
                        selectedBrv = it
                        viewModel.loadBRVHistory(it.id)
                        showHistoryDialog = true // The user will finalize from history
                    }
                )
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ProgressRing()
                }
            }
        }

        if (error != null) {
            InfoBar(
                title = {},
                message = { Text(error ?: "") },
                severity = InfoBarSeverity.Critical,
                closeAction = {
                    IconButton({ viewModel.clearError() }) {
                        Icon(imageVector = Icons.Regular.ArrowCounterclockwise, "")
                    }
                }
            )
        }
    }

    if (showAddDialog || brvToEdit != null) {
        AddBRVDialog(
            existingBrv = brvToEdit,
            onDismiss = { 
                showAddDialog = false
                brvToEdit = null
            },
            onConfirm = { plate, driver, phone, vendor, capacity ->
                if (brvToEdit != null) {
                    viewModel.updateBRV(
                        brvToEdit!!.copy(
                            plateNumber = plate,
                            driverName = driver,
                            driverPhone = phone,
                            vendor = vendor,
                            capacity = capacity
                        ),
                        onSuccess = { brvToEdit = null }
                    )
                } else {
                    viewModel.saveBRV(
                        BRVDTO(0, plate, driver, phone, vendor, capacity, "IDLE"),
                        onSuccess = { showAddDialog = false }
                    )
                }
            }
        )
    }

    if (brvToDelete != null) {
        DeleteDialog(
            showDeleteDialog = true,
            title = "Delete Tanker",
            message = "Are you sure you want to delete tanker ${brvToDelete?.plateNumber}?",
            onButtonClicked = { btn ->
                if (btn == ContentDialogButton.Primary) {
                    viewModel.deleteBRV(brvToDelete!!.id, onSuccess = { brvToDelete = null })
                } else {
                    brvToDelete = null
                }
            }
        )
    }

    if (showHistoryDialog && selectedBrv != null) {
        ContentDialog(
            title = "History for ${selectedBrv?.plateNumber}",
            visible = showHistoryDialog,
            onButtonClick = { showHistoryDialog = false },
            primaryButtonText = "OK",
            closeButtonText = "Close",
            content = {
                BRVHistoryList(history, onFinalize = { 
                    receiptToFinalize = it
                })
            }
        )
    }

    if (receiptToFinalize != null) {
        DeliveryFinalizationDialog(
            receipt = receiptToFinalize!!,
            onDismiss = { receiptToFinalize = null },
            onConfirm = { qty ->
                viewModel.finalizeDelivery(receiptToFinalize!!.id, qty) {
                    receiptToFinalize = null
                    showHistoryDialog = false
                }
            },
            isLoading = isLoading
        )
    }
}

@Composable
fun FleetTable(
    brvs: List<BRVDTO>,
    isLoading: Boolean,
    onViewHistory: (BRVDTO) -> Unit,
    onEdit: (BRVDTO) -> Unit,
    onDelete: (BRVDTO) -> Unit,
    onFinalize: (BRVDTO) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize()) {
        stickyHeader {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(FluentTheme.colors.background.mica.base.copy(alpha = 0.9f))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Plate Number", Modifier.weight(1.2f), style = FluentTheme.typography.title.copy(fontSize = 14.sp), textAlign = TextAlign.Center)
                Text("Driver Name", Modifier.weight(1.5f), style = FluentTheme.typography.title.copy(fontSize = 14.sp), textAlign = TextAlign.Center)
                Text("Vendor", Modifier.weight(1.2f), style = FluentTheme.typography.title.copy(fontSize = 14.sp), textAlign = TextAlign.Center)
                Text("Capacity", Modifier.weight(0.8f), style = FluentTheme.typography.title.copy(fontSize = 14.sp), textAlign = TextAlign.Center)
                Text("Status", Modifier.weight(1.2f), style = FluentTheme.typography.title.copy(fontSize = 14.sp), textAlign = TextAlign.Center)
                Text("Actions", Modifier.weight(0.8f), style = FluentTheme.typography.title.copy(fontSize = 14.sp), textAlign = TextAlign.Center)
            }
            // Use a simple Box or Spacer if Fluent doesn't have HorizontalDivider
            Box(Modifier.fillMaxWidth().height(1.dp).background(FluentTheme.colors.background.mica.base.copy(alpha = 0.3f)))
        }

        itemsIndexed(brvs, key = { _, brv -> brv.id }) { index, brv ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) Color.Transparent else FluentTheme.colors.fillAccent.default.copy(alpha = 0.1f))
                    .clickable { onViewHistory(brv) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(brv.plateNumber, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold)
                Text(brv.driverName ?: "N/A", textAlign = TextAlign.Center, modifier = Modifier.weight(1.5f))
                Text(brv.vendor ?: "N/A", textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                Text("${brv.capacity} L", textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
                StatusBadge(brv.status, modifier = Modifier.weight(1.2f))
                Row(modifier = Modifier.weight(0.8f), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { onEdit(brv) }) {
                        Icon(imageVector = Icons.Regular.Edit, contentDescription = "Edit", tint = FluentTheme.colors.fillAccent.default)
                    }
                    IconButton(onClick = { onDelete(brv) }) {
                        Icon(imageVector = Icons.Regular.Delete, contentDescription = "Delete", tint = FluentTheme.colors.system.critical)
                    }
                    IconButton(onClick = { onViewHistory(brv) }) {
                        Icon(imageVector = Icons.Regular.History, contentDescription = "History", tint = FluentTheme.colors.fillAccent.default)
                    }
                    if (brv.status == "LOADED") {
                        IconButton(onClick = { onFinalize(brv) }) {
                            Icon(imageVector = Icons.Regular.CheckmarkCircle, contentDescription = "Finalize", tint = FluentTheme.colors.system.success)
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(FluentTheme.colors.background.mica.base.copy(alpha = 0.15f)))
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = when (status) {
        "IDLE" -> FluentTheme.colors.system.success
        "LOADED" -> FluentTheme.colors.system.caution
        "EN_ROUTE" -> FluentTheme.colors.fillAccent.default
        "MAINTENANCE" -> FluentTheme.colors.system.critical
        else -> Color.Gray
    }

    Box(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(Modifier) {
            Text(
                text = status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AddBRVDialog(
    existingBrv: BRVDTO? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, java.math.BigDecimal) -> Unit
) {
    var plate by remember { mutableStateOf(existingBrv?.plateNumber ?: "") }
    var driver by remember { mutableStateOf(existingBrv?.driverName ?: "") }
    var phone by remember { mutableStateOf(existingBrv?.driverPhone ?: "") }
    var vendor by remember { mutableStateOf(existingBrv?.vendor ?: "") }
    var capacity by remember { mutableStateOf(existingBrv?.capacity?.toString() ?: "") }

    // Validation errors — null means no error
    var plateError by remember { mutableStateOf<String?>(null) }
    var driverError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var vendorError by remember { mutableStateOf<String?>(null) }
    var capacityError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        plateError = if (plate.isBlank()) "Plate number is required" else null
        driverError = if (driver.isBlank()) "Driver name is required" else null
        phoneError = when {
            phone.isBlank() -> "Phone number is required"
            !phone.matches(Regex("^[+]?\\d{7,15}$")) -> "Enter a valid phone number"
            else -> null
        }
        vendorError = if (vendor.isBlank()) "Vendor is required" else null
        capacityError = when {
            capacity.isBlank() -> "Capacity is required"
            capacity.toBigDecimalOrNull() == null || capacity.toBigDecimal() <= java.math.BigDecimal.ZERO -> "Enter a valid capacity > 0"
            else -> null
        }
        return listOf(plateError, driverError, phoneError, vendorError, capacityError).all { it == null }
    }

    ContentDialog(
        title = if (existingBrv != null) "Edit Tanker" else "Add New Tanker (BRV)",
        visible = true,
        onButtonClick = {
            if (it == ContentDialogButton.Primary) {
                if (validate()) onConfirm(plate, driver, phone, vendor, capacity.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO)
            } else {
                onDismiss()
            }
        },
        primaryButtonText = if (existingBrv != null) "Update Tanker" else "Add Tanker",
        closeButtonText = "Cancel",
        content = {
            Column(Modifier.padding(16.dp)) {
                ABLogisticsTextField(
                    value = plate,
                    onValueChange = { plate = it; if (it.isNotBlank()) plateError = null },
                    header = { Text("Plate Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (plateError != null) Text(plateError!!, color = FluentTheme.colors.system.critical, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                ABLogisticsTextField(
                    value = driver,
                    onValueChange = { driver = it; if (it.isNotBlank()) driverError = null },
                    header = { Text("Driver Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (driverError != null) Text(driverError!!, color = FluentTheme.colors.system.critical, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                ABLogisticsTextField(
                    value = phone,
                    onValueChange = { phone = it; if (it.isNotBlank()) phoneError = null },
                    header = { Text("Driver Phone *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                if (phoneError != null) Text(phoneError!!, color = FluentTheme.colors.system.critical, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                ABLogisticsTextField(
                    value = vendor,
                    onValueChange = { vendor = it; if (it.isNotBlank()) vendorError = null },
                    header = { Text("Vendor *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (vendorError != null) Text(vendorError!!, color = FluentTheme.colors.system.critical, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                ABLogisticsTextField(
                    value = capacity,
                    onValueChange = { input -> if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) { capacity = input; capacityError = null } },
                    header = { Text("Capacity (Liters) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (capacityError != null) Text(capacityError!!, color = FluentTheme.colors.system.critical, fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun BRVHistoryList(history: List<ReceiptResponse>, onFinalize: (ReceiptResponse) -> Unit) {
    if (history.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("No history available for this tanker.")
        }
    } else {
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
            items(items = history) { receipt ->

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Receipt #${receipt.receiptId}", fontWeight = FontWeight.Bold)
                            Text(receipt.receiptType.name, color = if (receipt.receiptType.name == "INWARD") Color.Green else Color.Red)
                        }
                        Text("Amount: ${receipt.amount} L")
                        Text("Description: ${receipt.description ?: "N/A"}", style = FluentTheme.typography.body, color = FluentTheme.colors.text.accent.secondary)
                        
                        if (receipt.entityType == EntityType.CUSTOMER && receipt.receiptType == ReceiptType.OUTWARD && receipt.dispatchedQuantity == null) {
                            Spacer(Modifier.height(8.dp))
                            ABLogisticsAccentButton(
                                onClick = { onFinalize(receipt) },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Finalize Delivery", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
