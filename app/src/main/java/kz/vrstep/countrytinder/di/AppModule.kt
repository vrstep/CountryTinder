package kz.vrstep.countrytinder.di

import androidx.room.Room
import kz.vrstep.countrytinder.common.Constants
import kz.vrstep.countrytinder.data.local.db.CountryDatabase
import kz.vrstep.countrytinder.data.remote.api.RestCountriesApi
import kz.vrstep.countrytinder.data.remote.api.UnsplashApi
import kz.vrstep.countrytinder.domain.usecase.AddFavoriteCountryUseCase
import kz.vrstep.countrytinder.domain.usecase.GetDiscoverCountriesUseCase
import kz.vrstep.countrytinder.domain.usecase.GetFavoriteCountriesUseCase
import kz.vrstep.countrytinder.domain.usecase.GetUnsplashImageForCountryUseCase
import kz.vrstep.countrytinder.domain.usecase.IsCountryFavoriteUseCase
import kz.vrstep.countrytinder.domain.usecase.RemoveFavoriteCountryUseCase
import kz.vrstep.countrytinder.presentation.detail.CountryDetailViewModel
import kz.vrstep.countrytinder.presentation.favorites.FavoritesViewModel
import kz.vrstep.countrytinder.presentation.swipe.SwipeViewModel
import kz.vrstep.countrytinder.repository.CountryRepository
import kz.vrstep.countrytinder.repository.CountryRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.*
import org.koin.dsl.module

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    // OkHttpClient (General purpose, used by RestCountries and as base for Unsplash client)
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(60, TimeUnit.SECONDS) // Increased from 30
            .readTimeout(60, TimeUnit.SECONDS)    // Increased from 30
            .writeTimeout(60, TimeUnit.SECONDS)   // Increased from 30
            .build()
    }

    // Retrofit for RestCountries
    single<RestCountriesApi> {
        Retrofit.Builder()
            .baseUrl(Constants.REST_COUNTRIES_BASE_URL)
            .client(get()) // Koin will provide the general OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RestCountriesApi::class.java)
    }

    // Retrofit for Unsplash
    single<UnsplashApi> {
        // Create a specific OkHttpClient for Unsplash with authorization and potentially different timeouts if needed
        val unsplashClient = get<OkHttpClient>().newBuilder() // Start with the general OkHttpClient config
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Client-ID ${Constants.UNSPLASH_ACCESS_KEY}")
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            // If Unsplash needs even longer timeouts than the general client, you can override them here:
            // .connectTimeout(90, TimeUnit.SECONDS)
            // .readTimeout(90, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(Constants.UNSPLASH_BASE_URL)
            .client(unsplashClient) // Use the Unsplash-specific client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApi::class.java)
    }

    // Room Database
    single {
        Room.databaseBuilder(
            androidApplication(),
            CountryDatabase::class.java,
            Constants.DB_NAME
        ).fallbackToDestructiveMigration(false)
            .build()
    }

    // DAO
    single { get<CountryDatabase>().favoriteCountryDao() }

    // Repository
    single<CountryRepository> { CountryRepositoryImpl(get(), get(), get()) }

    // Use Cases
    factory { GetDiscoverCountriesUseCase(get()) }
    factory { AddFavoriteCountryUseCase(get()) }
    factory { RemoveFavoriteCountryUseCase(get()) }
    factory { GetFavoriteCountriesUseCase(get()) }
    factory { IsCountryFavoriteUseCase(get()) }
    factory { GetUnsplashImageForCountryUseCase(get()) }

    // ViewModels
    viewModel { SwipeViewModel(get(), get(), get(), get(), get())  }
    viewModel { FavoritesViewModel(get(), get()) }
    viewModel { params -> CountryDetailViewModel(savedStateHandle = params.get()) }
}
