package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import org.safieddine.ablogistics.ui.theme.ABLogisticsSubtleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme

import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.AddSquare
import io.github.composefluent.icons.filled.ArrowCounterclockwise
import io.github.composefluent.surface.Card

@Composable
fun HeaderWithAction(
    title: String,
    iconVector: ImageVector,
    onAdd: () -> Unit,
    onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(modifier = Modifier.size(25.dp),
                imageVector = iconVector,
                contentDescription = "",
                tint = FluentTheme.colors.background.mica.base)

            Spacer(Modifier.size(8.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = title,
                style = FluentTheme.typography.subtitle,
                color = FluentTheme.colors.background.mica.base,
                textAlign = TextAlign.Start
            )

            ABLogisticsSubtleButton(iconOnly = true, onClick = onRefresh) {
                Icon(modifier = Modifier.size(25.dp),
                    imageVector = Icons.Filled.ArrowCounterclockwise,
                    contentDescription = "Refresh",
                    tint = FluentTheme.colors.background.mica.base)
            }

            ABLogisticsSubtleButton(iconOnly = true, onClick = onAdd) {
                Icon(modifier = Modifier.size(25.dp),
                    imageVector = Icons.Filled.AddSquare,
                    contentDescription = "Add",
                    tint = FluentTheme.colors.background.mica.base)
            }
        }
    }
}
