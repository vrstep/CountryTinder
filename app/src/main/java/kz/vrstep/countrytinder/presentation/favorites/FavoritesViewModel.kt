package kz.vrstep.countrytinder.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kz.vrstep.countrytinder.domain.model.Country
import kz.vrstep.countrytinder.domain.usecase.GetFavoriteCountriesUseCase
import kz.vrstep.countrytinder.domain.usecase.RemoveFavoriteCountryUseCase

data class FavoritesScreenState(
    val isLoading: Boolean = true,
    val favoriteCountries: List<Country> = emptyList(),
    val error: String = ""
)

class FavoritesViewModel(
    private val getFavoriteCountriesUseCase: GetFavoriteCountriesUseCase,
    private val removeFavoriteCountryUseCase: RemoveFavoriteCountryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesScreenState())
    val state: StateFlow<FavoritesScreenState> = _state.asStateFlow()

    init {
        loadFavoriteCountries()
    }

    private fun loadFavoriteCountries() {
        viewModelScope.launch {
            getFavoriteCountriesUseCase()
                .onStart { _state.value = FavoritesScreenState(isLoading = true) }
                .catch { e ->
                    _state.value = FavoritesScreenState(
                        isLoading = false,
                        error = e.localizedMessage ?: "Failed to load favorites"
                    )
                }
                .collect { countries ->
                    _state.value = FavoritesScreenState(
                        isLoading = false,
                        favoriteCountries = countries
                    )
                }
        }
    }

    fun removeFavorite(countryName: String) {
        viewModelScope.launch {
            removeFavoriteCountryUseCase(countryName)
            // The flow from getFavoriteCountriesUseCase should automatically update the list.
        }
    }
}
