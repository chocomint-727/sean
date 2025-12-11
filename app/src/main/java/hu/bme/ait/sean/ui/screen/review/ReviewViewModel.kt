package hu.bme.ait.sean.ui.screen.review

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
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import hu.bme.ait.sean.data.Post
import hu.bme.ait.sean.data.StoredAlbumData
import hu.bme.ait.sean.data.User
import hu.bme.ait.sean.ui.screen.album.DetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.text.ifEmpty


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
        Firebase.firestore.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                user = it.toObject(User::class.java)!!
            }
            .addOnFailureListener {
                user = User()
            }
    }

    private val genModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = hu.bme.ait.sean.BuildConfig.GEMINI_API_KEY,

        generationConfig = generationConfig {
            temperature = 0.5f
        },

        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE)
        )
    )

    private val _textGenerationResult = MutableStateFlow<String?>(null)
    val textGenerationResult = _textGenerationResult.asStateFlow()

    fun generateContent(prompt: String = "Tell me the current time") {
        _textGenerationResult.value = "Generating..."
        viewModelScope.launch {
            try {
                val inputContent = content {
                    text (prompt)
                }
                var fullResponse = ""
                val aiResult = genModel.generateContentStream(inputContent).collect {
                        chunk ->
                    fullResponse += chunk.text
                    _textGenerationResult.value = fullResponse
                }
            } catch (e: Exception) {
                _textGenerationResult.value = "Error: ${e.message}"
            }
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
                img_url = img_url,
                reviewUids = listOf(auth.uid!!)
            )

            val db = Firebase.firestore

            val docRef = db.collection("albums").document(id)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document.data == null) {
                        createAlbumRecord(id, albumToUpload, postToUpload)
                    } else {
                        Log.d("CREATE_REVIEW", "Found document! ignoring...")
                        Log.d("CREATE_REVIEW", "DocumentSnapshot data: ${document.data}")
                        docRef.update("reviewUids",
                            FieldValue.arrayUnion(auth.uid!!)
                            )
                        postReview(postToUpload)
                    }
                }
                .addOnFailureListener {
                    writeReviewUIState = WriteReviewUIState.Error(it.message!!)
                }
        }
    }

    fun createAlbumRecord(id : String, albumToUpload: StoredAlbumData, postToUpload : Post){
        val db = Firebase.firestore
        Log.d("CREATE_REVIEW", "Didn't find document, creating...")
        db.collection("albums")
            .document(id)
            .set(albumToUpload)
            .addOnSuccessListener {
                postReview(postToUpload)
            }
    }

    fun postReview(postToUpload : Post) {
        val db = Firebase.firestore
        db.collection(DetailViewModel.REVIEW_COLLECTION)
            .add(postToUpload)
            .addOnSuccessListener {
                writeReviewUIState = WriteReviewUIState.Success
            }
            .addOnFailureListener {
                writeReviewUIState = WriteReviewUIState.Error(it.message!!)
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