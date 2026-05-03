package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CountryPickerDialog(
    onDismiss: () -> Unit,
    onCountrySelected: (Country) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allCountries = remember { Country.getAllCountries() }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) allCountries
        else allCountries.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp
        ) {
            Column {
                Text(
                    "Select Country",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredCountries) { country ->
                        CountryListItem(
                            country = country,
                            onClick = { onCountrySelected(country) }
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(16.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
