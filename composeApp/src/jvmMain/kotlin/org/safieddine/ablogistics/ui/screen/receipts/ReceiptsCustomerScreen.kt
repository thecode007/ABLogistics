package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.*
import io.github.composefluent.icons.regular.*
import io.github.composefluent.surface.Card
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ablogistics.composeapp.generated.resources.*
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.service.CustomerService
import org.safieddine.ablogistics.data.service.ReceiptService
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.data.session.WarehouseFundsStore
import org.safieddine.ablogistics.ui.screen.adminScreen.UserTableShimmerRow
import org.safieddine.ablogistics.ui.theme.DeleteDialog
import org.safieddine.ablogistics.ui.screen.StateOfAccountScreen
import org.safieddine.ablogistics.ui.screen.StatementOverlayDialog

import java.util.*
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFluentApi::class, ExperimentalTime::class)
@Composable
fun ReceiptsCustomerScreen() {
    val scope = rememberCoroutineScope()
    val selectedWarehouse by SessionStore.selectedWarehouse.collectAsState()

    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    // Customers
    var customers by remember { mutableStateOf<List<CustomerResponse>>(emptyList()) }
    var selectedCustomerIndex by remember { mutableStateOf<Int?>(null) }
    val selectedCustomerId = selectedCustomerIndex?.let { customers.getOrNull(it)?.id }

    // Dates
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var clearDatesKey by remember { mutableStateOf(System.currentTimeMillis()) }

    // Totals
    var totalInbound by remember { mutableStateOf(java.math.BigDecimal.ZERO) }
    var totalOutbound by remember { mutableStateOf(java.math.BigDecimal.ZERO) }

    // Pagination + filters
    var search by remember { mutableStateOf("") }
    var page by remember { mutableStateOf(0) }
    var size by remember { mutableStateOf(100) }
    var isLoading by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf(PageResponse<ReceiptResponse>()) }

    // Actions
    var toDelete by remember { mutableStateOf<ReceiptResponse?>(null) }
    var editing by remember { mutableStateOf<ReceiptResponse?>(null) }
    var toReturn by remember { mutableStateOf<ReceiptResponse?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var showStatement by remember { mutableStateOf(false) }

    // Load customers
    fun reloadCustomers() {
        scope.launch {
            val res = CustomerService.list(selectedWarehouse?.id ?: 0L)
            customers =
                res.getOrNull()?.data?.customers?.filter { it.warehouseId == selectedWarehouse?.id } ?: emptyList()
            if (customers.isNotEmpty() && selectedCustomerIndex == null) selectedCustomerIndex = 0
        }
    }

    LaunchedEffect(selectedWarehouse) {
        selectedCustomerIndex = null
        reloadCustomers()
    }

    // Load receipts
    fun load() {
        val wh = selectedWarehouse?.id ?: return
        val cid = selectedCustomerId ?: return
        isLoading = true
        scope.launch {
            val res = ReceiptService.listCustomer(
                warehouseId = wh,
                customerId = cid,
                start = startDate,
                end = endDate,
                page = page,
                size = size,
                receiptId = search.ifBlank { null }
            )
            if (res.isSuccess) {
                val summary = res.getOrNull()?.data
                if (summary != null) {
                    data = PageResponse(
                        content = summary.receipts,
                        number = summary.page,
                        size = summary.size,
                        totalElements = summary.totalElements,
                        totalPages = summary.totalPages
                    )
                    totalInbound = summary.totalInbound
                    totalOutbound = summary.totalOutbound
                    error = null
                    // Update funds in title bar
                    WarehouseFundsStore.refresh(wh)
                } else {
                    error = "No data returned"
                }
            } else {
                error = res.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedWarehouse, selectedCustomerId) {
        page = 0
        if (selectedCustomerId != null) load()
    }

    LaunchedEffect(search) {
        if (selectedCustomerId != null) {
            delay(350)
            page = 0
            load()
        }
    }

    // Use a Box to allow overlaying the Statement dialog above the table/content
    Box(Modifier.fillMaxSize()) {
        // Main content column
        Column(Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CustomerSearch(
                    customers = customers,
                    onSelected = { c ->
                        val idx = customers.indexOfFirst { it.id == c.id }
                        selectedCustomerIndex = if (idx >= 0) idx else null
                        page = 0
                    },
                    modifier = Modifier.width(320.dp)
                )


                Spacer(Modifier.width(8.dp))

                TextField(
                    modifier = Modifier.width(300.dp),
                    value = search,
                    onValueChange = { search = it },
                    singleLine = true,
                    placeholder = {
                        Text(stringResource(Res.string.search_by_id))
                    }
                )

                Spacer(Modifier.weight(1f))

                key(clearDatesKey) {
                    CalendarDatePicker(
                        onChoose = { localDate ->
                            val startOfDay = java.time.LocalDateTime.of(
                                localDate.year,
                                localDate.monthValue + 1, // picker month is 0-based
                                localDate.day,
                                0, 0, 0, 0
                            )
                            startDate = startOfDay.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                            page = 0
                            load()
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    CalendarDatePicker(
                        onChoose = { localDate ->
                            val endOfDay = java.time.LocalDateTime.of(
                                localDate.year,
                                localDate.monthValue + 1, // picker month is 0-based
                                localDate.day,
                                23, 59, 59, 0
                            ).plusNanos(999_000_000) // include the full last second
                            endDate = endOfDay.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                            page = 0
                            load()
                        }
                    )
                }

                SubtleButton(iconOnly = true, onClick = {
                    clearDatesKey = System.currentTimeMillis()
                    startDate = null; endDate = null
                    page = 0; load()
                }) {
                    Icon(Icons.Default.TextClearFormatting, contentDescription = "Clear Dates")
                }

                Spacer(Modifier.width(5.dp))
                SubtleButton(iconOnly = true, onClick = { reloadCustomers(); load() }) {
                    Icon(
                        Icons.Default.ArrowCounterclockwise,
                        contentDescription = stringResource(Res.string.reload)
                    )
                }

                Spacer(Modifier.width(6.dp))
                val balance = totalOutbound.minus(totalInbound)
                SubtleButton(
                    iconOnly = true,
                    onClick = { showForm = true },
                    disabled = selectedCustomerId == null || balance <= java.math.BigDecimal.ZERO
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(Res.string.add_receipt)
                    )
                }

                Spacer(Modifier.width(6.dp))
                SubtleButton(
                    iconOnly = true,
                    onClick = { showStatement = true },
                    disabled = selectedCustomerId == null
                ) {
                    Icon(Icons.Filled.PersonNote, contentDescription = "Statement of Account")
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.width(500.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Card(Modifier) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = stringResource(Res.string.app_title),
                            style = FluentTheme.typography.title
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InboundArrow(amount = totalOutbound.toPlainString())
                        val balance = totalOutbound.minus(totalInbound)
                        val locale = Locale.getDefault()
                        val formattedValue = formatLocalized(balance, locale)
                        Text(
                            text = formattedValue,
                            color = if (balance > java.math.BigDecimal.ZERO) FluentTheme.colors.system.critical else FluentTheme.colors.system.success,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutboundArrow(amount = totalInbound.toPlainString())
                    }

                    Card(Modifier) {
                        val selectedName = selectedCustomerIndex?.let { idx -> customers.getOrNull(idx)?.name }
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = selectedName ?: stringResource(Res.string.customer),
                            style = FluentTheme.typography.title
                        )
                    }
                }
            }


            Spacer(Modifier.height(16.dp))


            if (error != null) {
                InfoBar(
                    modifier = Modifier.height(40.dp).fillMaxWidth(),
                    title = { Text(error ?: "") },
                    severity = InfoBarSeverity.Critical,
                    message = {}
                )
            }
            if (info != null) {
                InfoBar(
                    modifier = Modifier.height(40.dp).fillMaxWidth(),
                    title = { Text(info ?: "") },
                    severity = InfoBarSeverity.Success,
                    message = {}
                )
            }

            // --- TABLE ---
            if (!isLoading && data.content.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Receipt,
                        contentDescription = "No receipts",
                        tint = FluentTheme.colors.background.smoke.default,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(Res.string.no_receipts_found),
                        color = FluentTheme.colors.background.smoke.default
                    )
                }
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    stickyHeader {
                        Row(
                            Modifier.fillMaxWidth()
                                .background(FluentTheme.colors.background.mica.base.copy(alpha = 0.9f))
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ID", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 14.sp)
                            Text(
                                stringResource(Res.string.type),
                                Modifier.weight(0.8f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Text(
                                stringResource(Res.string.amount),
                                Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Text(
                                stringResource(Res.string.description_header),
                                Modifier.weight(1.6f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Text(
                                stringResource(Res.string.actions),
                                Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Text(
                                stringResource(Res.string.date_header),
                                Modifier.weight(1.2f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(Res.string.impact),
                                Modifier.weight(1.2f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                        HorizontalDivider(color = FluentTheme.colors.background.mica.base.copy(alpha = 0.3f))
                    }

                    if (isLoading) {
                        items(5) {
                            UserTableShimmerRow()
                            HorizontalDivider(color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f))
                        }
                    } else {
                        itemsIndexed(data.content) { _, r ->
                            var statusColor = when (r.receiptType) {
                                ReceiptType.RETURNED -> FluentTheme.colors.system.attention
                                ReceiptType.INWARD -> Black
                                else ->FluentTheme.colors.system.critical
                            }

                            if (r.isReturned)
                                statusColor = FluentTheme.colors.system.attention

                            // Negate to show debt-balance perspective: OUTWARD increases the debt (positive)
                            val displayBefore = r.beforeImpactFunds.negate()
                            val displayAfter = r.afterImpactFunds.negate()
                            val icon = if (displayAfter > displayBefore) {
                                Icons.Filled.ArrowTrending
                            } else
                                Icons.Filled.ArrowTrendingDown


                            Row(
                                Modifier.fillMaxWidth()
                                    .background(statusColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(r.receiptId ?: "N/A", Modifier.weight(1f), textAlign = TextAlign.Center)
                                Row(Modifier.weight(0.8f), horizontalArrangement = Arrangement.Center) {
                                    Icon(icon, contentDescription = "", tint = statusColor)
                                    Spacer(Modifier.width(4.dp))
                                    Text(r.receiptType.name, color = statusColor)
                                }
                                val numericValueF = parseLocalizedNumber("%.2f".format(r.amount), Locale.getDefault())
                                val formattedValue = formatLocalized(numericValueF, Locale.getDefault())
                                Text(formattedValue, Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(r.description ?: "", Modifier.weight(1.6f), textAlign = TextAlign.Center)
                                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                                    if (!r.isReturned && !r.isReturnAdjustment) {
                                        IconButton(onClick = { editing = r }) {
                                            Icon(
                                                Icons.Filled.Pen,
                                                contentDescription = "",
                                                tint = FluentTheme.colors.system.attention
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(6.dp))
                                    if (r.receiptType == ReceiptType.OUTWARD) {
                                        IconButton(onClick = { toReturn = r }) {
                                            Icon(
                                                Icons.Filled.ArrowBounce,
                                                contentDescription = "Mark as returned",
                                                tint = FluentTheme.colors.system.caution
                                            )
                                        }
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    if (!r.isReturned && r.receiptType != ReceiptType.OUTWARD)
                                        IconButton(onClick = { toDelete = r }) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "",
                                                tint = FluentTheme.colors.system.critical
                                            )
                                        }
                                }
                                Text(formatDate(r.createdAt), Modifier.weight(1.2f), textAlign = TextAlign.Center)

                                // beforeColor: neutral for zero/paid, critical if there's existing debt
                                val previousColor = if (displayBefore > java.math.BigDecimal.ZERO)
                                    FluentTheme.colors.system.critical else Black
                                // afterColor: critical if debt increased, success if it decreased
                                val afterColor = if (displayAfter > displayBefore)
                                    FluentTheme.colors.system.critical else FluentTheme.colors.system.success

                                Row(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        color = previousColor,
                                        text = formatLocalized(displayBefore, Locale.getDefault()),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(Modifier.width(3.dp))

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = "",
                                        tint = afterColor
                                    )

                                    Spacer(Modifier.width(3.dp))

                                    Text(
                                        formatLocalized(displayAfter, Locale.getDefault()),
                                        textAlign = TextAlign.Center,
                                        color = afterColor
                                    )
                                }
                            }
                            HorizontalDivider(color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f))
                        }
                    }
                }
            }

            // --- PAGINATION FOOTER ---
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text("Page ${data.number + 1} / ${maxOf(data.totalPages, 1)}")
                Spacer(Modifier.width(8.dp))
                Button(disabled = page <= 0, onClick = {
                    if (page > 0) {
                        page -= 1; load()
                    }
                }) { Text("Prev") }
                Spacer(Modifier.width(8.dp))
                Button(disabled = page >= data.totalPages - 1, onClick = {
                    if (page < data.totalPages - 1) {
                        page += 1; load()
                    }
                }) { Text("Next") }
            }

            // --- RETURN DIALOG ---
            if (toReturn != null) {
                val r = toReturn!!
                var returnAmount by remember(toReturn) {
                    mutableStateOf("".ifBlank { "%.2f".format(r.amount) })
                }
                var touched by remember(toReturn) { mutableStateOf(false) }
                var isReturning by remember(toReturn) { mutableStateOf(false) }
                val maxAmount = r.amount
                val parsed = returnAmount.toBigDecimalOrNull() ?: java.math.BigDecimal("-1.0")
                val validReturn = parsed > java.math.BigDecimal.ZERO && parsed <= maxAmount
                val infoStr = stringResource(Res.string.receipt_marked_returned)

                ContentDialog(
                    title = "Return Receipt",
                    visible = true,
                    size = DialogSize.Max,
                    primaryButtonText = if (isReturning) "Returning..." else "Return",
                    closeButtonText = if (isReturning) "Please wait" else "Cancel",
                    onButtonClick = { btn ->
                        when (btn) {
                            ContentDialogButton.Primary -> {
                                if (isReturning) return@ContentDialog
                                touched = true
                                if (!validReturn) return@ContentDialog
                                isReturning = true
                                scope.launch {
                                    val res = ReceiptService
                                        .partialReturn(
                                            r.id,
                                            PartialReturnRequest(paidAmount = parsed)
                                        )
                                    if (res.isSuccess) {
                                        info = infoStr
                                        error = null
                                        toReturn = null
                                        load()
                                    } else {
                                        error = res.exceptionOrNull()?.message
                                        toReturn = null
                                    }
                                    isReturning = false
                                }
                            }

                            ContentDialogButton.Close -> if (!isReturning)
                                toReturn = null

                            else -> Unit
                        }
                    },
                    content = {
                        Column(Modifier.fillMaxWidth()) {
                            Text("Max: %.2f".format(maxAmount))
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = returnAmount,
                                onValueChange = {
                                    val filtered = it.filter { ch -> ch.isDigit() || ch == '.' }
                                        .replace(Regex("\\.(?=.*\\.)"), "")
                                    returnAmount = filtered
                                },
                                isError = touched && !validReturn,
                                label = { Text("Return Amount") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (touched && !validReturn) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Enter a value between 0 and %.2f".format(maxAmount),
                                    color = FluentTheme.colors.system.critical
                                )
                            }
                            if (isReturning) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProgressRing()
                                    Spacer(Modifier.width(8.dp))
                                    Text("Processing return...")
                                }
                            }
                        }
                    }
                )
            }

            // --- EDIT RECEIPT DIALOG ---
            if (editing != null) {
                var receiptId by remember(editing) { mutableStateOf(editing?.receiptId ?: "") }
                var amount by remember(editing) {
                    val plain = editing?.amount?.stripTrailingZeros()?.toPlainString() ?: ""
                    mutableStateOf(plain)
                }
                var description by remember(editing) { mutableStateOf(editing?.description ?: "") }
                var rtIndex by remember(editing) { mutableStateOf(0) } // Fixed to INWARD (0)
                var touched by remember { mutableStateOf(false) }
                var customDateMillis by remember(editing) {
                    mutableStateOf(
                        try {
                            editing?.createdAt?.let { s ->
                                java.time.OffsetDateTime.parse(s).toInstant().toEpochMilli()
                            }
                        } catch (_: Exception) {
                            null
                        }
                    )
                }
                val valid = receiptId.isNotBlank() && (amount.toDoubleOrNull()?.let { it > 0 } == true)
                val updatedStr = stringResource(Res.string.receipt_updated)

                ContentDialog(
                    title = stringResource(Res.string.save),
                    visible = true,
                    size = DialogSize.Max,
                    primaryButtonText = stringResource(Res.string.save),
                    closeButtonText = stringResource(Res.string.cancel),
                    onButtonClick = { btn ->
                        when (btn) {
                            ContentDialogButton.Primary -> {
                                touched = true
                                if (!valid || editing?.id == null) return@ContentDialog
                                val req = UpdateReceiptRequest(
                                    receiptId = receiptId.trim(),
                                    receiptType = ReceiptType.INWARD,
                                    entityType = EntityType.CUSTOMER,
                                    amount = amount.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO,
                                    description = description.ifBlank { null },
                                    customerId = editing?.customerId,
                                    createdAtMillis = customDateMillis
                                )
                                scope.launch {
                                    val res = ReceiptService.update(editing!!.id, req)
                                    if (res.isSuccess) {
                                        info = updatedStr; error = null; editing = null; load()
                                    } else {
                                        error = res.exceptionOrNull()?.message; info = null
                                    }
                                }
                            }

                            ContentDialogButton.Close -> editing = null
                            else -> Unit
                        }
                    },
                    content = {
                        Column(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = receiptId, onValueChange = { receiptId = it },
                                label = { Text(stringResource(Res.string.receipt_id)) },
                                isError = touched && receiptId.isBlank(),
                                singleLine = true, modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {

                                CalendarDatePicker(
                                    onChoose = { localDate ->
                                        val now = java.time.LocalDateTime.now()
                                        val ldt = java.time.LocalDateTime.of(
                                            localDate.year,
                                            localDate.monthValue + 1,
                                            localDate.day,
                                            now.hour,
                                            now.minute,
                                            now.second
                                        )
                                        val odt = java.time.OffsetDateTime.of(ldt, java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now()))
                                        customDateMillis = odt.toInstant().toEpochMilli()
                                    }
                                )
                            }
                            if (customDateMillis != null) {
                                Spacer(Modifier.height(6.dp))
                                val label = java.time.Instant.ofEpochMilli(customDateMillis!!)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                Text("Selected: $label")
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = amount,
                                onValueChange = {
                                    amount = it.filter { ch -> ch.isDigit() || ch == '.' }
                                        .replace(Regex("\\.(?=.*\\.)"), "")
                                },
                                label = { Text(stringResource(Res.string.amount)) },
                                isError = touched && (amount.toBigDecimalOrNull()?.let { it > java.math.BigDecimal.ZERO } != true),
                                singleLine = true, modifier = Modifier.fillMaxWidth())

                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description, onValueChange = { description = it },
                                label = { Text(stringResource(Res.string.description_optional)) },
                                singleLine = true, modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            }

            // --- DELETE DIALOG ---
            if (toDelete != null) {
                val r = toDelete!!
                val deletedStr = stringResource(Res.string.receipt_deleted)
                DeleteDialog(
                    showDeleteDialog = true,
                    title = stringResource(Res.string.delete_receipt_title),
                    message = stringResource(Res.string.delete_receipt_message, r.receiptId ?: "N/A")
                ) { btn ->
                    if (btn == ContentDialogButton.Primary) {
                        scope.launch {
                            val res = ReceiptService.delete(r.id)
                            if (res.isSuccess) {
                                info = deletedStr
                                error = null
                                toDelete = null
                                load()
                            } else {
                                error = res.exceptionOrNull()?.message
                                toDelete = null
                            }
                        }
                    } else {
                        toDelete = null
                    }
                }
            }

            // --- CREATE CUSTOMER RECEIPT DIALOG ---
            if (showForm) {
                var receiptId by remember { mutableStateOf("") }
                var amount by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                var touched by remember { mutableStateOf(false) }
                var creating by remember { mutableStateOf(false) }
                var customDateMillis by remember {
                    mutableStateOf<Long?>(
                        java.time.Instant.now().toEpochMilli()
                    )
                }
                val availableBalance = totalOutbound.minus(totalInbound)
                val amountParsed = amount.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
                val amountExceedsBalance = amountParsed > availableBalance
                val valid =
                    receiptId.isNotBlank() &&
                            (amount.toDoubleOrNull()?.let { it > 0 } == true) &&
                            !amountExceedsBalance &&
                            (selectedWarehouse != null && selectedCustomerId != null)

                ContentDialog(
                    title = "Create Customer Receipt",
                    visible = true,
                    size = DialogSize.Max,
                    primaryButtonText = if (creating) "Creating..." else "Create",
                    closeButtonText = if (creating) "Please wait" else "Cancel",
                    onButtonClick = { btn ->
                        when (btn) {
                            ContentDialogButton.Primary -> {
                                if (creating) return@ContentDialog
                                touched = true
                                if (!valid) return@ContentDialog
                                creating = true
                                val req = CreateReceiptRequest(
                                    receiptId = receiptId.trim(),
                                    receiptType = ReceiptType.INWARD,
                                    entityType = EntityType.CUSTOMER,
                                    warehouseId = selectedWarehouse!!.id,
                                    customerId = selectedCustomerId,
                                    amount = amount.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO,
                                    description = description.ifBlank { null },
                                    createdAtMillis = customDateMillis
                                )
                                scope.launch {
                                    val res = ReceiptService.create(req)
                                    if (res.isSuccess) {
                                        info = "Receipt created"; error = null; showForm = false; load()
                                    } else {
                                        error = res.exceptionOrNull()?.message; info = null
                                    }
                                    creating = false
                                }
                            }

                            ContentDialogButton.Close -> if (!creating)
                                showForm = false

                            else -> Unit
                        }
                    },
                    content = {
                        Column(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = receiptId,
                                onValueChange = { receiptId = it },
                                label = { Text("Receipt ID") },
                                isError = touched && receiptId.isBlank(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {

                                CalendarDatePicker(
                                    onChoose = { localDate ->
                                        val now = java.time.LocalDateTime.now()
                                        val ldt = java.time.LocalDateTime.of(
                                            localDate.year,
                                            localDate.monthValue + 1,
                                            localDate.day,
                                            now.hour,
                                            now.minute,
                                            now.second
                                        )
                                        val odt = ldt.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime()
                                        customDateMillis = odt.toInstant().toEpochMilli()
                                    }
                                )
                            }
                            if (customDateMillis != null) {
                                Spacer(Modifier.height(6.dp))
                                val label = java.time.Instant.ofEpochMilli(customDateMillis!!)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                Text("Selected: $label")
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Available balance: ${formatLocalized(availableBalance, Locale.getDefault())}",
                                color = FluentTheme.colors.text.accent.secondary
                            )
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = amount,
                                onValueChange = {
                                    amount =
                                        it.filter { ch -> ch.isDigit() || ch == '.' }
                                            .replace(Regex("\\.(?=.*\\.)"), "")
                                },
                                label = { Text("Amount") },
                                isError = touched && (amount.toBigDecimalOrNull()?.let { it > java.math.BigDecimal.ZERO } != true || amountExceedsBalance),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth())
                            if (touched && amountExceedsBalance) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Amount exceeds available balance (${formatLocalized(availableBalance, Locale.getDefault())})",
                                    color = FluentTheme.colors.system.critical
                                )
                            }

                            if (creating) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ProgressRing()
                                    Spacer(Modifier.width(8.dp))
                                    Text("Creating receipt...")
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description (optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            }
        }

        // Statement of Account overlay, rendered above the content
        if (showStatement) {
            val whId = selectedWarehouse?.id ?: 0L
            val customer = selectedCustomerIndex?.let { customers.getOrNull(it) }
            if (customer != null) {
                StatementOverlayDialog(
                    visible = true,
                    onDismiss = { showStatement = false }
                ) {
                    StateOfAccountScreen(
                        warehouseId = whId,
                        customer = customer,
                        start = startDate,
                        end = endDate,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
