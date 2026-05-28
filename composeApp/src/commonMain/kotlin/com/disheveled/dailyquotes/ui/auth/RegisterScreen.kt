package com.disheveled.dailyquotes.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disheveled.dailyquotes.ui.components.BackRow
import com.disheveled.dailyquotes.ui.components.FilledButton
import com.disheveled.dailyquotes.ui.components.GroupedFormGroup
import com.disheveled.dailyquotes.ui.components.GroupedFormRow
import com.disheveled.dailyquotes.ui.register.RegisterViewModel
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val submitting = state.isSubmitting
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RenungColors.Paper),
    ) {
        BackRow(label = "Masuk", onClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Buat akun.",
                    style = RenungTheme.typography.h1.copy(color = RenungColors.Ink),
                )
                Text(
                    text = "Setiap pagi kami pilihkan satu untukmu.",
                    style = RenungTheme.typography.bodySmall.copy(color = RenungColors.Ink3),
                )
            }

            GroupedFormGroup(
                footer = "Kami pakai email untuk mengirimkan kutipan harianmu — tidak ada yang lain.",
            ) {
                GroupedFormRow(
                    label = "Username",
                    value = state.login,
                    onValueChange = viewModel::onLoginChange,
                    placeholder = "namamu",
                    enabled = !submitting,
                )
                GroupedFormRow(
                    label = "Email",
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = "kamu@email.com",
                    keyboardType = KeyboardType.Email,
                    isLast = true,
                    enabled = !submitting,
                )
            }

            GroupedFormGroup(footer = "Minimal 8 karakter.") {
                GroupedFormRow(
                    label = "Sandi",
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "••••••••",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    enabled = !submitting,
                )
                GroupedFormRow(
                    label = "Ulangi",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
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
                    text = if (submitting) "" else "Buat akun",
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
                        append("Sudah punya akun? ")
                    }
                    withStyle(
                        SpanStyle(color = RenungColors.Clay, fontWeight = FontWeight.SemiBold),
                    ) {
                        append("Masuk")
                    }
                },
                style = RenungTheme.typography.bodySmall,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
            )
        }
    }
}
