package org.safieddine.ablogistics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.FluentTheme
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.ReceiptResponse
import org.safieddine.ablogistics.data.ReceiptType
import org.safieddine.ablogistics.data.WarehouseDTO
import org.safieddine.ablogistics.data.service.ReceiptService
import org.safieddine.ablogistics.data.service.WarehouseService
import org.safieddine.ablogistics.ui.screen.receipts.formatLocalized
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WarehouseStatementScreen(
    warehouseId: Long,
    type: ReceiptType? = null,
    start: Long? = null,
    end: Long? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var receipts by remember { mutableStateOf<List<ReceiptResponse>>(emptyList()) }
    var startBalance by remember { mutableStateOf<java.math.BigDecimal?>(null) }
    var endBalance by remember { mutableStateOf<java.math.BigDecimal?>(null) }
    var wh: WarehouseDTO? by remember { mutableStateOf(null) }
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yy") }

    var fromStr = remember(start) { start?.let { OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), java.time.ZoneOffset.UTC).format(formatter) } }
    var toStr = remember(end) { end?.let { OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), java.time.ZoneOffset.UTC).format(formatter) } }
    var dateRangeText = remember(fromStr, toStr) {
        if (fromStr != null || toStr != null) "From ${fromStr ?: "-"} to ${toStr ?: "-"}" else null
    }
    LaunchedEffect(warehouseId, type, start, end) {
        isLoading = true
        error = null
        scope.launch {
            // Load warehouse details (manager/person in charge)
            WarehouseService.getWarehouse(warehouseId).onSuccess { resp ->
                wh = resp.data
            }



            val res = ReceiptService.listWarehouse(
                warehouseId = warehouseId,
                type = type,
                start = start,
                end = end,
                page = 0,
                size = 10000
            )
            if (res.isSuccess) {
                val data = res.getOrNull()?.data
                val list = data?.receipts ?: emptyList()
                // Compute balances from true chronological order; display newest first
                val asc = list.sortedBy { r ->
                    try {
                        r.createdAt?.let { OffsetDateTime.parse(it).toInstant().toEpochMilli() } ?: Long.MIN_VALUE
                    } catch (_: Exception) {
                        Long.MIN_VALUE
                    }
                }
                val desc = asc.asReversed()
                receipts = desc
                startBalance = desc.firstOrNull()?.beforeImpactFunds
                endBalance = desc.lastOrNull()?.afterImpactFunds ?: startBalance

                if ((start == null || end == null) && receipts.isNotEmpty() ){
                    fromStr = receipts.firstOrNull()?.createdAt?.split("T")[0]
                    toStr = receipts.lastOrNull()?.createdAt?.split("T")[0]
                    dateRangeText = "From ${fromStr ?: "-"} to ${toStr ?: "-"}"
                }
            } else {
                error = res.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }



    Column(modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.primary)
                .padding(6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Statement of Account",
                style = FluentTheme.typography.title.copy(
                    fontSize = 20.sp,
                    color = White
                )
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, Black)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp)) {
                    // Left: Warehouse ID/Name
                    Column(Modifier.weight(1f)) {
                        val wid = wh?.id ?: warehouseId
                        val wname = wh?.name ?: ""
                        Text("Warehouse: ${wname.ifBlank { wid.toString() }}")
                    }

                    // Middle: Person in charge (name + phone)
                    Column(Modifier.weight(1.5f)) {
                        val manager = wh?.users?.firstOrNull()
                        Text(manager?.fullName ?: "Person in charge: -")
                        if (!manager?.phoneNumber.isNullOrBlank())
                            Text(manager.phoneNumber)
                    }

                    // Right: Date range and issuance
                    Column {
                        Text(dateRangeText ?: "All Dates")
                        Text("Issued on ${DateTimeFormatter.ofPattern("dd-MM-yy").format(OffsetDateTime.now())}")
                        Text("Transactions in ${java.util.Locale.getDefault().displayCountry}")
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = Black)

                TableRow(
                    cells = listOf("Date", "Our Ref", "Label", "Debit", "Credit", "Balance"),
                    weights = listOf(0.9f, 1.4f, 3.8f, 1.6f, 1.6f, 2.2f),
                    isHeader = true,
                    withVerticalDividers = true
                )

                HorizontalDivider(thickness = 1.dp, color = Black)

                Column (Modifier.verticalScroll(rememberScrollState())) {

                    if (startBalance != null) {
                        val startLabel = if (fromStr != null) "Balance at $fromStr" else "Balance at start"
                        TableRow(
                            cells = listOf("", "", startLabel, "", "", formatLocalized(startBalance!!)),
                            weights = listOf(0.9f, 1.4f, 3.8f, 1.6f, 1.6f, 2.2f),
                            withVerticalDividers = true
                        )
                        HorizontalDivider(thickness = 1.dp, color = Black)
                    }

                    receipts.forEachIndexed { _, r ->
                        val dateStr = r.createdAt?.split("T")[0] ?:""

                        // For warehouse statement: Debit = INWARD, Credit = OUTWARD (or return adjustments)
                        val debit = if (r.receiptType == ReceiptType.INWARD && !r.isReturnAdjustment) r.amount else java.math.BigDecimal.ZERO
                        val credit = if (r.receiptType == ReceiptType.OUTWARD || r.isReturnAdjustment) r.amount else java.math.BigDecimal.ZERO

                        TableRow(
                            cells = listOf(
                                dateStr,
                                r.receiptId ?: "",
                                r.description ?: "",
                                if (debit > java.math.BigDecimal.ZERO) formatLocalized(debit) else "",
                                if (credit > java.math.BigDecimal.ZERO) formatLocalized(credit) else "",
                                formatLocalized(r.afterImpactFunds)
                            ),
                            weights = listOf(0.9f, 1.4f, 3.8f, 1.6f, 1.6f, 2.2f),
                            withVerticalDividers = true
                        )

                        HorizontalDivider(thickness = 1.dp, color = Black)
                    }


                    if (endBalance != null) {
                        val endLabel = if (toStr != null) "Balance at $toStr" else "Balance at end"
                        TableRow(
                            cells = listOf("", "", endLabel, "", "", formatLocalized(endBalance!!)),
                            weights = listOf(0.9f, 1.4f, 3.8f, 1.6f, 1.6f, 2.2f),
                            withVerticalDividers = true
                        )
                    }

                }
                if (isLoading) {
                    Text(
                        "Loading...",
                        modifier = Modifier.padding(10.dp),
                        style = FluentTheme.typography.body
                    )
                }
            }
        }
    }
}
