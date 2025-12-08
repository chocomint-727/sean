package hu.bme.ait.sean.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.auth.User


sealed interface HomeUiState {
    object Init: HomeUiState
    object Loading: HomeUiState
    object Success: HomeUiState
    data class Error(val errorMessage: String?): HomeUiState
}

class HomeViewModel() : ViewModel() {
    var homeUiState: HomeUiState by mutableStateOf(HomeUiState.Init)

    private lateinit var auth: FirebaseAuth

    init {
        auth = Firebase.auth
    }

    fun getUser() : FirebaseUser? {
        return auth.currentUser
    }

}