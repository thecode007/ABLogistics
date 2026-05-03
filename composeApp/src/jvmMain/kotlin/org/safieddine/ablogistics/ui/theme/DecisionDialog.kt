package org.safieddine.ablogistics.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.composefluent.component.ContentDialog
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.DialogSize
import androidx.compose.ui.unit.dp

@Composable
fun DecisionDialog(
    visible: Boolean,
    title: String,
    message: String,
    details: Map<String, String> = emptyMap(),
    onButtonClicked: (ContentDialogButton) -> Unit
) {
    ContentDialog(
        title = title,
        visible = visible,
        size = DialogSize.Max,
        primaryButtonText = "Approve",
        secondaryButtonText = "Reject",
        closeButtonText = "Cancel",
        onButtonClick = { button -> onButtonClicked(button) },
        content = {

        Column {
            Text(message)
            if (details.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                details.forEach { (k, v) ->
                    Text("${k}: $v")
                }
            }
        }
    })
}

