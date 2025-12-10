package hu.bme.ait.sean.ui.screen.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.ait.sean.data.SearchResponse.SearchResponse
import hu.bme.ait.sean.network.LastFMAPI
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUIState {
    object Init : SearchUIState
    object Loading : SearchUIState
    data class Success(val res : SearchResponse) : SearchUIState
    data class Error(val msg : String)  : SearchUIState
}

@HiltViewModel
class SearchViewModel @Inject constructor(val api : LastFMAPI) : ViewModel(){
    var searchUIState : SearchUIState by mutableStateOf(SearchUIState.Init)

    fun search(query : String){
        viewModelScope.launch {
            try {
                searchUIState = SearchUIState.Success(api.searchAlbums(query))
            } catch (e : Exception) {
                searchUIState = SearchUIState.Error(e.message!!)
            }
        }
    }
}