package kz.vrstep.countrytinder.presentation.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.google.gson.Gson
import kz.vrstep.countrytinder.domain.model.Country
import kz.vrstep.countrytinder.presentation.navigation.Screen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun FavoriteCountryItem(
    country: Country,
    onRemoveClick: () -> Unit,
    navController: NavController, // Added NavController
    modifier: Modifier = Modifier
) {
    val TAG = "FavoriteItem[${country.name}]"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)) // User's preference
            .clickable {
                Log.d(TAG, "Favorite item clicked for ${country.name}")
                try {
                    val countryJson = Gson().toJson(country)
                    val encodedJson = URLEncoder.encode(countryJson, StandardCharsets.UTF_8.name())
                    navController.navigate(Screen.CountryDetailScreen.createRoute(encodedJson))
                } catch (e: Exception) {
                    Log.e(TAG, "Error serializing or encoding country JSON for navigation", e)
                }
            },
        shape = RoundedCornerShape(8.dp), // User's preference
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // User's preference
        // colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Optional: keep or remove based on desired theme
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = country.flagUrl), // Coil 3
                contentDescription = "Flag of ${country.name}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = country.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = country.capital ?: "N/A", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove Favorite", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
