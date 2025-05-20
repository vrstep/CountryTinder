package kz.vrstep.countrytinder.presentation.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kz.vrstep.countrytinder.presentation.decision.DecisionScreen
import kz.vrstep.countrytinder.presentation.favorites.FavoritesScreen
import kz.vrstep.countrytinder.presentation.detail.CountryDetailScreen
import kz.vrstep.countrytinder.presentation.navigation.Screen
import kz.vrstep.countrytinder.presentation.swipe.SwipeScreen
import kz.vrstep.countrytinder.presentation.swipe.SwipeViewModel
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(swipeViewModel: SwipeViewModel = koinViewModel()) { // Inject SwipeViewModel
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }
    val swipeScreenState by swipeViewModel.state.collectAsState() // Collect state for decisionPending

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            showBottomBar = when (destination.route) {
                Screen.DecisionScreen.route -> false
                Screen.CountryDetailScreen.route.substringBefore("/{") -> false // Hide bottom bar on detail screen
                else -> true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    navController = navController,
                    isDecisionPending = swipeScreenState.isDecisionPending,
                    onDiscoverClickOverride = {
                        navController.navigate(Screen.DecisionScreen.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.SwipeScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.SwipeScreen.route) {
                SwipeScreen(
                    navController = navController,
                    onNavigateToDecision = {
                        swipeViewModel.setDecisionPending(true)
                        navController.navigate(Screen.DecisionScreen.route)
                    }
                )
            }
            composable(Screen.FavoritesScreen.route) {
                FavoritesScreen(navController = navController)
            }
            composable(Screen.DecisionScreen.route) {
                DecisionScreen(
                    swipeViewModel = koinViewModel(),
                    onContinueSwiping = {
                        swipeViewModel.setDecisionPending(false)
                        navController.navigate(Screen.SwipeScreen.route) {
                            popUpTo(Screen.SwipeScreen.route) { inclusive = true }
                        }
                    },
                    onViewFavorites = {
                        navController.navigate(Screen.FavoritesScreen.route)
                    }
                )
            }
            composable(
                route = Screen.CountryDetailScreen.route,
                arguments = listOf(navArgument("countryJson") { type = NavType.StringType })
            ) { backStackEntry ->
                // CountryDetailScreen gets its own ViewModel instance via Koin,
                // which will use SavedStateHandle to get the countryJson argument.
                CountryDetailScreen(navController = navController)
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    navController: NavHostController,
    isDecisionPending: Boolean,
    onDiscoverClickOverride: () -> Unit // Callback for special Discover navigation
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val TAG = "AppBottomNavBar"

    data class BottomNavItem(val screen: Screen, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)

    val items = listOf(
        BottomNavItem(Screen.SwipeScreen, Icons.Filled.Swipe, "Discover"),
        BottomNavItem(Screen.FavoritesScreen, Icons.Filled.Favorite, "Favorites")
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.screen.route || (item.screen == Screen.SwipeScreen && currentDestination?.route == Screen.DecisionScreen.route && isDecisionPending) } == true,
                onClick = {
                    if (item.screen == Screen.SwipeScreen && isDecisionPending && currentDestination?.route != Screen.DecisionScreen.route) {
                        Log.d(TAG, "Discover clicked, decision pending. Navigating to DecisionScreen.")
                        onDiscoverClickOverride() // Use the override to navigate to DecisionScreen
                    } else if (item.screen == Screen.SwipeScreen && currentDestination?.route == Screen.DecisionScreen.route) {
                        // Already on DecisionScreen (or effectively, as Discover leads there), do nothing or pop?
                        // For now, let standard logic handle it, which might re-navigate to SwipeScreen if not handled carefully.
                        // The override should handle this. If on DecisionScreen, Discover is not shown.
                        // If on Favorites and decision pending, override is called.
                        Log.d(TAG, "Discover clicked, already on Decision or should be. Standard nav might apply if override isn't specific enough.")
                        navController.navigate(item.screen.route) { // Fallback to standard if override doesn't cover all cases
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    else {
                        Log.d(TAG, "Standard bottom nav click for ${item.label}")
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

