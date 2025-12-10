package hu.bme.ait.sean.ui.screen.otheruser

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import hu.bme.ait.sean.data.Post
import hu.bme.ait.sean.data.PostID
import hu.bme.ait.sean.data.StoredAlbumData
import hu.bme.ait.sean.data.User
import hu.bme.ait.sean.ui.screen.album.PostDetailsUIState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

sealed interface OtherUserUIState {
    object Init : OtherUserUIState
    object Loading : OtherUserUIState
    data class Success (val user : User) : OtherUserUIState
    data class Error (val msg : String) : OtherUserUIState
}

class OtherUserViewModel : ViewModel() {
    var otherUserUIState : OtherUserUIState by mutableStateOf(OtherUserUIState.Init)
    private lateinit var auth : FirebaseAuth

    private val usersCollection = Firebase.firestore.collection("users")

    init {
        otherUserUIState = OtherUserUIState.Loading
        auth = Firebase.auth
        Firebase.firestore.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                otherUserUIState = OtherUserUIState.Success(user = it.toObject(User::class.java)!!)
            }
            .addOnFailureListener {
                otherUserUIState = OtherUserUIState.Error(msg = it.localizedMessage!!)
            }
    }

    fun loadReviewsForUser() = callbackFlow {
        val success = otherUserUIState as? OtherUserUIState.Success
        if (success == null){
            trySend(PostDetailsUIState.Loading)
            close()
            return@callbackFlow
        }

        val snapshotListener = Firebase.firestore.collection("reviews")
            .whereEqualTo("uid", auth.uid!!)
            .orderBy("postDate")
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
                    PostDetailsUIState.Error((e?.localizedMessage ?: "") + " for user ${success.user}")
                }
                trySend(res)
            }
        awaitClose {
            snapshotListener.remove()
        }
    }

    fun getAlbumData(id : String) = callbackFlow {
        val success = otherUserUIState as? OtherUserUIState.Success
        if (success == null){
            trySend(StoredAlbumData())
            close()
            return@callbackFlow
        }

        val doc = Firebase.firestore.collection("albums").document(id)
        doc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot!!.data != null){
                    trySend(documentSnapshot.toObject(StoredAlbumData::class.java))
                } else {
                    Log.d("LOAD_ALBUM_DATA", "document was null")
                    trySend(StoredAlbumData())
                }
            }
            .addOnFailureListener {
                Log.d("LOAD_ALBUM_DATA", "failed to fetch with error ${it.localizedMessage}")
                trySend(StoredAlbumData())
            }

        awaitClose {
            return@awaitClose
        }
    }

    fun updateUsername(newName: String){
        val currentUserState = otherUserUIState as? OtherUserUIState.Success ?: return
        val firebaseUser = auth.currentUser ?: return
        val uid = firebaseUser.uid

        otherUserUIState = OtherUserUIState.Loading

        usersCollection.document(uid)
            .update("name", newName)
            .addOnSuccessListener {
                val updatedUser = currentUserState.user.copy(name = newName)
                otherUserUIState = OtherUserUIState.Success(updatedUser)
            }
            .addOnFailureListener { e ->
                Log.e("USERNAME_CHANGE","Username update failed", e)
                otherUserUIState = OtherUserUIState.Error(e.localizedMessage ?: "Unknown error")
            }
    }

}