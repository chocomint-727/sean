package hu.bme.ait.sean.data.AlbumResponse


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tracks(
    @SerialName("track")
    @Serializable(SafeListToTrackSerializer::class)
    var track: List<Track?>?
)