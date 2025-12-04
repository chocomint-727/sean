package hu.bme.ait.sean.ui.screen.album

import android.util.Log
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


    private lateinit var auth: FirebaseAuth

    init {
        auth = Firebase.auth
    }

    companion object {
        const val REVIEW_COLLECTION = "reviews"
    }

    fun getDetails(album: String, artist: String) {
        albumDetailsUIState = AlbumDetailsUIState.Loading
        try {
            viewModelScope.launch {
                albumDetailsUIState = AlbumDetailsUIState.Success(api.getAlbumInfo(album, artist))

            }
        } catch (e: Exception) {
            albumDetailsUIState = AlbumDetailsUIState.Error(e.message!!)
        }
    }

    fun loadReviews() = callbackFlow {
        val success = albumDetailsUIState as? AlbumDetailsUIState.Success
        if (success == null){
            trySend(PostDetailsUIState.Loading)
            close()
            return@callbackFlow
        }

        Log.d("ALBUM ID", success.res.album?.mbid?: "")

        val snapshotListener = Firebase.firestore.collection(REVIEW_COLLECTION)
            .orderBy("postDate")
            .whereEqualTo(
                "albumID",
                success.res.album?.mbid
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
                    PostDetailsUIState.Error((e?.localizedMessage ?: "") + " for album id ${success.res.album?.mbid}")
                }

                trySend(res)
            }

        awaitClose {
            snapshotListener.remove()
        }

    }

//    fun findUserPost(): Post? {
//        if (postDetailsUIState is PostDetailsUIState.Success) {
//            (postDetailsUIState as PostDetailsUIState.Success).posts.forEach { postWithId ->
//                if (postWithId.post.uid == auth.currentUser?.uid) {
//                    return postWithId.post
//                }
//            }
//        }
//        return null
//    }
}