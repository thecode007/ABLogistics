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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.*
import io.github.composefluent.icons.regular.Add
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.TextClearFormatting
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ablogistics.composeapp.generated.resources.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.service.ReceiptService
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.data.session.WarehouseFundsStore
import org.safieddine.ablogistics.ui.screen.adminScreen.UserTableShimmerRow
import org.safieddine.ablogistics.ui.screen.StatementOverlayDialog
import org.safieddine.ablogistics.ui.screen.WarehouseStatementScreen
import org.safieddine.ablogistics.ui.theme.DeleteDialog
import java.util.Locale
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalFluentApi::class)
@Composable
fun ReceiptsAdminScreen() {
    val scope = rememberCoroutineScope()
    val selectedWarehouse by SessionStore.selectedWarehouse.collectAsState()

    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    // Filters + pagination
    var typeIndex by remember { mutableStateOf(0) }
    // 0 All, 1 INWARD, 2 OUTWARD, 3 RETURNED
    var search by remember { mutableStateOf("") }
    var page by remember { mutableStateOf(0) }
    var size by remember { mutableStateOf(100) }
    var isLoading by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf(PageResponse<ReceiptResponse>()) }

    var toDelete by remember { mutableStateOf<ReceiptResponse?>(null) }
    var editing by remember { mutableStateOf<ReceiptResponse?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var showStatement by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }


    fun load() {
        val wh = selectedWarehouse?.id ?: return
        isLoading = true
        scope.launch {
            val type = when (typeIndex) {
                1 -> ReceiptType.INWARD
                2 -> ReceiptType.OUTWARD
                3 -> ReceiptType.RETURNED
                else -> null
            }


            val res = ReceiptService.listWarehouse(
                warehouseId = wh,
                type = type,
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

    LaunchedEffect(selectedWarehouse) {
        page = 0
        load()
    }

    // Debounced search
    LaunchedEffect(search) {
        page = 0
        delay(350)
        load()
    }

    // Use a Box so the statement dialog can overlay the content cleanly
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier.width(300.dp),
                    value = search,
                    onValueChange = { search = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(Res.string.search_by_id)) }
                )
                Spacer(Modifier.width(8.dp))
                ComboBox(
                    placeholder = stringResource(Res.string.type),
                    selected = typeIndex,
                    items = listOf("All", "INWARD", "OUTWARD"),
                    onSelectionChange = { i, _ -> typeIndex = i; page = 0; load() }
                )

                var clearDatesKey by remember {
                    mutableStateOf(System.currentTimeMillis())
                }

                Spacer(Modifier.weight(1f))


                key(clearDatesKey) {
                    CalendarDatePicker(
                        onChoose = { localDate ->
                            val startOfDay = java.time.LocalDateTime.of(
                                localDate.year,
                                localDate.monthValue + 1, // Fix: Convert 0-based to 1-based month
                                localDate.day,
                                0, 0, 0
                            )
                            startDate = startOfDay.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()

                            page = 0
                            load()
                        }
                    )

                    Spacer(Modifier.width(12.dp))

                    CalendarDatePicker(
                        onChoose = { localDate ->
                            println("=== END DATE CLICKED ===")
                            println("Raw: Year=${localDate.year}, Month=${localDate.monthValue}, Day=${localDate.day}")

                            // Add +1 to monthValue since it's 0-based (0=Jan, 11=Dec)
                            val endOfDay = java.time.LocalDateTime.of(
                                localDate.year,
                                localDate.monthValue + 1, // Fix: Convert 0-based to 1-based month
                                localDate.day,
                                23, 59, 59, 0
                            ).plusNanos(999_000_000)

                            endDate = endOfDay.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()

                            println("Converted to: $endOfDay")
                            println("======================")

                            page = 0
                            load()
                        }
                    )
                }



                SubtleButton(
                    iconOnly = true,
                    onClick = {
                        clearDatesKey = System.currentTimeMillis()
                        startDate = null
                        endDate = null
                        page = 0
                        load()
                    }
                ) {
                    Icon(
                        Icons.Default.TextClearFormatting,
                        contentDescription = "Clear date filters"
                    )
                }



                Spacer(Modifier.width(5.dp))

                SubtleButton(iconOnly = true, onClick = { load() }) {
                    Icon(
                        Icons.Default.ArrowCounterclockwise,
                        contentDescription = stringResource(Res.string.reload)
                    )
                }
                Spacer(Modifier.width(6.dp))
                SubtleButton(iconOnly = true, onClick = { showForm = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(Res.string.add_receipt)
                    )
                }
                Spacer(Modifier.width(6.dp))
                SubtleButton(
                    iconOnly = true,
                    onClick = { showStatement = true }
                ) {
                    Icon(Icons.Filled.PersonNote, contentDescription = "Statement of Account")
                }
            }
