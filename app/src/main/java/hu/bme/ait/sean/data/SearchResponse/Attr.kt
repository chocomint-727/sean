package hu.bme.ait.sean.data.SearchResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attr(
    @SerialName("for")
    var forX: String?
)