package org.safieddine.ablogistics.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.FluentTheme
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.CustomerResponse
import org.safieddine.ablogistics.data.ReceiptResponse
import org.safieddine.ablogistics.data.ReceiptType
import org.safieddine.ablogistics.data.service.ReceiptService
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.safieddine.ablogistics.ui.screen.receipts.formatLocalized

@Composable
fun StateOfAccountScreen(
    warehouseId: Long,
    customer: CustomerResponse,
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
    var fromStr by remember { mutableStateOf("") }
    var endStr by remember { mutableStateOf("") }

    var dateRangeText = remember(start, end) {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yy")
         fromStr = start?.let {
            OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it),
                java.time.ZoneOffset.UTC
            ).format(formatter)
        }?:""
        endStr = end?.let {
            OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it),
                java.time.ZoneOffset.UTC
            ).format(formatter)
        }?:""
        if (fromStr.isNotEmpty() &&  endStr.isNotEmpty()) "From $fromStr to $endStr" else {
            ""
        }
    }

    LaunchedEffect(warehouseId, customer.id, start, end) {
        isLoading = true
        error = null
        scope.launch {
            val res = ReceiptService.listCustomer(
                warehouseId = warehouseId,
                customerId = customer.id,
                start = start,
                end = end,
                page = 0,
                size = 1000
            )
            if (res.isSuccess) {
                val data = res.getOrNull()?.data
                val list = data?.receipts ?: emptyList()
                // Sort receipts chronologically ascending to compute balances, then display descending
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
                endBalance = desc.lastOrNull()?.afterImpactFunds

                if ((start == null || end == null) && receipts.isNotEmpty() ){
                    val fromStr = receipts.firstOrNull()?.createdAt?.split("T")[0]
                    val toStr = receipts.lastOrNull()?.createdAt?.split("T")[0]
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
                    Text(
                        text = "ID: ${customer.id}",
                        modifier = Modifier.weight(1f),
                        style = FluentTheme.typography.body
                    )

                    Column(Modifier.weight(1.5f)) {
                        Text(customer.name)
                        Text(customer.phoneNumber)
                    }

                    Column {
                        Text(dateRangeText)
                        Text(
                            "Issued on ${
                                DateTimeFormatter.ofPattern("dd-MM-yy")
                                    .format(OffsetDateTime.now())
                            }"
                        )

                        Text("Transactions in ${Locale.getDefault().displayCountry}")
                    }
                }

                if (error != null) {
                    HorizontalDivider(thickness = 1.dp, color = Black)
                    Text(
                        text = error ?: "",
                        modifier = Modifier.padding(10.dp),
                        color = FluentTheme.colors.system.critical
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = Black)

                TableRow(
                    cells = listOf("date", "ourref", "label", "debit", "credit", "balance"),
                    weights = listOf(0.9f, 1.4f, 3.8f, 1.6f, 1.6f, 2.2f),
                    isHeader = true,
                    withVerticalDividers = true
                )

                HorizontalDivider(thickness = 1.dp, color = Black)

                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (startBalance != null) {
                        val statedAt = fromStr.ifEmpty {
                            receipts.firstOrNull()?.createdAt?.split("T")[0]
                        }

                        TableRow(
                            cells = listOf("", "", "Balance at $statedAt", "", "", formatLocalized(startBalance!!)),
                            weights = listOf(0.9f, 1.4f, 3.8f, 1.6f, 1.6f, 2.2f),
                            withVerticalDividers = true
                        )
                        HorizontalDivider(thickness = 1.dp, color = Black)
                    }
                receipts.forEach { r ->
                    val dateStr = r.createdAt?.split("T")[0] ?:""

                    val label = r.description ?: ""
                    val credit: java.math.BigDecimal = if (r.receiptType == ReceiptType.OUTWARD && !r.isReturnAdjustment) r.amount else java.math.BigDecimal.ZERO
                    val debit: java.math.BigDecimal  = if (r.receiptType == ReceiptType.INWARD || r.isReturnAdjustment) r.amount else java.math.BigDecimal.ZERO
                    TableRow(
                        cells = listOf(
                            dateStr,
                            r.receiptId ?: "",
                            label,
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
                        val endAt = endStr.ifEmpty {
                            receipts.lastOrNull()?.createdAt?.split("T")[0]
                        }
                        TableRow(
                            cells = listOf("", "", "Balance at $endAt", "", "", formatLocalized(endBalance!!)),
                            weights = listOf(0.9f, 1.4f, 3.8f, 1.6f, 1.6f, 2.2f),
                            withVerticalDividers = true
                        )
                    }

                    HorizontalDivider(thickness = 1.dp, color = Black)

                    // Footer notes
                    val footerDate = end?.let {
                        OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), java.time.ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("dd-MM-yy"))
                    } ?: OffsetDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yy"))
                    val footerAmount = endBalance ?: java.math.BigDecimal.ZERO
                    Text(
                        text = "On $footerDate, You Owe Us The Amount Of FCFA ${formatLocalized(footerAmount)}.",
                        modifier = Modifier.padding(10.dp),
                        style = FluentTheme.typography.body.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "N.B: ATTENTION AUX CHANGEMENTS DE PRIX",
                        modifier = Modifier.padding(start = 10.dp, bottom = 10.dp),
                        style = FluentTheme.typography.body.copy(fontWeight = FontWeight.Bold)
                    )

                }

                if (isLoading) {
                    Text(
                        "Loading...",
                        modifier = Modifier.padding(10.dp),
                        style = FluentTheme.typography.body.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
fun TableRow(
    cells: List<String>,
    weights: List<Float>,
    isHeader: Boolean = false,
    withVerticalDividers: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        cells.forEachIndexed { index, text ->
            Box(
                Modifier
                    .weight(weights[index])
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                contentAlignment = androidx.compose.ui.Alignment.CenterStart
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    fontSize = if (isHeader) 14.sp else 13.sp,
                    fontWeight = if (isHeader) FontWeight.SemiBold
                    else
                        FontWeight.Bold,
                    textAlign = when (index) {
                        3, 4 -> TextAlign.End
                        2, 5 -> TextAlign.Center
                        else -> TextAlign.Start
                    }
                )
            }

            if (withVerticalDividers && index < cells.size - 1) {
                VerticalDivider(thickness = 1.dp, color = Black)
            }
        }
    }
}
