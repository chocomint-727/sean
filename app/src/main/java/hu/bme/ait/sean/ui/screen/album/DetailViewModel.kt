package hu.bme.ait.sean.ui.screen.album

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.ait.sean.data.AlbumResponse.AlbumResponse
import hu.bme.ait.sean.data.Post
import hu.bme.ait.sean.data.PostID
import hu.bme.ait.sean.network.LastFMAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.google.ai.client.generativeai.type.content
import com.google.firebase.firestore.Query
import hu.bme.ait.sean.data.StoredAlbumData
import hu.bme.ait.sean.data.StoredAlbumDataID
import hu.bme.ait.sean.data.User
import hu.bme.ait.sean.ui.screen.user.UserUIState


sealed interface AlbumDetailsUIState {
    object Loading : AlbumDetailsUIState
    data class Error(val msg: String) : AlbumDetailsUIState
    data class Success(val res: AlbumResponse) : AlbumDetailsUIState
}

sealed interface PostDetailsUIState {
    object Loading : PostDetailsUIState
    data class Error(val msg: String) : PostDetailsUIState
    data class Success(val posts: List<PostID>) : PostDetailsUIState
}

@HiltViewModel
class DetailViewModel @Inject constructor(val api: LastFMAPI) : ViewModel() {

    var albumDetailsUIState: AlbumDetailsUIState by mutableStateOf(AlbumDetailsUIState.Loading)

    var storedAlbumData : StoredAlbumDataID? by mutableStateOf(null)

    private lateinit var user : User
    private lateinit var auth: FirebaseAuth

    init {
        auth = Firebase.auth
        Firebase.firestore.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                user = it.toObject(User::class.java)!!
            }
            .addOnFailureListener {
                user = User()
            }
    }

    companion object {
        const val REVIEW_COLLECTION = "reviews"
    }

    fun getDetails(album: String, artist: String) {
        albumDetailsUIState = AlbumDetailsUIState.Loading
        try {
            viewModelScope.launch {
                val res = api.getAlbumInfo(album, artist)
                albumDetailsUIState = AlbumDetailsUIState.Success(res)
                val id = (res.album!!.mbid ?: "").ifEmpty { "${res.album!!.name} - ${res.album!!.artist}" }
                Log.d("GET_ALBUM", id)
                getStoredAlbumData(id)
            }
        } catch (e: Exception) {
            albumDetailsUIState = AlbumDetailsUIState.Error(e.message!!)
        }
    }

    fun getStoredAlbumData(id : String)  {
        viewModelScope.launch {
            val doc = Firebase.firestore.collection("albums").document(id)
            doc.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot!!.data != null) {
                        storedAlbumData = StoredAlbumDataID(
                            documentSnapshot.toObject(StoredAlbumData::class.java) ?: StoredAlbumData(),
                            id
                        )
                    } else {
                        Log.d("LOAD_ALBUM_DATA", "document was null")
                        storedAlbumData = StoredAlbumDataID()
                    }
                }
                .addOnFailureListener {
                    Log.d("LOAD_ALBUM_DATA", "failed to fetch with error ${it.localizedMessage}")
                    storedAlbumData = StoredAlbumDataID()
                }
        }
    }

    fun getUserData(id : String) = callbackFlow {

        val doc = Firebase.firestore.collection("users").document(id)
        doc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot!!.data != null){
                    trySend(documentSnapshot.toObject(User::class.java))
                } else {
                    Log.d("LOAD_USER_DATA", "document was null")
                    trySend(User())
                }
            }
            .addOnFailureListener {
                Log.d("LOAD_ALBUM_DATA", "failed to fetch with error ${it.localizedMessage}")
                trySend(User())
            }

        awaitClose {
            return@awaitClose
        }
    }

    fun loadReviews(uid : String) = callbackFlow {
        if (storedAlbumData == null){
            trySend(PostDetailsUIState.Loading)
            close()
            return@callbackFlow
        }

        val success = storedAlbumData!!

        val snapshotListener = Firebase.firestore.collection(REVIEW_COLLECTION)
            .orderBy("postDate", Query.Direction.DESCENDING)
            .whereEqualTo("uid", uid)
            .whereEqualTo(
                "albumID",
                success.id
            )
            .addSnapshotListener { snapshot, e ->
                val res = if (snapshot != null) {
                    var postList = snapshot.toObjects(Post::class.java)
                    val postListWithIDs = mutableListOf<PostID>()

                    postList.forEachIndexed { ix, post ->
                        postListWithIDs.add(
                            PostID(
                                snapshot.documents[ix].id,
                                post
                            )
                        )
                    }

                    PostDetailsUIState.Success(postListWithIDs)
                } else {
                    PostDetailsUIState.Error((e?.localizedMessage ?: "") + " for album id ${success.id}")
                }

                trySend(res)
            }

        awaitClose {
            snapshotListener.remove()
        }

    }

    fun openInMusic(ctx : Context, song : String, album : String, artist : String){
        val query = Uri.encode("$song $album $artist")
        val url = "https://www.youtube.com/results?search_query=$query"
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            ctx.startActivity(intent)
        } catch (e : Exception) {
            Toast.makeText(ctx, e.localizedMessage?:"", Toast.LENGTH_SHORT).show()
        }
    }
}