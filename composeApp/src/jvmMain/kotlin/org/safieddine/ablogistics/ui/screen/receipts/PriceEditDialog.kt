package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
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
    onDismiss: () -> Unit,
    onSave: (List<MaterialPriceDTO>) -> Unit
) {
    if (!visible) return

    var fuelCost by remember { mutableStateOf(currentPrices.find { it.materialType == MaterialType.FUEL }?.costPrice?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") }
    var fuelSell by remember { mutableStateOf(currentPrices.find { it.materialType == MaterialType.FUEL }?.sellingPrice?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") }
    var dieselCost by remember { mutableStateOf(currentPrices.find { it.materialType == MaterialType.DIESEL }?.costPrice?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") }
    var dieselSell by remember { mutableStateOf(currentPrices.find { it.materialType == MaterialType.DIESEL }?.sellingPrice?.setScale(2, java.math.RoundingMode.HALF_UP)?.toPlainString() ?: "0.00") }

    ContentDialog(
        title = "Edit Global Material Prices",
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
            }
            onDismiss()
        },
        content = {
            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                Text("Fuel (GASOLINE)", style = io.github.composefluent.FluentTheme.typography.bodyStrong)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ABLogisticsTextField(
                        value = fuelCost,
                        onValueChange = { fuelCost = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Cost Price") },
                        visualTransformation = NumberCommaTransformation(),
                        modifier = Modifier.weight(1f)
                    )
                    ABLogisticsTextField(
                        value = fuelSell,
                        onValueChange = { fuelSell = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Selling Price") },
                        visualTransformation = NumberCommaTransformation(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text("Diesel (AGO)", style = io.github.composefluent.FluentTheme.typography.bodyStrong)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ABLogisticsTextField(
                        value = dieselCost,
                        onValueChange = { dieselCost = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Cost Price") },
                        visualTransformation = NumberCommaTransformation(),
                        modifier = Modifier.weight(1f)
                    )
                    ABLogisticsTextField(
                        value = dieselSell,
                        onValueChange = { dieselSell = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        header = { Text("Selling Price") },
                        visualTransformation = NumberCommaTransformation(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}
