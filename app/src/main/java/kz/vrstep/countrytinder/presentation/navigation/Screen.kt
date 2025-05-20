package kz.vrstep.countrytinder.presentation.navigation

sealed class Screen(val route: String) {
    object SwipeScreen : Screen("swipe_screen")
    object FavoritesScreen : Screen("favorites_screen")
    object DecisionScreen : Screen("decision_screen") // Navigated to after 10 swipes
    // Potentially: object CountryDetailScreen : Screen("country_detail_screen/{countryName}")
}