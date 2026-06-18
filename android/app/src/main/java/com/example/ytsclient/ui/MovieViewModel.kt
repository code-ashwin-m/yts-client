package com.example.ytsclient.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ytsclient.data.BrowseSettings
import com.example.ytsclient.data.Movie
import com.example.ytsclient.data.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class AppTab {
    Browse,
    Favorites,
    Settings
}

data class MovieUiState(
    val tab: AppTab = AppTab.Browse,
    val settings: BrowseSettings = BrowseSettings(),
    val movies: List<Movie> = emptyList(),
    val favoriteMovies: List<Movie> = emptyList(),
    val selectedMovie: Movie? = null,
    val selectedMovieBookmarked: Boolean = false,
    val query: String = "",
    val quality: String = "all",
    val rating: String = "all",
    val genre: String = "all",
    val page: Int = 1,
    val totalMovies: Int = 0,
    val canLoadMore: Boolean = true,
    val filtersVisible: Boolean = true,
    val isLoading: Boolean = false,
    val isLoadingFavorites: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val error: String? = null
)

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)
    private val _state = MutableStateFlow(MovieUiState(settings = repository.settings()))
    val state: StateFlow<MovieUiState> = _state

    init {
        loadFirstPage()
    }

    fun setTab(tab: AppTab) {
        _state.update { it.copy(tab = tab, selectedMovie = null, error = null) }
        if (tab == AppTab.Favorites) {
            loadFavorites()
        }
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun setQuality(quality: String) {
        _state.update { it.copy(quality = quality) }
        loadFirstPage()
    }

    fun setRating(rating: String) {
        _state.update { it.copy(rating = rating) }
        loadFirstPage()
    }

    fun setGenre(genre: String) {
        _state.update { it.copy(genre = genre) }
        loadFirstPage()
    }

    fun applySearch() {
        loadFirstPage()
    }

    fun saveSettings(settings: BrowseSettings) {
        repository.saveSettings(settings)
        _state.update { it.copy(settings = repository.settings(), error = null) }
        loadFirstPage()
    }

    fun toggleFilters() {
        _state.update { it.copy(filtersVisible = !it.filtersVisible) }
    }

    fun hideFilters() {
        _state.update { it.copy(filtersVisible = false) }
    }

    fun setBrowseMode(browseMode: String) {
        val next = copySettings(_state.value.settings).apply {
            this.browseMode = browseMode
        }
        repository.saveSettings(next)
        _state.update { it.copy(settings = repository.settings()) }
    }

    fun changeGridColumns(delta: Int) {
        val current = _state.value.settings
        val next = copySettings(current).apply {
            this.gridColumns = (current.gridColumns + delta).coerceIn(1, 5)
        }
        repository.saveSettings(next)
        _state.update { it.copy(settings = repository.settings()) }
    }

    fun loadFirstPage() {
        loadPage(1, reset = true)
    }

    fun loadNextPage() {
        val current = _state.value
        if (current.isLoading || !current.canLoadMore || current.tab != AppTab.Browse || current.selectedMovie != null) {
            return
        }
        loadPage(current.page + 1, reset = false)
    }

    private fun loadPage(page: Int, reset: Boolean) {
        val current = _state.value
        if (current.isLoading) {
            return
        }
        _state.update {
            it.copy(
                isLoading = true,
                error = null,
                movies = if (reset) emptyList() else it.movies,
                page = if (reset) 1 else it.page
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                repository.browse(current.query, current.quality, current.rating, current.genre, page)
            }.onSuccess { result ->
                _state.update {
                    val nextMovies = if (reset) result.movies else it.movies + result.movies
                    it.copy(
                        movies = nextMovies,
                        page = page,
                        totalMovies = result.movieCount,
                        canLoadMore = nextMovies.size < result.movieCount && result.movies.isNotEmpty(),
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        canLoadMore = false,
                        error = throwable.message ?: "Could not load movies"
                    )
                }
            }
        }
    }

    fun loadFavorites() {
        _state.update { it.copy(isLoadingFavorites = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.favoriteMovies() }
                .onSuccess { movies ->
                    _state.update { it.copy(favoriteMovies = movies, isLoadingFavorites = false) }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoadingFavorites = false,
                            error = throwable.message ?: "Could not load favorites"
                        )
                    }
                }
        }
    }

    fun openMovie(movieId: Int) {
        _state.update { it.copy(isLoadingDetails = true, selectedMovie = null, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val movie = repository.movie(movieId)
                movie to repository.isBookmarked(movieId)
            }.onSuccess { (movie, bookmarked) ->
                _state.update {
                    it.copy(
                        selectedMovie = movie,
                        selectedMovieBookmarked = bookmarked,
                        isLoadingDetails = false
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoadingDetails = false,
                        error = throwable.message ?: "Could not load movie"
                    )
                }
            }
        }
    }

    fun closeMovie() {
        _state.update { it.copy(selectedMovie = null, isLoadingDetails = false) }
    }

    fun toggleBookmark() {
        val movie = _state.value.selectedMovie ?: return
        val next = !_state.value.selectedMovieBookmarked
        viewModelScope.launch(Dispatchers.IO) {
            repository.setBookmarked(movie.id, next)
            _state.update { it.copy(selectedMovieBookmarked = next) }
            if (_state.value.tab == AppTab.Favorites) {
                loadFavorites()
            }
        }
    }

    suspend fun cachedImageFile(url: String): File? = withContext(Dispatchers.IO) {
        runCatching { repository.cachedImage(url) }.getOrNull()
    }

    suspend fun magnetUrl(movie: Movie, torrentIndex: Int): String? = withContext(Dispatchers.IO) {
        val torrent = movie.torrents.getOrNull(torrentIndex) ?: return@withContext null
        runCatching { repository.magnetUrl(movie, torrent) }.getOrNull()
    }

    private fun copySettings(settings: BrowseSettings): BrowseSettings {
        return BrowseSettings().apply {
            baseUrl = settings.baseUrl
            browseMode = settings.browseMode
            gridColumns = settings.gridColumns
            pageSize = settings.pageSize
        }
    }
}
