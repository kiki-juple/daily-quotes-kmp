package com.disheveled.dailyquotes.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.trash
import org.jetbrains.compose.resources.painterResource

@Composable
fun FavoriteRow(
    quote: String,
    author: String,
    date: String,
    onDelete: () -> Unit,
    onUnsave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val offsetX by animateDpAsState(if (open) (-96).dp else 0.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RenungColors.Error),
    ) {
        // Delete action behind
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(96.dp)
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDelete,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.trash),
                    contentDescription = "Hapus",
                    modifier = Modifier.size(22.dp),
                    colorFilter = ColorFilter.tint(RenungColors.Cream),
                )
                Text(
                    text = "Hapus",
                    style = RenungTheme.typography.button.copy(color = RenungColors.Cream, fontSize = androidx.compose.ui.unit.TextUnit(13f, androidx.compose.ui.unit.TextUnitType.Sp)),
                )
            }
        }

        // Foreground row that slides
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .fillMaxWidth()
                .background(RenungColors.Cream, RoundedCornerShape(16.dp))
                .border(1.dp, RenungColors.Mist, RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { open = !open }
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quote,
                        style = RenungTheme.typography.h3.copy(
                            color = RenungColors.Ink,
                            fontSize = androidx.compose.ui.unit.TextUnit(17f, androidx.compose.ui.unit.TextUnitType.Sp),
                            lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp),
                            fontFamily = RenungTheme.typography.display.fontFamily,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "— $author",
                            style = RenungTheme.typography.caption.copy(color = RenungColors.Ink3),
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(RenungColors.Ink4, RoundedCornerShape(50)),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = date,
                            style = RenungTheme.typography.caption.copy(color = RenungColors.Ink3),
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                HeartToggle(
                    saved = true,
                    onToggle = { onUnsave() },
                    size = HeartSize.Small,
                )
            }
        }
    }
}

