package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.component.AutoSuggestBoxDefaults
import io.github.composefluent.component.AutoSuggestionBox
import io.github.composefluent.component.ListItem
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.CustomerResponse
import ablogistics.composeapp.generated.resources.Res
import ablogistics.composeapp.generated.resources.customer

@OptIn(ExperimentalFluentApi::class)
@Composable
fun CustomerAutoSuggestionBox(
    customers: List<CustomerResponse>,
    selectedIndex: Int?,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var keyword by remember(selectedIndex, customers) {
        mutableStateOf(
            when (selectedIndex) {
                null -> ""
                else -> customers.getOrNull(selectedIndex)?.name ?: ""
            }
        )
    }

    data class SuggestItem(val index: Int, val name: String, val description: String)

    val flatMapComponents = remember(customers) {
        customers.mapIndexed { idx, c ->
            SuggestItem(index = idx, name = c.name, description = c.phoneNumber)
        }
    }

    AutoSuggestionBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        ABLogisticsTextField(
            value = keyword,
            onValueChange = { new ->
                keyword = new
                expanded = true // keep flyout open while typing, including when cleared
            },
            shape = AutoSuggestBoxDefaults.textFieldShape(expanded),
            modifier = Modifier
                .widthIn(300.dp)
                .flyoutAnchor()
                .onFocusChanged { f -> if (f.isFocused) expanded = true },
            placeholder = { androidx.compose.material.Text(text = stringResource(Res.string.customer)) }
        )

        val searchResult = remember(flatMapComponents) {
            snapshotFlow { keyword }.map { kw ->
                if (kw.isBlank()) flatMapComponents
                else flatMapComponents.filter { item ->
                    item.name.contains(kw, ignoreCase = true) ||
                            item.description.contains(kw, ignoreCase = true)
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
                            expanded = false
                            keyword = item.name
                            onSelected(item.index)
                        },
                        text = { androidx.compose.material.Text(item.name, maxLines = 1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            modifier = Modifier.flyoutSize(matchAnchorWidth = true)
        )
    }
}
