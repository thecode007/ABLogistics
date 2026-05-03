package org.safieddine.ablogistics.ui.theme

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import io.github.composefluent.component.ContentDialog
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.DialogSize

@Composable
fun DeleteDialog(
    showDeleteDialog: Boolean,
    title: String,
    message: String,
    onButtonClicked:(ContentDialogButton) -> Unit
) {
    ContentDialog(
        title = title,
        visible = showDeleteDialog,
        size = DialogSize.Max,
        primaryButtonText = "Confirm",
        closeButtonText = "Cancel",
        onButtonClick = { button ->
            onButtonClicked(button)
        },
        content = {
            Text(message)
        }
    )
}