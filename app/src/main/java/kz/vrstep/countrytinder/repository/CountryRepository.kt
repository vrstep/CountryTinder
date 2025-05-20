package kz.vrstep.countrytinder.repository

import kotlinx.coroutines.flow.Flow
import kz.vrstep.countrytinder.common.Resource
import kz.vrstep.countrytinder.domain.model.Country


interface CountryRepository {
    // Fetches a list of countries, potentially combining with Unsplash images
    fun getDiscoverCountries(fetchNew: Boolean = false): Flow<Resource<List<Country>>>

    suspend fun getCountryImage(countryName: String): Resource<String?> // Returns image URL

    suspend fun addFavoriteCountry(country: Country)
    suspend fun removeFavoriteCountry(countryName: String)
    fun getFavoriteCountries(): Flow<List<Country>>
    suspend fun isCountryFavorite(countryName: String): Boolean
}