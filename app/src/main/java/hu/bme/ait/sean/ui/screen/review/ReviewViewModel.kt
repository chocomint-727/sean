package hu.bme.ait.sean.ui.screen.review

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.type.DateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.ait.sean.data.Post
import hu.bme.ait.sean.data.StoredAlbumData
import hu.bme.ait.sean.data.User
import hu.bme.ait.sean.ui.screen.album.DetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



sealed interface WriteReviewUIState {
    object Idle : WriteReviewUIState
    object Loading : WriteReviewUIState
    data object Success : WriteReviewUIState
    data class Error(val msg : String) : WriteReviewUIState
}

class ReviewViewModel : ViewModel() {

    var writeReviewUIState : WriteReviewUIState by mutableStateOf(WriteReviewUIState.Idle)

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

    fun pushReview(
        albumID : String,
        album : String,
        artist : String,
        img_url : String,
        content : String,
        rating : Float
    ) {
        writeReviewUIState = WriteReviewUIState.Loading
        val id = albumID.ifEmpty { "$album - $artist" }
        viewModelScope.launch {
            val postToUpload = Post(
                uid = auth.uid!!,
                author = user.name,
                postDate = Timestamp.now(),
                albumID = id,
                rating = rating,
                postBody = content
            )

            val albumToUpload  = StoredAlbumData(
                name = album,
                artist = artist,
                img_url = img_url
            )

            val db = Firebase.firestore

            val docRef = db.collection("albums").document(id)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.data == null) {
                        Log.d("CREATE_REVIEW", "Didn't find document, creating...")
                        db.collection("albums")
                            .document(id)
                            .set(albumToUpload)
                            .addOnSuccessListener {
                                db.collection(DetailViewModel.REVIEW_COLLECTION)
                                    .add(postToUpload)
                                    .addOnSuccessListener {
                                        writeReviewUIState = WriteReviewUIState.Success
                                        Log.d("POST_REVIEW", "posted review to ${albumID.ifEmpty { "$album - $artist" }}")
                                    }
                                    .addOnFailureListener {
                                        writeReviewUIState = WriteReviewUIState.Error(it.message!!)
                                    }
                            }
                    } else {
                        Log.d("CREATE_REVIEW", "Found document! ignoring...")
                        Log.d("CREATE_REVIEW", "DocumentSnapshot data: ${document.data}")
                        db.collection(DetailViewModel.REVIEW_COLLECTION)
                            .add(postToUpload)
                            .addOnSuccessListener {
                                writeReviewUIState = WriteReviewUIState.Success
                                Log.d("POST_REVIEW", "posted review to ${albumID.ifEmpty { "$album - $artist" }}")
                            }
                            .addOnFailureListener {
                                writeReviewUIState = WriteReviewUIState.Error(it.message!!)
                            }
                    }
                }
                .addOnFailureListener {
                    writeReviewUIState = WriteReviewUIState.Error(it.message!!)
                }
        }
    }

    fun runAfterDelay(
        delay : Long,
        f : () -> Unit
    ){
        viewModelScope.launch {
            delay(delay)
            f()
        }
    }
}