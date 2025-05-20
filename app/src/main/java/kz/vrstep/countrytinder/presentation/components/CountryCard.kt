package kz.vrstep.countrytinder.presentation.components

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kz.vrstep.countrytinder.domain.model.Country
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CountryCard(
    country: Country,
    isImageLoading: Boolean,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val TAG = "CountryCard[${country.name}]"

    // **MODIFIED: Key Animatable states to country.name to ensure they reset for new cards**
    val offsetX = remember(country.name) { Animatable(0f) }
    val offsetY = remember(country.name) { Animatable(0f) }
    val rotation = remember(country.name) { Animatable(0f) }

    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    val swipeThreshold = screenWidthPx / 3.0f
    val rotationRange = 15f

    Log.d(TAG, "Composing. OffsetX Start: ${offsetX.value} UnsplashURL: ${country.unsplashImageUrl}, isImageLoading (ViewModel): $isImageLoading")

    Card(
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer(
                rotationZ = rotation.value,
                alpha = (1f - (abs(offsetX.value) / (screenWidthPx * 0.7f))).coerceIn(0f, 1f)
            )
            .pointerInput(country.name) {
                detectDragGestures(
                    onDragStart = {
                        Log.d(TAG, "DragStart")
                    },
                    onDragEnd = {
                        Log.d(TAG, "DragEnd. Current offsetX: ${offsetX.value}")
                        scope.launch {
                            val currentOffsetX = offsetX.value
                            if (currentOffsetX > swipeThreshold) {
                                Log.d(TAG, "Swipe Right triggered (offset: $currentOffsetX > threshold: $swipeThreshold)")
                                launch { offsetX.animateTo(targetValue = screenWidthPx * 1.2f, animationSpec = tween(durationMillis = 300)) }
                                launch { rotation.animateTo(targetValue = rotationRange, animationSpec = tween(durationMillis = 300)) }
                                onSwipeRight()
                            } else if (currentOffsetX < -swipeThreshold) {
                                Log.d(TAG, "Swipe Left triggered (offset: $currentOffsetX < threshold: ${-swipeThreshold})")
                                launch { offsetX.animateTo(targetValue = -screenWidthPx * 1.2f, animationSpec = tween(durationMillis = 300)) }
                                launch { rotation.animateTo(targetValue = -rotationRange, animationSpec = tween(durationMillis = 300)) }
                                onSwipeLeft()
                            } else {
                                Log.d(TAG, "Swipe did not meet threshold. Animating back to center.")
                                launch { offsetX.animateTo(targetValue = 0f, animationSpec = spring(stiffness = Spring.StiffnessMedium)) }
                                launch { offsetY.animateTo(targetValue = 0f, animationSpec = spring(stiffness = Spring.StiffnessMedium)) }
                                launch { rotation.animateTo(targetValue = 0f, animationSpec = spring(stiffness = Spring.StiffnessMedium)) }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y * 0.3f)
                            rotation.snapTo((offsetX.value / screenWidthPx * rotationRange * 1.5f).coerceIn(-rotationRange, rotationRange))
                        }
                    }
                )
            }
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image loading logic (same as your provided version)
            if (country.unsplashImageUrl == null && isImageLoading) {
                Log.d(TAG, "ViewModel is fetching URL. Showing general loader.")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val imageModel = country.unsplashImageUrl ?: country.flagUrl
                Log.d(TAG, "Using image model for Coil: $imageModel")
                SubcomposeAsyncImage(
                    model = imageModel,
                    contentDescription = "Image of ${country.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val currentImageState by painter.state.collectAsState()
                    Log.d(TAG, "Coil painter state for $imageModel: ${currentImageState::class.simpleName}")

                    when (val S = currentImageState) {
                        is AsyncImagePainter.State.Loading -> {
                            Log.d(TAG, "Coil: Loading image ($imageModel)")
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is AsyncImagePainter.State.Error -> {
                            Log.e(TAG, "Coil: Error loading $imageModel. Error: ${S.result.throwable?.message}. Falling back to flag.")
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = country.flagUrl),
                                    contentDescription = "Flag of ${country.name} (Fallback for error)",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().padding(32.dp)
                                )
                            }
                        }
                        is AsyncImagePainter.State.Success -> {
                            Log.i(TAG, "Coil: Successfully loaded image ($imageModel)")
                            SubcomposeAsyncImageContent()
                        }
                        is AsyncImagePainter.State.Empty -> {
                            Log.w(TAG, "Coil: Empty state for $imageModel. Model was likely null or empty. Falling back to flag.")
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = country.flagUrl),
                                    contentDescription = "Flag of ${country.name} (Fallback for empty)",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().padding(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Gradient overlay (same as your provided version)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = screenHeightPx * 0.4f,
                            endY = screenHeightPx
                        )
                    )
            )
            // Text content (same as your provided version)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(text = country.name, style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp ), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Population: ~${(country.population / 1000000.0).let { "%.1f".format(it) }}M", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Capital: ${country.capital ?: "N/A"}", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            // Swipe action indicators (updated to use animated offsetX.value for alpha)
            val iconAlpha = (abs(offsetX.value) / swipeThreshold).coerceIn(0f, 1f) * 0.8f // Use animated offset
            val iconSize = 72.dp
            val iconPadding = 24.dp

            if (offsetX.value > 20) { // Show "Like" indicator based on animated offset
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Like",
                    tint = Color(0xFF4CAF50).copy(alpha = iconAlpha),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = iconPadding)
                        .size(iconSize)
                        .graphicsLayer(
                            rotationZ = -15f + (offsetX.value / screenWidthPx * 30f).coerceIn(-10f, 10f),
                            alpha = iconAlpha
                        )
                )
            } else if (offsetX.value < -20) { // Show "Nope" indicator based on animated offset
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Nope",
                    tint = Color(0xFFF44336).copy(alpha = iconAlpha),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = iconPadding)
                        .size(iconSize)
                        .graphicsLayer(
                            rotationZ = 15f + (offsetX.value / screenWidthPx * 30f).coerceIn(-10f, 10f),
                            alpha = iconAlpha
                        )
                )
            }
        }
    }
}
