package org.safieddine.ablogistics.ui.screen.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.*
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.PersonNote
import io.github.composefluent.icons.regular.Add
import io.github.composefluent.icons.regular.ArrowCounterclockwise
import io.github.composefluent.icons.regular.Delete
import io.github.composefluent.icons.regular.Pen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.CreateCustomerRequest
import org.safieddine.ablogistics.data.CustomerResponse
import org.safieddine.ablogistics.data.UpdateCustomerRequest
import org.safieddine.ablogistics.data.service.CustomerService
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.ui.screen.StateOfAccountScreen
import org.safieddine.ablogistics.ui.screen.StatementOverlayDialog
import org.safieddine.ablogistics.ui.screen.adminScreen.UserTableShimmerRow
import org.safieddine.ablogistics.ui.theme.DeleteDialog
import ablogistics.composeapp.generated.resources.*
import org.safieddine.ablogistics.ui.screen.receipts.formatLocalized
import java.util.Locale
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import org.safieddine.ablogistics.ui.theme.ABLogisticsSubtleButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsButton

@Composable
fun ColumnScope.  CustomerScreen() {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var customers by remember { mutableStateOf<List<CustomerResponse>>(emptyList()) }
    var totalFunds by remember { mutableStateOf(0.0) }

    var query by remember { mutableStateOf("") }

    val visibleCustomers by remember(customers, query) {
        mutableStateOf(
            if (query.isBlank()) customers else customers.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phoneNumber.contains(query, ignoreCase = true)
            }
        )
    }

    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CustomerResponse?>(null) }
    var toDelete by remember { mutableStateOf<CustomerResponse?>(null) }
    var showStatementFor by remember { mutableStateOf<CustomerResponse?>(null) }

    fun load() {
        isLoading = true
        error = null
        scope.launch {
            val res = CustomerService.list(warehouseId = SessionStore.selectedWarehouse.value?.id ?: 0L)
            if (res.isSuccess) {
                customers = res.getOrNull()?.data?.customers ?: emptyList()
                totalFunds = res.getOrNull()?.data?.totalFundsSum ?: 0.0
            } else {
                error = res.exceptionOrNull()?.message
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    // Header actions
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ABLogisticsTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = {
                Text(stringResource(Res.string.search_by_name_or_phone))
            },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))
        ABLogisticsSubtleButton(iconOnly = true, onClick = { load() }) {
            Icon(Icons.Default.ArrowCounterclockwise, contentDescription = null)
        }
        Spacer(Modifier.width(6.dp))
        ABLogisticsSubtleButton(iconOnly = true, onClick = { editing = null; showForm = true }) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

    // Error info bar
    if (error != null) {
        InfoBar(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            title = { Text(text = error ?: "") },
            severity = InfoBarSeverity.Critical,
            message = {},
            closeAction = {
                InfoBarDefaults.CloseActionButton({ error = null })
            }
        )
    }

    // Empty state or table
    if (!isLoading && visibleCustomers.isEmpty()) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PersonNote,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = FluentTheme.colors.background.smoke.default
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (query.isBlank())
                    stringResource(Res.string.no_customers_found)
                else
                    stringResource(Res.string.no_matching_customers),
                style = FluentTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = FluentTheme.colors.background.smoke.default
            )
        }
    } else {
        LazyColumn(Modifier.weight(1f)) {
            stickyHeader {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(FluentTheme.colors.background.mica.base.copy(alpha = 0.9f))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.name), Modifier.weight(1.8f),
                        style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stringResource(Res.string.phone_number), Modifier.weight(1.4f),
                        style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stringResource(Res.string.location), Modifier.weight(1.2f),
                        style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stringResource(Res.string.total), Modifier.weight(1f),
                        style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stringResource(Res.string.actions), Modifier.weight(1f),
                        style = FluentTheme.typography.title.copy(fontSize = 14.sp),
                        textAlign = TextAlign.Center
                    )
                }
                HorizontalDivider(
                    color = FluentTheme.colors.background.mica.base.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
            }

            if (isLoading) {
                items(5) {
                    UserTableShimmerRow()
                    HorizontalDivider(
                        color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )
                }
            } else {
                itemsIndexed(visibleCustomers, key = { _, c -> c.id }) { index, c ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if (index % 2 == 0) Color.Transparent
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            .padding(vertical = 4.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(c.name, textAlign = TextAlign.Center, modifier = Modifier.weight(1.8f))
                        Text(c.phoneNumber, textAlign = TextAlign.Center, modifier = Modifier.weight(1.4f))
                        Text(c.location, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                        Text(
                            formatLocalized(c.totalFunds, Locale.getDefault()),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold,
                            color = FluentTheme.colors.fillAccent.default
                        )
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                            IconButton({ editing = c; showForm = true }) {
                                Icon(
                                    Icons.Default.Pen,
                                    contentDescription = stringResource(Res.string.edit),
                                    tint = FluentTheme.colors.system.attention
                                )
                            }

                            if (SessionStore.currentUser.value?.isAdmin() == true) {
                                Spacer(Modifier.width(6.dp))
                                IconButton({ toDelete = c }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(Res.string.delete),
                                        tint = FluentTheme.colors.system.critical
                                    )
                                }
                            }

                            Spacer(Modifier.width(6.dp))
                            IconButton({ showStatementFor = c }) {
                                Icon(
                                    Icons.Filled.PersonNote,
                                    contentDescription = "Statement of Account",
                                    tint = FluentTheme.colors.fillAccent.default
                                )
                            }

                        }
                    }
                    HorizontalDivider(
                        color = FluentTheme.colors.background.mica.base.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )
                }


            }
        }
        HorizontalDivider(
            color = FluentTheme.colors.background.mica.base,
            thickness = 1.dp
        )
        Row(Modifier.fillMaxWidth()) {
            Spacer(
                Modifier.weight(1.8f),
            )
            Spacer(
                Modifier.weight(1.4f),
            )
            Spacer(
                Modifier.weight(1.2f),
            )

            Text(
                formatLocalized(totalFunds, Locale.getDefault()),
                Modifier.weight(1f),
                color = FluentTheme.colors.fillAccent.default,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                Modifier.weight(1f),
            )

        }


    }

    // Add / Edit dialog
    if (showForm) {
        var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
        var phone by remember(editing) { mutableStateOf(editing?.phoneNumber ?: "") }
        var location by remember(editing) { mutableStateOf(editing?.location ?: "") }

        var nameTouched by remember { mutableStateOf(false) }
        var phoneTouched by remember { mutableStateOf(false) }
        var locationTouched by remember { mutableStateOf(false) }

        val isNameValid = name.trim().length >= 3
        val isPhoneValid = phone.trim().length in 6..20
        val isLocationValid = location.trim().isNotEmpty()

        val selectedWarehouse = SessionStore.selectedWarehouse.collectAsState().value
        val erroware = stringResource(Res.string.please_select_warehouse_first)
        ContentDialog(
            title = stringResource(if (editing == null) Res.string.add_customer else Res.string.edit_customer),
            visible = true,
            size = DialogSize.Max,
            primaryButtonText = stringResource(if (editing == null) Res.string.create else Res.string.save),
            closeButtonText = stringResource(Res.string.cancel),
            onButtonClick = { btn ->
                if (btn == ContentDialogButton.Primary) {
                    if (!isNameValid || !isPhoneValid || !isLocationValid) {
                        nameTouched = true
                        phoneTouched = true
                        locationTouched = true
                        return@ContentDialog
                    }
                    if (editing == null) {
                        val whId = selectedWarehouse?.id
                        if (whId != null) {
                            scope.launch {
                                val res = CustomerService.create(
                                    CreateCustomerRequest(
                                        name = name,
                                        phoneNumber = phone,
                                        location = location,
                                        totalFunds = 0.0,
                                        warehouseId = whId
                                    )
                                )
                                if (res.isSuccess) {
                                    showForm = false
                                    load()
                                } else {
                                    error = res.exceptionOrNull()?.message
                                }
                            }
                        } else {
                            error = erroware
                        }
                    } else {
                        scope.launch {
                            val res = CustomerService.update(
                                editing!!.id,
                                UpdateCustomerRequest(
                                    name = name,
                                    location = location,
                                    totalFunds = editing!!.totalFunds,
                                    phoneNumber = phone,
                                    warehouseId = selectedWarehouse?.id
                                )
                            )
                            if (res.isSuccess) {
                                showForm = false
                                load()
                            } else {
                                error = res.exceptionOrNull()?.message
                            }
                        }
                    }
                } else {
                    showForm = false
                }
            },
            content = {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    ABLogisticsTextField(
                        value = name,
                        onValueChange = { name = it; nameTouched = true },
                        header = { Text(stringResource(Res.string.full_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = nameTouched && !isNameValid,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(Modifier.height(8.dp))
                    ABLogisticsTextField(
                        value = phone,
                        onValueChange = { phone = it; phoneTouched = true },
                        header = { Text(stringResource(Res.string.phone_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = phoneTouched && !isPhoneValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    ABLogisticsTextField(
                        value = location,
                        onValueChange = { location = it; locationTouched = true },
                        header = { Text(stringResource(Res.string.location)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = locationTouched && !isLocationValid,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
            }
        )
    }

    // Delete dialog
    DeleteDialog(
        showDeleteDialog = toDelete != null,
        title = stringResource(Res.string.delete_customer_title),
        message = stringResource(Res.string.delete_customer_message, toDelete?.name ?: ""),
    ) { btn ->
        if (btn == ContentDialogButton.Primary && toDelete != null) {
            scope.launch {
                val res = CustomerService.delete(toDelete!!.id)
                if (res.isSuccess) load() else error = res.exceptionOrNull()?.message
                toDelete = null
            }
        } else {
            toDelete = null
        }
    }

    // Statement of Account dialog
    if (showStatementFor != null) {
        val whId = SessionStore.selectedWarehouse.value?.id ?: 0L
        StatementOverlayDialog(
            visible = true,
            onDismiss = { showStatementFor = null }
        ) {
            StateOfAccountScreen(
                warehouseId = whId,
                customer = showStatementFor!!,
                start = null,
                end = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
