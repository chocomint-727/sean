package hu.bme.ait.sean.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LoginScreenRoute : NavKey
@Serializable
data class ReviewScreenRoute(
    val albumName : String,
    val artistName : String
) : NavKey

@Serializable
data object HomeScreenRoute : NavKey

@Serializable
data class DetailScreenRoute(
    val albumName : String,
    val artistName : String
) : NavKey