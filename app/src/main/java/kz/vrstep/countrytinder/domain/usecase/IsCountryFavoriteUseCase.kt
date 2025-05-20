package kz.vrstep.countrytinder.domain.usecase

import kz.vrstep.countrytinder.repository.CountryRepository

class IsCountryFavoriteUseCase(private val repository: CountryRepository) {
    suspend operator fun invoke(countryName: String): Boolean {
        return repository.isCountryFavorite(countryName)
    }
}
