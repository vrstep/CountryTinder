package kz.vrstep.countrytinder.presentation.decision

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionScreen(
    onContinueSwiping: () -> Unit,
    onViewFavorites: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("What's Next?") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "You've swiped through a batch of countries!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onContinueSwiping,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue Swiping More Countries")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onViewFavorites,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View My Favorite Countries")
            }
        }
    }
}
