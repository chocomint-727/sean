package hu.bme.ait.sean.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import hu.bme.ait.sean.nav.DetailScreenRoute
import hu.bme.ait.sean.nav.SearchScreenRoute
import hu.bme.ait.sean.nav.UserScreenRoute
import hu.bme.ait.sean.ui.theme.Background1
import hu.bme.ait.sean.ui.theme.Background2
import hu.bme.ait.sean.ui.theme.BottomPillIndicatorColor
import hu.bme.ait.sean.ui.theme.Primary

data class BottomDestination(
    val route: NavKey,
    val label: String,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(
        route = SearchScreenRoute,
        label = "Search",
        icon = Icons.Filled.Search
    ),
    BottomDestination(
        route = UserScreenRoute,
        label = "Profile",
        icon = Icons.Filled.Person
    )
)

@Composable
fun AppBottomBar(
    currentRoute: NavKey?,
    onNavigate: (NavKey) -> Unit
) {
    NavigationBar(
        containerColor = Background1
    ) {
        bottomDestinations.forEach { dest ->
            NavigationBarItem(
                selected = currentRoute == dest.route,
                onClick = { onNavigate(dest.route) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = BottomPillIndicatorColor // The color of the active 'pill' indicator
                )
            )
        }
    }
}
