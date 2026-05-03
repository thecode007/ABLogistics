package org.safieddine.ablogistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.Icon
import io.github.composefluent.component.Badge
import io.github.composefluent.component.BadgeStatus
import io.github.composefluent.component.InfoBar
import io.github.composefluent.component.InfoBarDefaults
import io.github.composefluent.component.ProgressRing
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Alert
import io.github.composefluent.icons.regular.ClipboardTextLtr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.AuthManager
import org.safieddine.ablogistics.data.UserDTO
import org.safieddine.ablogistics.data.WarehouseInfo
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.data.NotificationManager
import org.safieddine.ablogistics.data.TrayNotifier
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.service.UserService
import org.safieddine.ablogistics.data.service.WarehouseService
import org.safieddine.ablogistics.data.session.SessionStore.language
import org.safieddine.ablogistics.ui.AppNavigation
import org.safieddine.ablogistics.ui.AppScreen
import org.safieddine.ablogistics.ui.screen.adminScreen.UserFormDialog
import org.safieddine.ablogistics.ui.theme.TitleBar
import org.safieddine.ablogistics.ui.theme.ABLogisticsTheme
import org.safieddine.ablogistics.ui.theme.BrandingWhite
import org.safieddine.ablogistics.ui.screen.logs.LogsSidebar
import org.safieddine.ablogistics.ui.screen.notifications.NotificationsSidebar
import org.safieddine.ablogistics.ui.theme.DecisionDialog
import org.safieddine.ablogistics.ui.theme.TitleBarState

