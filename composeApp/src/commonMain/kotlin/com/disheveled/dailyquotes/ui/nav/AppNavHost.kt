package com.disheveled.dailyquotes.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import com.disheveled.dailyquotes.data.repository.AuthRepository
import com.disheveled.dailyquotes.ui.components.NavBar
import com.disheveled.dailyquotes.ui.components.NavBarItem
import com.disheveled.dailyquotes.ui.components.RenungSnackbar
import com.disheveled.dailyquotes.ui.favorites.FavoritesScreen
import com.disheveled.dailyquotes.ui.home.HomeScreen
import com.disheveled.dailyquotes.ui.theme.RenungColors
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.heart
import dailyquotes.composeapp.generated.resources.heart_fill
import dailyquotes.composeapp.generated.resources.home
import dailyquotes.composeapp.generated.resources.home_fill
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun AppNavHost(authRepository: AuthRepository = koinInject()) {
    val user by authRepository.currentUser.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(RenungColors.Paper)) {
        if (user == null) {
            AuthNavHost()
        } else {
            MainNavHost()
        }
    }
}

@Composable
private fun MainNavHost() {
    val backStack = remember { NavBackStack<MainScreen>(MainScreen.Home) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2400)
            toastMessage = null
        }
    }

    val top = backStack.lastOrNull() ?: MainScreen.Home
    val activeId = when (top) {
        MainScreen.Home -> "home"
        MainScreen.Favorites -> "favorites"
    }

    val items = remember {
        listOf(
            NavBarItem(
                id = "home",
                label = "Beranda",
                icon = Res.drawable.home,
                iconActive = Res.drawable.home_fill,
            ),
            NavBarItem(
                id = "favorites",
                label = "Favorit",
                icon = Res.drawable.heart,
                iconActive = Res.drawable.heart_fill,
            ),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            val provider: (MainScreen) -> NavEntry<MainScreen> = entryProvider {
                entry<MainScreen.Home> {
                    HomeScreen(
                        onShowToast = { toastMessage = it },
                    )
                }
                entry<MainScreen.Favorites> {
                    FavoritesScreen(
                        onBack = { backStack.removeLastOrNull() },
                        onShowToast = { toastMessage = it },
                    )
                }
            }

            RenungNavDisplay(
                backStack = backStack,
                entryProvider = provider,
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                RenungSnackbar(
                    message = toastMessage,
                    visible = toastMessage != null,
                )
            }
        }

        NavBar(
            items = items,
            active = activeId,
            onSelect = { id ->
                val target = when (id) {
                    "home" -> MainScreen.Home
                    "favorites" -> MainScreen.Favorites
                    else -> MainScreen.Home
                }
                if (target != backStack.lastOrNull()) {
                    backStack.clear()
                    backStack.add(target)
                }
            },
        )
    }
}
