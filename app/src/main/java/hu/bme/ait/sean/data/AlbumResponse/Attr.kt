package hu.bme.ait.sean.data.AlbumResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attr(
    @SerialName("rank")
    var rank: Int?
)