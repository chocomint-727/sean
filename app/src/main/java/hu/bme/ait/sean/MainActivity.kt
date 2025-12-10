package hu.bme.ait.sean

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.ait.sean.nav.DetailScreenRoute
import hu.bme.ait.sean.nav.HomeScreenRoute
import hu.bme.ait.sean.nav.LoginScreenRoute
import hu.bme.ait.sean.nav.ReviewScreenRoute
import hu.bme.ait.sean.nav.SearchScreenRoute
import hu.bme.ait.sean.nav.UserScreenRoute
import hu.bme.ait.sean.ui.AppBottomBar
import hu.bme.ait.sean.ui.screen.album.DetailScreen
import hu.bme.ait.sean.ui.screen.login.LoginScreen
import hu.bme.ait.sean.ui.screen.review.ReviewScreen
import hu.bme.ait.sean.ui.screen.search.SearchScreen
import hu.bme.ait.sean.ui.screen.user.UserScreen
import hu.bme.ait.sean.ui.theme.SeanTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeanTheme {
                // backstack here so can be passedto navgraph and seen by bottombar
                val backStack = rememberNavBackStack(LoginScreenRoute)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // hide bottom bar on login
                        val current = backStack.lastOrNull()
                        if (current !is LoginScreenRoute) {
                            AppBottomBar(
                                currentRoute = current,
                                onNavigate = { route ->
                                    if (backStack.lastOrNull() != route) { //avoid double stacking which is what i do erryday
                                        backStack.add(route)
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        modifier = Modifier.padding(innerPadding),
                        backStack = backStack
                    )
                }
            }
        }
    }
}



@Composable
fun NavGraph(
    modifier: Modifier,
    backStack: NavBackStack<NavKey> //tspmo bruh
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider  = entryProvider {
            entry<LoginScreenRoute> {
                LoginScreen(
                    onLoginSuccess = {
                        backStack.add(UserScreenRoute) // go to user after login
                    }
                )
            }
            entry<UserScreenRoute> {
                UserScreen { album, artist ->
                    backStack.add(DetailScreenRoute(album, artist))
                }
            }
            entry<HomeScreenRoute> {
                // HomeScreen()
            }
            entry<SearchScreenRoute> {
                SearchScreen { album, artist ->
                    backStack.add(DetailScreenRoute(album, artist))
                }
            }
            entry<DetailScreenRoute> { (albumName, artistName) ->
                DetailScreen(albumName, artistName, modifier = modifier) { albumID, album, artist, img_url ->
                    backStack.add(ReviewScreenRoute(albumID, album, artist, img_url))
                }
            }
            entry<ReviewScreenRoute> { (albumID, album, artist, img_url) ->
                ReviewScreen(albumID, album, artist, img_url) {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}
