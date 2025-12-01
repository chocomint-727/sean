package hu.bme.ait.sean.data.AlbumResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Album(
    @SerialName("artist")
    var artist: String?,
    @SerialName("image")
    var image: List<Image?>?,
    @SerialName("listeners")
    var listeners: String?,
    @SerialName("mbid")
    var mbid: String?,
    @SerialName("name")
    var name: String?,
    @SerialName("playcount")
    var playcount: String?,
    @SerialName("tags")
    var tags: Tags?,
    @SerialName("tracks")
    var tracks: Tracks?,
    @SerialName("url")
    var url: String?,
    @SerialName("wiki")
    var wiki: Wiki?
)