// TODO add flag in settigns later
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        Row(
//            modifier = Modifier.width(500.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Card(Modifier) {
//                Text(
//                    modifier = Modifier.padding(8.dp),
//                    text = stringResource(Res.string.admin),
//                    style = FluentTheme.typography.title
//                )
//            }
//
//            Column(
//                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//
//                InboundArrow(amount = "%.2f".format(totalInbound))
//
//                val total = totalInbound.minus(totalOutbound)
//
//                val locale = Locale.getDefault()
//                val numericValue = parseLocalizedNumber("%.2f".format(total), locale)
//                val formattedValue = formatLocalized(numericValue, locale)
//
//                Text(
//                    text = formattedValue,
//                    color = FluentTheme.colors.system.success,
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                OutboundArrow(amount = "%.2f".format(totalOutbound))
//            }
//
//            Card(Modifier) {
//                Text(
//                    modifier = Modifier.padding(8.dp),
//                    text = stringResource(Res.string.warehouse),
//                    style = FluentTheme.typography.title
//                )
//            }
//        }
//    }

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

                scope.launch {
                    delay(2000)
                    info = null
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!isLoading && data.content.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Receipt,
                        contentDescription = "No receipts",
                        tint = FluentTheme.colors.background.smoke.default,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.no_receipts_found),
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
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "ID",
                                Modifier.weight(1f),
                                style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(Res.string.type),
                                modifier = Modifier.weight(0.8f),
                                style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(Res.string.amount),
                                Modifier.weight(1f),
                                style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(Res.string.description_header),
                                Modifier.weight(1.6f),
                                style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(Res.string.actions),
                                Modifier.weight(1f),
                                style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(Res.string.date_header),
                                Modifier.weight(1.2f),
                                style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = stringResource(Res.string.impact),
                                Modifier.weight(1.2f),
                                style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                                textAlign = TextAlign.Center
                            )
                        }
                        HorizontalDivider(
                            color = FluentTheme.colors.background.mica.base.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }

                    if (isLoading) {
                        items(5) {
                            UserTableShimmerRow()
                            HorizontalDivider(
                                color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                                thickness = 0.5.dp
                            )
                        }
                    } else {
                        itemsIndexed(data.content) { _, r ->
                            val statusColor = when (r.receiptType) {
                                ReceiptType.RETURNED -> FluentTheme.colors.system.attention
                                ReceiptType.INWARD -> Black
                                else -> FluentTheme.colors.system.critical
                            }
                            val icon = when (r.receiptType) {
                                ReceiptType.RETURNED -> Icons.Filled.ArrowBounce
                                ReceiptType.INWARD -> Icons.Filled.ArrowTrending
                                else -> Icons.Filled.ArrowTrendingDown
                            }

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(statusColor.copy(alpha = 0.1f))
                                    .padding(vertical = 4.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(r.receiptId, Modifier.weight(1f), textAlign = TextAlign.Center)
                                Row(Modifier.weight(0.8f), horizontalArrangement = Arrangement.Center) {
                                    Icon(imageVector = icon, contentDescription = "", tint = statusColor)
                                    Spacer(Modifier.width(4.dp))
                                    Text(r.receiptType.name, color = statusColor, textAlign = TextAlign.Center)
                                }

                                val realFunds = r.amount

                                val numericValueF = parseLocalizedNumber("%.2f".format(realFunds), Locale.getDefault())
                                val formattedValue = formatLocalized(numericValueF, Locale.getDefault())

                                Text(formattedValue, Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(r.description ?: "", Modifier.weight(1.6f), textAlign = TextAlign.Center)
                                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                                    IconButton(onClick = { editing = r }) {
                                        Icon(
                                            Icons.Filled.Pen,
                                            contentDescription = "",
                                            tint = FluentTheme.colors.system.attention
                                        )
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    IconButton(onClick = { toDelete = r }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "",
                                            tint = FluentTheme.colors.system.critical
                                        )
                                    }
                                }
                                Text(
                                    formatDate(r.createdAt),
                                    Modifier.weight(1.2f),
                                    textAlign = TextAlign.Center
                                )


                                val previousColor = when (r.receiptType) {
                                    ReceiptType.INWARD -> {
                                        FluentTheme.colors.system.critical
                                    }

                                    ReceiptType.OUTWARD -> {
                                        Black
                                    }

                                    ReceiptType.RETURNED -> {
                                        Black
                                    }
                                }

                                Row(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        color = previousColor,
                                        text = formatLocalized(r.beforeImpactFunds, Locale.getDefault()),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(Modifier.width(3.dp))

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = "",
                                        tint = statusColor
                                    )

                                    Spacer(Modifier.width(3.dp))

                                    Text(
                                        formatLocalized(r.afterImpactFunds, Locale.getDefault()),
                                        textAlign = TextAlign.Center,
                                        color = statusColor
                                    )
                                }


                            }
                            HorizontalDivider(
                                color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

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
                Button(
                    disabled = page >= data.totalPages - 1,
                    onClick = {
                        if (page < data.totalPages - 1) {
                            page += 1; load()
                        }
                    }) { Text("Next") }
            }

            // existing dialogs: editing/delete/create remain where they are below
        }

        // Statement overlay (full screen), uses the same API of the admin screen (listWarehouse)
        if (showStatement) {
            val whId = selectedWarehouse?.id ?: 0L
            StatementOverlayDialog(
                visible = true,
                onDismiss = { showStatement = false }
            ) {
                val type = when (typeIndex) {
                    1 -> ReceiptType.INWARD
                    2 -> ReceiptType.OUTWARD
                    3 -> ReceiptType.RETURNED
                    else -> null
                }
                WarehouseStatementScreen(
                    warehouseId = whId,
                    type = type,
                    start = startDate,
                    end = endDate,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }


    if (showForm) {
        var receiptId by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var rtIndex by remember { mutableStateOf(0) } // 0 INWARD, 1 OUTWARD
        var touched by remember { mutableStateOf(false) }
        var creating by remember { mutableStateOf(false) }
        var useCustomDate by remember { mutableStateOf(true) }
        var customDateIso by remember { mutableStateOf<String?>(null) }
        var customDateMillis by remember {
            mutableStateOf<Long?>(
                java.time.LocalDate.now(java.time.ZoneOffset.UTC)
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            )
        }
        val valid =
            receiptId.isNotBlank() &&
                    (amount.toDoubleOrNull()?.let { it > 0 } == true)
                    && (if (useCustomDate) customDateMillis != null else true)
                    && selectedWarehouse != null

        ContentDialog(
            title = "Create Warehouse Receipt",
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
                        val req = CreateReceiptRequest(
                            receiptId = receiptId.trim(),
                            receiptType = if (rtIndex == 0) ReceiptType.INWARD else ReceiptType.OUTWARD,
                            entityType = EntityType.WAREHOUSE,
                            warehouseId = selectedWarehouse!!.id,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            description = description.ifBlank { null },
                            createdAtMillis = if (useCustomDate) customDateMillis else null
                        )
                        scope.launch {
                            creating = true
                            val res = ReceiptService.create(req)
                            if (res.isSuccess) {
                                info = "Receipt created"
                                error = null
                                showForm = false
                                load()
                            } else {
                                error = res.exceptionOrNull()?.message; info = null
                            }
                            creating = false
                        }
                    }

                    ContentDialogButton.Close -> if (!creating) showForm = false
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
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ComboBox(
                            placeholder = "Type",
                            selected = rtIndex,
                            items = listOf("INWARD", "OUTWARD"),
                            onSelectionChange = { i, _ -> rtIndex = i })
                        Spacer(Modifier.weight(1f))

                        CalendarDatePicker(
                            onChoose = { localDate ->
                                val ldt = java.time.LocalDateTime.of(
                                    localDate.year,
                                    localDate.monthValue + 1,
                                    localDate.day,
                                    0, 0, 0
                                )
                                val odt = OffsetDateTime.of(ldt, java.time.ZoneOffset.UTC)
                                customDateIso = odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                customDateMillis = odt.toInstant().toEpochMilli()
                            }
                        )
                    }
                    if (customDateMillis != null) {
                        Spacer(Modifier.height(6.dp))
                        val label = java.time.Instant.ofEpochMilli(customDateMillis!!)
                            .atOffset(java.time.ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        Text("Selected: $label")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount =
                                it.filter { ch -> ch.isDigit() || ch == '.' }
                                    .replace(Regex("\\.(?=.*\\.)"), "")
                            it.filter { ch -> ch.isDigit() || ch == '.' }
                                .replace(Regex("\\.(?=.*\\.)"), "")
                        },
                        label = { Text("Amount") },
                        supportingText = {
                            if (amount.isNotBlank()) {
                                val v = amount.toDoubleOrNull()
                                if (v != null) Text(formatLocalized(v, Locale.getDefault()))
                            }
                        },
                        isError = touched && (amount.toDoubleOrNull()?.let { it > 0 } != true),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    if (creating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProgressRing()
                            Spacer(Modifier.width(8.dp))
                            Text("Creating receipt...")
                        }
                        Spacer(Modifier.height(8.dp))
                    }
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

    if (editing != null) {
        var receiptId by remember(editing) { mutableStateOf(editing?.receiptId ?: "") }
        var amount by remember(editing) {
            // Avoid scientific notation when prefilling amount; keep a plain string
            val plain =
                editing?.amount?.let { java.math.BigDecimal.valueOf(it).stripTrailingZeros().toPlainString() } ?: ""
            mutableStateOf(plain)
        }
        var description by remember(editing) { mutableStateOf(editing?.description ?: "") }
        var rtIndex by remember(editing) { mutableStateOf(if (editing?.receiptType == ReceiptType.INWARD) 0 else 1) }
        var touched by remember { mutableStateOf(false) }
        var customDateMillis by remember(editing) {
            mutableStateOf(
                try {
                    editing?.createdAt?.let { s ->
                        OffsetDateTime.parse(s).toInstant().toEpochMilli()
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
                            receiptType = if (rtIndex == 0) ReceiptType.INWARD else ReceiptType.OUTWARD,
                            entityType = EntityType.WAREHOUSE,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            description = description.ifBlank { null },
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
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ComboBox(
                            placeholder = stringResource(Res.string.type), selected = rtIndex,
                            items = listOf("INWARD", "OUTWARD"),
                            onSelectionChange = { i, _ -> rtIndex = i })

                        Spacer(Modifier.weight(1f))

                        CalendarDatePicker(
                            onChoose = { localDate ->
                                val ldt = java.time.LocalDateTime.of(
                                    localDate.year,
                                    localDate.monthValue + 1,
                                    localDate.day,
                                    0, 0, 0
                                )
                                val odt = OffsetDateTime.of(ldt, java.time.ZoneOffset.UTC)
                                customDateMillis = odt.toInstant().toEpochMilli()
                            }
                        )
                    }
                    if (customDateMillis != null) {
                        Spacer(Modifier.height(6.dp))
                        val label = java.time.Instant.ofEpochMilli(customDateMillis!!)
                            .atOffset(java.time.ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
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
                        supportingText = {
                            if (amount.isNotBlank()) {
                                val v = amount.toDoubleOrNull()
                                if (v != null) Text(formatLocalized(v, Locale.getDefault()))
                            }
                        },
                        isError = touched && (amount.toDoubleOrNull()?.let { it > 0 } != true),
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

    val deletedStr = stringResource(Res.string.receipt_deleted)
    DeleteDialog(
        showDeleteDialog = toDelete != null,
        title = stringResource(Res.string.delete_receipt_title),
        message = stringResource(
            Res.string.delete_receipt_message,
            toDelete?.receiptId ?: ""
        )
    ) { btn ->
        if (btn == ContentDialogButton.Primary && toDelete?.id != null) {
            scope.launch {
                val res = ReceiptService.delete(toDelete!!.id)
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
        } else
            toDelete = null
    }


}

fun formatDate(s: String?): String {
    if (s.isNullOrBlank()) return ""
    return try {
        OffsetDateTime.parse(s)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (_: Exception) {
        s.take(16)
    }
}
