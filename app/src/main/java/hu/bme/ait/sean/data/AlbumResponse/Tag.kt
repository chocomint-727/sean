package hu.bme.ait.sean.data.AlbumResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    @SerialName("name")
    var name: String?,
    @SerialName("url")
    var url: String?
)