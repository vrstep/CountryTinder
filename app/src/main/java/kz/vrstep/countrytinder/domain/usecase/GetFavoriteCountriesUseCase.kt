package kz.vrstep.countrytinder.domain.usecase

import kotlinx.coroutines.flow.Flow
import kz.vrstep.countrytinder.domain.model.Country
import kz.vrstep.countrytinder.repository.CountryRepository

class GetFavoriteCountriesUseCase(private val repository: CountryRepository) {
    operator fun invoke(): Flow<List<Country>> {
        return repository.getFavoriteCountries()
    }
}
