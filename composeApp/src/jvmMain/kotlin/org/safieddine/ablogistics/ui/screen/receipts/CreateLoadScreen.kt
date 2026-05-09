package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Add
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.service.*
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.ui.theme.*
import org.safieddine.ablogistics.ui.theme.ABLogisticsAccentButton

@OptIn(ExperimentalFluentApi::class)
@Composable
fun CreateLoadScreen() {
    val scope = rememberCoroutineScope()
    val selectedWarehouse by SessionStore.selectedWarehouse.collectAsState()

    var customers by remember { mutableStateOf<List<CustomerResponse>>(emptyList()) }
    var brvs by remember { mutableStateOf<List<BRVDTO>>(emptyList()) }

    var selectedCustomer by remember { mutableStateOf<CustomerResponse?>(null) }
    var brvIndex by remember { mutableStateOf<Int?>(null) }
    var selectedBrv by remember { mutableStateOf<BRVDTO?>(null) }

    var quantity by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var deliveryCost by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    var isProcessing by remember { mutableStateOf(false) }

    // Initial load
    LaunchedEffect(selectedWarehouse) {
        val whId = selectedWarehouse?.id ?: return@LaunchedEffect
        isLoading = true
        scope.launch {
            // Load Customers
            val custRes = CustomerService.list(whId)
            customers = custRes.getOrNull()?.data?.customers?.filter { it.warehouseId == whId } ?: emptyList()

            // Load Fleet
            val fleetRes = BRVService.getFleetStatus()
            brvs = fleetRes.getOrNull()?.data?.brvs ?: emptyList()

            isLoading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (error != null) {
            InfoBar(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                title = { Text("Failed to process load") },
                severity = InfoBarSeverity.Critical,
                message = { Text(error ?: "") }
            )
        }
        if (info != null) {
            InfoBar(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                title = { Text(info ?: "") },
                severity = InfoBarSeverity.Success,
                message = {}
            )
        }

        LazyColumn(Modifier.weight(1f)) {
            item {
                Text("Create New Fuel Load", style = FluentTheme.typography.titleLarge)
                Spacer(Modifier.height(24.dp))

                // Customer Selection
                Text("Customer", style = FluentTheme.typography.bodyStrong)
                Spacer(Modifier.height(4.dp))
                CustomerSearch(
                    customers = customers,
                    onSelected = { selectedCustomer = it },
                    modifier = Modifier.fillMaxWidth()
                )
                selectedCustomer?.let {
                    Text("Selected: ${it.name}", style = FluentTheme.typography.caption, color = FluentTheme.colors.text.accent.secondary)
                }
                Spacer(Modifier.height(16.dp))

                // BRV Selection
                Text("Tanker (BRV)", style = FluentTheme.typography.bodyStrong)
                Spacer(Modifier.height(4.dp))
                ComboBox(
                    placeholder = "Select Tanker",
                    selected = brvIndex,
                    items = brvs.map { "${it.plateNumber} (${it.driverName ?: "No Driver"})" },
                    onSelectionChange = { i, _ -> brvIndex = i; selectedBrv = brvs[i] },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                // Numbers
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        ABLogisticsTextField(
                            value = quantity,
                            onValueChange = { quantity = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            header = { Text("Quantity (Liters)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        ABLogisticsTextField(
                            value = costPrice,
                            onValueChange = { costPrice = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            header = { Text("Cost per Liter (Admin)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        ABLogisticsTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            header = { Text("Selling Price per Liter") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        ABLogisticsTextField(
                            value = deliveryCost,
                            onValueChange = { deliveryCost = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            header = { Text("Delivery Cost (Total)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                ABLogisticsTextField(
                    value = description,
                    onValueChange = { description = it },
                    header = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))

                ABLogisticsAccentButton(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    disabled = isProcessing || selectedCustomer == null || selectedBrv == null || quantity.isBlank(),
                    onClick = {
                        val qty = quantity.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                        val cp = costPrice.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                        val spBase = sellingPrice.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                        val dc = deliveryCost.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO

                        // Calculate total selling price per liter including delivery
                        val spTotal = if (qty.compareTo(java.math.BigDecimal.ZERO) > 0) 
                            (spBase.multiply(qty).add(dc)).divide(qty, 4, java.math.RoundingMode.HALF_UP) 
                            else spBase

                        val req = ProcessLoadRequest(
                            brvId = selectedBrv!!.id,
                            customerId = selectedCustomer!!.id,
                            warehouseId = selectedWarehouse?.id ?: 0L,
                            loadedQuantity = qty,
                            costPrice = cp,
                            sellingPrice = spTotal,
                            brvCost = dc,
                            description = description.ifBlank { "Load for ${selectedCustomer?.name}" }
                        )

                        isProcessing = true
                        scope.launch {
                            val res = BRVService.processLoad(req)
                            if (res.isSuccess) {
                                info = "Load processed successfully. Two receipts created."
                                error = null
                                // Reset fields
                                quantity = ""
                                costPrice = ""
                                sellingPrice = ""
                                deliveryCost = ""
                                description = ""
                                brvIndex = null
                                selectedBrv = null
                                selectedCustomer = null
                            } else {
                                error = res.exceptionOrNull()?.message
                                println("CreateLoad Error: $error")
                                info = null
                            }
                            isProcessing = false
                        }
                    }
                ) {
                    if (isProcessing) {
                        ProgressRing(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Text("Create Load")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
