package kz.vrstep.countrytinder.domain.usecase

import kz.vrstep.countrytinder.domain.model.Country
import kz.vrstep.countrytinder.repository.CountryRepository

class AddFavoriteCountryUseCase(private val repository: CountryRepository) {
    suspend operator fun invoke(country: Country) {
        repository.addFavoriteCountry(country)
    }
}
