package kz.vrstep.countrytinder.presentation.swipe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.vrstep.countrytinder.common.Constants
import kz.vrstep.countrytinder.common.Resource
import kz.vrstep.countrytinder.domain.model.Country
import kz.vrstep.countrytinder.domain.usecase.AddFavoriteCountryUseCase
import kz.vrstep.countrytinder.domain.usecase.GetDiscoverCountriesUseCase
import kz.vrstep.countrytinder.domain.usecase.GetUnsplashImageForCountryUseCase
import kz.vrstep.countrytinder.domain.usecase.IsCountryFavoriteUseCase
import kz.vrstep.countrytinder.domain.usecase.RemoveFavoriteCountryUseCase

data class SwipeScreenState(
    val isLoadingInitialBatch: Boolean = false,
    val allFetchedCountries: List<Country> = emptyList(),
    val countriesForSwipe: List<Country> = emptyList(),
    val currentCardIndexInBatch: Int = 0, // Index for countriesForSwipe
    val error: String = "",
    val swipedCountInBatch: Int = 0, // Counts swipes towards the 10-card decision
    val seenCountryNames: Set<String> = emptySet(),
    val loadingImageForCountryName: Set<String> = emptySet()
)

class SwipeViewModel(
    private val getDiscoverCountriesUseCase: GetDiscoverCountriesUseCase,
    private val addFavoriteCountryUseCase: AddFavoriteCountryUseCase,
    private val removeFavoriteCountryUseCase: RemoveFavoriteCountryUseCase,
    private val isCountryFavoriteUseCase: IsCountryFavoriteUseCase,
    private val getUnsplashImageForCountryUseCase: GetUnsplashImageForCountryUseCase
) : ViewModel() {

    private val TAG = "SwipeViewModel"
    private val _state = MutableStateFlow(SwipeScreenState())
    val state: StateFlow<SwipeScreenState> = _state.asStateFlow()

    fun loadNextBatchOfCountries(fetchNewFromApi: Boolean = true) { // Renamed for clarity
        Log.d(TAG, "loadNextBatchOfCountries called. FetchNew: $fetchNewFromApi, current allFetched: ${_state.value.allFetchedCountries.size}, seen: ${_state.value.seenCountryNames.size}")
        viewModelScope.launch {
            if (!fetchNewFromApi && _state.value.allFetchedCountries.isNotEmpty()) {
                val remainingPreviouslyFetched = _state.value.allFetchedCountries
                    .filterNot { it.name in _state.value.seenCountryNames }
                Log.d(TAG, "Using cached allFetchedCountries. Remaining after filter: ${remainingPreviouslyFetched.size}")
                if (remainingPreviouslyFetched.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            isLoadingInitialBatch = false,
                            countriesForSwipe = remainingPreviouslyFetched.take(Constants.INITIAL_COUNTRY_LOAD_COUNT),
                            allFetchedCountries = remainingPreviouslyFetched.drop(Constants.INITIAL_COUNTRY_LOAD_COUNT),
                            currentCardIndexInBatch = 0, // Reset index for new batch
                            swipedCountInBatch = 0, // Reset swipe count for decision
                            error = ""
                        )
                    }
                    Log.d(TAG, "Updated state from cache. Countries for swipe: ${_state.value.countriesForSwipe.size}, currentCardIndex: 0")
                    if (_state.value.countriesForSwipe.isNotEmpty()) return@launch
                } else {
                    Log.d(TAG, "No remaining countries in cache after filtering seen ones.")
                }
            }

            Log.d(TAG, "Fetching new batch from API.")
            _state.update { it.copy(isLoadingInitialBatch = true, error = "", currentCardIndexInBatch = 0, swipedCountInBatch = 0) } // Reset index and swipe count before new API fetch

            getDiscoverCountriesUseCase(fetchNew = true).onEach { result ->
                when (result) {
                    is Resource.Loading -> { /* Handled by initial update */ }
                    is Resource.Success -> {
                        val newCountriesFromApi = result.data ?: emptyList()
                        Log.d(TAG, "Fetched ${newCountriesFromApi.size} countries from API.")
                        val uniqueNewCountries = newCountriesFromApi
                            .filterNot { it.name in _state.value.seenCountryNames }
                            .distinctBy { it.name }
                        Log.d(TAG, "After filtering seen and distinct: ${uniqueNewCountries.size} unique new countries.")

                        _state.update {
                            it.copy(
                                isLoadingInitialBatch = false,
                                countriesForSwipe = uniqueNewCountries.take(Constants.INITIAL_COUNTRY_LOAD_COUNT),
                                allFetchedCountries = uniqueNewCountries.drop(Constants.INITIAL_COUNTRY_LOAD_COUNT),
                                // currentCardIndexInBatch is already 0
                                // swipedCountInBatch is already 0
                                error = if (uniqueNewCountries.isEmpty() && newCountriesFromApi.isNotEmpty()) "No new countries to discover right now."
                                else if (uniqueNewCountries.isEmpty()) "No countries found."
                                else ""
                            )
                        }
                        Log.d(TAG, "Successfully loaded initial batch. Countries for swipe: ${_state.value.countriesForSwipe.size}, Error: '${_state.value.error}'")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error loading initial batch: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoadingInitialBatch = false,
                                error = result.message ?: "An unknown error occurred",
                                countriesForSwipe = emptyList(), allFetchedCountries = emptyList()
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun fetchImageForCountry(country: Country, indexInSwipeList: Int) {
        // ... (fetchImageForCountry logic remains the same, logging is important here)
        if (country.unsplashImageUrl != null || _state.value.loadingImageForCountryName.contains(country.name)) {
            Log.d(TAG, "Image for ${country.name} already loaded/loading. URL: ${country.unsplashImageUrl}, LoadingSetContains: ${_state.value.loadingImageForCountryName.contains(country.name)}")
            return
        }
        Log.i(TAG, "Fetching Unsplash image for: ${country.name} at index $indexInSwipeList")
        viewModelScope.launch {
            _state.update { it.copy(loadingImageForCountryName = it.loadingImageForCountryName + country.name) }
            // ... (rest of fetchImageForCountry logic)
            val imageResource = getUnsplashImageForCountryUseCase(country.name)
            val currentSwipeList = _state.value.countriesForSwipe.toMutableList()
            if (indexInSwipeList >= 0 && indexInSwipeList < currentSwipeList.size && currentSwipeList[indexInSwipeList].name == country.name) {
                val newImageUrl = if (imageResource is Resource.Success) imageResource.data else null
                currentSwipeList[indexInSwipeList] = currentSwipeList[indexInSwipeList].copy(unsplashImageUrl = newImageUrl)
                _state.update {
                    it.copy(
                        countriesForSwipe = currentSwipeList.toList(),
                        loadingImageForCountryName = it.loadingImageForCountryName - country.name
                    )
                }
            } else {
                _state.update { it.copy(loadingImageForCountryName = it.loadingImageForCountryName - country.name) }
            }
        }
    }

    fun resetSwipeCountForBatch() { // This is called when user continues swiping after decision
        Log.d(TAG, "Resetting swipe count for batch (swipedCountInBatch to 0).")
        _state.update { it.copy(swipedCountInBatch = 0) }
        // currentCardIndexInBatch will be reset if loadNextBatchOfCountries is called or if it's already at 0.
    }

    private fun moveToNextCard() {
        if (_state.value.currentCardIndexInBatch < _state.value.countriesForSwipe.size -1) {
            _state.update { it.copy(currentCardIndexInBatch = it.currentCardIndexInBatch + 1) }
            Log.d(TAG, "Moved to next card. New index: ${_state.value.currentCardIndexInBatch}")
        } else {
            // Last card of the current visual batch swiped.
            // The LaunchedEffect in SwipeScreen will handle navigation to DecisionScreen.
            Log.d(TAG, "Last card of current visual batch swiped. Index: ${_state.value.currentCardIndexInBatch}")
            // To ensure the UI updates to show no card before navigation, we can clear the current index or similar
            // but the navigation to DecisionScreen should take precedence.
            // For safety, we can ensure the index doesn't go out of bounds if something tries to access it after this.
            _state.update { it.copy(currentCardIndexInBatch = it.currentCardIndexInBatch + 1) } // Allow index to go "out of bounds" to trigger UI update
        }
    }

    fun onSwipeLeft(country: Country) {
        Log.d(TAG, "Swiped Left on: ${country.name}")
        _state.update {
            it.copy(
                swipedCountInBatch = it.swipedCountInBatch + 1, // Counts towards the 10-card decision
                seenCountryNames = it.seenCountryNames + country.name
            )
        }
        moveToNextCard()
    }

    fun onSwipeRight(country: Country) {
        Log.d(TAG, "Swiped Right on: ${country.name}. Adding to favorites.")
        viewModelScope.launch { addFavoriteCountryUseCase(country) }
        _state.update {
            it.copy(
                swipedCountInBatch = it.swipedCountInBatch + 1, // Counts towards the 10-card decision
                seenCountryNames = it.seenCountryNames + country.name
            )
        }
        moveToNextCard()
    }
}

