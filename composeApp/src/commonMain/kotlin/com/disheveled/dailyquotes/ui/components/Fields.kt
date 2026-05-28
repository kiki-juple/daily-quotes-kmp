package com.disheveled.dailyquotes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: DrawableResource? = null,
    trailingIcon: DrawableResource? = null,
    onTrailingClick: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPassword: Boolean = false,
    error: String? = null,
    hint: String? = null,
    enabled: Boolean = true,
) {
    val transformation = if (isPassword) PasswordVisualTransformation() else visualTransformation

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = transformation,
            isError = error != null,
            shape = RoundedCornerShape(10.dp),
            leadingIcon = leadingIcon?.let {
                {
                    Image(
                        painter = painterResource(it),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(RenungColors.Ink3),
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = onTrailingClick != null,
                            ) { onTrailingClick?.invoke() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(it),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(RenungColors.Ink3),
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = RenungColors.Cream,
                unfocusedContainerColor = RenungColors.Cream,
                disabledContainerColor = RenungColors.Cream,
                errorContainerColor = RenungColors.Cream,
                focusedBorderColor = RenungColors.Clay,
                unfocusedBorderColor = RenungColors.Mist2,
                disabledBorderColor = RenungColors.Mist,
                errorBorderColor = RenungColors.Error,
                cursorColor = RenungColors.Clay,
                focusedLabelColor = RenungColors.Clay,
                unfocusedLabelColor = RenungColors.Ink3,
                focusedTextColor = RenungColors.Ink,
                unfocusedTextColor = RenungColors.Ink,
                disabledTextColor = RenungColors.Ink2,
                focusedLeadingIconColor = RenungColors.Clay,
                unfocusedLeadingIconColor = RenungColors.Ink3,
            ),
            textStyle = RenungTheme.typography.body,
        )

        if (error != null || hint != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = error ?: hint!!,
                style = RenungTheme.typography.caption.copy(
                    color = if (error != null) RenungColors.Error else RenungColors.Ink3,
                ),
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

// ============= Grouped form (iOS-style) =============

@Composable
fun GroupedFormGroup(
    modifier: Modifier = Modifier,
    footer: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(RenungColors.Cream)
                .border(1.dp, RenungColors.Mist, RoundedCornerShape(14.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
        if (footer != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = footer,
                style = RenungTheme.typography.caption.copy(color = RenungColors.Ink3),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
fun GroupedFormRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isLast: Boolean = false,
    enabled: Boolean = true,
) {
    val typo = RenungTheme.typography
    Box(modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = typo.label.copy(color = RenungColors.Ink2),
                modifier = Modifier.width(92.dp),
            )

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = typo.body.copy(color = RenungColors.Ink4, textAlign = TextAlign.End),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = typo.body.copy(color = RenungColors.Ink, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    cursorBrush = SolidColor(RenungColors.Clay),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(RenungColors.Mist)
                    .align(Alignment.BottomCenter),
            )
        }
    }
}
