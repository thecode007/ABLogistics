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
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.component.AutoSuggestBoxDefaults
import io.github.composefluent.component.AutoSuggestionBox
import io.github.composefluent.component.ListItem
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import kotlinx.coroutines.flow.map
import org.safieddine.ablogistics.data.CustomerResponse
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Dismiss
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope

@OptIn(ExperimentalFluentApi::class)
@Composable
fun CustomerSearch(
    customers: List<CustomerResponse>,
    onSelected: (CustomerResponse) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Search Customer",
    onClear: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var keyword by remember { mutableStateOf("") }

    data class SuggestItem(val name: String, val description: String, val customer: CustomerResponse)

    val flatMapComponents = remember(customers) {
        customers.map { c -> SuggestItem(name = c.name, description = c.phoneNumber, customer = c) }
    }

    AutoSuggestionBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        ABLogisticsTextField(
            value = keyword,
            onValueChange = { input -> keyword = input; expanded = true },
            shape = AutoSuggestBoxDefaults.textFieldShape(expanded),
            modifier = Modifier.fillMaxWidth().flyoutAnchor(),
            singleLine = true,
            enabled = enabled,
            placeholder = { Text(placeholder) },
            trailing = if (keyword.isNotEmpty() && onClear != null) {
                {
                    IconButton(onClick = {
                        keyword = ""
                        onClear()
                    }) {
                        Icon(
                            Icons.Default.Dismiss,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else null
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
                            keyword = item.name
                            expanded = false
                            onSelected(item.customer)
                        },
                        text = { Text(item.name, maxLines = 1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            modifier = Modifier.flyoutSize(matchAnchorWidth = true)
        )
    }
}

