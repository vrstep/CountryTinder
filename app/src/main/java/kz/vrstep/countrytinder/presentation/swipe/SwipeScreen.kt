package kz.vrstep.countrytinder.presentation.swipe

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kz.vrstep.countrytinder.common.Constants
import kz.vrstep.countrytinder.presentation.components.CountryCard
import kz.vrstep.countrytinder.presentation.components.ErrorView
import kz.vrstep.countrytinder.presentation.components.LoadingIndicator
import org.koin.androidx.compose.koinViewModel

@Composable
fun SwipeScreen(
    navController: NavController,
    viewModel: SwipeViewModel = koinViewModel(),
    onNavigateToDecision: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    // currentCardIndexInBatch is now managed by the ViewModel via state.currentCardIndexInBatch
    val TAG = "SwipeScreen"

    LaunchedEffect(Unit) {
        // ... (existing logic for initial load)
        if (state.countriesForSwipe.isEmpty() || state.currentCardIndexInBatch >= state.countriesForSwipe.size) {
            if (!state.isLoadingInitialBatch && state.error.isBlank()) {
                Log.d(TAG, "Calling loadNextBatchOfCountries from LaunchedEffect(Unit)")
                viewModel.loadNextBatchOfCountries() // This will set isDecisionPending to false
            }
        }
    }

    LaunchedEffect(state.swipedCountInBatch, state.countriesForSwipe.size) {
        Log.d(TAG, "LaunchedEffect(swipedCountInBatch): ${state.swipedCountInBatch}, countriesForSwipe.size=${state.countriesForSwipe.size}")
        if (state.countriesForSwipe.isNotEmpty() && state.swipedCountInBatch >= Constants.INITIAL_COUNTRY_LOAD_COUNT) {
            Log.i(TAG, "Swiped ${state.swipedCountInBatch} cards for decision. Setting decisionPending=true and navigating.")
            viewModel.setDecisionPending(true) // **SET FLAG HERE**
            onNavigateToDecision()
        }
    }

    LaunchedEffect(state.currentCardIndexInBatch, state.countriesForSwipe.size, state.isLoadingInitialBatch) {
        Log.d(TAG, "LaunchedEffect(index, size, loading): index=${state.currentCardIndexInBatch}, size=${state.countriesForSwipe.size}, loading=${state.isLoadingInitialBatch}, swipedCount=${state.swipedCountInBatch}")
        if (!state.isLoadingInitialBatch && state.countriesForSwipe.isNotEmpty() && state.currentCardIndexInBatch >= state.countriesForSwipe.size) {
            Log.i(TAG, "Current batch exhausted (index ${state.currentCardIndexInBatch} >= size ${state.countriesForSwipe.size}). Setting decisionPending=true and navigating.")
            viewModel.setDecisionPending(true) // **SET FLAG HERE**
            onNavigateToDecision()
        }
    }

    val currentCountry = state.countriesForSwipe.getOrNull(state.currentCardIndexInBatch)
    currentCountry?.let { country ->
        LaunchedEffect(country.name, country.unsplashImageUrl, state.currentCardIndexInBatch) {
            Log.d(TAG, "LaunchedEffect(country.name, unsplashImageUrl, index): name=${country.name}, url=${country.unsplashImageUrl}, loadingSetContains=${state.loadingImageForCountryName.contains(country.name)}, currentIndex=${state.currentCardIndexInBatch}")
            if (country.unsplashImageUrl == null && !state.loadingImageForCountryName.contains(country.name)) {
                Log.d(TAG, "Requesting image fetch for ${country.name} at actual list index for this country.")
                // Pass the actual index of this country within the countriesForSwipe list.
                // This is important if countriesForSwipe list itself changes.
                val actualIndexInList = state.countriesForSwipe.indexOf(country)
                if (actualIndexInList != -1) {
                    viewModel.fetchImageForCountry(country, actualIndexInList)
                } else {
                    Log.w(TAG, "Country ${country.name} not found in current countriesForSwipe list for image fetch. This shouldn't happen.")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        when {
            state.isLoadingInitialBatch && state.countriesForSwipe.isEmpty() -> {
                Log.d(TAG, "Displaying LoadingIndicator (initial batch)")
                LoadingIndicator()
            }
            state.error.isNotBlank() && state.countriesForSwipe.isEmpty() -> {
                Log.e(TAG, "Displaying ErrorView: ${state.error}")
                ErrorView(message = state.error, onRetry = {
                    Log.d(TAG, "ErrorView: Retry clicked.")
                    viewModel.loadNextBatchOfCountries(fetchNewFromApi = true)
                })
            }
            currentCountry != null -> {
                Log.d(TAG, "Displaying CountryCard for: ${currentCountry.name} (index ${state.currentCardIndexInBatch}), isImageLoading (VM): ${state.loadingImageForCountryName.contains(currentCountry.name)}")
                CountryCard(
                    country = currentCountry,
                    isImageLoading = state.loadingImageForCountryName.contains(currentCountry.name),
                    onSwipeLeft = {
                        Log.d(TAG, "Swiped Left on: ${currentCountry.name}")
                        viewModel.onSwipeLeft(currentCountry)
                        // ViewModel now handles currentCardIndexInBatch increment
                    },
                    onSwipeRight = {
                        Log.d(TAG, "Swiped Right on: ${currentCountry.name}")
                        viewModel.onSwipeRight(currentCountry)
                        // ViewModel now handles currentCardIndexInBatch increment
                    },
                    navController = navController,
                    modifier = Modifier.fillMaxSize(0.9f)
                )
            }
            !state.isLoadingInitialBatch && state.countriesForSwipe.isEmpty() && state.error.isBlank() -> {
                Log.d(TAG, "Displaying 'No more countries to show'")
                Text("No more countries to show right now. Try again later!", style = MaterialTheme.typography.bodyLarge)
            }
            // This case should ideally be covered by navigation to DecisionScreen
            state.countriesForSwipe.isNotEmpty() && state.currentCardIndexInBatch >= state.countriesForSwipe.size && !state.isLoadingInitialBatch -> {
                Log.d(TAG, "Batch done, currentCardIndex ${state.currentCardIndexInBatch} >= size ${state.countriesForSwipe.size}. Showing temporary loader before decision navigation.")
                CircularProgressIndicator()
            }
        }
    }
}