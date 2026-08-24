package com.disheveled.dailyquotes.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.disheveled.dailyquotes.ui.auth.LoginContent
import com.disheveled.dailyquotes.ui.auth.RegisterContent
import com.disheveled.dailyquotes.ui.login.LoginUiState
import com.disheveled.dailyquotes.ui.register.RegisterUiState
import com.disheveled.dailyquotes.ui.theme.DailyQuotesTheme

// Previews live in :androidApp rather than next to the composables in :composeApp because
// rendering them needs `compose.uiTooling` on the runtime classpath. The KMP library plugin has
// no per-variant source sets, so adding it to :composeApp would ship an exported PreviewActivity
// in the release APK. Here it stays a debugImplementation dependency.

@Composable
private fun LoginPreview(state: LoginUiState) {
    DailyQuotesTheme {
        LoginContent(
            state = state,
            onGoToRegister = {},
            onLoginChange = {},
            onPasswordChange = {},
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun LoginContentEmptyPreview() {
    LoginPreview(LoginUiState())
}

@Preview
@Composable
private fun LoginContentFilledPreview() {
    LoginPreview(LoginUiState(login = "kiki", password = "rahasia"))
}

@Preview
@Composable
private fun LoginContentSubmittingPreview() {
    LoginPreview(
        LoginUiState(login = "kiki", password = "rahasia", isSubmitting = true),
    )
}

@Preview
@Composable
private fun LoginContentErrorPreview() {
    LoginPreview(
        LoginUiState(login = "kiki", errorMessage = "Username dan sandi wajib diisi"),
    )
}

@Composable
private fun RegisterPreview(state: RegisterUiState) {
    DailyQuotesTheme {
        RegisterContent(
            state = state,
            onBack = {},
            onLoginChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun RegisterContentEmptyPreview() {
    RegisterPreview(RegisterUiState())
}

@Preview
@Composable
private fun RegisterContentFilledPreview() {
    RegisterPreview(
        RegisterUiState(
            login = "kiki",
            email = "kiki@email.com",
            password = "rahasia123",
            confirmPassword = "rahasia123",
        ),
    )
}

@Preview
@Composable
private fun RegisterContentSubmittingPreview() {
    RegisterPreview(
        RegisterUiState(
            login = "kiki",
            email = "kiki@email.com",
            password = "rahasia123",
            confirmPassword = "rahasia123",
            isSubmitting = true,
        ),
    )
}

@Preview
@Composable
private fun RegisterContentErrorPreview() {
    RegisterPreview(
        RegisterUiState(
            login = "kiki",
            email = "kiki@email.com",
            password = "rahasia123",
            confirmPassword = "rahasia",
            errorMessage = "Sandi tidak cocok",
        ),
    )
}
