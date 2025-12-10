package hu.bme.ait.sean.ui.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {

    val user = viewModel.getUser()
    val name: String = user?.displayName ?: "TESTUSERNAME"
    val email: String = user?.displayName ?: "TESTEMAIL"
    Column(
    ) {
        Text(name)
    }
}