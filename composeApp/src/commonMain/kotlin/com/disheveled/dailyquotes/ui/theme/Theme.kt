package com.disheveled.dailyquotes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

private val RenungLightColors = lightColorScheme(
    primary = RenungColors.Clay,
    onPrimary = RenungColors.Cream,
    primaryContainer = RenungColors.ClayTint,
    onPrimaryContainer = RenungColors.ClayInk,
    secondary = RenungColors.Sage,
    onSecondary = RenungColors.Cream,
    secondaryContainer = RenungColors.SageTint,
    onSecondaryContainer = RenungColors.Ink,
    tertiary = RenungColors.Ink2,
    onTertiary = RenungColors.Cream,
    background = RenungColors.Paper,
    onBackground = RenungColors.Ink,
    surface = RenungColors.Cream,
    onSurface = RenungColors.Ink,
    surfaceVariant = RenungColors.Paper2,
    onSurfaceVariant = RenungColors.Ink2,
    outline = RenungColors.Mist2,
    outlineVariant = RenungColors.Mist,
    error = RenungColors.Error,
    onError = RenungColors.Cream,
)

val LocalRenungTypography: ProvidableCompositionLocal<RenungTypography> =
    staticCompositionLocalOf { error("RenungTypography not provided") }

@Composable
fun DailyQuotesTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val families = rememberRenungFontFamilies()
    val typo = renungTypography(families)

    val materialTypography = Typography(
        displayLarge = typo.display,
        headlineLarge = typo.h1,
        headlineMedium = typo.h1,
        headlineSmall = typo.h2,
        titleLarge = typo.h2,
        titleMedium = typo.h3,
        titleSmall = typo.label,
        bodyLarge = typo.body,
        bodyMedium = typo.body,
        bodySmall = typo.bodySmall,
        labelLarge = typo.button,
        labelMedium = typo.label,
        labelSmall = typo.caption,
    )

    CompositionLocalProvider(LocalRenungTypography provides typo) {
        MaterialTheme(
            colorScheme = RenungLightColors,
            typography = materialTypography,
            shapes = MaterialShapes,
            content = content,
        )
    }
}

object RenungTheme {
    val typography: RenungTypography
        @Composable get() = LocalRenungTypography.current
}
