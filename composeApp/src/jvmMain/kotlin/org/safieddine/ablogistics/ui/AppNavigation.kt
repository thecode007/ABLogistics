package org.safieddine.ablogistics.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.composefluent.component.ContentDialog
import io.github.composefluent.component.ContentDialogButton
import io.github.composefluent.component.DialogSize
import org.jetbrains.compose.resources.*
import org.safieddine.ablogistics.data.WarehouseInfo
import org.safieddine.ablogistics.ui.screen.BlockedUser
import org.safieddine.ablogistics.ui.screen.LoginScreen
import org.safieddine.ablogistics.ui.screen.NoWarehouseAssigned
import org.safieddine.ablogistics.ui.screen.SplashScreen
import org.safieddine.ablogistics.ui.screen.adminScreen.AdminScreen
import ablogistics.composeapp.generated.resources.*

@Composable
fun AppNavigation(
    modifier: Modifier,
    displayLogoutDialog: Boolean = false,
    routToScreen: AppScreen? = null,
    onAuthenticationSuccess:() -> Unit,
    onWareHouseSelected:(WarehouseInfo?)-> Unit,
    onSplashPassed:() -> Unit,
    onLogout:(ContentDialogButton)-> Unit
) {

    Box(modifier = modifier,
        contentAlignment = Alignment.Center) {
        var currentScreen by remember {
            mutableStateOf<AppScreen>(AppScreen.Splash)
        }

        val currentUser by org.safieddine.ablogistics.data.session.SessionStore.currentUser.collectAsState()

        LaunchedEffect(currentUser) {
            if (currentUser == null && currentScreen != AppScreen.Splash && currentScreen != AppScreen.Login) {
                currentScreen = AppScreen.Login
            }
        }

        LaunchedEffect(routToScreen) {
            if (routToScreen != null)
                currentScreen = routToScreen
        }

        if (displayLogoutDialog) {
            ContentDialog(
                title = stringResource(Res.string.logout_dialog_title),
                visible = displayLogoutDialog,
                size = DialogSize.Max,
                primaryButtonText = stringResource(Res.string.logout_dialog_confirm),
                closeButtonText = stringResource(Res.string.logout_dialog_cancel),
                onButtonClick = {
                    onLogout(it)
                    if (it == ContentDialogButton.Primary) {
                        currentScreen = AppScreen.Login
                    }
                },
                content = {
                    Text(stringResource(Res.string.logout_dialog_message))
                }
            )
        }

        when (currentScreen) {
            is AppScreen.Splash -> {
                SplashScreen { screen ->
                    currentScreen = screen
                    onSplashPassed()
                }
            }

            is AppScreen.Admin -> {
                // Admin screen is now hidden, but we keep the code in case it's needed
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Admin Screen is currently disabled")
                }
            }

            is AppScreen.Login -> {
                LoginScreen(
                    onNavigateToMain = { screen, warehouse ->
                        onAuthenticationSuccess()
                        // Always go to Dashboard (Main Screen)
                        currentScreen = AppScreen.Dashboard
                        if (warehouse != null) {
                            onWareHouseSelected(warehouse)
                        }
                    }
                )
            }
            is AppScreen.Blocked -> {
                BlockedUser()
            }

            is AppScreen.NoWarehouseAssigned -> {
                NoWarehouseAssigned()
            }
            else -> {
                TeamsNavigationRail()
            }
        }
    }

}
