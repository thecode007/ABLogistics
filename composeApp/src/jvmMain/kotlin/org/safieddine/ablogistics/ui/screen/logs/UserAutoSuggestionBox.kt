package org.safieddine.ablogistics.ui.screen.logs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.component.AutoSuggestBoxDefaults
import io.github.composefluent.component.AutoSuggestionBox
import io.github.composefluent.component.ListItem
import io.github.composefluent.component.TextField
import kotlinx.coroutines.flow.map
import org.safieddine.ablogistics.data.UserDTO

@OptIn(ExperimentalFluentApi::class)
@Composable
fun UserAutoSuggestionBox(
    users: List<UserDTO>,
    selectedUsername: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var keyword by remember { mutableStateOf(selectedUsername ?: "") }

    data class SuggestItem(val index: Int, val username: String, val name: String, val description: String)

    val flat = remember(users) {
        users.mapIndexed { idx, u ->
            SuggestItem(index = idx, username = u.username, name = u.fullName, description = u.phoneNumber)
        }
    }

    AutoSuggestionBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = keyword,
            onValueChange = { new ->
                keyword = new
                expanded = true
            },
            shape = AutoSuggestBoxDefaults.textFieldShape(expanded),
            modifier = Modifier
                .widthIn(300.dp)
                .flyoutAnchor()
                .onFocusChanged { f -> if (f.isFocused) expanded = true },
            placeholder = { androidx.compose.material.Text(text = "User") }
        )

        val searchResult = remember(flat) {
            snapshotFlow { keyword }.map { kw ->
                if (kw.isBlank()) flat
                else flat.filter { item ->
                    item.username.contains(kw, ignoreCase = true) ||
                            item.name.contains(kw, ignoreCase = true) ||
                            item.description.contains(kw, ignoreCase = true)
                }
            }
        }.collectAsState(flat)

        AutoSuggestBoxDefaults.suggestFlyout(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            itemsContent = {
                items(items = searchResult.value) { item ->
                    ListItem(
                        onClick = {
                            expanded = false
                            keyword = item.name
                            onSelected(users.getOrNull(item.index)?.username)
                        },
                        text = { androidx.compose.material.Text("${item.name} (${item.username})", maxLines = 1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            modifier = Modifier.flyoutSize(matchAnchorWidth = true)
        )
    }
}
