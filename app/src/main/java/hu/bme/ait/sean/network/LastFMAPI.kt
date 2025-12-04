package hu.bme.ait.sean.network

import hu.bme.ait.sean.BuildConfig
import hu.bme.ait.sean.data.AlbumResponse.AlbumResponse
import hu.bme.ait.sean.data.SearchResponse.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

sealed interface LastFMAPI {
    @GET("?method=album.getinfo&format=json&api_key=${BuildConfig.MUSIC_API_KEY}")
    suspend fun getAlbumInfo(
        @Query("album") album : String,
        @Query("artist") artist : String,
    ) : AlbumResponse

    @GET("?method=album.search&format=json&api_key=${BuildConfig.MUSIC_API_KEY}")
    suspend fun searchAlbums(
        @Query("album") query : String
    ) : SearchResponse
}