fun main() = application {
    val windowState = rememberWindowState(WindowPlacement.Maximized)
    val authManager = remember { AuthManager }
    val currentUser by SessionStore.currentUser.collectAsState(null)
    var routToScreen by remember {
        mutableStateOf<AppScreen?>(null)
    }
    val warehouseDTO by SessionStore.selectedWarehouse.collectAsState()
    var titleBarState by remember {
        mutableStateOf(TitleBarState())
    }
    val scope = rememberCoroutineScope()
    var displayLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDecisionDialog by remember { mutableStateOf(false) }
    var decisionTitle by remember { mutableStateOf("") }
    var decisionMessage by remember { mutableStateOf("") }
    var decisionDetails by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showLogsSidebar by remember { mutableStateOf(false) }
    var showNotificationsSidebar by remember { mutableStateOf(false) }

    // Unified close handler: stop notifications, close services, then exit
    val closeApp: () -> Unit = {
        NotificationManager.stop()
        scope.launch(Dispatchers.IO) {
            UserService.close()
            WarehouseService.close()
            authManager.close()
        }
        exitApplication()
    }


    LaunchedEffect(currentUser) {
        scope.launch(Dispatchers.IO) {
            titleBarState = if (currentUser != null) {
                titleBarState.copy(
                    userName = currentUser!!.username,
                    isAdmin = currentUser!!.isAdmin()
                )
            } else {
                TitleBarState()
            }
        }
        if (currentUser != null) {
            NotificationManager.refreshPendingDecisions()
        }
    }

    LaunchedEffect(warehouseDTO) {
        titleBarState = if (warehouseDTO != null) {
            titleBarState.copy(
                warehouseName = warehouseDTO!!.name,
            )
        } else {
            titleBarState.copy(
                warehouseName = ""
            )
        }
    }

    Window(
        onCloseRequest = { closeApp() },
        title = "Warehouse Hub",
        state = windowState,
        undecorated = true,
        transparent = false
    ) {

        val isMaximized by remember {
            derivedStateOf { windowState.placement == WindowPlacement.Maximized }
        }

        val language by language.collectAsState()


        ABLogisticsTheme {
            FluentTheme {
            val mainViewModel: MainViewModel = viewModel { MainViewModel(authManager = authManager) }

            // Hook tray click to bring app front and show approval dialog if last event is DECISION
            LaunchedEffect(Unit) {
                TrayNotifier.setOnClick {
                    // Bring window to front (best effort)
                    try {
                        val frames = java.awt.Frame.getFrames()
                        val f = frames.firstOrNull()
                        f?.isAlwaysOnTop = true
                        f?.isAlwaysOnTop = false
                        f?.toFront()
                        f?.requestFocus()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
            val error by mainViewModel.usersError.collectAsState()
            val usersLoading by mainViewModel.usersLoading.collectAsState()
            val isOnline by NotificationManager.connected.collectAsState()
            key(language) {

                UserFormDialog(
                    scope,
                    visible = showEditDialog,
                    isSelfEdit = true,
                    isAdminUser = currentUser?.isAdmin() ?: false,
                    onDismiss = {
                        showEditDialog = false
                    },
                    existingUser = currentUser?.run {
                        UserDTO(
                            username = username,
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            warehouses = warehouses.map {
                                WarehouseInfo(it.id, it.name)
                            },
                            roles = roles.toList(),
                            isBlocked = isBlocked
                        )
                    }) { userForm ->

                    mainViewModel.updateAdminUser(
                        userForm.username,
                        userForm.fullName,
                        userForm.phoneNumber,
                        userForm.password ?: ""
                    )
                }

                Box(contentAlignment = Alignment.Center) {
                    Column {
                        TitleBar(
                            titleBarState = titleBarState.copy(
                                isOnline = isOnline
                            ),
                            onClose = closeApp,
                            onMinimize = { windowState.isMinimized = true },
                            onWarehouseExit = {
                                scope.launch {
                                    SessionStore.setSelectedWarehouse(null)
                                    mainViewModel.setSelectedWarehouse(null)
                                    routToScreen = AppScreen.Admin
                                    delay(1000L)
                                    routToScreen = null
                                }


                            },
                            onEdit = {
                                showEditDialog = true
                            },
                            onMaximize = {
                                windowState.placement = if (isMaximized) {
                                    WindowPlacement.Floating
                                } else {
                                    WindowPlacement.Maximized
                                }
                            },
                            isMaximized = isMaximized
                        ) {
                            displayLogoutDialog = true
                        }
                        Row {
                            AppNavigation(
                                modifier = Modifier.weight(1f),
                                routToScreen = routToScreen,
                                displayLogoutDialog = displayLogoutDialog,
                                onAuthenticationSuccess = {
                                    val token = SessionStore.token.value
                                    val user = currentUser
                                    if (!token.isNullOrBlank() && user != null) {
                                        scope.launch(Dispatchers.IO) {
                                            NotificationManager.startAfterLogin(
                                                baseUrl = AppConfig.baseUrl
                                                    .removeSuffix("/")
                                                    .removeSuffix("/api"),
                                                jwt = token,
                                                username = user.username
                                            )
                                        }
                                    }
                                }, onLogout = {
                                    displayLogoutDialog = false
                                    scope.launch(Dispatchers.IO) {
                                        if (it == ContentDialogButton.Primary) {
                                            authManager.logout()
                                            val token = SessionStore.token.value
                                            if (!token.isNullOrBlank()) {
                                                NotificationManager.stopOnLogout(
                                                    baseUrl = AppConfig.baseUrl
                                                        .removeSuffix("/")
                                                        .removeSuffix("/api"),
                                                    jwt = token
                                                )
                                            }
                                        }
                                    }
                                },
                                onWareHouseSelected = {
                                    SessionStore.setSelectedWarehouse(it)
                                },
                                onSplashPassed = {
                                    // SessionStore already reflects current user
                                    val token = SessionStore.token.value
                                    val user = currentUser
                                    if (!token.isNullOrBlank() && user != null) {
                                        scope.launch(Dispatchers.IO) {
                                            NotificationManager.startAfterLogin(
                                                baseUrl = AppConfig.baseUrl.removeSuffix("/").removeSuffix("/api"),
                                                jwt = token,
                                                username = user.username
                                            )
                                        }
                                        if (warehouseDTO == null) {
                                            val firstWh = user.warehouses.firstOrNull()
                                            if (firstWh != null) {
                                                SessionStore.setSelectedWarehouse(firstWh)
                                                mainViewModel.setSelectedWarehouse(firstWh)
                                            }
                                        }
                                    }
                                })

                            AnimatedVisibility(currentUser != null && currentUser?.isAdmin() == true) {
                                Column(
                                    Modifier
                                        .background(FluentTheme.colors.background.mica.base)
                                        .fillMaxHeight()
                                        .width(50.dp)
                                ) {


                                    IconButton({ showNotificationsSidebar = !showNotificationsSidebar }) {
                                        val pending by NotificationManager.pendingDecisions.collectAsState()
                                        Box(
                                            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Alert,
                                                modifier = Modifier.size(25.dp),
                                                contentDescription = null
                                            )
                                            if (pending > 0) {
                                                val content = if (pending > 99) "99+" else pending.toString()
                                                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                                                    Badge(
                                                        status = BadgeStatus.Attention,
                                                        modifier = Modifier.size(15.dp)
                                                    ) {
                                                        Text(
                                                            text = content, color = White,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (currentUser?.isAdmin() == true) {
                                        IconButton({
                                            showLogsSidebar = !showLogsSidebar
                                        }) {

                                            Box(
                                                modifier = Modifier
                                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Regular.ClipboardTextLtr,
                                                    contentDescription = "ClipboardTextLtr",
                                                    modifier = Modifier.size(25.dp),

                                                    )
                                            }
                                        }
                                    }


                                    Spacer(Modifier.weight(1f))

                                    HorizontalDivider(color = BrandingWhite)
                                }
                            }
                        }
                    }

                    if (usersLoading) {
                        ProgressRing()
                    }

                    if (error != null) {
                        InfoBar(
                            title = {
                            },
                            message = {
                                Text(error ?: "")
                            },
                            closeAction = {
                                InfoBarDefaults.CloseActionButton({
                                    mainViewModel.clearError()
                                })
                            }
                        )
                    }

                    DecisionDialog(
                        visible = showDecisionDialog,
                        title = decisionTitle,
                        message = decisionMessage,
                        details = decisionDetails,
                        onButtonClicked = { _ ->
                            // TODO: Wire approve/reject API if available
                            showDecisionDialog = false
                        }
                    )

                    // If blocked by server, route to blocked view
                    val isBlocked by NotificationManager.blocked.collectAsState()
                    LaunchedEffect(isBlocked) {
                        println(isBlocked)
                    }
                    if (isBlocked) {
                        routToScreen = AppScreen.Blocked
                    }

                    if (!isBlocked && routToScreen == AppScreen.Blocked) {
                        routToScreen = AppScreen.Splash
                    }

                    // Show last notification as a transient banner
                    val lastEvent by NotificationManager.lastEvent.collectAsState()
                    if (lastEvent != null) {
                        scope.launch {
                            delay(500)
                            NotificationManager.clearLastEvent()
                        }
                    }
                }

                // Right-side logs sidebar overlay
                LogsSidebar(
                    visible = showLogsSidebar,
                    onClose = { showLogsSidebar = false }
                )

                // Right-side notifications sidebar overlay
                NotificationsSidebar(
                    visible = showNotificationsSidebar,
                    onClose = { showNotificationsSidebar = false }
                )
            }
            }
        }
    }
}

