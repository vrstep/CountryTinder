package kz.vrstep.countrytinder.data.remote.api

import kz.vrstep.countrytinder.data.remote.dto.CountryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RestCountriesApi {
    // Fetches all countries, or specific fields for all countries
    @GET("all")
    suspend fun getAllCountries(
        @Query("fields") fields: String = "name,flags,population,area,languages,region,subregion,capital,currencies"
    ): Response<List<CountryDto>>
    // You might want to fetch a smaller subset initially or implement pagination if the API supports it well.
    // For simplicity, "all" is used here.
}