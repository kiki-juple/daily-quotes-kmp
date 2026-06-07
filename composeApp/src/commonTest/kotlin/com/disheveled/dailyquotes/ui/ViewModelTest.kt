package com.disheveled.dailyquotes.ui

import com.disheveled.dailyquotes.data.repository.AuthRepository
import com.disheveled.dailyquotes.data.repository.FavoritesRepository
import com.disheveled.dailyquotes.data.repository.QuoteRepository
import com.disheveled.dailyquotes.domain.model.Quote
import com.disheveled.dailyquotes.domain.model.User
import com.disheveled.dailyquotes.ui.favorites.FavoritesViewModel
import com.disheveled.dailyquotes.ui.home.HomeViewModel
import com.disheveled.dailyquotes.ui.login.LoginViewModel
import com.disheveled.dailyquotes.ui.register.RegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    @Test
    fun homeLoadsQuoteWhenUserIsRestored() = runViewModelTest {
        val quote = sampleQuote()
        val favorites = FakeFavoritesRepository(initialFavoriteIds = setOf(quote.id))
        val viewModel = HomeViewModel(
            quoteRepository = FakeQuoteRepository(Result.success(quote)),
            favoritesRepository = favorites,
            authRepository = FakeAuthRepository(),
        )

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(quote, viewModel.state.value.quote)
        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun homeToggleFavoriteReportsSuccessAfterRepositoryUpdate() = runViewModelTest {
        val quote = sampleQuote()
        val favorites = FakeFavoritesRepository()
        val viewModel = HomeViewModel(
            quoteRepository = FakeQuoteRepository(Result.success(quote)),
            favoritesRepository = favorites,
            authRepository = FakeAuthRepository(),
        )
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        assertTrue(favorites.isFavorite(quote.id))
        assertEquals("Disimpan ke favorit", viewModel.state.value.actionMessage)

        viewModel.consumeActionMessage()

        assertNull(viewModel.state.value.actionMessage)
    }

    @Test
    fun homeToggleFavoriteReportsFailure() = runViewModelTest {
        val quote = sampleQuote()
        val favorites = FakeFavoritesRepository(addError = IllegalStateException("Database penuh"))
        val viewModel = HomeViewModel(
            quoteRepository = FakeQuoteRepository(Result.success(quote)),
            favoritesRepository = favorites,
            authRepository = FakeAuthRepository(),
        )
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        assertFalse(favorites.isFavorite(quote.id))
        assertEquals("Database penuh", viewModel.state.value.actionMessage)
    }

    @Test
    fun loginValidatesBlankFieldsBeforeCallingRepository() = runViewModelTest {
        val auth = FakeAuthRepository()
        val viewModel = LoginViewModel(auth)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Username dan sandi wajib diisi", viewModel.state.value.errorMessage)
        assertEquals(0, auth.loginCalls)
    }

    @Test
    fun registerValidatesPasswordConfirmationBeforeCallingRepository() = runViewModelTest {
        val auth = FakeAuthRepository()
        val viewModel = RegisterViewModel(auth)

        viewModel.onLoginChange("kiki")
        viewModel.onEmailChange("kiki@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.onConfirmPasswordChange("different")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Sandi tidak cocok", viewModel.state.value.errorMessage)
        assertEquals(0, auth.registerCalls)
    }

    @Test
    fun favoritesObserveAndReportRemoval() = runViewModelTest {
        val quote = sampleQuote()
        val favorites = FakeFavoritesRepository(initialQuotes = listOf(quote))
        val viewModel = FavoritesViewModel(favorites)
        advanceUntilIdle()

        assertEquals(listOf(quote), viewModel.state.value.quotes)

        viewModel.remove(quote.id)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.quotes.isEmpty())
        assertEquals("Dihapus dari favorit", viewModel.state.value.actionMessage)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    try {
        block()
    } finally {
        Dispatchers.resetMain()
    }
}

private fun sampleQuote(): Quote = Quote(
    id = 42,
    body = "Tetap jalan pelan-pelan.",
    author = "Renung",
    favoritesCount = 7,
)

private class FakeQuoteRepository(
    private val result: Result<Quote>,
) : QuoteRepository {
    override suspend fun getQuoteOfTheDay(): Result<Quote> = result
}

private class FakeFavoritesRepository(
    initialQuotes: List<Quote> = emptyList(),
    initialFavoriteIds: Set<Long> = initialQuotes.map { it.id }.toSet(),
    private val addError: Throwable? = null,
    private val removeError: Throwable? = null,
) : FavoritesRepository {

    private val favoriteQuotes = MutableStateFlow(initialQuotes)
    private val favoriteIds = MutableStateFlow(initialFavoriteIds)

    override fun observeFavorites(): Flow<List<Quote>> = favoriteQuotes

    override fun observeIsFavorite(quoteId: Long): Flow<Boolean> =
        favoriteIds.map { quoteId in it }

    override suspend fun refreshFavorites() = Unit

    override suspend fun add(quote: Quote) {
        addError?.let { throw it }
        favoriteIds.value += quote.id
        favoriteQuotes.value = (listOf(quote) + favoriteQuotes.value)
            .distinctBy { it.id }
    }

    override suspend fun remove(quoteId: Long) {
        removeError?.let { throw it }
        favoriteIds.value -= quoteId
        favoriteQuotes.value = favoriteQuotes.value.filterNot { it.id == quoteId }
    }

    fun isFavorite(quoteId: Long): Boolean = quoteId in favoriteIds.value
}

private class FakeAuthRepository(
    initialUser: User? = User(login = "kiki", email = "kiki@example.com", userToken = "token"),
    private val loginResult: Result<User> = Result.success(
        User(login = "kiki", email = "kiki@example.com", userToken = "token"),
    ),
    private val registerResult: Result<User> = loginResult,
) : AuthRepository {

    private val user = MutableStateFlow(initialUser)
    override val currentUser: StateFlow<User?> = user

    var loginCalls = 0
        private set
    var registerCalls = 0
        private set

    override suspend fun login(login: String, password: String): Result<User> {
        loginCalls += 1
        loginResult.onSuccess { user.value = it }
        return loginResult
    }

    override suspend fun register(login: String, email: String, password: String): Result<User> {
        registerCalls += 1
        registerResult.onSuccess { user.value = it }
        return registerResult
    }

    override fun logout() {
        user.value = null
    }
}
