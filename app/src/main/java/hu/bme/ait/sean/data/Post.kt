package hu.bme.ait.sean.data

import com.google.firebase.Timestamp

data class Post (
    var uid : String = "",
    var author : String = "",
    var postDate : Timestamp = Timestamp.now(),
    var albumID : String = "",
    var rating : Float = 0f,
    var postBody : String = ""
)

data class PostID (
    var id : String = "",
    var post : Post
)