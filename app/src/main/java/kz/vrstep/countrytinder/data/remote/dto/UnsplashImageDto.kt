package kz.vrstep.countrytinder.data.remote.dto

data class UnsplashSearchResponse(
    val results: List<UnsplashPhotoDto>?
)

data class UnsplashPhotoDto(
    val id: String,
    val urls: UnsplashUrlsDto,
    val description: String?,
    val alt_description: String?
)

data class UnsplashUrlsDto(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
)
