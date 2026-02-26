package com.louis.app.cavity.ui.navigation

object NavigationProvider {
    val appNavigator: AppNavigator by lazy {
        NavComponentNavigator(
            resolvers = listOf(
                NavComponentHomeRouteResolver(),
                NavComponentWineOptionsRouteResolver()
            )
        )
    }
}
