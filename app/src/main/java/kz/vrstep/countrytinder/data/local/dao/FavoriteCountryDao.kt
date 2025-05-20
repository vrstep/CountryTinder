package kz.vrstep.countrytinder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kz.vrstep.countrytinder.data.local.entity.FavoriteCountryEntity

@Dao
interface FavoriteCountryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(country: FavoriteCountryEntity)

    @Delete
    suspend fun removeFavorite(country: FavoriteCountryEntity)

    @Query("SELECT * FROM favorite_countries")
    fun getFavoriteCountries(): Flow<List<FavoriteCountryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_countries WHERE name = :countryName LIMIT 1)")
    suspend fun isFavorite(countryName: String): Boolean

    @Query("DELETE FROM favorite_countries WHERE name = :countryName")
    suspend fun removeFavoriteByName(countryName: String)
}