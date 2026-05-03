package org.safieddine.ablogistics.ui.screen.receipts

import ablogistics.composeapp.generated.resources.Res
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
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
import io.github.composefluent.icons.filled.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.TextClearFormatting
import io.github.composefluent.surface.Card
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ablogistics.composeapp.generated.resources.*
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.service.ReceiptService
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.data.session.WarehouseFundsStore
import org.safieddine.ablogistics.ui.screen.adminScreen.UserTableShimmerRow
import java.util.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFluentApi::class, ExperimentalTime::class)
@Composable
fun ColumnScope.WarehouseSummaryScreen() {
    val scope = rememberCoroutineScope()
    val selectedWarehouse by SessionStore.selectedWarehouse.collectAsState()

    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    // Dates
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var clearDatesKey by remember { mutableStateOf(System.currentTimeMillis()) }

    var inboundWarehouse by remember { mutableStateOf(0.0) }
    var outboundWarehouse by remember { mutableStateOf(0.0) }
    var inboundCustomer by remember { mutableStateOf(0.0) }
    var outboundCustomer by remember { mutableStateOf(0.0) }

    // Pagination
    var page by remember { mutableStateOf(0) }
    var size by remember { mutableStateOf(100) }
    var isLoading by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf(PageResponse<ReceiptResponse>()) }

    fun load() {
        val wh = selectedWarehouse?.id ?: return
        isLoading = true
        scope.launch {
            val res = ReceiptService.listWarehouseDetailed(
                warehouseId = wh,
                type = null,
                start = startDate,
                end = endDate,
                page = page,
                size = size,
                receiptId = null
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
                    inboundWarehouse = summary.inboundByEntityType["WAREHOUSE"] ?: 0.0
                    outboundWarehouse = summary.outboundByEntityType["WAREHOUSE"] ?: 0.0
                    inboundCustomer = summary.inboundByEntityType["CUSTOMER"] ?: 0.0
                    outboundCustomer = summary.outboundByEntityType["CUSTOMER"] ?: 0.0
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

    // --- HEADER ---
    Row(Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
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
                    startDate = startOfDay.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                    page = 0; load()
                })
            Spacer(Modifier.width(12.dp))
            CalendarDatePicker(
                onChoose = { localDate ->
                    val endOfDay = java.time.LocalDateTime.of(
                        localDate.year,
                        localDate.monthValue + 1, // picker month is 0-based
                        localDate.day,
                        23, 59, 59, 0
                    ).plusNanos(999_000_000) // include full last second
                    endDate = endOfDay.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                    page = 0
                    load()
                }
            )
        }

        SubtleButton(iconOnly = true, onClick = {
            clearDatesKey = System.currentTimeMillis()
            startDate = null
            endDate = null
            page = 0
            load()
        }) {
            Icon(Icons.Regular.TextClearFormatting,
                contentDescription = "Clear Dates")
        }

        Spacer(Modifier.width(5.dp))
        SubtleButton(iconOnly = true, onClick = { load() }) {
            Icon(Icons.Regular.ArrowCounterclockwise,
                contentDescription = stringResource(Res.string.reload))
        }
    }

    Spacer(Modifier.height(16.dp))

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.width(750.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Card(Modifier) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(Res.string.admin),
                    style = FluentTheme.typography.title
                )
            }

            // Arrows between Admin and Warehouse (use WAREHOUSE split totals)
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InboundArrow(amount = "%.2f".format(inboundWarehouse))
                val total = inboundWarehouse - outboundWarehouse
                val locale = Locale.getDefault()
                val numericValue = parseLocalizedNumber("%.2f".format(total), locale)
                val formattedValue = formatLocalized(numericValue, locale)
                Text(
                    text = formattedValue,
                    color = FluentTheme.colors.system.success,
                    fontWeight = FontWeight.SemiBold
                )
                OutboundArrow(amount = "%.2f".format(outboundWarehouse))
            }

            Card(Modifier) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(Res.string.warehouse),
                    style = FluentTheme.typography.title
                )
            }

            // Extra arrows between Warehouse and Customer (use CUSTOMER split totals)
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InboundArrow(amount = "%.2f".format(outboundCustomer))
                val total = outboundCustomer - inboundCustomer
                val locale = Locale.getDefault()
                val numericValue = parseLocalizedNumber("%.2f".format(total), locale)
                val formattedValue = formatLocalized(numericValue, locale)
                Text(
                    text = formattedValue,
                    color = FluentTheme.colors.system.success,
                    fontWeight = FontWeight.SemiBold
                )
                OutboundArrow(amount = "%.2f".format(inboundCustomer))
            }

            Card(Modifier) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(Res.string.customer),
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

    Spacer(Modifier.height(12.dp))

    // --- TABLE (read-only) ---
    if (!isLoading && data.content.isEmpty()) {
        Column(
            Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Receipt,
                contentDescription = "No receipts",
                tint = FluentTheme.colors.background.smoke.default,
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(stringResource(Res.string.no_receipts_found),
                color = FluentTheme.colors.background.smoke.default)
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
                    Text(stringResource(Res.string.id_header),
                        Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Text(stringResource(Res.string.type), Modifier.weight(0.8f), textAlign = TextAlign.Center, fontSize = 14.sp)
                    Text(stringResource(Res.string.amount), Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 14.sp)
                    Text(stringResource(Res.string.description_header), Modifier.weight(1.6f), textAlign = TextAlign.Center, fontSize = 14.sp)
                    Text(stringResource(Res.string.date_header), Modifier.weight(1.2f), textAlign = TextAlign.Center, fontSize = 14.sp)
                    Text(text = stringResource(Res.string.impact), Modifier.weight(1.2f), textAlign = TextAlign.Center, fontSize = 14.sp)
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
                        else -> FluentTheme.colors.system.critical
                    }

                    if (r.isReturned)
                        statusColor = FluentTheme.colors.system.attention

                    val icon = when (r.receiptType) {
                        ReceiptType.RETURNED -> Icons.Filled.ArrowBounce
                        ReceiptType.INWARD -> Icons.Filled.ArrowTrending
                        else -> Icons.Filled.ArrowTrendingDown
                    }

                    Row(
                        Modifier.fillMaxWidth()
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(vertical = 4.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            Icon(if (r.entityType == EntityType.WAREHOUSE)
                                Icons.Filled.BuildingBank
                                else
                                Icons.Filled.PeopleMoney ,
                                contentDescription = "",
                                tint = FluentTheme.colors.background.smoke.default,
                                modifier = Modifier.size(14.dp))
                        }
                        Text(r.receiptId, Modifier.weight(1f), textAlign = TextAlign.Center)
                        Row(Modifier.weight(0.8f), horizontalArrangement = Arrangement.Center) {
                            Icon(icon, contentDescription = "", tint = statusColor)
                            Spacer(Modifier.width(4.dp))
                            Text(r.receiptType.name, color = statusColor)
                        }
                        val numericValueF = parseLocalizedNumber("%.2f".format(r.amount), Locale.getDefault())
                        val formattedValue = formatLocalized(numericValueF, Locale.getDefault())
                        Text(formattedValue, Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(r.description ?: "", Modifier.weight(1.6f), textAlign = TextAlign.Center)
                        Text(formatDate(r.createdAt), Modifier.weight(1.2f), textAlign = TextAlign.Center)

                        val previousColor = if (r.receiptType == ReceiptType.OUTWARD) FluentTheme.colors.system.critical else Black
                        Row(
                            modifier = Modifier.weight(1.2f),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                color = previousColor,
                                text = formatLocalized(r.warehouseRealFundsBefore, Locale.getDefault()),
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
                                formatLocalized(r.warehouseRealFundsAfter, Locale.getDefault()),
                                textAlign = TextAlign.Center,
                                color = statusColor
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
            if (page > 0) { page -= 1; load() }
        }) { Text(stringResource(Res.string.prev)) }
        Spacer(Modifier.width(8.dp))
        Button(disabled = page >= data.totalPages - 1, onClick = {
            if (page < data.totalPages - 1) { page += 1; load() }
        }) { Text(stringResource(Res.string.next)) }
    }
}
