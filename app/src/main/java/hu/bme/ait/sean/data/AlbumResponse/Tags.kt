package hu.bme.ait.sean.data.AlbumResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tags(
    @SerialName("tag")
    @Serializable(SafeListToObjectSerializer::class)
    var tag: List<Tag?>? = emptyList()
)