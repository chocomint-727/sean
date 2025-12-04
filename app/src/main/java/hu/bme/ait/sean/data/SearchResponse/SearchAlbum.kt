package hu.bme.ait.sean.data.SearchResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchAlbum(
    @SerialName("artist")
    var artist: String?,
    @SerialName("image")
    var image: List<Image?>?,
    @SerialName("mbid")
    var mbid: String?,
    @SerialName("name")
    var name: String?,
    @SerialName("streamable")
    var streamable: String?,
    @SerialName("url")
    var url: String?
)