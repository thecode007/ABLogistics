package org.safieddine.ablogistics.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.CalendarEmpty
import org.jetbrains.compose.resources.stringResource
import ablogistics.composeapp.generated.resources.Res
import ablogistics.composeapp.generated.resources.no_warehouse_assigned

@Composable
fun NoWarehouseAssigned() {
    Column (modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        Icon(imageVector = Icons.Filled.CalendarEmpty,
            modifier = Modifier.size(50.dp),
            contentDescription = "",
            tint = FluentTheme.colors.system.critical)

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.no_warehouse_assigned),
            style = FluentTheme.typography.title,
            color = FluentTheme.colors.system.critical,
            textAlign = TextAlign.Center
        )
    }
}