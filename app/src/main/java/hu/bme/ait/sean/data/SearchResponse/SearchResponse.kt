package hu.bme.ait.sean.data.SearchResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    @SerialName("results")
    var results: Results?
)