package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.GridViewItem
import io.github.composefluent.component.GridViewItemDefaults
import io.github.composefluent.component.InfoBar
import io.github.composefluent.component.InfoBarSeverity
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.*
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.Delete
import io.github.composefluent.icons.regular.Edit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.WarehouseDTO
import org.safieddine.ablogistics.ui.screen.receipts.formatLocalized
import org.safieddine.ablogistics.ui.screen.receipts.parseLocalizedNumber
import ablogistics.composeapp.generated.resources.Res
import ablogistics.composeapp.generated.resources.*
import java.util.Locale

@Composable
fun RowScope.WarehousePanel(
    isLoading: Boolean,
    data: List<WarehouseDTO>,
    error: String?,
    onReload: () -> Unit,
    onUnAssign: (String) -> Unit,
    onDelete: (WarehouseDTO) -> Unit,
    onEdite: (WarehouseDTO) -> Unit,
    onAdd: () -> Unit,
    onWarehouseSelected: (WarehouseDTO?) -> Unit
) {
    Box(
        Modifier.weight(1f).fillMaxHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderWithAction(
                title = stringResource(Res.string.warehouses),
                iconVector = Icons.Filled.BuildingRetail,
                onAdd = onAdd
            ) {
                onReload()
            }

            var selectedIndex by remember { mutableStateOf(-1) }

            when {
                isLoading -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(GridViewItemDefaults.spacing),
                    verticalArrangement = Arrangement.spacedBy(GridViewItemDefaults.spacing),
                    contentPadding = PaddingValues(GridViewItemDefaults.spacing),
                    modifier = Modifier.weight(1f)
                ) {
                    items(3) { ShimmerGridItemPlaceholder() }
                }

                error != null -> ErrorPanel(error, onReload)

                else -> {
                    if (data.isEmpty()) {
                        Spacer(Modifier.weight(1f))
                        Icon(
                            modifier = Modifier.size(50.dp),
                            imageVector = Icons.Filled.BuildingPeople,
                            tint = FluentTheme.colors.background.smoke.default,
                            contentDescription = null
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.no_warehouses_available),
                            style = FluentTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = FluentTheme.colors.background.smoke.default
                        )
                        Spacer(Modifier.weight(1f))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(200.dp),
                            horizontalArrangement = Arrangement.spacedBy(GridViewItemDefaults.spacing),
                            verticalArrangement = Arrangement.spacedBy(GridViewItemDefaults.spacing),
                            contentPadding = PaddingValues(GridViewItemDefaults.spacing),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(
                                data,
                                key = { _, item -> item.id }
                            ) { index, item ->

                                GridViewItem(
                                    selected = index == selectedIndex,
                                    onSelectedChange = {
                                        selectedIndex = if (it) {
                                            onWarehouseSelected(data[index])
                                            index
                                        } else {
                                            onWarehouseSelected(null)
                                            -1
                                        }
                                    },
                                    content = {
                                        Box(
                                            Modifier
                                                .background(color = FluentTheme.colors.background.mica.base)
                                                .fillMaxSize()
                                        ) {
                                            Column(
                                                Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .background(FluentTheme.colors.fillAccent.default)
                                                        .fillMaxWidth()
                                                        .padding(8.dp)
                                                ) {
                                                    Row(Modifier.fillMaxWidth()) {
                                                        Text(
                                                            text = "#${item.id} ${item.name}",
                                                            modifier = Modifier.weight(1f),
                                                            color = FluentTheme.colors.background.mica.base,
                                                            fontWeight = FontWeight.SemiBold,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Icon(
                                                            modifier = Modifier.clickable { onEdite(item) },
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = stringResource(Res.string.edit_warehouse),
                                                            tint = FluentTheme.colors.background.mica.base
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Icon(
                                                            modifier = Modifier.clickable { onDelete(item) },
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = stringResource(Res.string.delete_warehouse),
                                                            tint = FluentTheme.colors.background.mica.base
                                                        )
                                                    }

                                                    Row(Modifier.fillMaxWidth()) {
                                                        Icon(
                                                            modifier = Modifier.size(15.dp),
                                                            imageVector = Icons.Filled.Location,
                                                            contentDescription = null,
                                                            tint = FluentTheme.colors.background.mica.base
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = item.location ?: "",
                                                            color = FluentTheme.colors.background.mica.base,
                                                            fontWeight = FontWeight.SemiBold,
                                                            overflow = TextOverflow.Ellipsis
                                                        )

                                                        if (item.users.isNotEmpty()) {
                                                            val username = item.users.first().username
                                                            Row(
                                                                Modifier.weight(1f),
                                                                horizontalArrangement = Arrangement.End,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    modifier = Modifier.size(15.dp),
                                                                    imageVector = Icons.Filled.NotepadPerson,
                                                                    contentDescription = null,
                                                                    tint = FluentTheme.colors.background.mica.base
                                                                )
                                                                Spacer(Modifier.width(4.dp))
                                                                Text(
                                                                    text = username,
                                                                    textAlign = TextAlign.End,
                                                                    color = FluentTheme.colors.background.mica.base,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Spacer(Modifier.width(2.dp))

                                                                Icon(
                                                                    modifier = Modifier.clickable {
                                                                        onUnAssign(username)
                                                                    }.size(15.dp),
                                                                    imageVector = Icons.Filled.PersonDelete,
                                                                    contentDescription = null,
                                                                    tint = FluentTheme.colors.system.critical
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                val iso = item.isoCode
                                                if (iso.isNotBlank()) {
                                                    Country.findByCode(iso)?.let { ctry ->
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Image(
                                                                modifier = Modifier.width(25.dp),
                                                                painter = painterResource(ctry.flag),
                                                                contentDescription = ctry.name
                                                            )
                                                            Text(
                                                                text = ctry.name,
                                                                style = FluentTheme.typography.bodyStrong
                                                            )
                                                        }
                                                    }
                                                }

                                                Icon(Icons.Filled.Receipt, contentDescription = null)
                                                Text("${item.receiptsCount}", style = FluentTheme.typography.bodyStrong)

                                                Icon(Icons.Filled.PeopleAudience, contentDescription = null)
                                                Text(
                                                    "${item.customersCount}",
                                                    style = FluentTheme.typography.bodyStrong
                                                )

                                                Icon(Icons.Filled.ArrowSwap, contentDescription = null)

                                                val total = (item.totalFunds ?: java.math.BigDecimal.ZERO).abs()

                                                val locale = Locale.getDefault()
                                                val numericValue = parseLocalizedNumber("%.2f".format(total), locale)
                                                val formattedValue = formatLocalized(numericValue, locale)

                                                Text(
                                                    formattedValue,
                                                    style = FluentTheme.typography.bodyStrong,
                                                    color = FluentTheme.colors.system.success
                                                )

                                                Spacer(Modifier.height(6.dp))
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.69f)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (error != null) {
            InfoBar(
                title = {},
                message = { Text(error) },
                severity = InfoBarSeverity.Critical,
                closeAction = {
                    IconButton(onClick = { onReload() }) {
                        Icon(imageVector = Icons.Default.ArrowCounterclockwise, contentDescription = null)
                    }
                }
            )
        }
    }
}

@Composable
fun ShimmerGridItemPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shimmerEffect()
    )
}
