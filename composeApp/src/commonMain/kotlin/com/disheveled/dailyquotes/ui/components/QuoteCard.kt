package com.disheveled.dailyquotes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.share
import dailyquotes.composeapp.generated.resources.sun
import org.jetbrains.compose.resources.painterResource

@Composable
fun QuoteCard(
    quote: String,
    author: String,
    eyebrow: String,
    saved: Boolean,
    onToggleSave: (Boolean) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RenungColors.Cream, RoundedCornerShape(24.dp))
            .border(1.dp, RenungColors.Mist, RoundedCornerShape(24.dp))
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.sun),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(RenungColors.Clay),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = eyebrow.uppercase(),
                style = RenungTheme.typography.eyebrow.copy(color = RenungColors.Ink3),
            )
        }

        Text(
            text = quote,
            style = RenungTheme.typography.display.copy(
                fontSize = androidx.compose.ui.unit.TextUnit(26f, androidx.compose.ui.unit.TextUnitType.Sp),
                lineHeight = androidx.compose.ui.unit.TextUnit(32f, androidx.compose.ui.unit.TextUnitType.Sp),
            ),
            color = RenungColors.Ink,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "— $author",
                style = RenungTheme.typography.attribution.copy(color = RenungColors.Ink3),
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                RenungIconButton(
                    icon = Res.drawable.share,
                    contentDescription = "Bagikan",
                    onClick = onShare,
                    tint = RenungColors.Ink2,
                )
                Spacer(Modifier.width(4.dp))
                HeartToggle(
                    saved = saved,
                    onToggle = onToggleSave,
                    size = HeartSize.Small,
                )
            }
        }
    }
}
