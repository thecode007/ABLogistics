@file:OptIn(io.github.composefluent.ExperimentalFluentApi::class)
package org.safieddine.ablogistics.ui.theme

import ablogistics.composeapp.generated.resources.Res
import ablogistics.composeapp.generated.resources.app_title
import ablogistics.composeapp.generated.resources.blocked_message
import ablogistics.composeapp.generated.resources.france
import ablogistics.composeapp.generated.resources.ab_logo
import ablogistics.composeapp.generated.resources.online
import ablogistics.composeapp.generated.resources.role_admin
import ablogistics.composeapp.generated.resources.role_manager
import ablogistics.composeapp.generated.resources.united_kingdom
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.background.MaterialContainerScope
import io.github.composefluent.background.MaterialDefaults
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import org.safieddine.ablogistics.ui.theme.ABLogisticsSubtleButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import io.github.composefluent.FluentTheme
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.BuildingFactory
import io.github.composefluent.icons.filled.ArrowSwap
import io.github.composefluent.icons.filled.Edit
import io.github.composefluent.icons.filled.PersonCircle
import io.github.composefluent.icons.filled.SignOut
import io.github.composefluent.icons.filled.Live
import io.github.composefluent.icons.filled.LiveOff
import io.github.composefluent.icons.regular.Subtract
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import io.github.composefluent.icons.regular.Dismiss
import io.github.composefluent.icons.regular.Maximize
import io.github.composefluent.icons.regular.SquareMultiple
import org.jetbrains.compose.resources.painterResource
import org.safieddine.ablogistics.data.MaterialPriceDTO
import org.safieddine.ablogistics.data.MaterialType
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.ui.screen.receipts.formatLocalized
import io.github.composefluent.icons.regular.Edit
import java.math.BigDecimal
import org.safieddine.ablogistics.ui.screen.receipts.parseLocalizedNumber
import org.jetbrains.compose.resources.stringResource
import java.util.Locale
import kotlinx.coroutines.delay

// ─── Teams 2.0 brand palette (neutral grey bar) ────────────────────────────
private val TeamsBackground    = Color(0xFFEFEFEF)   // Teams 2.0 light grey
private val TeamsBackgroundEnd = Color(0xFFE5E5E5)   // slightly deeper grey
private val TeamsOnSurface     = Color(0xFF242424)   // near-black text
private val TeamsOnSurface70   = Color(0xFF616161)   // secondary grey text
private val TeamsOnSurface40   = Color(0x33000000)   // 20% black overlay
private val TeamsControlHover  = Color(0x18000000)   // 10% black hover
private val TeamsCloseHover    = Color(0xFFD32F2F)
private val TeamsAvatarBg      = Color(0xFF6264A7)   // Teams purple avatar
private val TeamsOnlineDot     = Color(0xFF13A10E)

