package kz.vrstep.countrytinder.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kz.vrstep.countrytinder.common.Resource
import kz.vrstep.countrytinder.data.local.dao.FavoriteCountryDao
import kz.vrstep.countrytinder.data.local.entity.FavoriteCountryEntity
import kz.vrstep.countrytinder.data.remote.api.RestCountriesApi
import kz.vrstep.countrytinder.data.remote.api.UnsplashApi
import kz.vrstep.countrytinder.domain.model.Country
import retrofit2.HttpException
import java.io.IOException

class CountryRepositoryImpl(
    private val restCountriesApi: RestCountriesApi,
    private val unsplashApi: UnsplashApi,
    private val favoriteCountryDao: FavoriteCountryDao
) : CountryRepository {

    private val TAG = "CountryRepositoryImpl"
    private var cachedCountriesBasicData: List<Country>? = null

    override fun getDiscoverCountries(fetchNew: Boolean): Flow<Resource<List<Country>>> = flow {
        Log.d(TAG, "getDiscoverCountries called. FetchNew: $fetchNew")
        emit(Resource.Loading())

        if (!fetchNew && cachedCountriesBasicData != null && cachedCountriesBasicData!!.isNotEmpty()) {
            Log.d(TAG, "Returning cached basic country data. Count: ${cachedCountriesBasicData!!.size}")
            emit(Resource.Success(cachedCountriesBasicData!!))
            return@flow
        }

        try {
            val response = restCountriesApi.getAllCountries()
            Log.d(TAG, "RestCountries API response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val countryDtos = response.body()!!
                Log.d(TAG, "Fetched ${countryDtos.size} DTOs from RestCountries.")
                val countriesWithBasicData = countryDtos.shuffled().take(30)
                    .mapNotNull { dto ->
                        Country(
                            name = dto.name.common,
                            officialName = dto.name.official,
                            flagUrl = dto.flags.png,
                            population = dto.population,
                            area = dto.area,
                            languages = dto.languages?.values?.toList() ?: emptyList(),
                            region = dto.region,
                            subregion = dto.subregion,
                            capital = dto.capital?.firstOrNull(),
                            currencies = dto.currencies?.mapNotNull { entry ->
                                "${entry.value.name} (${entry.value.symbol ?: entry.key})"
                            } ?: emptyList(),
                            unsplashImageUrl = null
                        )
                    }
                cachedCountriesBasicData = countriesWithBasicData
                if (cachedCountriesBasicData!!.isNotEmpty()){
                    Log.d(TAG, "Successfully mapped DTOs to ${cachedCountriesBasicData!!.size} Country models (basic data).")
                    emit(Resource.Success(cachedCountriesBasicData!!))
                } else {
                    Log.e(TAG, "No countries mapped from RestCountries DTOs.")
                    emit(Resource.Error("No countries found from RestCountries API."))
                }
            } else {
                Log.e(TAG, "Failed to fetch countries from RestCountries: ${response.code()} ${response.message()}")
                emit(Resource.Error("Failed to fetch countries: ${response.code()} ${response.message()}"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException in getDiscoverCountries: ${e.message}", e)
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage ?: "HTTP Error"}"))
        } catch (e: IOException) {
            Log.e(TAG, "IOException in getDiscoverCountries: ${e.message}", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getDiscoverCountries: ${e.message}", e)
            emit(Resource.Error("An unknown error occurred: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getCountryImage(countryName: String): Resource<String?> {
        Log.d(TAG, "getCountryImage called for: $countryName")
        return withContext(Dispatchers.IO) {
            try {
                // ** MODIFIED QUERY: Using just the country name for more specific results **
                // You can experiment with other suffixes like "$countryName landmark" or "$countryName scenery"
                // if just the country name is too broad or doesn't yield good visual results.
                val searchQuery = countryName
                Log.d(TAG, "Unsplash search query: $searchQuery")
                val response = unsplashApi.searchPhotos(query = searchQuery)
                Log.d(TAG, "Unsplash API response for '$countryName': Code ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val imageUrl = response.body()?.results?.firstOrNull()?.urls?.regular
                    if (imageUrl != null) {
                        Log.i(TAG, "Unsplash image URL for '$countryName': $imageUrl")
                        Resource.Success(imageUrl)
                    } else {
                        Log.w(TAG, "No image URL found in Unsplash response for '$countryName'. Results: ${response.body()?.results?.size}")
                        Resource.Error("No image found for $countryName on Unsplash.")
                    }
                } else {
                    Log.e(TAG, "Failed to fetch image from Unsplash for '$countryName': ${response.code()} ${response.message()}")
                    Resource.Error("Failed to fetch image from Unsplash: ${response.code()} ${response.message()}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HttpException in getCountryImage for '$countryName': ${e.message}", e)
                Resource.Error("An unexpected error occurred during Unsplash image fetch: ${e.localizedMessage ?: "HTTP Error"}")
            } catch (e: IOException) {
                Log.e(TAG, "IOException in getCountryImage for '$countryName': ${e.message}", e)
                Resource.Error("Couldn't reach Unsplash server. Check your internet connection.")
            } catch (e: Exception) {
                Log.e(TAG, "Exception in getCountryImage for '$countryName': ${e.message}", e)
                Resource.Error("An unknown error occurred while fetching Unsplash image: ${e.localizedMessage}")
            }
        }
    }
    // addFavoriteCountry, removeFavoriteCountry, getFavoriteCountries, isCountryFavorite remain the same
    override suspend fun addFavoriteCountry(country: Country) {
        withContext(Dispatchers.IO) {
            val entity = FavoriteCountryEntity(
                name = country.name, officialName = country.officialName, flagUrl = country.flagUrl,
                population = country.population, area = country.area, languages = country.languages,
                region = country.region, subregion = country.subregion, capital = country.capital,
                currencies = country.currencies, unsplashImageUrl = country.unsplashImageUrl // This will now have the loaded URL if favorited after image load
            )
            favoriteCountryDao.addFavorite(entity)
        }
    }

    override suspend fun removeFavoriteCountry(countryName: String) {
        withContext(Dispatchers.IO) {
            favoriteCountryDao.removeFavoriteByName(countryName)
        }
    }

    override fun getFavoriteCountries(): Flow<List<Country>> {
        return favoriteCountryDao.getFavoriteCountries().map { entities ->
            entities.map { entity ->
                Country(
                    name = entity.name, officialName = entity.officialName, flagUrl = entity.flagUrl,
                    population = entity.population, area = entity.area, languages = entity.languages,
                    region = entity.region, subregion = entity.subregion, capital = entity.capital,
                    currencies = entity.currencies, unsplashImageUrl = entity.unsplashImageUrl
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun isCountryFavorite(countryName: String): Boolean {
        return withContext(Dispatchers.IO) {
            favoriteCountryDao.isFavorite(countryName)
        }
    }
}
