package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.Checkmark
import io.github.composefluent.icons.filled.Warning
import org.safieddine.ablogistics.data.ReceiptResponse
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import org.safieddine.ablogistics.ui.utils.NumberCommaTransformation
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
    var dispatchedStr by remember { mutableStateOf(receipt.loadedQuantity?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "") }
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
                    visualTransformation = NumberCommaTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (dispatchedQty > loadedQty) {
                    Text("Error: Cannot exceed loaded quantity", color = FluentTheme.colors.system.critical, style = FluentTheme.typography.caption)
                }
                
                Spacer(Modifier.height(24.dp))
                
                if (shortage > BigDecimal.ZERO) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = FluentTheme.colors.system.critical.copy(alpha = 0.05f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FluentTheme.colors.system.critical.copy(alpha = 0.2f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = FluentTheme.colors.system.critical, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Shortage Accountability", style = FluentTheme.typography.bodyStrong, color = FluentTheme.colors.system.critical)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("The actual quantity delivered is less than loaded. A penalty will be applied.", style = FluentTheme.typography.caption)
                            Spacer(Modifier.height(12.dp))
                            
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Shortage Amount:", style = FluentTheme.typography.body)
                                Text("${shortage.setScale(2, RoundingMode.HALF_UP)} L", style = FluentTheme.typography.bodyStrong)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Penalty ($loadedQty - $dispatchedQty) * $costPrice:", style = FluentTheme.typography.caption)
                                Text("- ${formatLocalized(penalty)}", style = FluentTheme.typography.bodyStrong, color = FluentTheme.colors.system.critical)
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = FluentTheme.colors.system.critical.copy(alpha = 0.1f))
                            Spacer(Modifier.height(8.dp))
                            Text("Note: This penalty will reduce the final BRV delivery cost.", style = FluentTheme.typography.caption, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(FluentTheme.colors.system.success.copy(alpha = 0.05f), shape = FluentTheme.shapes.control).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Checkmark, contentDescription = null, tint = FluentTheme.colors.system.success, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Full delivery confirmed. No shortage penalty.", color = FluentTheme.colors.system.success, style = FluentTheme.typography.body)
                    }
                }

                if (isLoading) {
                    Spacer(Modifier.height(16.dp))
                    ProgressRing(Modifier.fillMaxWidth())
                }
            }
        }
    )
}
