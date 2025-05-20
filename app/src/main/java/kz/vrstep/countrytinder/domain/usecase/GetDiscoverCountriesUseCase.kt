package kz.vrstep.countrytinder.domain.usecase

import kotlinx.coroutines.flow.Flow
import kz.vrstep.countrytinder.common.Resource
import kz.vrstep.countrytinder.domain.model.Country
import kz.vrstep.countrytinder.repository.CountryRepository

class GetDiscoverCountriesUseCase(private val repository: CountryRepository) {
    operator fun invoke(fetchNew: Boolean = false): Flow<Resource<List<Country>>> {
        return repository.getDiscoverCountries(fetchNew)
    }
}
