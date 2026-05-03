package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.InfoBar
import io.github.composefluent.component.InfoBarSeverity
import io.github.composefluent.component.SubtleButton
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.People
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.ArrowNext
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.UserDTO
import org.safieddine.ablogistics.data.WarehouseDTO
import org.safieddine.ablogistics.data.WarehouseUpdateRequest
import org.safieddine.ablogistics.ui.theme.DeleteDialog
import ablogistics.composeapp.generated.resources.*
import org.safieddine.ablogistics.ui.theme.BrandingLightGray
import org.safieddine.ablogistics.ui.theme.BrandingWhite

@Composable
fun AdminScreen(
    usersViewModel: AdminUsersViewModel = viewModel(),
    warehousesViewModel: AdminWarehousesViewModel = viewModel(),
    onWareHouseSelected: (WarehouseDTO?) -> Unit
) {

    val users by usersViewModel.users.collectAsState()
    var userToDelete by remember { mutableStateOf<UserDTO?>(null) }
    val usersLoading by usersViewModel.usersLoading.collectAsState()
    val usersError by usersViewModel.usersError.collectAsState()

    var showUserErrorInfo by remember { mutableStateOf(false) }

    LaunchedEffect(usersError) {
        showUserErrorInfo = usersError != null && usersError!!.isNotEmpty()
    }

    LaunchedEffect(Unit) {
        usersViewModel.loadUsers()
        warehousesViewModel.loadWarehouses()
    }

    val warehouses by warehousesViewModel.warehouses.collectAsState()
    val warehousesLoading by warehousesViewModel.warehousesLoading.collectAsState()
    val warehousesError by warehousesViewModel.warehousesError.collectAsState()

    var showUserForm by remember { mutableStateOf(false) }
    var showWarehouseForm by remember { mutableStateOf(false) }

    var warehouseToEdit by remember { mutableStateOf<WarehouseDTO?>(null) }
    var warehouseToDelete by remember { mutableStateOf<WarehouseDTO?>(null) }
    var showEditUserForm by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserDTO?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedWarehouse by remember { mutableStateOf<WarehouseDTO?>(null) }

    val scope = rememberCoroutineScope()

    // Create User Dialog
    UserFormDialog(
        scope = scope,
        visible = showUserForm,
        warehouses = warehouses,
        onDismiss = { showUserForm = false },
        isAdminUser = false,
        onSubmit = {
            it.apply {
                usersViewModel.createAdminUser(
                    username,
                    fullName,
                    phoneNumber,
                    password ?: "",
                    isAdmin,
                    warehouseID
                )
                warehousesViewModel.loadWarehouses()
                showUserForm = false
            }
        }
    )

    // Create Warehouse Dialog
    WarehouseDialog(
        scope = scope,
        visible = showWarehouseForm,
        onDismiss = { showWarehouseForm = false },
        onSubmit = {
            it.apply {
                warehousesViewModel.createWarehouse(
                    it.name,
                    it.location ?: "",
                    it.totalFunds ?: 0.0,
                    it.isoCode
                )
            }
            showWarehouseForm = false
        }
    )

    // Edit Warehouse Dialog
    WarehouseDialog(
        scope = scope,
        existingWarehouse = warehouseToEdit,
        visible = warehouseToEdit != null,
        onDismiss = { warehouseToEdit = null },
        onSubmit = {
            it.apply {
                warehousesViewModel.updateWarehouse(
                    it.id,
                    WarehouseUpdateRequest(name = it.name, location = it.location ?: "", isoCode = it.isoCode)
                )
                usersViewModel.loadUsers()
            }
            warehouseToEdit = null
        }
    )

    // Delete User Dialog
    DeleteDialog(
        showDeleteDialog = showDeleteDialog && userToDelete != null,
        title = stringResource(Res.string.attention_title),
        message = stringResource(Res.string.delete_user_message, userToDelete?.fullName ?: "")
    ) { button ->
        if (button == ContentDialogButton.Primary) {
            userToDelete?.let { user ->
                usersViewModel.deleteAdminUser(user.username)
                warehousesViewModel.loadWarehouses()
            }
        }
        showDeleteDialog = false
        userToDelete = null
    }

    // Delete Warehouse Dialog
    DeleteDialog(
        showDeleteDialog = warehouseToDelete != null,
        title = stringResource(Res.string.attention_title),
        message = stringResource(
            Res.string.delete_warehouse_message,
            warehouseToDelete?.name ?: ""
        )
    ) { button ->
        if (button == ContentDialogButton.Primary) {
            warehouseToDelete?.let { warehouse ->
                warehousesViewModel.deleteWarehouse(warehouse.id)
                usersViewModel.loadUsers()
            }
        }
        warehouseToDelete = null
    }

    // Edit User Dialog
    UserFormDialog(
        scope = scope,
        visible = showEditUserForm && userToEdit != null,
        existingUser = userToEdit,
        isAdminUser = userToEdit?.isAdmin ?: false,
        warehouses = warehouses,
        onDismiss = {
            showEditUserForm = false
            userToEdit = null
        },
        onSubmit = { formData ->
            usersViewModel.updateAdminUser(
                username = formData.username,
                fullName = formData.fullName,
                phone = formData.phoneNumber,
                password = formData.password ?: "",
                isAdmin = formData.isAdmin,
                warehouseID = formData.warehouseID
            ) {
                warehousesViewModel.loadWarehouses()
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(1.dp))
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = BrandingLightGray.copy(alpha = 0.5f)
                        )
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    HeaderWithAction(
                        title = stringResource(Res.string.system_users),
                        iconVector = Icons.Filled.People,
                        onAdd = { showUserForm = true }
                    ) {
                        usersViewModel.loadUsers()
                    }

                    UserTable(
                        users = users,
                        isLoading = usersLoading,
                        onEdit = {
                            userToEdit = it
                            showEditUserForm = true
                        },
                        onDelete = {
                            userToDelete = it
                            showDeleteDialog = true
                        },
                        onStatusChanges = { dto, bool ->
                            usersViewModel.changeUserStatus(dto, bool)
                        }
                    )
                }

                if (showUserErrorInfo && !usersError.isNullOrBlank()) {
                    InfoBar(
                        title = {},
                        message = { Text(usersError ?: "") },
                        severity = InfoBarSeverity.Critical,
                        closeAction = {
                            IconButton({
                                showUserErrorInfo = false
                                usersViewModel.loadUsers()
                            }) {
                                Icon(imageVector = Icons.Default.ArrowCounterclockwise, "")
                            }
                        }
                    )
                }
            }

            HorizontalDivider(
                Modifier.width(1.dp).fillMaxHeight(),
                color = BrandingWhite
            )

            WarehousePanel(
                isLoading = warehousesLoading,
                data = warehouses,
                error = warehousesError,
                onReload = { warehousesViewModel.loadWarehouses() },
                onAdd = { showWarehouseForm = true },
                onDelete = { warehouseToDelete = it },
                onEdite = { warehouseToEdit = it },
                onUnAssign = {
                    warehousesViewModel.unAssign(it) {
                        usersViewModel.loadUsers()
                    }
                }
            ) {
                selectedWarehouse = it
            }
        }

        InfoBar(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            title = { Text(text = stringResource(Res.string.please_select_warehouse)) },
            severity = InfoBarSeverity.Informational,
            message = {},
            closeAction = {
                SubtleButton(
                    iconOnly = true,
                    disabled = selectedWarehouse == null,
                    onClick = { onWareHouseSelected(selectedWarehouse) },
                    content = {
                        Icon(Icons.Default.ArrowNext, contentDescription = null)
                    },
                    modifier = Modifier.defaultMinSize(38.dp, 38.dp)
                )
            })
    }
}

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "shimmerAnim"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value, 0f),
        end = Offset(translateAnim.value + 200f, 0f)
    )

    return this.background(brush)
}

@Composable
fun UserTableShimmerRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(2f).height(16.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1.5f).height(16.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1.5f).height(16.dp).shimmerEffect())
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(16.dp).shimmerEffect())
    }
}

@Composable
fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = Color.Red, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(6.dp)) {
            Text(stringResource(Res.string.retry))
        }
    }
}
