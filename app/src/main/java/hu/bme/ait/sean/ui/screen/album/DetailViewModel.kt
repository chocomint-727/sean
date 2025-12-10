package hu.bme.ait.sean.ui.screen.album

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
import hu.bme.ait.sean.data.StoredAlbumData
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

    private lateinit var user : User
    private lateinit var auth: FirebaseAuth

    init {
        auth = Firebase.auth
        Firebase.firestore.collection("users").document(auth.currentUser!!.email!!).get()
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
                albumDetailsUIState = AlbumDetailsUIState.Success(api.getAlbumInfo(album, artist))

            }
        } catch (e: Exception) {
            albumDetailsUIState = AlbumDetailsUIState.Error(e.message!!)
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

    fun loadReviews() = callbackFlow {
        val success = albumDetailsUIState as? AlbumDetailsUIState.Success
        if (success == null){
            trySend(PostDetailsUIState.Loading)
            close()
            return@callbackFlow
        }

        val snapshotListener = Firebase.firestore.collection(REVIEW_COLLECTION)
            .orderBy("postDate")
            .whereEqualTo(
                "albumID",
                success.res.album?.mbid!!.ifEmpty() { "${success.res.album?.name} - ${success.res.album?.artist}" }
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

    fun openInMusic(ctx : Context, song : String, album : String, artist : String){
        val query = Uri.encode("$song $album $artist")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "content://media/external/audio/media/".toUri() // general media content URI
            putExtra("query", query)
            setPackage("com.google.android.music")
        }
        if (intent.resolveActivity(ctx.packageManager) != null){
            ctx.startActivity(intent)
        } else {
            Toast.makeText(ctx, "No music app could be found.", Toast.LENGTH_SHORT).show()
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