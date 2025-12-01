package hu.bme.ait.sean.data.AlbumResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Streamable(
    @SerialName("fulltrack")
    var fulltrack: String?,
    @SerialName("#text")
    var text: String?
)