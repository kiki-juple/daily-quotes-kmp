package com.disheveled.dailyquotes.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object RenungShapes {
    val Xs = RoundedCornerShape(6.dp)
    val Sm = RoundedCornerShape(10.dp)
    val Md = RoundedCornerShape(16.dp)
    val Lg = RoundedCornerShape(24.dp)
    val Pill = RoundedCornerShape(999.dp)
}

internal val MaterialShapes = Shapes(
    extraSmall = RenungShapes.Xs,
    small = RenungShapes.Sm,
    medium = RenungShapes.Md,
    large = RenungShapes.Lg,
    extraLarge = RoundedCornerShape(32.dp),
)
