package hu.bme.ait.sean.data.SearchResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Results(
    @SerialName("albummatches")
    var albummatches: Albummatches?,
    @SerialName("@attr")
    var attr: Attr?,
    @SerialName("opensearch:itemsPerPage")
    var opensearchItemsPerPage: String?,
    @SerialName("opensearch:Query")
    var opensearchQuery: OpensearchQuery?,
    @SerialName("opensearch:startIndex")
    var opensearchStartIndex: String?,
    @SerialName("opensearch:totalResults")
    var opensearchTotalResults: String?
)