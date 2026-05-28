package com.disheveled.dailyquotes.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface MainScreen : NavKey {

    @Serializable
    data object Home : MainScreen

    @Serializable
    data object Favorites : MainScreen
}

@Serializable
sealed interface AuthScreen : NavKey {

    @Serializable
    data object Login : AuthScreen

    @Serializable
    data object Register : AuthScreen
}
