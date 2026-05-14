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
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.ui.screen.DistributorScreen
import org.safieddine.ablogistics.ui.screen.SettingsScreen
import org.safieddine.ablogistics.ui.screen.customers.CustomerScreen
import org.safieddine.ablogistics.ui.screen.fleet.FleetScreen
import org.safieddine.ablogistics.ui.screen.receipts.DirectLoadEntryScreen
import org.safieddine.ablogistics.ui.screen.receipts.ReceiptsAdminScreen
import org.safieddine.ablogistics.ui.screen.receipts.ReceiptsCustomerScreen
import ablogistics.composeapp.generated.resources.*

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
    object Distributors : AppScreen("Suppliers")
    object Settings : AppScreen("Settings")
    object Fleet : AppScreen("Fleet Tracking")
    object DirectLoad : AppScreen("Direct Load")
}

@OptIn(ExperimentalFluentApi::class)
@Composable
fun TeamsNavigationRail() {
    var selectedScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
    var financialsExpanded by remember { mutableStateOf(false) }
    val navigationState = rememberNavigationState()

    NavigationView(
        modifier = Modifier,
        displayMode = NavigationDisplayMode.Left,
        state = navigationState,
        color = Color.Transparent,
        border = null,
        menuItems = {
  
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
                icon = { Icon(imageVector = Icons.Regular.DocumentTableTruck, contentDescription = "Direct Load") }
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
        // Content pane
        when (selectedScreen) {
            is AppScreen.Dashboard -> DashboardScreen()
            is AppScreen.Customers -> CustomerScreen()
            is AppScreen.Distributors -> DistributorScreen()
            is AppScreen.Settings -> SettingsScreen()
            is AppScreen.Fleet -> FleetScreen()
            is AppScreen.DirectLoad -> DirectLoadEntryScreen()
            is AppScreen.ReceiptsCustomer -> ReceiptsCustomerScreen()
            is AppScreen.Admin -> ReceiptsAdminScreen()
            else -> {}
        }
    }
}
