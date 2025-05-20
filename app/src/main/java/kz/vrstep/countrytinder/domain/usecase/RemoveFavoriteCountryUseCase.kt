package kz.vrstep.countrytinder.domain.usecase

import kz.vrstep.countrytinder.repository.CountryRepository

class RemoveFavoriteCountryUseCase(private val repository: CountryRepository) {
    suspend operator fun invoke(countryName: String) {
        repository.removeFavoriteCountry(countryName)
    }
}
