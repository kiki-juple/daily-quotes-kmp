package com.disheveled.dailyquotes.ui.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import com.disheveled.dailyquotes.ui.auth.LoginScreen
import com.disheveled.dailyquotes.ui.auth.RegisterScreen

@Composable
fun AuthNavHost() {
    // Survives configuration changes and process death, so a rotate on Register does not bounce
    // the user back to Login.
    val backStack = rememberSerializable(
        serializer = NavBackStackSerializer(AuthScreen.serializer()),
    ) { NavBackStack<AuthScreen>(AuthScreen.Login) }

    val provider: (AuthScreen) -> NavEntry<AuthScreen> = entryProvider {
        entry<AuthScreen.Login> {
            LoginScreen(
                onGoToRegister = { backStack.add(AuthScreen.Register) },
            )
        }
        entry<AuthScreen.Register> {
            RegisterScreen(
                onBack = { backStack.removeLastOrNull() },
            )
        }
    }

    RenungNavDisplay(
        backStack = backStack,
        entryProvider = provider,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
    )
}
