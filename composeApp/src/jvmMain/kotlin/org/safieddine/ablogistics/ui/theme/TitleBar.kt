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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import io.github.composefluent.icons.regular.Dismiss
import io.github.composefluent.icons.regular.Maximize
import io.github.composefluent.icons.regular.SquareMultiple
import org.jetbrains.compose.resources.painterResource
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.data.session.WarehouseFundsStore
import org.safieddine.ablogistics.ui.screen.receipts.formatLocalized
import org.safieddine.ablogistics.ui.screen.receipts.parseLocalizedNumber
import org.jetbrains.compose.resources.stringResource
import java.util.Locale
import kotlinx.coroutines.delay


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
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onWarehouseExit: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    isMaximized: Boolean,
    onLogout: () -> Unit
) {
    WindowDraggableArea {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(BrandingWhite),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(16.dp))
            Image(
                painter = painterResource(Res.drawable.ab_logo),
                contentDescription = "Warehouse Hub",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )


            Spacer(Modifier.width(8.dp))

            Text(
                text = stringResource(Res.string.app_title),
                style = FluentTheme.typography.title.copy(
                    fontSize = 12.sp
                )
            )

            Spacer(Modifier.weight(1f))

            val selectedWarehouse by SessionStore.selectedWarehouse.collectAsState()
            val totalFunds by WarehouseFundsStore.totalFunds.collectAsState()
            val realFunds by WarehouseFundsStore.realFunds.collectAsState()

            LaunchedEffect(selectedWarehouse?.id) {
                val id = selectedWarehouse?.id ?: return@LaunchedEffect
                WarehouseFundsStore.refresh(id)
                while (true) {
                    delay(20_000)
                    WarehouseFundsStore.refresh(id)
                }
            }

            if (titleBarState.warehouseName.isNotEmpty()) {
                Card(
                    modifier = Modifier.height(30.dp),
                    colors = CardDefaults.cardColors().copy(
                    containerColor = BrandingBlack,
                    contentColor = BrandingWhite
                ),
                shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Filled.BuildingFactory,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "warehouse",
                            tint = BrandingWhite
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = titleBarState.warehouseName,
                            style = FluentTheme.typography.title.copy(
                                fontSize = 15.sp
                            ),
                            color = BrandingWhite
                        )
                            // Funds chips shown and kept in sync
                            Spacer(Modifier.width(10.dp))
                            val locale = Locale.getDefault()
                            val totalFmt =
                                formatLocalized(parseLocalizedNumber("%.2f".format(totalFunds), locale), locale)


                            AssistChip(
                                modifier = Modifier.padding(5.dp),
                                onClick = {},
                                label = { Text(totalFmt) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.ArrowSwap,
                                        contentDescription = null,
                                        tint = BrandingWhite
                                    )
                                }
                            )

                        if (titleBarState.isAdmin) {

                            Icon(
                                imageVector = Icons.Filled.SignOut,
                                modifier = Modifier.clickable {
                                    onWarehouseExit()
                                }.size(20.dp),
                                contentDescription = "Get out of warehouse",
                                tint = BrandingWhite
                            )
                        }
                        Spacer(Modifier.width(5.dp))
                    }
                }
            }

            Spacer(Modifier.width(16.dp))


            if (titleBarState.userName.isNotEmpty()) {
                Card(
                    modifier = Modifier.height(30.dp),
                    colors = CardDefaults.cardColors().copy(
                    containerColor = BrandingGray,
                    contentColor = BrandingWhite
                ),
                shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val user = titleBarState.userName

                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.PersonCircle,
                            modifier = Modifier.size(25.dp),
                            contentDescription = "Person",
                            tint = BrandingWhite
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = "${user}(${
                                if (titleBarState.isAdmin)
                                    stringResource(Res.string.role_admin)
                                else
                                    stringResource(
                                        Res.string.role_manager
                                    )
                            })",
                            style = FluentTheme.typography.title.copy(
                                fontSize = 15.sp
                            ),
                            color = BrandingWhite
                        )

                        Spacer(Modifier.width(8.dp))


                        Icon(
                            imageVector = Icons.Filled.Edit,
                            modifier = Modifier.onPointerEvent(PointerEventType.Enter) { }
                                .onPointerEvent(PointerEventType.Exit) { }
                                .size(20.dp).clickable {
                                    onEdit()
                                },
                            contentDescription = "SignOut",
                            tint = BrandingWhite
                        )

                        Spacer(Modifier.width(4.dp))

                        Icon(
                            imageVector = Icons.Filled.SignOut,
                            modifier = Modifier.onPointerEvent(PointerEventType.Enter) { }
                                .onPointerEvent(PointerEventType.Exit) { }.size(20.dp).clickable {
                                    onLogout()
                                },
                            contentDescription = "SignOut",
                            tint = BrandingWhite
                        )

                        Spacer(Modifier.width(8.dp))

                    }
                }
            }
            Spacer(Modifier.width(8.dp))

            // Language switcher
            var langMenuOpen by remember { mutableStateOf(false) }
            val lang by SessionStore.language.collectAsState()
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .background(
                        BrandingWhite,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { langMenuOpen = true }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Card(
                        modifier = Modifier.width(20.dp)
                            .height(20.dp),
                        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.primary),
                        shape = CircleShape
                    ) {
                        Image(
                            modifier = Modifier.padding(1.dp).fillMaxSize(),
                            painter = painterResource(
                                if (lang == Locale.ENGLISH)
                                    Res.drawable.united_kingdom
                                else
                                    Res.drawable.france,
                            ),
                            contentDescription = "",
                            contentScale = ContentScale.Crop
                        )
                    }



                    Spacer(Modifier.width(6.dp))

                    Text(
                        if (lang == Locale.ENGLISH)
                            "EN"
                        else
                            "FR",
                        style = FluentTheme.typography.title.copy(fontSize = 12.sp)
                    )
                }
                DropdownMenu(expanded = langMenuOpen, onDismissRequest = { langMenuOpen = false }) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Image(
                                modifier = Modifier.width(25.dp),
                                painter = painterResource(Res.drawable.united_kingdom),
                                contentDescription = ""
                            )
                        },
                        text = {
                            Text("English")
                        },
                        onClick = { SessionStore.setLanguage(Locale.ENGLISH); langMenuOpen = false }
                    )
                    DropdownMenuItem(
                        leadingIcon = {

                            Image(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .width(25.dp),
                                painter = painterResource(Res.drawable.france),
                                contentDescription = ""
                            )

                        },
                        text = { Text("Français") },
                        onClick = { SessionStore.setLanguage(Locale.FRENCH); langMenuOpen = false }
                    )
                }
            }


            Icon(
                imageVector = if (titleBarState.isOnline)
                    Icons.Filled.Live
                else
                    Icons.Filled.LiveOff,
                modifier = Modifier.size(20.dp),
                contentDescription = if (titleBarState.isOnline)
                    stringResource(Res.string.online)
                else
                    stringResource(
                    Res.string.blocked_message
                ),
                tint = if (titleBarState.isOnline)
                    FluentTheme.colors.system.success
                else
                    FluentTheme.colors.system.critical
            )


            Spacer(Modifier.width(8.dp))

            TitleBarButton(
                icon = Icons.Default.Subtract,
                contentDescription = "Minimize",
                hoverBackground = Color(0xFFE5E5E5),
                hoverIconTint = Color.Unspecified,
                onClick = onMinimize
            )

            TitleBarButton(
                icon = if (isMaximized) Icons.Default.SquareMultiple else Icons.Default.Maximize,
                contentDescription = if (isMaximized) "Restore" else "Maximize",
                hoverBackground = Color(0xFFE5E5E5),
                hoverIconTint = Color.Unspecified,
                onClick = onMaximize
            )

            TitleBarButton(
                icon = Icons.Default.Dismiss,
                contentDescription = "Close",
                hoverBackground = Color(0xFFD32F2F),
                hoverIconTint = Color.White,
                onClick = onClose
            )
        }
    }
}

/** Individual Button */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleBarButton(
    icon: ImageVector,
    contentDescription: String,
    hoverBackground: Color,
    hoverIconTint: Color,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    val backgroundColor = if (isHovered) hoverBackground else Color.Transparent
    val iconColor = if (isHovered && hoverIconTint != Color.Unspecified) hoverIconTint else Color.Unspecified

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(backgroundColor)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor
        )
    }
}



