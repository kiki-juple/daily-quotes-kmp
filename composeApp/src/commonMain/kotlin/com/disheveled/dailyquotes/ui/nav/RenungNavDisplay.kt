package com.disheveled.dailyquotes.ui.nav

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Multiplatform stand-in for `androidx.navigation3.ui.NavDisplay` (which is Android-only in 1.1.2).
 *
 * Renders the top entry of [backStack] resolved via [entryProvider], with horizontal slide
 * transitions matched to push/pop direction.
 */
@Suppress("DEPRECATION")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : NavKey> RenungNavDisplay(
    backStack: NavBackStack<T>,
    entryProvider: (T) -> androidx.navigation3.runtime.NavEntry<T>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val top = backStack.lastOrNull() ?: return

    BackHandler(enabled = backStack.size > 1) { onBack() }

    AnimatedContent(
        // The stack depth travels with the state instead of being read off `backStack` inside
        // `transitionSpec`: by the time a transition is set up the popped entry is already gone from
        // the list, so looking up its index there returns -1 and every pop animates like a push.
        targetState = NavSlot(top, backStack.size),
        transitionSpec = {
            val goingForward = targetState.depth >= initialState.depth
            val enter = if (goingForward) {
                slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn()
            } else {
                slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()
            }
            val exit = if (goingForward) {
                slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
            } else {
                slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut()
            }
            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        modifier = modifier.fillMaxSize(),
        contentKey = { entryProvider(it.key).contentKey },
        label = "RenungNavDisplay",
    ) { slot ->
        Box(Modifier.fillMaxSize()) {
            entryProvider(slot.key).Content()
        }
    }
}

/** The rendered destination plus the stack depth it sat at, so pops can be told from pushes. */
private data class NavSlot<T : NavKey>(val key: T, val depth: Int)
