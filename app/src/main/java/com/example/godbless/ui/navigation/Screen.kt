package com.example.godbless.ui.navigation
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object Shopping : Screen("shopping")
    object Profile : Screen("profile")
    object AddEditProduct : Screen("add_edit_product/{productId}") {
        fun createRoute(productId: Long? = null): String {
            return if (productId != null) {
                "add_edit_product/$productId"
            } else {
                "add_edit_product/-1"
            }
        }
    }
    object ProductSelection : Screen("product_selection")
}
