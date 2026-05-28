package com.disheveled.dailyquotes.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.disheveled.dailyquotes.ui.theme.RenungColors
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.heart
import dailyquotes.composeapp.generated.resources.heart_fill
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

enum class HeartSize(val container: Int, val icon: Int) {
    Large(64, 28),
    Small(44, 22),
}

@Composable
fun HeartToggle(
    saved: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: HeartSize = HeartSize.Small,
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val containerDp = size.container.dp
    val iconDp = size.icon.dp

    Box(
        modifier = modifier
            .size(containerDp)
            .clip(CircleShape)
            .background(if (saved) RenungColors.ClayTint else RenungColors.Cream)
            .then(if (!saved) Modifier.border(1.dp, RenungColors.Mist, CircleShape) else Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle(!saved)
                scope.launch {
                    scale.snapTo(1f)
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = keyframes {
                            durationMillis = 360
                            1f at 0 using LinearEasing
                            1.22f at 90
                            0.92f at 180
                            1.06f at 270
                            1f at 360
                        },
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(if (saved) Res.drawable.heart_fill else Res.drawable.heart),
            contentDescription = if (saved) "Tersimpan" else "Simpan",
            modifier = Modifier.size(iconDp).scale(scale.value),
            colorFilter = ColorFilter.tint(if (saved) RenungColors.Clay else RenungColors.Ink2),
        )
    }
}
