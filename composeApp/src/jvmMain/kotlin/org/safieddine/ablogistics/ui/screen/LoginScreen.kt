package org.safieddine.ablogistics.ui.screen

import ablogistics.composeapp.generated.resources.Res
import ablogistics.composeapp.generated.resources.ab_logo
import ablogistics.composeapp.generated.resources.login_bg
import ablogistics.composeapp.generated.resources.password
import ablogistics.composeapp.generated.resources.sign_in
import ablogistics.composeapp.generated.resources.sign_in_to_continue
import ablogistics.composeapp.generated.resources.username
import ablogistics.composeapp.generated.resources.warehouse_hub_logo
import ablogistics.composeapp.generated.resources.welcome_back
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.Icon
import io.github.composefluent.component.InfoBar
import io.github.composefluent.component.InfoBarSeverity
import io.github.composefluent.component.ProgressRing
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Person
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.WarehouseInfo
import org.safieddine.ablogistics.ui.AppScreen
import org.safieddine.ablogistics.ui.theme.ABLogisticsAccentButton
import org.safieddine.ablogistics.ui.theme.ABLogisticsSecureTextField
import org.safieddine.ablogistics.ui.theme.ABLogisticsTextField
import org.safieddine.ablogistics.ui.theme.BrandingWhite

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToMain: (AppScreen, WarehouseInfo?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is LoginNavigationEvent.LoginSuccess -> {
                    if (event.loginData.user.isBlocked) {
                        onNavigateToMain(AppScreen.Blocked, null)
                        return@LaunchedEffect
                    }
                    onNavigateToMain(
                        if (event.loginData.user.isAdmin()) AppScreen.Admin else AppScreen.Dashboard,
                        event.loginData.user.warehouses.firstOrNull()
                    )

                    viewModel.clearNavigationEvent()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(Res.drawable.login_bg),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )


        Box(Modifier.background(Black.copy(alpha = 0.6f)).fillMaxSize())

        LoginCard(
            uiState = uiState,
            onUsernameChange = viewModel::updateUsername,
            onPasswordChange = viewModel::updatePassword,
            onLoginClick = viewModel::login
        )
    }
}

@Composable
fun LoginCard(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val passwordState = rememberTextFieldState()

    LaunchedEffect(passwordState) {
        snapshotFlow { passwordState.text.toString() }
            .collect { onPasswordChange(it) }
    }
    Card(
        modifier = Modifier
            .width(400.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors().copy(containerColor = BrandingWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

                Image(
                    painter = painterResource(Res.drawable.ab_logo),
                    contentDescription = stringResource(Res.string.warehouse_hub_logo),
                    modifier = Modifier.size(70.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "AB LOGISTICS LTD",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.welcome_back),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.sign_in_to_continue),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                enter = fadeIn(),
                exit = fadeOut(),
                visible = uiState.errorMessage != null
            ) {
                InfoBar(
                    title = {},
                    message = { Text(uiState.errorMessage ?: "") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    severity = InfoBarSeverity.Critical
                )
            }

            ABLogisticsTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                header = { Text(stringResource(Res.string.username)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = uiState.errorMessage != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            ABLogisticsSecureTextField(
                state = passwordState,
                modifier = Modifier.fillMaxWidth(),
                header = { Text(stringResource(Res.string.password)) },
                isError = uiState.errorMessage != null
            )

            Spacer(modifier = Modifier.height(24.dp))

            ABLogisticsAccentButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                disabled = uiState.username.isBlank() || passwordState.text.isBlank() || uiState.isLoading,
            ) {
                if (uiState.isLoading) {
                    ProgressRing(
                        modifier = Modifier.size(20.dp),
                        color = BrandingWhite
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.sign_in),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp,
                        color = BrandingWhite
                    )
                }
            }
        }
    }
}