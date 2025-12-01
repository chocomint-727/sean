package hu.bme.ait.sean.network

import hu.bme.ait.sean.BuildConfig
import hu.bme.ait.sean.data.AlbumResponse.AlbumResponse
import retrofit2.http.GET
import retrofit2.http.Query

sealed interface LastFMAPI {
    @GET("")
    suspend fun getAlbumInfo(
        @Query("artist") artist : String,
        @Query("album") album : String,
        @Query("format") format : String = "json",
        @Query("api_key") apiKey : String = BuildConfig.MUSIC_API_KEY,
        @Query("method") methodName : String = "album.getinfo",
    ) : AlbumResponse
}