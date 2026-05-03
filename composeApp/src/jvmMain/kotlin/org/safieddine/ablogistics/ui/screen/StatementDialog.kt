package org.safieddine.ablogistics.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.composefluent.FluentTheme

@Composable
fun StatementOverlayDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
    ) {
        // Panel fills available width/height with rounded corners and padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(8.dp, RectangleShape)
                .clip(RectangleShape)
                .background(Color.White)
                // Use system bars insets instead of fixed padding
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Close",
                    color = FluentTheme.colors.system.attention,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
