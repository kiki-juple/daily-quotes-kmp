package com.disheveled.dailyquotes.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.disheveled.dailyquotes.ui.components.EmptyState
import com.disheveled.dailyquotes.ui.components.FavoriteRow
import com.disheveled.dailyquotes.ui.theme.RenungColors
import com.disheveled.dailyquotes.ui.theme.RenungTheme
import dailyquotes.composeapp.generated.resources.Res
import dailyquotes.composeapp.generated.resources.heart
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(
    onBack: () -> Unit = {},
    onShowToast: (String) -> Unit = {},
    viewModel: FavoritesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RenungColors.Paper),
    ) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RenungColors.Clay)
                }
            }

            state.quotes.isEmpty() -> {
                EmptyState(
                    icon = Res.drawable.heart,
                    title = "Belum ada yang kamu simpan.",
                    body = "Quote yang kamu sukai akan muncul di sini. Buka beranda dan ketuk hati.",
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        top = 8.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Favorit",
                                style = RenungTheme.typography.h1.copy(color = RenungColors.Ink),
                            )
                            Text(
                                text = "${state.quotes.size} kutipan",
                                style = RenungTheme.typography.bodySmall.copy(color = RenungColors.Ink3),
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    items(state.quotes, key = { it.id }) { quote ->
                        FavoriteRow(
                            quote = "“${quote.body.trim()}”",
                            author = quote.author,
                            date = "Tersimpan",
                            onDelete = {
                                viewModel.remove(quote.id)
                                onShowToast("Dihapus dari favorit")
                            },
                            onUnsave = {
                                viewModel.remove(quote.id)
                                onShowToast("Dihapus dari favorit")
                            },
                        )
                    }
                }
            }
        }
    }
}
