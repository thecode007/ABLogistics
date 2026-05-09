package org.safieddine.ablogistics.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.composefluent.FluentTheme
import io.github.composefluent.component.SideNav
import io.github.composefluent.component.SideNavItem
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.filled.PeopleCommunity
import io.github.composefluent.icons.filled.PeopleMoney
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.safieddine.ablogistics.ui.screen.RailScreen
import org.safieddine.ablogistics.ui.screen.customers.CustomerScreen
import org.safieddine.ablogistics.ui.screen.DistributorScreen
import org.safieddine.ablogistics.ui.screen.SettingsScreen
import org.safieddine.ablogistics.ui.screen.fleet.FleetScreen
import org.safieddine.ablogistics.ui.screen.receipts.DirectLoadEntryScreen
import ablogistics.composeapp.generated.resources.*
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.component.rememberIndicatorState
import io.github.composefluent.icons.filled.VehicleTruckProfile
import io.github.composefluent.icons.regular.*

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
fun TeamsNavigationRail(
) {
    val navigationItems = listOf(
        NavigationItem(
            "Dashboard",
            Icons.Regular.Apps,
            AppScreen.Dashboard
        ),
        NavigationItem(
            "Fleet",
            Icons.Filled.VehicleTruckProfile,
            AppScreen.Fleet
        ),
        NavigationItem(
            "Direct Load",
            Icons.Regular.Add,
            AppScreen.DirectLoad
        ),
        NavigationItem(
            stringResource(Res.string.nav_customers),
            Icons.Filled.PeopleCommunity,
            AppScreen.Customers
        ),
        NavigationItem(
            "Suppliers",
            Icons.Filled.PeopleMoney, 
            AppScreen.Distributors
        ),
        NavigationItem(
            "Settings",
            Icons.Regular.Settings,
            AppScreen.Settings
        )
    )


    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf(navigationItems[0].title) }
    val indicatorState = rememberIndicatorState()

    Row(Modifier.fillMaxSize()) {
        SideNav(
            indicatorState = indicatorState,
            expanded = expanded,
            onExpandStateChange = { isExpanded -> expanded = isExpanded },
            modifier = Modifier.background(FluentTheme.colors.background.mica.base).fillMaxHeight(),
            header = {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ab_logo),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    if (expanded) {
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "AB LOGISTICS LTD",
                            style = FluentTheme.typography.bodyStrong.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            },
            content = {
                navigationItems.forEachIndexed { index, navitem ->
                    SideNavItem(
                        selected = selectedIndex == index,
                        onClick = { 
                            selectedIndex = index
                            title = navitem.title
                        },
                        content = {
                            Text(navitem.title)
                        },
                        icon = {
                            Icon(imageVector = navitem.icon, "")
                        }
                    )
                }
            }
        )


        RailScreen(title = title,navigationItems[selectedIndex].icon) {
            when (navigationItems[selectedIndex].screen) {
                is AppScreen.Dashboard -> {
                    DashboardScreen()
                }
                is AppScreen.Customers -> {
                    CustomerScreen()
                }
                is AppScreen.Distributors -> {
                    DistributorScreen()
                }
                is AppScreen.Settings -> {
                    SettingsScreen()
                }
                is AppScreen.Fleet -> {
                    FleetScreen()
                }
                is AppScreen.DirectLoad -> {
                    DirectLoadEntryScreen()
                }
                else -> {}
            }
        }
        }
}
