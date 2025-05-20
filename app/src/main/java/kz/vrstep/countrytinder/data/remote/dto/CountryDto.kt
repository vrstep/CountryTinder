package kz.vrstep.countrytinder.data.remote.dto

// Simplified DTO based on requested fields.
// The actual RestCountries API response is more complex; map only what you need.
data class CountryDto(
    val name: NameDto,
    val flags: FlagsDto,
    val population: Long,
    val area: Double,
    val languages: Map<String, String>?, // e.g., {"eng": "English", "fra": "French"}
    val region: String,
    val subregion: String?,
    val capital: List<String>?,
    val currencies: Map<String, CurrencyDetailDto>? // e.g., {"USD": {"name": "United States dollar", "symbol": "$"}}}
)

data class NameDto(
    val common: String,
    val official: String
)

data class FlagsDto(
    val png: String,
    val svg: String?, // Optional
    val alt: String?
)

data class CurrencyDetailDto(
    val name: String,
    val symbol: String?
)
