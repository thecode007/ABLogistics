package org.safieddine.ablogistics.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Settings
import org.safieddine.ablogistics.ui.AppTopBar

@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            AppTopBar("Settings", Icons.Regular.Settings)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Settings Screen Content Coming Soon")
        }
    }
}
