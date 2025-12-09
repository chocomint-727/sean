package hu.bme.ait.sean.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import hu.bme.ait.sean.nav.HomeScreenRoute
import hu.bme.ait.sean.nav.SearchScreenRoute
import hu.bme.ait.sean.nav.UserScreenRoute

data class BottomDestination(
    val route: NavKey,
    val label: String,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    /*BottomDestination(
        route = HomeScreenRoute,
        label = "Home",
        icon = Icons.Filled.Home
    ),*/
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
    NavigationBar {
        bottomDestinations.forEach { dest ->
            NavigationBarItem(
                selected = currentRoute == dest.route,
                onClick = { onNavigate(dest.route) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label) }
            )
        }
    }
}
