package org.safieddine.ablogistics.ui

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import ablogistics.composeapp.generated.resources.Res
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.component.NavigationDisplayMode
import io.github.composefluent.component.NavigationView
import io.github.composefluent.component.SideNavItem
import io.github.composefluent.component.menuItem
import io.github.composefluent.component.rememberNavigationState
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.PeopleCommunity
import io.github.composefluent.icons.filled.PeopleMoney
import io.github.composefluent.icons.filled.Receipt
import io.github.composefluent.icons.filled.VehicleTruckProfile
import io.github.composefluent.icons.regular.*
import io.github.composefluent.icons.filled.Home
import androidx.compose.ui.unit.dp

import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.ui.screen.SettingsScreen
import org.safieddine.ablogistics.ui.screen.customers.CustomerScreen
import org.safieddine.ablogistics.ui.screen.fleet.FleetScreen
import org.safieddine.ablogistics.ui.screen.receipts.DirectLoadEntryScreen
import org.safieddine.ablogistics.ui.screen.receipts.ReceiptsAdminScreen
import org.safieddine.ablogistics.ui.screen.receipts.ReceiptsCustomerScreen
import org.safieddine.ablogistics.ui.screen.receipts.BrvPaymentsScreen
import ablogistics.composeapp.generated.resources.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import org.safieddine.ablogistics.ui.theme.BrandingWhite
import org.safieddine.ablogistics.ui.theme.TeamsBackground
import org.safieddine.ablogistics.ui.screen.RailScreen
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import io.github.composefluent.icons.filled.BuildingFactory
import io.github.composefluent.icons.filled.DocumentTableTruck


data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val screen: AppScreen
)

sealed class AppScreen(val title: String) {
    object Dashboard : AppScreen("Dashboard")
    object Blocked : AppScreen("Blocked")
    object Admin : AppScreen("Admin")
    object Customers : AppScreen("Customers")
    object ReceiptsWarehouse : AppScreen("Warehouse Receipts")
    object WarehouseSummary : AppScreen("Warehouse Summary")
    object NoWarehouseAssigned : AppScreen("NoWarehouseAssigned")
    object ReceiptsCustomer : AppScreen("Customer Receipts")
    object Splash : AppScreen("Splash")
    object Login : AppScreen("Login")
    object Settings : AppScreen("Settings")
    object Fleet : AppScreen("Fleet Tracking")
    object DirectLoad : AppScreen("Direct Load")
    object BrvPayments : AppScreen("BRV Payments")
}

@OptIn(ExperimentalFluentApi::class)
@Composable
fun TeamsNavigationRail() {
    var selectedScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
    var financialsExpanded by remember { mutableStateOf(false) }
    val navigationState = rememberNavigationState()

    NavigationView(
        modifier = Modifier.background(TeamsBackground),
        displayMode = NavigationDisplayMode.Left,
        state = navigationState,
        border = null,
        menuItems = {
 
             // Dashboard
            menuItem(
                selected = selectedScreen is AppScreen.Dashboard,
                onClick = { selectedScreen = AppScreen.Dashboard },
                text = { Text("Dashboard") },
                icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Dashboard") }
            )
 
            // Fleet
            menuItem(
                selected = selectedScreen is AppScreen.Fleet,
                onClick = { selectedScreen = AppScreen.Fleet },
                text = { Text("Fleet") },
                icon = { Icon(imageVector = Icons.Filled.VehicleTruckProfile, contentDescription = "Fleet") }
            )

            // Direct Load
            menuItem(
                selected = selectedScreen is AppScreen.DirectLoad,
                onClick = { selectedScreen = AppScreen.DirectLoad },
                text = { Text("Direct Load") },
                icon = { Icon(imageVector = Icons.Filled.DocumentTableTruck, contentDescription = "Direct Load") }
            )

            // Customers
            menuItem(
                selected = selectedScreen is AppScreen.Customers,
                onClick = { selectedScreen = AppScreen.Customers },
                text = { Text(stringResource(Res.string.nav_customers)) },
                icon = { Icon(imageVector = Icons.Filled.PeopleCommunity, contentDescription = "Customers") }
            )



            // Financials — expandable group with nested SideNavItems
            menuItem(
                selected = selectedScreen is AppScreen.ReceiptsCustomer || selectedScreen is AppScreen.Admin,
                onClick = { financialsExpanded = !financialsExpanded },
                text = { Text("Financials") },
                icon = { Icon(imageVector = Icons.Filled.PeopleMoney, contentDescription = "Financials") },
                expandItems = financialsExpanded,
                onExpandItemsChanged = { financialsExpanded = it },
                items = {
                    // Customer Receipts
                    SideNavItem(
                        selected = selectedScreen is AppScreen.ReceiptsCustomer,
                        onClick = { selectedScreen = AppScreen.ReceiptsCustomer },
                        icon = { Icon(imageVector = Icons.Regular.Receipt, contentDescription = "Customer Receipts") },
                        content = { Text("Customer Receipts") }
                    )
                    // Admin
                    SideNavItem(
                        selected = selectedScreen is AppScreen.Admin,
                        onClick = { selectedScreen = AppScreen.Admin },
                        icon = { Icon(imageVector = Icons.Filled.Receipt, contentDescription = "Admin") },
                        content = { Text("Admin") }
                    )
                    // BRV Payments
                    SideNavItem(
                        selected = selectedScreen is AppScreen.BrvPayments,
                        onClick = { selectedScreen = AppScreen.BrvPayments },
                        icon = { Icon(imageVector = Icons.Filled.PeopleMoney, contentDescription = "BRV Payments") },
                        content = { Text("BRV Payments Todo") }
                    )
                }
            )
        },
        footerItems = {
            // Settings in the footer
            menuItem(
                selected = selectedScreen is AppScreen.Settings,
                onClick = { selectedScreen = AppScreen.Settings },
                text = { Text("Settings") },
                icon = { Icon(imageVector = Icons.Regular.Settings, contentDescription = "Settings") }
            )
        }
    ) {
        // Content pane - Wrapped in a styled container for the Teams 2.0 layered look
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandingWhite, shape = RoundedCornerShape(topStart = 8.dp))
        ) {
            when (selectedScreen) {
                is AppScreen.Dashboard -> RailScreen("Dashboard", Icons.Regular.Home) { DashboardScreen() }
                is AppScreen.Customers -> RailScreen("Customers", Icons.Filled.PeopleCommunity) { CustomerScreen() }
                is AppScreen.Settings -> RailScreen("Settings", Icons.Regular.Settings) { SettingsScreen() }
                is AppScreen.Fleet -> RailScreen("Fleet Tracking", Icons.Filled.VehicleTruckProfile) { FleetScreen() }
                is AppScreen.DirectLoad -> RailScreen("Direct Load", Icons.Regular.DocumentTableTruck) { DirectLoadEntryScreen() }
                is AppScreen.ReceiptsCustomer -> RailScreen("Customer Receipts", Icons.Regular.Receipt) { ReceiptsCustomerScreen() }
                is AppScreen.Admin -> RailScreen("Admin", Icons.Filled.Receipt) { ReceiptsAdminScreen() }
                is AppScreen.BrvPayments -> BrvPaymentsScreen()
                else -> {}
            }
        }
    }
}
