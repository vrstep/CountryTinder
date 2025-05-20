package kz.vrstep.countrytinder.domain.model

data class Country(
    val name: String,
    val officialName: String,
    val flagUrl: String,
    val population: Long,
    val area: Double,
    val languages: List<String>,
    val region: String,
    val subregion: String?,
    val capital: String?,
    val currencies: List<String>,
    var unsplashImageUrl: String? // Mutable if fetched separately after initial load
)
