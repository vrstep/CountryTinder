package kz.vrstep.countrytinder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_countries")
data class FavoriteCountryEntity(
    @PrimaryKey val name: String, // Common name, assuming it's unique enough for PK
    val officialName: String,
    val flagUrl: String,
    val population: Long,
    val area: Double,
    val languages: List<String>, // Will require a TypeConverter
    val region: String,
    val subregion: String?,
    val capital: String?,
    val currencies: List<String>, // Will require a TypeConverter
    val unsplashImageUrl: String? // Image from Unsplash
)