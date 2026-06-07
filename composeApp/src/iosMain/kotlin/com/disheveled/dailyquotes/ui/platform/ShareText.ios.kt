package com.disheveled.dailyquotes.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
actual fun rememberShareText(): (String) -> Boolean = remember {
    share@{ text ->
        val presenter =
            UIApplication.sharedApplication.keyWindow?.rootViewController?.topPresenter()
                ?: return@share false
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        presenter.presentViewController(controller, animated = true, completion = null)
        true
    }
}

private fun UIViewController.topPresenter(): UIViewController {
    var current = this
    while (current.presentedViewController != null) {
        current = current.presentedViewController!!
    }
    return current
}
