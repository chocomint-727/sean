package hu.bme.ait.sean

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import hu.bme.ait.sean.nav.DetailScreenRoute
import hu.bme.ait.sean.nav.HomeScreenRoute
import hu.bme.ait.sean.nav.LoginScreenRoute
import hu.bme.ait.sean.nav.ReviewScreenRoute
import hu.bme.ait.sean.ui.theme.SeanTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {

                }
            }
        }
    }
}


@Composable
fun NavGraph(modifier: Modifier) {
    val backStack = rememberNavBackStack(LoginScreenRoute)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {backStack.removeLastOrNull()},
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider  = entryProvider {
            entry<LoginScreenRoute>{
//                LoginScreen(
//                    onLoginSuccess =
//                )
            }
            entry<HomeScreenRoute> {
                //HomeScreen()
            }
            entry<DetailScreenRoute> { (albumName, artistName) ->
                //DetailScreen()
            }
            entry<ReviewScreenRoute> { (albumName, artistName) ->
                //ReviewScreen()
            }
        }
    )
}