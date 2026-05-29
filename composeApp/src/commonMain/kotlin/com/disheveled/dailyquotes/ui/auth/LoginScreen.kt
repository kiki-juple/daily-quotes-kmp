package com.disheveled.dailyquotes.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disheveled.dailyquotes.ui.components.FilledButton
import com.disheveled.dailyquotes.ui.components.GroupedFormGroup
import com.disheveled.dailyquotes.ui.components.GroupedFormRow
import com.disheveled.dailyquotes.ui.login.LoginViewModel
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.logo_mark
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val submitting = state.isSubmitting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RenungColors.Paper),
    ) {
        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo_mark),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                )
                Text(
                    text = "Selamat datang kembali.",
                    style = RenungTheme.typography.h1.copy(color = RenungColors.Ink),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Satu kalimat untuk memulai hari.",
                    style = RenungTheme.typography.bodySmall.copy(color = RenungColors.Ink3),
                    textAlign = TextAlign.Center,
                )
            }

            GroupedFormGroup {
                GroupedFormRow(
                    label = "Username",
                    value = state.login,
                    onValueChange = viewModel::onLoginChange,
                    placeholder = "namamu",
                    enabled = !submitting,
                )
                GroupedFormRow(
                    label = "Sandi",
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "••••••••",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    isLast = true,
                    enabled = !submitting,
                )
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    style = RenungTheme.typography.caption.copy(color = RenungColors.Error),
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }

            Box {
                FilledButton(
                    text = if (submitting) "" else "Masuk",
                    onClick = { viewModel.submit() },
                    enabled = !submitting,
                )
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp).align(Alignment.Center),
                        color = RenungColors.Cream,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = RenungColors.Ink3)) {
                        append("Belum punya akun? ")
                    }
                    withStyle(
                        SpanStyle(color = RenungColors.Clay, fontWeight = FontWeight.SemiBold),
                    ) {
                        append("Buat akun")
                    }
                },
                style = RenungTheme.typography.bodySmall,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onGoToRegister,
                ),
            )
        }
    }
}
