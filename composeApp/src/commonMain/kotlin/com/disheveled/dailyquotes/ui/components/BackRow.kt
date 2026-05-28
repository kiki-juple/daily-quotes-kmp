package com.disheveled.dailyquotes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.arrow_left
import org.jetbrains.compose.resources.painterResource

@Composable
fun BackRow(
    label: String = "Kembali",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(start = 4.dp, top = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.arrow_left),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(RenungColors.Clay),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = RenungTheme.typography.label.copy(color = RenungColors.Clay),
            )
        }
    }
}
