package kz.vrstep.countrytinder.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kz.vrstep.countrytinder.data.local.dao.FavoriteCountryDao
import kz.vrstep.countrytinder.data.local.entity.FavoriteCountryEntity
import kz.vrstep.countrytinder.data.local.typeconverters.StringListConverter

@Database(
    entities = [FavoriteCountryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class) // For List<String> fields
abstract class CountryDatabase : RoomDatabase() {
    abstract fun favoriteCountryDao(): FavoriteCountryDao
}
