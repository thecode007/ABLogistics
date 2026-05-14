package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.component.AutoSuggestBoxDefaults
import io.github.composefluent.component.AutoSuggestionBox
import io.github.composefluent.component.ListItem
import kotlinx.coroutines.flow.map
import org.safieddine.ablogistics.data.BRVDTO
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField

@OptIn(ExperimentalFluentApi::class)
@Composable
fun BRVSearch(
    brvs: List<BRVDTO>,
    onSelected: (BRVDTO) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var keyword by remember { mutableStateOf("") }

    data class SuggestItem(val plate: String, val driver: String, val brv: BRVDTO)

    val flatMapComponents = remember(brvs) {
        brvs.map { b -> SuggestItem(plate = b.plateNumber, driver = b.driverName ?: "N/A", brv = b) }
    }

    AutoSuggestionBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        ABLogisticsTextField(
            value = keyword,
            onValueChange = { keyword = it; expanded = true },
            shape = AutoSuggestBoxDefaults.textFieldShape(expanded),
            placeholder = { Text("Search Plate or Driver") },
            modifier = Modifier.widthIn(300.dp).flyoutAnchor(),
            singleLine = true
        )

        val searchResult = remember(flatMapComponents) {
            snapshotFlow { keyword }.map { kw ->
                if (kw.isBlank()) flatMapComponents
                else flatMapComponents.filter { item ->
                    item.plate.contains(kw, ignoreCase = true) ||
                            item.driver.contains(kw, ignoreCase = true)
                }
            }
        }.collectAsState(flatMapComponents)

        AutoSuggestBoxDefaults.suggestFlyout(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            itemsContent = {
                items(items = searchResult.value) { item ->
                    ListItem(
                        onClick = {
                            keyword = item.plate
                            expanded = false
                            onSelected(item.brv)
                        },
                        text = { Text("${item.plate} - ${item.driver}", maxLines = 1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            modifier = Modifier.flyoutSize(matchAnchorWidth = true)
        )
    }
}
