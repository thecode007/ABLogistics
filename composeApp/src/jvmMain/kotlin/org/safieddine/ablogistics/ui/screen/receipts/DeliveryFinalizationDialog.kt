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
    onConfirm: (BigDecimal, BigDecimal?, BigDecimal?) -> Unit,
    isLoading: Boolean = false
) {
    val isMixed = receipt.material == "MIXED"

    var fuelDispatchedStr by remember { mutableStateOf(receipt.fuelQuantity?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "") }
    var dieselDispatchedStr by remember { mutableStateOf(receipt.dieselQuantity?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "") }
    var dispatchedStr by remember { mutableStateOf(receipt.loadedQuantity?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "") }

    val loadedQty = receipt.loadedQuantity ?: BigDecimal.ZERO
    val fuelLoaded = receipt.fuelQuantity ?: BigDecimal.ZERO
    val dieselLoaded = receipt.dieselQuantity ?: BigDecimal.ZERO

    val fuelDispatchedQty = fuelDispatchedStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val dieselDispatchedQty = dieselDispatchedStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val dispatchedQty = if (isMixed) fuelDispatchedQty.add(dieselDispatchedQty) else (dispatchedStr.toBigDecimalOrNull() ?: BigDecimal.ZERO)

    val fuelCostPrice = receipt.fuelCostPrice ?: BigDecimal.ZERO
    val dieselCostPrice = receipt.dieselCostPrice ?: BigDecimal.ZERO
    val costPrice = receipt.costPrice ?: BigDecimal.ZERO

    val fuelShortage = if (isMixed && fuelDispatchedQty < fuelLoaded) fuelLoaded.subtract(fuelDispatchedQty) else BigDecimal.ZERO
    val dieselShortage = if (isMixed && dieselDispatchedQty < dieselLoaded) dieselLoaded.subtract(dieselDispatchedQty) else BigDecimal.ZERO
    val shortage = if (isMixed) fuelShortage.add(dieselShortage) else (if (dispatchedQty < loadedQty) loadedQty.subtract(dispatchedQty) else BigDecimal.ZERO)

    val penalty = if (isMixed) fuelShortage.multiply(fuelCostPrice).add(dieselShortage.multiply(dieselCostPrice)).setScale(4, RoundingMode.HALF_UP)
                  else shortage.multiply(costPrice).setScale(4, RoundingMode.HALF_UP)

    ContentDialog(
        title = "Finalize Delivery",
        visible = true,
        onButtonClick = { btn ->
            if (btn == ContentDialogButton.Primary) {
                if (isMixed) {
                    if (fuelDispatchedQty > BigDecimal.ZERO && fuelDispatchedQty <= fuelLoaded &&
                        dieselDispatchedQty > BigDecimal.ZERO && dieselDispatchedQty <= dieselLoaded) {
                        onConfirm(dispatchedQty, fuelDispatchedQty, dieselDispatchedQty)
                    }
                } else {
                    if (dispatchedQty > BigDecimal.ZERO && dispatchedQty <= loadedQty) {
                        onConfirm(dispatchedQty, null, null)
                    }
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
                
                if (isMixed) {
                    Text("Loaded Fuel: ${fuelLoaded.setScale(2, RoundingMode.HALF_UP)} L", style = FluentTheme.typography.bodyStrong)
                    Spacer(Modifier.height(4.dp))
                    ABLogisticsTextField(
                        value = fuelDispatchedStr,
                        onValueChange = { fuelDispatchedStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Dispatched Fuel Liters") },
                        visualTransformation = NumberCommaTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (fuelDispatchedQty > fuelLoaded) {
                        Text("Error: Fuel cannot exceed loaded quantity", color = FluentTheme.colors.system.critical, style = FluentTheme.typography.caption)
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    Text("Loaded Diesel: ${dieselLoaded.setScale(2, RoundingMode.HALF_UP)} L", style = FluentTheme.typography.bodyStrong)
                    Spacer(Modifier.height(4.dp))
                    ABLogisticsTextField(
                        value = dieselDispatchedStr,
                        onValueChange = { dieselDispatchedStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Dispatched Diesel Liters") },
                        visualTransformation = NumberCommaTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dieselDispatchedQty > dieselLoaded) {
                        Text("Error: Diesel cannot exceed loaded quantity", color = FluentTheme.colors.system.critical, style = FluentTheme.typography.caption)
                    }
                } else {
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
                                val text = if (isMixed) "Shortage Accountability (Mixed)" else "Shortage Accountability"
                                Text(text, style = FluentTheme.typography.bodyStrong, color = FluentTheme.colors.system.critical)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("The actual quantity delivered is less than loaded. A penalty will be applied.", style = FluentTheme.typography.caption)
                            Spacer(Modifier.height(12.dp))
                            
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Shortage Amount:", style = FluentTheme.typography.body)
                                Text("${shortage.setScale(2, RoundingMode.HALF_UP)} L", style = FluentTheme.typography.bodyStrong)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val penaltyLabel = if (isMixed) "Penalty (Fuel: ${fuelLoaded - fuelDispatchedQty}*${fuelCostPrice} + Diesel: ${dieselLoaded - dieselDispatchedQty}*${dieselCostPrice}):" 
                                                   else "Penalty ($loadedQty - $dispatchedQty) * $costPrice:"
                                Text(penaltyLabel, style = FluentTheme.typography.caption)
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
