package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.surface.Card
import org.safieddine.ablogistics.data.ReceiptResponse
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalFluentApi::class)
@Composable
fun DeliveryFinalizationDialog(
    receipt: ReceiptResponse,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal) -> Unit,
    isLoading: Boolean = false
) {
    var dispatchedStr by remember { mutableStateOf(receipt.loadedQuantity?.toString() ?: "") }
    val loadedQty = receipt.loadedQuantity ?: BigDecimal.ZERO
    val costPrice = receipt.costPrice ?: BigDecimal.ZERO
    
    val dispatchedQty = dispatchedStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val shortage = if (dispatchedQty < loadedQty) loadedQty.subtract(dispatchedQty) else BigDecimal.ZERO
    val penalty = shortage.multiply(costPrice).setScale(4, RoundingMode.HALF_UP)

    ContentDialog(
        title = "Finalize Delivery",
        visible = true,
        onButtonClick = { btn ->
            if (btn == ContentDialogButton.Primary) {
                if (dispatchedQty > BigDecimal.ZERO && dispatchedQty <= loadedQty) {
                    onConfirm(dispatchedQty)
                }
            } else {
                onDismiss()
            }
        },
        primaryButtonText = "Finalize",
        closeButtonText = "Cancel",
        content = {
            Column(Modifier.padding(16.dp)) {
                Text("Confirm actual liters delivered to customer.", style = FluentTheme.typography.body)
                Spacer(Modifier.height(16.dp))
                
                Text("Loaded Quantity: ${loadedQty.setScale(2, RoundingMode.HALF_UP)} L", style = FluentTheme.typography.bodyStrong)
                Spacer(Modifier.height(8.dp))

                ABLogisticsTextField(
                    value = dispatchedStr,
                    onValueChange = { dispatchedStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    header = { Text("Dispatched Liters") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (dispatchedQty > loadedQty) {
                    Text("Error: Cannot exceed loaded quantity", color = FluentTheme.colors.system.critical, style = FluentTheme.typography.caption)
                }
                
                Spacer(Modifier.height(24.dp))
                
                if (shortage > BigDecimal.ZERO) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Shortage Detected", style = FluentTheme.typography.bodyStrong, color = FluentTheme.colors.system.caution)
                            Text("Shortage: ${shortage.setScale(2, RoundingMode.HALF_UP)} L", style = FluentTheme.typography.body)
                            Text("Penalty: ${penalty.setScale(2, RoundingMode.HALF_UP)} (deducted from BRV cost)", 
                                style = FluentTheme.typography.caption,
                                color = FluentTheme.colors.system.critical
                            )
                        }
                    }
                } else {
                    Text("No shortage detected. Full delivery.", color = FluentTheme.colors.system.success, style = FluentTheme.typography.body)
                }

                if (isLoading) {
                    Spacer(Modifier.height(16.dp))
                    ProgressRing(Modifier.fillMaxWidth())
                }
            }
        }
    )
}
