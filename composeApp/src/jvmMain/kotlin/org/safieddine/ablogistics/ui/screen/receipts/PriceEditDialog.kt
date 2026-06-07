package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import org.safieddine.ablogistics.data.MaterialPriceDTO
import org.safieddine.ablogistics.data.MaterialType
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import org.safieddine.ablogistics.ui.utils.NumberCommaTransformation
import java.math.BigDecimal

@OptIn(ExperimentalFluentApi::class)
@Composable
fun PriceEditDialog(
    visible: Boolean,
    currentPrices: List<MaterialPriceDTO>,
    currentReceiptCounter: Long?,
    onDismiss: () -> Unit,
    onSave: (List<MaterialPriceDTO>) -> Unit,
    onSaveReceiptCounter: (Long) -> Unit
) {
    if (!visible) return

    var fuelCost by remember { mutableStateOf(currentPrices.find { it.materialType == MaterialType.FUEL }?.costPrice?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") }
    var fuelSell by remember { mutableStateOf(currentPrices.find { it.materialType == MaterialType.FUEL }?.sellingPrice?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") }
    var dieselCost by remember { mutableStateOf(currentPrices.find { it.materialType == MaterialType.DIESEL }?.costPrice?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") }
    var dieselSell by remember { mutableStateOf(currentPrices.find { it.materialType == MaterialType.DIESEL }?.sellingPrice?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") }

    // Receipt counter state — initialised from the server value, editable by user
    var receiptCounterInput by remember(currentReceiptCounter) {
        mutableStateOf(currentReceiptCounter?.toString() ?: "")
    }

    ContentDialog(
        title = "Global Configuration",
        visible = visible,
        size = DialogSize.Standard,
        primaryButtonText = "Save Changes",
        closeButtonText = "Cancel",
        onButtonClick = {
            if (it == ContentDialogButton.Primary) {
                val newPrices = listOf(
                    MaterialPriceDTO(
                        materialType = MaterialType.FUEL,
                        costPrice = fuelCost.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        sellingPrice = fuelSell.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    ),
                    MaterialPriceDTO(
                        materialType = MaterialType.DIESEL,
                        costPrice = dieselCost.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        sellingPrice = dieselSell.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    )
                )
                onSave(newPrices)

                val counterValue = receiptCounterInput.trim().toLongOrNull()
                if (counterValue != null && counterValue != currentReceiptCounter) {
                    onSaveReceiptCounter(counterValue)
                }
            }
            onDismiss()
        },
        content = {
            Column(Modifier.fillMaxWidth().padding(8.dp)) {

                // ── Material Prices ────────────────────────────────────────────
                Text("Material Prices", style = FluentTheme.typography.subtitle.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(12.dp))

                Text("Fuel (GASOLINE)", style = FluentTheme.typography.bodyStrong)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ABLogisticsTextField(
                        value = fuelCost,
                        onValueChange = { fuelCost = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Cost Price") },
                        visualTransformation = NumberCommaTransformation(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    ABLogisticsTextField(
                        value = fuelSell,
                        onValueChange = { fuelSell = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Selling Price") },
                        visualTransformation = NumberCommaTransformation(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("Diesel (AGO)", style = FluentTheme.typography.bodyStrong)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ABLogisticsTextField(
                        value = dieselCost,
                        onValueChange = { dieselCost = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Cost Price") },
                        visualTransformation = NumberCommaTransformation(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    ABLogisticsTextField(
                        value = dieselSell,
                        onValueChange = { dieselSell = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Selling Price") },
                        visualTransformation = NumberCommaTransformation(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(16.dp))

                // ── Receipt Number ─────────────────────────────────────────────
                Text("Receipt Number", style = FluentTheme.typography.subtitle.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Text(
                    "Set the starting number for the next auto-generated receipt. " +
                    "Each receipt created will use this number and then increment it by 1.",
                    style = FluentTheme.typography.caption
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ABLogisticsTextField(
                        value = receiptCounterInput,
                        onValueChange = { input -> if (input.all { it.isDigit() }) receiptCounterInput = input },
                        header = { Text("Next Receipt Number") },
                        placeholder = { Text("e.g. 300") },
                        singleLine = true,
                        isError = receiptCounterInput.isNotBlank() && receiptCounterInput.toLongOrNull() == null,
                        modifier = Modifier.weight(1f)
                    )
                    Column(Modifier.weight(1f)) {
                        Text("Current server value", style = FluentTheme.typography.caption)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = currentReceiptCounter?.toString() ?: "Loading…",
                            style = FluentTheme.typography.bodyStrong.copy(fontSize = 20.sp),
                            color = FluentTheme.colors.fillAccent.default
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun Divider() {
    androidx.compose.material.Divider(
        color = FluentTheme.colors.background.mica.base.copy(alpha = 0.4f),
        thickness = 1.dp
    )
}
