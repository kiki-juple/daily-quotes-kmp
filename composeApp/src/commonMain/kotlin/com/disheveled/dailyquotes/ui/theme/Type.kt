package com.disheveled.dailyquotes.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.newsreader
import dailyquotes.composeapp.generated.resources.newsreader_italic
import dailyquotes.composeapp.generated.resources.plus_jakarta_sans
import org.jetbrains.compose.resources.Font

@Composable
fun rememberRenungFontFamilies(): RenungFontFamilies {
    val newsreader = FontFamily(
        Font(Res.font.newsreader, weight = FontWeight.Normal, style = FontStyle.Normal),
        Font(Res.font.newsreader, weight = FontWeight.Medium, style = FontStyle.Normal),
        Font(Res.font.newsreader, weight = FontWeight.SemiBold, style = FontStyle.Normal),
        Font(Res.font.newsreader, weight = FontWeight.Bold, style = FontStyle.Normal),
        Font(Res.font.newsreader_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(Res.font.newsreader_italic, weight = FontWeight.Medium, style = FontStyle.Italic),
        Font(Res.font.newsreader_italic, weight = FontWeight.SemiBold, style = FontStyle.Italic),
    )
    val plusJakarta = FontFamily(
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.Normal),
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.Medium),
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.SemiBold),
        Font(Res.font.plus_jakarta_sans, weight = FontWeight.Bold),
    )
    return RenungFontFamilies(display = newsreader, ui = plusJakarta)
}

data class RenungFontFamilies(
    val display: FontFamily,
    val ui: FontFamily,
)

data class RenungTypography(
    val display: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val caption: TextStyle,
    val eyebrow: TextStyle,
    val attribution: TextStyle,
    val button: TextStyle,
    val label: TextStyle,
)

fun renungTypography(families: RenungFontFamilies): RenungTypography {
    val display = families.display
    val ui = families.ui
    return RenungTypography(
        display = TextStyle(
            fontFamily = display,
            fontSize = 40.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.01).em,
        ),
        h1 = TextStyle(
            fontFamily = display,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.015).em,
        ),
        h2 = TextStyle(
            fontFamily = ui,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.01).em,
        ),
        h3 = TextStyle(
            fontFamily = ui,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        body = TextStyle(
            fontFamily = ui,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal,
        ),
        bodySmall = TextStyle(
            fontFamily = ui,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
        ),
        caption = TextStyle(
            fontFamily = ui,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
        ),
        eyebrow = TextStyle(
            fontFamily = ui,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.16.em,
        ),
        attribution = TextStyle(
            fontFamily = ui,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.02.em,
        ),
        button = TextStyle(
            fontFamily = ui,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        label = TextStyle(
            fontFamily = ui,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        ),
    )
}
