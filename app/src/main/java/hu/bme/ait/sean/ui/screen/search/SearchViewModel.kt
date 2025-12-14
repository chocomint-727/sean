package hu.bme.ait.sean.ui.screen.search

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.ait.sean.data.SearchResponse.SearchAlbum
import hu.bme.ait.sean.data.SearchResponse.SearchResponse
import hu.bme.ait.sean.data.StoredAlbumData
import hu.bme.ait.sean.network.LastFMAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUIState {
    object Init : SearchUIState
    object Loading : SearchUIState
    data class Success(val res: SearchResponse) : SearchUIState
    data class Error(val msg: String) : SearchUIState
}

@HiltViewModel
class SearchViewModel @Inject constructor(val api: LastFMAPI) : ViewModel() {
    var searchUIState: SearchUIState by mutableStateOf(SearchUIState.Init)

    var randomAlbum by mutableStateOf(StoredAlbumData())

    fun getRandomAlbum() {
        Firebase.firestore.collection("albums").get()
            .addOnSuccessListener { res ->
                val items = res.documents.map { doc -> doc.toObject(StoredAlbumData::class.java) }
                if (items.isNotEmpty()) {
                    randomAlbum = items.random() ?: StoredAlbumData()
                }
            }
    }

    fun search(query: String) {
        viewModelScope.launch {
            try {
                searchUIState = SearchUIState.Success(api.searchAlbums(query))
            } catch (e: Exception) {
                searchUIState = SearchUIState.Error(e.message!!)
            }
        }
    }

    fun uploadToDB(album: SearchAlbum, toggleSearched : () -> Unit) {
        viewModelScope.launch {

            val id = album.mbid!!.ifEmpty { "${album.name!!} - ${album.artist!!}" }

            val docRef = Firebase.firestore.collection("albums").document(id)
            docRef.get()
                .addOnSuccessListener { document ->
                    try {

                        val imgUrl = album.image?.lastOrNull()?.text

                        require(!album.name.isNullOrEmpty()) { "Album name missing." }
                        require(!album.artist.isNullOrEmpty()) { "Album artist missing." }
                        require(!imgUrl.isNullOrEmpty()) { "Image URL missing." }

                        val albumToStore = StoredAlbumData(
                            name = album.name!!,
                            artist = album.artist!!,
                            img_url = imgUrl,
                            reviewUids = emptyList()
                        )

                        if (document.data == null) {
                            Log.d("CREATE_REVIEW", "Didn't find document, creating...")
                            Firebase.firestore.collection("albums")
                                .document(id)
                                .set(
                                    albumToStore
                                )
                        }

                        toggleSearched()
                    } catch (e: Exception) {
                        Log.d("FAILED_TO_CREATE", e.localizedMessage ?: "")
                    }
                }
        }
    }
}