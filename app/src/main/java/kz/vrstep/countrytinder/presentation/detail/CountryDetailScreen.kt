package kz.vrstep.countrytinder.presentation.detail

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImagePainter // Coil 3 import
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.compose.rememberAsyncImagePainter
import kz.vrstep.countrytinder.domain.model.Country
import kz.vrstep.countrytinder.presentation.components.ErrorView
import kz.vrstep.countrytinder.presentation.components.LoadingIndicator
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDetailScreen(
    navController: NavController,
    viewModel: CountryDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.country?.name ?: "Country Details", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> ErrorView(
                    message = state.error!!,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                state.country != null -> {
                    CountryDetailsContent(country = state.country!!)
                }
                else -> {
                    Text("No country data available.", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun CountryDetailsContent(country: Country) {
    val TAG = "CountryDetailsContent[${country.name}]"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val imageModel = country.unsplashImageUrl ?: country.flagUrl
        Log.d(TAG, "Displaying image with model: $imageModel")

        SubcomposeAsyncImage(
            model = imageModel,
            contentDescription = "Image of ${country.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        ) {
            // If painter.state in Coil 3.2.0 is indeed a StateFlow for this scope:
            val painterState by painter.state.collectAsState()
            Log.d(TAG, "Coil painter state for $imageModel: ${painterState::class.simpleName}")

            when (painterState) { // Now comparing with the collected state value
                is AsyncImagePainter.State.Loading -> {
                    Log.d(TAG, "Coil: Loading image ($imageModel)")
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AsyncImagePainter.State.Error -> {
                    val errorResult = (painterState as AsyncImagePainter.State.Error).result
                    Log.e(TAG, "Coil: Error loading $imageModel. Error: ${errorResult.throwable?.message}. Falling back to flag.")
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = rememberAsyncImagePainter(model = country.flagUrl),
                            contentDescription = "Flag of ${country.name} (Fallback for error)",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    }
                }
                is AsyncImagePainter.State.Success -> {
                    Log.i(TAG, "Coil: Successfully loaded image ($imageModel)")
                    SubcomposeAsyncImageContent()
                }
                is AsyncImagePainter.State.Empty -> {
                    Log.w(TAG, "Coil: Empty state for $imageModel. Model was likely null or empty. Falling back to flag.")
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = rememberAsyncImagePainter(model = country.flagUrl),
                            contentDescription = "Flag of ${country.name} (Fallback for empty)",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        )
                    }
                }
                // It's good practice to have an else or ensure all sealed subtypes are covered
                // If AsyncImagePainter.State is not a sealed class exhaustive in these 4,
                // an else branch might be needed by the compiler.
                // else -> { /* Potentially handle other unknown states or do nothing */ }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = country.officialName,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        DetailRow(label = "Common Name", value = country.name)
        DetailRow(label = "Capital(s)", value = country.capital ?: "N/A")
        DetailRow(label = "Region", value = country.region)
        DetailRow(label = "Subregion", value = country.subregion ?: "N/A")
        DetailRow(label = "Population", value = "%,d".format(country.population))
        DetailRow(label = "Area", value = "%,.1f km²".format(country.area))

        Spacer(modifier = Modifier.height(16.dp))
        InfoSection(title = "Languages", items = country.languages)

        Spacer(modifier = Modifier.height(16.dp))
        InfoSection(title = "Currencies", items = country.currencies)

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Flag:",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Image(
            painter = rememberAsyncImagePainter(model = country.flagUrl),
            contentDescription = "Flag of ${country.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun InfoSection(title: String, items: List<String>) {
    if (items.isNotEmpty()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        items.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
    }
}