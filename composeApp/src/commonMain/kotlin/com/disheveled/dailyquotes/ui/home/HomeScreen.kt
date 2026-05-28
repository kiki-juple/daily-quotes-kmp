package com.disheveled.dailyquotes.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disheveled.dailyquotes.ui.components.FilledButton
import com.disheveled.dailyquotes.ui.components.QuoteCard
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme
import com.disheveled.dailyquotes.ui.util.todayInIndonesian
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.calendar
import dailyquotes.composeapp.generated.resources.sparkle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onShowToast: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RenungColors.Paper),
    ) {
        when {
            state.isLoading && state.quote == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RenungColors.Clay)
                }
            }

            state.errorMessage != null && state.quote == null -> {
                ErrorBlock(
                    message = state.errorMessage!!,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.quote != null -> {
                val quote = state.quote!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    // Header row: date + greeting + avatar
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = remember { todayInIndonesian() },
                                style = RenungTheme.typography.caption.copy(color = RenungColors.Ink3),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = state.user?.login?.let { "Halo, $it." } ?: "Halo.",
                                style = RenungTheme.typography.h1.copy(
                                    color = RenungColors.Ink,
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp,
                                ),
                            )
                        }
                        Avatar(
                            label = state.user?.login?.firstOrNull()?.uppercase() ?: "?",
                            onClick = { showLogoutDialog = true },
                        )
                    }

                    QuoteCard(
                        eyebrow = "Kutipan hari ini",
                        quote = "“${quote.body.trim()}”",
                        author = quote.author,
                        saved = state.isFavorite,
                        onToggleSave = { saved ->
                            viewModel.toggleFavorite()
                            onShowToast(
                                if (saved) "Disimpan ke favorit" else "Dihapus dari favorit",
                            )
                        },
                        onShare = { onShowToast("Tautan disalin") },
                    )

                    // Streak strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RenungColors.Paper2, RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.sparkle),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(RenungColors.Ink2),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${quote.favoritesCount} orang menyimpan ini",
                                style = RenungTheme.typography.label.copy(color = RenungColors.Ink),
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Kebiasaan kecil, sebuah kalimat sehari.",
                                style = RenungTheme.typography.caption.copy(color = RenungColors.Ink3),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.calendar),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                colorFilter = ColorFilter.tint(RenungColors.Ink3),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Kembali besok untuk yang baru.",
                                style = RenungTheme.typography.bodySmall.copy(color = RenungColors.Ink3),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Avatar(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(RenungColors.ClayTint, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = RenungTheme.typography.button.copy(color = RenungColors.ClayInk),
        )
    }
}

@Composable
private fun LogoutConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Keluar dari akun?",
                style = RenungTheme.typography.h2.copy(color = RenungColors.Ink),
            )
        },
        text = {
            Text(
                text = "Kamu perlu masuk lagi untuk melihat kutipan harianmu.",
                style = RenungTheme.typography.body.copy(color = RenungColors.Ink2),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Keluar",
                    style = RenungTheme.typography.button.copy(color = RenungColors.Clay),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Batal",
                    style = RenungTheme.typography.button.copy(color = RenungColors.Ink3),
                )
            }
        },
        containerColor = RenungColors.Cream,
    )
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Gagal memuat.",
            style = RenungTheme.typography.h1.copy(color = RenungColors.Ink),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = RenungTheme.typography.bodySmall.copy(color = RenungColors.Ink3),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        FilledButton(text = "Coba lagi", onClick = onRetry, fullWidth = false)
    }
}
