package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.composefluent.component.ComboBox
import io.github.composefluent.component.ContentDialog
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.DialogSize
import io.github.composefluent.component.Switcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.UserDTO
import org.safieddine.ablogistics.data.WarehouseDTO
import ablogistics.composeapp.generated.resources.*

data class UserFormData(
    val username: String,
    val fullName: String,
    val phoneNumber: String,
    val password: String?,
    val isAdmin: Boolean = false,
    val warehouseID: Long? = null
)

@Composable
fun UserFormDialog(
    scope: CoroutineScope,
    visible: Boolean,
    isSelfEdit: Boolean = false,
    isAdminUser: Boolean,
    warehouses: List<WarehouseDTO> = emptyList(),
    onDismiss: () -> Unit,
    existingUser: UserDTO? = null,
    onSubmit: (UserFormData) -> Unit
) {
    // -------------------------
    // State
    // -------------------------
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(isAdminUser) }
    var selectedWarehouseIndex by remember { mutableStateOf<Int?>(null) }
    var warehouseDropDownData by remember { mutableStateOf(warehouses) }

    var fullNameTouched by remember { mutableStateOf(false) }
    var usernameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }

    // -------------------------
    // Reset when opening dialog
    // -------------------------
    LaunchedEffect(visible, existingUser, warehouses, isAdminUser) {
        if (visible) {
            isAdmin = isAdminUser

            // Rebuild dropdowns depending on role
            if (isAdmin) {
                selectedWarehouseIndex = null
                warehouseDropDownData = warehouses.filter { it.users.isEmpty() && it.id != 1L }
            } else {
                if (existingUser != null) {
                    val userWarehouse = existingUser.userWarehouse
                    if (userWarehouse != null) {
                        warehouseDropDownData = warehouses.filter {
                            it.id == userWarehouse.id || (it.users.isEmpty() && it.id != 1L)
                        }
                        selectedWarehouseIndex = 0
                    } else {
                        warehouseDropDownData = warehouses.filter { it.users.isEmpty() }
                        selectedWarehouseIndex = null
                    }
                } else {
                    warehouseDropDownData = warehouses.filter { it.users.isEmpty() }
                    selectedWarehouseIndex = null
                }
            }

            // Populate user data
            fullName = existingUser?.fullName ?: ""
            username = existingUser?.username ?: ""
            phoneNumber = existingUser?.phoneNumber ?: ""
            password = ""

            // Reset touch flags
            fullNameTouched = false
            usernameTouched = false
            phoneTouched = false
            passwordTouched = false
            isSubmitting = false
        }
    }

    // -------------------------
    // Validation
    // -------------------------
    val isFullNameValid = fullName.trim().length >= 3
    val isUsernameValid = username.trim().length >= 3
    val isPhoneValid = phoneNumber.trim().length in 6..20
    val isPasswordValid = password.isBlank() || password.trim().length >= 6

    val isFormValid = if (isSelfEdit || isAdmin)
        isFullNameValid && isUsernameValid && isPhoneValid && isPasswordValid
    else
        isFullNameValid && isUsernameValid && isPhoneValid && isPasswordValid && selectedWarehouseIndex != null

    // -------------------------
    // Dialog + Loading Overlay
    // -------------------------
    Box {
        ContentDialog(
            title = if (existingUser == null)
                stringResource(Res.string.add_system_user)
            else
                stringResource(Res.string.edit_system_user),
            visible = visible,
            size = DialogSize.Max,
            primaryButtonText = if (existingUser == null)
                stringResource(Res.string.create)
            else
                stringResource(Res.string.save),
            closeButtonText = stringResource(Res.string.cancel),
            onButtonClick = { button ->
                when (button) {
                    ContentDialogButton.Primary -> {
                        if (isFormValid && !isSubmitting) {
                            isSubmitting = true
                            scope.launch(Dispatchers.IO) {
                                val dto = UserFormData(
                                    username = username.trim(),
                                    fullName = fullName.trim(),
                                    phoneNumber = phoneNumber.trim(),
                                    password = password.trim().ifBlank { null },
                                    isAdmin = isAdmin,
                                    warehouseID = if (selectedWarehouseIndex == null)
                                        null else warehouseDropDownData[selectedWarehouseIndex ?: 0].id
                                )

                                onSubmit(dto)

                                // After submit, close dialog safely on main thread
                                withContext(Dispatchers.Main) {
                                    isSubmitting = false
                                    onDismiss()
                                }
                            }
                        } else {
                            fullNameTouched = true
                            usernameTouched = true
                            phoneTouched = true
                            passwordTouched = true
                        }
                    }

                    ContentDialogButton.Close -> if (!isSubmitting) onDismiss()
                    else -> Unit
                }
            },
            content = {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameTouched = true
                        },
                        label = { Text(stringResource(Res.string.full_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = fullNameTouched && !isFullNameValid,
                        singleLine = true,
                        enabled = !isSubmitting,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            usernameTouched = true
                        },
                        label = { Text(stringResource(Res.string.username)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = existingUser == null && !isSubmitting,
                        isError = usernameTouched && !isUsernameValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneTouched = true
                        },
                        label = { Text(stringResource(Res.string.phone_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = phoneTouched && !isPhoneValid,
                        singleLine = true,
                        enabled = !isSubmitting,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordTouched = true
                        },
                        label = {
                            Text(
                                if (existingUser == null)
                                    stringResource(Res.string.password)
                                else
                                    stringResource(Res.string.new_password_optional)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordTouched && !isPasswordValid,
                        singleLine = true,
                        enabled = !isSubmitting,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    if (!isSelfEdit) {
                        Row(Modifier.fillMaxWidth()) {
                            ComboBox(
                                placeholder = stringResource(Res.string.pick_a_warehouse),
                                selected = selectedWarehouseIndex,
                                items = warehouseDropDownData.map { it.name },
                                disabled = isAdmin || isSubmitting,
                                onSelectionChange = { i, _ -> selectedWarehouseIndex = i }
                            )

                            Spacer(Modifier.weight(1f))

                            Switcher(
                                checked = isAdmin,
                                onCheckStateChange = {
                                    if (!isSubmitting) {
                                        isAdmin = it
                                        if (it) selectedWarehouseIndex = null
                                    }
                                },
                                text = if (isAdmin)
                                    stringResource(Res.string.admin)
                                else
                                    stringResource(Res.string.manager)
                            )
                        }
                    }
                }
            }
        )

        // -------------------------
        // Loading Overlay Spinner
        // -------------------------
        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
