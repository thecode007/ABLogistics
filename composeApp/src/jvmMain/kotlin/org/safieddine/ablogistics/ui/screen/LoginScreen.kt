package org.safieddine.ablogistics.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.Icon
import io.github.composefluent.component.InfoBar
import io.github.composefluent.component.InfoBarSeverity
import io.github.composefluent.component.ProgressRing
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Eye
import io.github.composefluent.icons.regular.EyeOff
import io.github.composefluent.icons.regular.LockClosed
import io.github.composefluent.icons.regular.Person
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.WarehouseInfo
import org.safieddine.ablogistics.ui.AppScreen
import ablogistics.composeapp.generated.resources.*
import ablogistics.composeapp.generated.resources.login_bg
import ablogistics.composeapp.generated.resources.sign_in
import ablogistics.composeapp.generated.resources.ab_logo
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
                    if (event.loginData.user.isAdmin()) {
                        onNavigateToMain(AppScreen.Admin, null)
                    } else {
                        onNavigateToMain(
                            AppScreen.Dashboard,
                            event.loginData.user.warehouses.firstOrNull()
                        )
                    }
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
            onPasswordVisibilityChange = { viewModel.togglePasswordVisibility() },
            onLoginClick = viewModel::login
        )
    }
}

@Composable
fun LoginCard(
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(450.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors().copy(containerColor = BrandingWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        Color.Transparent,
                        RoundedCornerShape(35.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.ab_logo),
                    contentDescription = stringResource(Res.string.warehouse_hub_logo),
                    modifier = Modifier.size(70.dp),
                    contentScale = ContentScale.Fit
                )
            }

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

            OutlinedTextField(
                value = uiState.username,
                onValueChange = onUsernameChange,
                label = { Text(stringResource(Res.string.username)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Regular.Person,
                        contentDescription = stringResource(Res.string.username)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = uiState.errorMessage != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(Res.string.password)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Regular.LockClosed,
                        contentDescription = stringResource(Res.string.password)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityChange) {
                        Icon(
                            imageVector = if (uiState.passwordVisible)
                                Icons.Regular.Eye
                            else
                                Icons.Regular.EyeOff,
                            contentDescription = if (uiState.passwordVisible)
                                stringResource(Res.string.hide_password)
                            else
                                stringResource(Res.string.show_password)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (uiState.passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.errorMessage != null
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                enabled = uiState.username.isNotBlank() && uiState.password.isNotBlank() && !uiState.isLoading,
            ) {
                if (uiState.isLoading) {
                    ProgressRing(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.sign_in),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}