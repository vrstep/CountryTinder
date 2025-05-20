package kz.vrstep.countrytinder.presentation.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kz.vrstep.countrytinder.presentation.decision.DecisionScreen
import kz.vrstep.countrytinder.presentation.favorites.FavoritesScreen
import kz.vrstep.countrytinder.presentation.navigation.Screen
import kz.vrstep.countrytinder.presentation.swipe.SwipeScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            showBottomBar = when (destination.route) {
                Screen.DecisionScreen.route -> false
                else -> true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(navController = navController)
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
                        navController.navigate(Screen.DecisionScreen.route) {
                            // Do not pop SwipeScreen here, DecisionScreen is on top of it.
                        }
                    }
                )
            }
            composable(Screen.FavoritesScreen.route) {
                FavoritesScreen(navController = navController) // NavController can be used for back navigation
            }
            composable(Screen.DecisionScreen.route) {
                DecisionScreen(
                    onContinueSwiping = {
                        // Navigate to SwipeScreen and clear DecisionScreen from backstack.
                        // This ensures SwipeScreen's LaunchedEffect(Unit) for loading can run if needed.
                        navController.navigate(Screen.SwipeScreen.route) {
                            popUpTo(Screen.SwipeScreen.route) { inclusive = true }
                        }
                    },
                    onViewFavorites = {
                        // Simply navigate to FavoritesScreen. DecisionScreen will be on the backstack.
                        navController.navigate(Screen.FavoritesScreen.route)
                    }
                )
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
                selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
