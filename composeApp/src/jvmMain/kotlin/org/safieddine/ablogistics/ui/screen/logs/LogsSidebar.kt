package org.safieddine.ablogistics.ui.screen.logs

import AttributeChange
import LogPayload
import LogResponse
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.FluentTheme
import io.github.composefluent.ExperimentalFluentApi
import org.safieddine.ablogistics.ui.theme.ABLogisticsButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsSubtleButton
import io.github.composefluent.component.CalendarDatePicker
import io.github.composefluent.component.SubtleButton
import io.github.composefluent.component.Icon
import io.github.composefluent.component.ProgressRing
import io.github.composefluent.component.Text
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.ClearFormatting
import io.github.composefluent.icons.regular.Dismiss
import io.github.composefluent.icons.regular.History
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.BaseResponse

import org.safieddine.ablogistics.data.service.LogService
import org.safieddine.ablogistics.data.service.UserService
import org.safieddine.ablogistics.data.UserDTO
import io.github.composefluent.component.Expander
import io.github.composefluent.icons.filled.Add
import io.github.composefluent.icons.filled.Delete
import io.github.composefluent.icons.filled.Edit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.util.Locale
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalFluentApi::class, ExperimentalTime::class)
@Composable
fun LogsSidebar(
    visible: Boolean,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var logs by remember { mutableStateOf<List<LogResponse>>(emptyList()) }
    var users by remember { mutableStateOf<List<UserDTO>>(emptyList()) }

    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var clearDatesKey by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedUsername by remember { mutableStateOf<String?>(null) }

    var isUndoing by remember{ mutableStateOf(false)}
    fun refresh() {
        loading = true
        error = null
        scope.launch(Dispatchers.IO) {
            val res = LogService.list(
                page = 0,
                size = 50,
                username = selectedUsername,
                start = startDate,
                end = endDate,
                sortAsc = false
            )
            if (res.isSuccess) {
                val body: BaseResponse<List<LogResponse>>? = res.getOrNull()
                logs = body?.data ?: emptyList()
                error = if (body?.success == false) body.message else null
            } else {
                error = res.exceptionOrNull()?.message
            }
            loading = false
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            refresh()
            // Load users for suggestion
            scope.launch(Dispatchers.IO) {
                val res = UserService.getAllUsers()
                if (res.isSuccess) {
                    users = res.getOrNull()?.data ?: emptyList()
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(animationSpec = tween(200)) { it },
            exit = slideOutHorizontally(animationSpec = tween(200)) { it }
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(800.dp)
                        .padding(end = 50.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(FluentTheme.colors.background.mica.base)
                    ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Regular.History, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Activity Logs", style = FluentTheme.typography.title.copy(fontSize = 16.sp))
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onClose) {
                            Icon(imageVector = Icons.Regular.Dismiss, contentDescription = "Close")
                        }
                    }

                        HorizontalDivider()

                    // Filters row: User autosuggest + From/To dates + actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAutoSuggestionBox(
                            users = users,
                            selectedUsername = selectedUsername,
                            onSelected = { uname ->
                                selectedUsername = uname
                                refresh()
                            },
                            modifier = Modifier.width(260.dp)
                        )

                        Spacer(Modifier.weight(1f))

                        key(clearDatesKey) {
                            CalendarDatePicker(
                                onChoose = { localDate ->
                                    val startOfDay = java.time.LocalDateTime.of(
                                        localDate.year,
                                        localDate.monthValue + 1,
                                        localDate.day,
                                        0, 0, 0
                                    )
                                    startDate = startOfDay.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                                    refresh()
                                }
                            )

                            Spacer(Modifier.width(8.dp))

                            CalendarDatePicker(
                                onChoose = { localDate ->
                                    val endOfDay = java.time.LocalDateTime.of(
                                        localDate.year,
                                        localDate.monthValue + 1,
                                        localDate.day,
                                        23, 59, 59, 0
                                    ).plusNanos(999_000_000)
                                    endDate = endOfDay.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                                    refresh()
                                }
                            )
                        }


                        ABLogisticsSubtleButton(iconOnly = true, onClick = {
                            clearDatesKey = System.currentTimeMillis()
                            startDate = null
                            endDate = null
                            refresh()
                        }) {
                            Icon(imageVector = Icons.Regular.ClearFormatting, contentDescription = "Clear dates")
                        }
                    }

                    // Content
                    if (loading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ProgressRing()
                        }
                    } else if (!error.isNullOrBlank()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(error!!, color = Color.Red)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(logs) { log ->
                                LogItem(log = log,
                                    isLoading =  isUndoing,
                                    onUndo = {
                                    scope.launch(Dispatchers.IO) {
                                        isUndoing = true
                                        val res = LogService.undo(log.id)
                                        if (res.isSuccess && (res.getOrNull()?.success == true)) {
                                            isUndoing = false

                                            refresh()
                                        } else {
                                            error = res.exceptionOrNull()?.message ?: res.getOrNull()?.message
                                            isUndoing = false
                                        }
                                    }
                                })
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: LogResponse,
                    isLoading: Boolean,
                    onUndo: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val payload = remember(log.payloadJson) {
        log.payloadJson?.let {
            try {
                Json { ignoreUnknownKeys = true }.decodeFromString<LogPayload>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    Expander(
        expanded = expanded,
        onExpandedChanged = { expanded = it },
        heading = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = log.executedBy, style = FluentTheme.typography.bodyStrong)
                Spacer(Modifier.width(8.dp))
                Text(text = log.description, style = FluentTheme.typography.body)
            }
        },
        caption = {
            Text(text = log.executedAt?.replace("T", " ")?.split(".")?.get(0) ?: "",
                style = FluentTheme.typography.caption)
        },
        icon = {
            val (icon, color) = when (log.action) {
                "CREATE" -> Icons.Filled.Add to FluentTheme.colors.system.success
                "DELETE" -> Icons.Filled.Delete to FluentTheme.colors.system.critical
                else -> Icons.Filled.Edit to FluentTheme.colors.fillAccent.default
            }
            Icon(imageVector = icon, contentDescription = null, tint = color)
        },
        trailing = {
            ABLogisticsButton(onClick = { onUndo() }, content = {
                if (isLoading) ProgressRing(Modifier.size(15.dp))
                else Text("Undo")
            }, disabled = isLoading)
        }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (payload?.attributeChanges?.isNotEmpty() == true) {
                ChangeDetailsTable(payload.attributeChanges)
            } else {
                Text("No detailed changes available for this action.", style = FluentTheme.typography.caption)
            }
        }
    }
}

@Composable
fun ChangeDetailsTable(changes: List<AttributeChange>) {
    Column(Modifier.fillMaxWidth()) {
        // Table Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Attribute", Modifier.weight(1f), style = FluentTheme.typography.bodyStrong)
            Text("Old Value", Modifier.weight(1f), style = FluentTheme.typography.bodyStrong)
            Text("New Value", Modifier.weight(1f), style = FluentTheme.typography.bodyStrong)
        }
        HorizontalDivider(color = FluentTheme.colors.stroke.divider.default)

        changes.forEach { change ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(change.field.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, Modifier.weight(1f))
                Text(formatValue(change.field, change.before), Modifier.weight(1f))
                Text(formatValue(change.field, change.after), Modifier.weight(1f))
            }
        }
    }
}

private fun formatValue(field: String, element: JsonElement?): String {
    if (element == null || element is kotlinx.serialization.json.JsonNull) return "-"
    
    val value = if (element is JsonPrimitive) element.contentOrNull ?: "null" else element.toString()
    if (value == "null") return "-"
    
    return try {
        when {
            field.lowercase().contains("amount") || field.lowercase().contains("price") || field.lowercase().contains("fee") -> {
                val num = value.toDoubleOrNull()
                if (num != null) String.format("%.2f", num) else value
            }
            field.lowercase().contains("date") || field.lowercase().contains("at") -> {
                value.replace("T", " ").split(".")[0]
            }
            else -> value
        }
    } catch (e: Exception) {
        value
    }
}
