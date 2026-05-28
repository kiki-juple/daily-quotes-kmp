package com.disheveled.dailyquotes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme

@Composable
fun RenungSnackbar(
    message: String?,
    visible: Boolean,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = visible && message != null,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color(0xEB1E1A17), RoundedCornerShape(14.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message ?: "",
                    style = RenungTheme.typography.bodySmall.copy(color = RenungColors.Cream),
                    modifier = Modifier.weight(1f),
                )
                if (action != null) {
                    Text(
                        text = action,
                        style = RenungTheme.typography.button.copy(
                            color = RenungColors.ClaySoft,
                            fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp),
                        ),
                        modifier = if (onAction != null) Modifier
                            .padding(start = 12.dp)
                            .background(Color.Transparent)
                            .let { it } else Modifier,
                    )
                }
            }
        }
    }
}