data class TitleBarState(
    val userName: String = "",
    val warehouseName: String = "",
    val isAdmin: Boolean = false,
    val isOnline: Boolean = false
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.TitleBar(
    titleBarState: TitleBarState = TitleBarState(),
    globalPrices: List<MaterialPriceDTO> = emptyList(),
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onWarehouseExit: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    isMaximized: Boolean,
    onLogout: () -> Unit,
    onPriceEdit: () -> Unit
) {
    WindowDraggableArea {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(TeamsBackground.copy(alpha = 0.72f))
                .shadow(elevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ── Left: logo + app name ────────────────────────────────────
                Spacer(Modifier.width(12.dp))
                Image(
                    painter = painterResource(Res.drawable.ab_logo),
                    contentDescription = "Warehouse Hub",
                    modifier = Modifier.size(26.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.app_title),
                    color = TeamsOnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )

                // Flexible spacer pushes right-side content to the right
                Spacer(Modifier.weight(1f))

                // ── Right section: chips, status, language, controls ─────────
                
                val fuelPrice = globalPrices.find { it.materialType == MaterialType.FUEL } 
                    ?: MaterialPriceDTO(MaterialType.FUEL, BigDecimal.ZERO, BigDecimal.ZERO)
                val dieselPrice = globalPrices.find { it.materialType == MaterialType.DIESEL }
                    ?: MaterialPriceDTO(MaterialType.DIESEL, BigDecimal.ZERO, BigDecimal.ZERO)

                val displayPrices = listOf(fuelPrice, dieselPrice)

                displayPrices.forEach { price ->
                    val icon = if (price.materialType == MaterialType.FUEL) "⛽" else "🚛"
                    val label = if (price.materialType == MaterialType.FUEL) "Fuel" else "Diesel"
                    val costFmt = formatLocalized(price.costPrice, Locale.getDefault())
                    val sellFmt = formatLocalized(price.sellingPrice, Locale.getDefault())

                    PricePill(
                        icon = icon,
                        label = label,
                        cost = costFmt,
                        selling = sellFmt
                    )
                    Spacer(Modifier.width(8.dp))
                }

                IconButton(
                    onClick = onPriceEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Regular.Edit,
                        contentDescription = "Edit Prices",
                        tint = TeamsOnSurface70,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Online / Offline indicator
                Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (titleBarState.isOnline) TeamsOnlineDot
                                else Color(0xFFE57373)
                            )
                    )

                Spacer(Modifier.width(8.dp))

                // Language switcher
                var langMenuOpen by remember { mutableStateOf(false) }
                val lang by SessionStore.language.collectAsState()
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(TeamsControlHover)
                        .clickable { langMenuOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            if (lang == Locale.ENGLISH) Res.drawable.united_kingdom
                            else Res.drawable.france
                        ),
                        contentDescription = "Language",
                        modifier = Modifier.size(18.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    DropdownMenu(expanded = langMenuOpen, onDismissRequest = { langMenuOpen = false }) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Image(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(Res.drawable.united_kingdom),
                                    contentDescription = ""
                                )
                            },
                            text = { Text("English") },
                            onClick = { SessionStore.setLanguage(Locale.ENGLISH); langMenuOpen = false }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Image(
                                    modifier = Modifier.clip(CircleShape).size(20.dp),
                                    painter = painterResource(Res.drawable.france),
                                    contentDescription = ""
                                )
                            },
                            text = { Text("Français") },
                            onClick = { SessionStore.setLanguage(Locale.FRENCH); langMenuOpen = false }
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // User avatar pill
                if (titleBarState.userName.isNotEmpty()) {
                    var userMenuOpen by remember { mutableStateOf(false) }
                    Box {
                        Row(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(TeamsAvatarBg)
                                .clickable { userMenuOpen = true }
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar initials circle
                            val initials = titleBarState.userName
                                .split(" ")
                                .take(2)
                                .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
                                .ifEmpty { "?" }
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF5254A3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = titleBarState.userName,
                                color = TeamsOnSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (titleBarState.isAdmin)
                                    "(${stringResource(Res.string.role_admin)})"
                                else
                                    "(${stringResource(Res.string.role_manager)})",
                                color = TeamsOnSurface70,
                                fontSize = 11.sp
                            )
                        }
                        DropdownMenu(
                            expanded = userMenuOpen,
                            onDismissRequest = { userMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit profile",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = { Text("Edit Profile") },
                                onClick = { onEdit(); userMenuOpen = false }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.SignOut,
                                        contentDescription = "Sign Out",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = { Text("Sign Out") },
                                onClick = { onLogout(); userMenuOpen = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // ── Window controls (minimize / maximize / close) ─────────────
                TeamsTitleBarButton(
                    icon = Icons.Default.Subtract,
                    contentDescription = "Minimize",
                    hoverBackground = TeamsControlHover,
                    onClick = onMinimize
                )
                TeamsTitleBarButton(
                    icon = if (isMaximized) Icons.Default.SquareMultiple else Icons.Default.Maximize,
                    contentDescription = if (isMaximized) "Restore" else "Maximize",
                    hoverBackground = TeamsControlHover,
                    onClick = onMaximize
                )
                TeamsTitleBarButton(
                    icon = Icons.Default.Dismiss,
                    contentDescription = "Close",
                    hoverBackground = TeamsCloseHover,
                    onClick = onClose
                )
            }
        }
    }
}

// ─── Chip helper ─────────────────────────────────────────────────────────────
@Composable
private fun TeamsChip(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(TeamsControlHover)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

// ─── Window control button ────────────────────────────────────────────────────
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TeamsTitleBarButton(
    icon: ImageVector,
    contentDescription: String,
    hoverBackground: Color,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        targetValue = if (isHovered) hoverBackground else Color.Transparent,
        animationSpec = tween(100),
        label = "titleBarBtnBg"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(46.dp)
            .background(bgColor)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TeamsOnSurface,
            modifier = Modifier.size(16.dp)
        )
    }
}
@Composable
private fun PricePill(
    icon: String,
    label: String,
    cost: String,
    selling: String
) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White.copy(alpha = 0.6f))
            .border(1.dp, TeamsOnSurface.copy(alpha = 0.15f), RoundedCornerShape(15.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$label:",
            color = TeamsOnSurface70,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$cost / $selling",
            color = TeamsOnSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
