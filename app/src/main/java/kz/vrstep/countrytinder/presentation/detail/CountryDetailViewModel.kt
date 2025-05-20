package kz.vrstep.countrytinder.presentation.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kz.vrstep.countrytinder.domain.model.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class CountryDetailState(
    val country: Country? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class CountryDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(CountryDetailState())
    val state: StateFlow<CountryDetailState> = _state.asStateFlow()

    private val TAG = "CountryDetailVM"

    init {
        savedStateHandle.get<String>("countryJson")?.let { encodedJson ->
            try {
                val countryJson = URLDecoder.decode(encodedJson, StandardCharsets.UTF_8.name())
                Log.d(TAG, "Received countryJson: $countryJson")
                val country = Gson().fromJson(countryJson, Country::class.java)
                _state.value = CountryDetailState(country = country, isLoading = false)
                Log.d(TAG, "Successfully parsed country: ${country?.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing countryJson", e)
                _state.value = CountryDetailState(error = "Failed to load country details.", isLoading = false)
            }
        } ?: run {
            Log.e(TAG, "countryJson argument is null")
            _state.value = CountryDetailState(error = "Country data not found.", isLoading = false)
        }
    }
}