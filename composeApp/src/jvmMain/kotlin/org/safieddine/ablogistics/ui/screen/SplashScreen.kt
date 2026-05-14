package org.safieddine.ablogistics.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.data.AuthManager
import org.safieddine.ablogistics.data.UserDTO
import org.safieddine.ablogistics.data.service.UserService
import org.safieddine.ablogistics.ui.AppScreen
import ablogistics.composeapp.generated.resources.*

@Composable
fun SplashScreen(
    authManager: AuthManager = AuthManager,
    onNavigation: (AppScreen) -> Unit
) {
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        showText = true
        delay(1000)

        val isLoggedIn = checkAuthentication(authManager)

        if (isLoggedIn && isBlocked(authManager) != true) {
            val refreshed = refreshCurrentUser(authManager)
            if (refreshed && authManager.getCurrentUser()?.isAdmin() != true && authManager.getSelectedWarehouse() == null) {
                onNavigation(AppScreen.NoWarehouseAssigned)
                return@LaunchedEffect
            }
            else if (!refreshed) {
                authManager.logout()
                onNavigation(AppScreen.Login)
                return@LaunchedEffect
            }
        }

        if (isLoggedIn && isBlocked(authManager) == true) {
            onNavigation(AppScreen.Blocked)
            return@LaunchedEffect
        }

        if (isLoggedIn && authManager.getCurrentUser()?.isAdmin() == true) {
            onNavigation(AppScreen.Dashboard)
            return@LaunchedEffect
        }

        if (isLoggedIn) {
            onNavigation(AppScreen.Dashboard)
        } else {
            authManager.logout()
            onNavigation(AppScreen.Login)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandingLogoAnimation()

            if (showText) {
                Spacer(modifier = Modifier.height(40.dp))
                AnimatedAppTitle()
            }
        }
    }
}

/* ------------------------------------ */
/* ----- WINDOWS ANIMATION LOGO  ------ */
/* ------------------------------------ */

@Composable
fun BrandingLogoAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Image(
        painter = painterResource(Res.drawable.splash_logo),
        contentDescription = "AB LOGISTICS LTD Logo",
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentScale = ContentScale.Fit
    )
}

/* ------------------------------------ */
/* ----------- TITLE ANIMATION -------- */
/* ------------------------------------ */

@Composable
fun AnimatedAppTitle() {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )
    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offsetY
        }
    ) {
        Text(
            text = stringResource(Res.string.app_title),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.management_system),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Colors are now integrated into BrandingLogoAnimation and MaterialTheme

private suspend fun checkAuthentication(authManager: AuthManager): Boolean =
    try {
        if (authManager.isLoggedIn()) authManager.validateCurrentToken() else false
    } catch (e: Exception) {
        println("Authentication check failed: ${e.message}")
        false
    }

private suspend fun isBlocked(authManager: AuthManager): Boolean? =
    try {
        authManager.isBlocked()
    } catch (e: Exception) {
        println("Blocked check failed: ${e.message}")
        false
    }

private suspend fun refreshCurrentUser(authManager: AuthManager): Boolean {
    val username = authManager.getCurrentUser()?.username ?: return false
    return try {
        val result = UserService.getUser(username)
        result.fold(
            onSuccess = { resp ->
                val fresh: UserDTO? = resp.data
                if (fresh != null) {
                    authManager.updateCurrentUSer(fresh.fullName, fresh.phoneNumber)
                    // Auto-select first warehouse for all users (admin and non-admin)
                    // if none is already persisted from a previous session
                    if (authManager.getSelectedWarehouse() == null) {
                        authManager.setSelectedWarehouse(fresh.warehouses.firstOrNull())
                    }
                    println("User refreshed successfully: ${fresh.username}")
                    true
                }
                else
                    false
            },
            onFailure = {
                println("Failed to refresh user: ${it.message}")
                false
            }
        )
    } catch (e: Exception) {
        println("Error refreshing user: ${e.message}")
        false
    }
}
