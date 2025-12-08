package hu.bme.ait.sean.data.SearchResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Image(
    @SerialName("size")
    var size: String?,
    @SerialName("#text")
    var text: String?
)