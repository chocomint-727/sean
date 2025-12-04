package hu.bme.ait.sean.data.SearchResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpensearchQuery(
    @SerialName("role")
    var role: String?,
    @SerialName("searchTerms")
    var searchTerms: String?,
    @SerialName("startPage")
    var startPage: String?,
    @SerialName("#text")
    var text: String?
)