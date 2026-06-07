package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
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
import io.github.composefluent.icons.filled.PeopleMoney
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.Checkmark
import io.github.composefluent.icons.regular.Print
import org.safieddine.ablogistics.data.BrvPaymentTodoResponse
import org.safieddine.ablogistics.ui.utils.PdfExporter
import org.safieddine.ablogistics.ui.theme.ABLogisticsAccentButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsSubtleButton
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFluentApi::class)
@Composable
fun RowScope.BrvPaymentsScreen(viewModel: BrvPaymentsViewModel = remember { BrvPaymentsViewModel() }) {
    val payments by viewModel.payments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPayments()
    }

    Column(Modifier.padding(16.dp).weight(1f).fillMaxHeight()) {
        // Header Bar with Title on left and Actions on right
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title and Icon (Left)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.PeopleMoney,
                    contentDescription = "",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "BRV Payments Todo",
                    style = FluentTheme.typography.title
                )
            }
            
            Spacer(Modifier.weight(1f))
            
            // Actions (Right)
            ABLogisticsSubtleButton(iconOnly = true, onClick = { viewModel.loadPayments() }) {
                Icon(Icons.Default.ArrowCounterclockwise, contentDescription = "Refresh")
            }
            
            Spacer(Modifier.width(6.dp))
            
            ABLogisticsSubtleButton(iconOnly = true, onClick = { PdfExporter.generateBrvPaymentsPdf(payments) }) {
                Icon(Icons.Regular.Print, contentDescription = "Print List")
            }
            
            Spacer(Modifier.width(6.dp))
            
            val tickedCount = payments.count { it.isTicked }
            ABLogisticsAccentButton(
                onClick = { if (tickedCount > 0) showConfirmDialog = true },
                disabled = tickedCount == 0,
                iconOnly = true
            ) {
                Icon(Icons.Regular.Checkmark, contentDescription = "Confirm Payments")
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
                        message = { Text(error!!) },
                        closeAction = { InfoBarDefaults.CloseActionButton(onClick = { viewModel.clearMessages() }) }
                    )
                }
                if (success != null) {
                    InfoBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { Text("Success") },
                        severity = InfoBarSeverity.Success,
                        message = { Text(success!!) },
                        closeAction = { InfoBarDefaults.CloseActionButton(onClick = { viewModel.clearMessages() }) }
                    )
                }
            }
        }

        // Table Content
        Spacer(Modifier.height(16.dp))

        if (!isLoading && payments.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PeopleMoney,
                    contentDescription = "No payments",
                    tint = FluentTheme.colors.background.smoke.default,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "No pending BRV payments found.",
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
                        TableHeader("Tick", Modifier.weight(0.5f))
                        TableHeader("Date", Modifier.weight(1.5f))
                        TableHeader("BRV Plate", Modifier.weight(1f))
                        TableHeader("Customer", Modifier.weight(1.5f))
                        TableHeader("Load Receipt ID", Modifier.weight(1.2f))
                        TableHeader("Amount Paid", Modifier.weight(1f))
                    }
                    Divider(
                        color = FluentTheme.colors.background.mica.base.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                }

                if (isLoading && payments.isEmpty()) {
                    items(5) {
                        LoadTableShimmerRow()
                        Divider(
                            color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                            thickness = 0.5.dp
                        )
                    }
                } else {
                    items(payments) { payment ->
                        PaymentRow(
                            payment = payment,
                            onToggleTick = { viewModel.tickPayment(payment.id) }
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

    if (showConfirmDialog) {
        val totalAmount = payments.filter { it.isTicked }.fold(BigDecimal.ZERO) { acc, payment -> acc.add(payment.amount) }
        ContentDialog(
            title = "Confirm Payments",
            visible = true,
            size = DialogSize.Standard,
            primaryButtonText = "Confirm",
            closeButtonText = "Cancel",
            onButtonClick = { btn ->
                if (btn == ContentDialogButton.Primary) {
                    viewModel.confirmPayments()
                }
                showConfirmDialog = false
            },
            content = {
                Column {
                    Text("Are you sure you want to confirm these ${payments.count { it.isTicked }} payments?")
                    Spacer(Modifier.height(8.dp))
                    val formattedTotal = java.text.DecimalFormat("#,##0.00").format(totalAmount)
                    Text("Total Amount Paid: $formattedTotal DZD", style = FluentTheme.typography.bodyStrong)
                }
            }
        )
    }
}

@Composable
fun PaymentRow(
    payment: BrvPaymentTodoResponse,
    onToggleTick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
            Checkbox(
                checked = payment.isTicked,
                onCheckedChange = { onToggleTick() }
            )
        }
        Text(
            payment.createdAt?.substringBefore("T") ?: "-",
            Modifier.weight(1.5f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.caption
        )
        Text(
            payment.plateNumber,
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.bodyStrong
        )
        Text(
            payment.customerName,
            Modifier.weight(1.5f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.body
        )
        Text(
            payment.customerReceiptId.toString(),
            Modifier.weight(1.2f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.body
        )
        val formattedAmount = java.text.DecimalFormat("#,##0.00").format(payment.amount)
        Text(
            formattedAmount,
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = FluentTheme.typography.bodyStrong.copy(color = FluentTheme.colors.system.success)
        )
    }
}
