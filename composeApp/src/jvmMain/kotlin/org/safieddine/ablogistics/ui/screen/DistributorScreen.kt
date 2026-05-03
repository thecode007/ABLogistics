package org.safieddine.ablogistics.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.PeopleMoney
import org.safieddine.ablogistics.ui.AppTopBar

@Composable
fun DistributorScreen() {
    Scaffold(
        topBar = {
            AppTopBar("Distributors", Icons.Filled.PeopleMoney)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Distributor Screen Content Coming Soon")
        }
    }
}
