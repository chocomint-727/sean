package hu.bme.ait.sean.data.AlbumResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Track(
    @SerialName("artist")
    var artist: Artist?,
    @SerialName("@attr")
    var attr: Attr?,
    @SerialName("duration")
    var duration: Int?,
    @SerialName("name")
    var name: String?,
    @SerialName("streamable")
    var streamable: Streamable?,
    @SerialName("url")
    var url: String?
)