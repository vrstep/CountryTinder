package kz.vrstep.countrytinder.domain.usecase

import kz.vrstep.countrytinder.common.Resource
import kz.vrstep.countrytinder.repository.CountryRepository

class GetUnsplashImageForCountryUseCase(private val repository: CountryRepository) {
    suspend operator fun invoke(countryName: String): Resource<String?> {
        return repository.getCountryImage(countryName)
    }
}