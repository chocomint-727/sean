package hu.bme.ait.sean.data.AlbumResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Wiki(
    @SerialName("content")
    var content: String?,
    @SerialName("published")
    var published: String?,
    @SerialName("summary")
    var summary: String?
)