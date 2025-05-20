package kz.vrstep.countrytinder.data.remote.api

import kz.vrstep.countrytinder.common.Constants
import kz.vrstep.countrytinder.data.remote.dto.UnsplashSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface UnsplashApi {
//    @Headers("Authorization: Client-ID ${Constants.UNSPLASH_ACCESS_KEY}")
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 1,
        @Query("orientation") orientation: String = "portrait" // Tinder-like cards are often portrait
    ): Response<UnsplashSearchResponse>
}
