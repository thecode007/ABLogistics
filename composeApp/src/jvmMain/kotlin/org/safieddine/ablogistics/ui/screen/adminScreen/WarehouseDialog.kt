package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.ContentDialog
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.DialogSize
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.WarehouseDTO
import ablogistics.composeapp.generated.resources.*
 

@Composable
fun WarehouseDialog(
    scope: CoroutineScope,
    visible: Boolean,
    onDismiss: () -> Unit,
    existingWarehouse: WarehouseDTO? = null,
    onSubmit: (WarehouseDTO) -> Unit
) {
    // --- State ---
    var name by remember { mutableStateOf(existingWarehouse?.name ?: "") }
    var location by remember { mutableStateOf(existingWarehouse?.location ?: "") }
    var nameTouched by remember { mutableStateOf(false) }
    var locationTouched by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf<Country?>(null) }

    // Reset fields only when dialog *opens*, not closes
    LaunchedEffect(visible, existingWarehouse) {
        if (visible) {
            name = existingWarehouse?.name ?: ""
            location = existingWarehouse?.location ?: ""
            nameTouched = false
            locationTouched = false

            // Load existing country if editing
            selectedCountry = if (existingWarehouse?.isoCode != null) {
                Country.findByCode(existingWarehouse.isoCode) ?: Country.findByCode("LB")
            } else {
                Country.findByCode("LB")
            }
        }
    }

    // --- Validation ---
    val isNameValid = name.trim().length >= 3
    val isLocationValid = location.trim().length >= 3
    val isFormValid = isNameValid && isLocationValid

    // --- Dialog ---
    ContentDialog(
        title = if (existingWarehouse == null)
            stringResource(Res.string.add_warehouse)
        else
            stringResource(Res.string.edit_warehouse),
        visible = visible,
        size = DialogSize.Max,
        primaryButtonText = stringResource(
            if (existingWarehouse == null) Res.string.create else Res.string.save
        ),
        closeButtonText = stringResource(Res.string.cancel),
        onButtonClick = { button ->
            when (button) {
                ContentDialogButton.Primary -> {
                    if (isFormValid) {
                        // Perform safe submit
                        scope.launch(Dispatchers.IO) {
                            val safeName = name.trim()
                            val safeLocation = location.trim()
                            val dto = WarehouseDTO(
                                id = existingWarehouse?.id ?: 0L,
                                name = safeName,
                                location = safeLocation,
                                isoCode = selectedCountry?.code ?: "LB",
                                totalFunds = existingWarehouse?.totalFunds ?: java.math.BigDecimal.ZERO,
                                createdAt = existingWarehouse?.createdAt,
                                users = existingWarehouse?.users ?: emptyList(),
                                customersCount = existingWarehouse?.customersCount ?: 0,
                                receiptsCount = existingWarehouse?.receiptsCount ?: 0
                            )

                            onSubmit(dto)

                            // Only close dialog once submit finishes
                            withContext(Dispatchers.Main) { onDismiss() }
                        }
                    } else {
                        nameTouched = true
                        locationTouched = true
                    }
                }
                else -> onDismiss()
            }
        },
        content = {
            Column(Modifier.fillMaxWidth()) {
                // Warehouse Name Field
                ABLogisticsTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    header = { Text(stringResource(Res.string.warehouse_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameTouched && !isNameValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                if (nameTouched && !isNameValid) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Warehouse name must be at least 3 characters",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Location Field with Country Picker
                CountryPickerTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        locationTouched = true
                    },
                    selectedCountry = selectedCountry,
                    onCountrySelected = { country ->
                        selectedCountry = country
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.location),
                    placeholder = "Enter location..."
                )

                if (locationTouched && !isLocationValid) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Location must be at least 3 characters",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                
            }
        }
    )
}
