package org.safieddine.ablogistics.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme


@Composable
fun RowScope.RailScreen(
    title: String,
    imageVector: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.padding(16.dp).weight(1f).fillMaxHeight()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = imageVector, "",
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = FluentTheme.typography.title
            )
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

