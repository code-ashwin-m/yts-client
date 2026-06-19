package com.example.ytsclient

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ytsclient.data.BrowseSettings
import com.example.ytsclient.data.Movie
import com.example.ytsclient.ui.AppTab
import com.example.ytsclient.ui.MovieUiState
import com.example.ytsclient.ui.MovieViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = androidx.compose.material3.darkColorScheme(
                    primary = Color(0xFF66D4A3),
                    secondary = Color(0xFFE0C56E),
                    tertiary = Color(0xFF91C8F6),
                    surface = Color(0xFF121715),
                    background = Color(0xFF0B0F0E),
                    surfaceVariant = Color(0xFF1B2420)
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    YtsApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YtsApp(viewModel: MovieViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.selectedMovie?.title ?: titleFor(state.tab)) },
                navigationIcon = {
                    if (state.selectedMovie != null || state.isLoadingDetails) {
                        IconButton(onClick = viewModel::closeMovie) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    TopBarActions(state, viewModel)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .mainPageSwipeNavigation(state, viewModel)
        ) {
            val selectedMovie = state.selectedMovie
            when {
                state.isLoadingDetails -> CenterMessage(loading = true, text = "Loading movie")
                selectedMovie != null -> MovieDetailsScreen(
                    movie = selectedMovie,
                    bookmarked = state.selectedMovieBookmarked,
                    viewModel = viewModel
                )
                state.tab == AppTab.Browse -> BrowseScreen(state, viewModel)
                state.tab == AppTab.Favorites -> FavoritesScreen(state, viewModel)
                state.tab == AppTab.Settings -> SettingsScreen(state, viewModel)
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TopBarActions(state: MovieUiState, viewModel: MovieViewModel) {
    if (state.selectedMovie != null) {
        IconButton(onClick = viewModel::toggleBookmark) {
            Icon(
                if (state.selectedMovieBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = "Bookmark"
            )
        }
        return
    }

    if (state.tab == AppTab.Browse) {
        IconButton(onClick = viewModel::toggleFilters) {
            Icon(Icons.Filled.FilterList, contentDescription = if (state.filtersVisible) "Hide filters" else "Show filters")
        }
        if (state.settings.browseMode == BrowseSettings.MODE_GRID) {
            IconButton(onClick = { viewModel.changeGridColumns(-1) }) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease columns")
            }
            Text("${state.settings.gridColumns}", fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { viewModel.changeGridColumns(1) }) {
                Icon(Icons.Filled.Add, contentDescription = "Increase columns")
            }
        }
        IconButton(
            onClick = {
                val nextMode = if (state.settings.browseMode == BrowseSettings.MODE_GRID) {
                    BrowseSettings.MODE_LIST
                } else {
                    BrowseSettings.MODE_GRID
                }
                viewModel.setBrowseMode(nextMode)
            }
        ) {
            Icon(
                if (state.settings.browseMode == BrowseSettings.MODE_GRID) Icons.Filled.ViewList else Icons.Filled.GridView,
                contentDescription = "Toggle list or grid"
            )
        }
    }

    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                expanded = false
                viewModel.setTab(AppTab.Settings)
            }
        )
    }
}

private fun Modifier.mainPageSwipeNavigation(state: MovieUiState, viewModel: MovieViewModel): Modifier {
    if (state.selectedMovie != null || state.isLoadingDetails) {
        return this
    }
    return pointerInput(state.tab) {
        var dragDistance = 0f
        detectHorizontalDragGestures(
            onDragEnd = {
                if (dragDistance > 90f) {
                    viewModel.setTab(AppTab.Favorites)
                } else if (dragDistance < -90f) {
                    viewModel.setTab(AppTab.Browse)
                }
                dragDistance = 0f
            },
            onDragCancel = { dragDistance = 0f },
            onHorizontalDrag = { _, dragAmount ->
                dragDistance += dragAmount
            }
        )
    }
}

@Composable
private fun BrowseScreen(state: MovieUiState, viewModel: MovieViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (state.filtersVisible) {
            SearchAndFilters(state, viewModel)
        }
        if (state.movies.isEmpty() && state.isLoading) {
            CenterMessage(loading = true, text = "Loading movies")
        } else if (state.movies.isEmpty()) {
            CenterMessage(loading = false, text = "No movies found")
        } else if (state.settings.browseMode == BrowseSettings.MODE_LIST) {
            MovieList(
                movies = state.movies,
                isLoadingMore = state.isLoading,
                onOpen = viewModel::openMovie,
                onNearBottom = viewModel::loadNextPage,
                onScrollDown = {
                    if (state.filtersVisible) viewModel.hideFilters()
                },
                viewModel = viewModel
            )
        } else {
            MovieGrid(
                movies = state.movies,
                columns = state.settings.gridColumns,
                isLoadingMore = state.isLoading,
                onOpen = viewModel::openMovie,
                onNearBottom = viewModel::loadNextPage,
                onScrollDown = {
                    Log.d("Main", "MovieGrid -> onScrollDown")
                    if (state.filtersVisible) viewModel.hideFilters()
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun SearchAndFilters(state: MovieUiState, viewModel: MovieViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Search") }
            )
            IconButton(onClick = viewModel::applySearch) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }

        DropdownSelector("Quality", state.quality, listOf("all", "480p", "720p", "1080p", "1080p.x265", "2160p", "3D"), viewModel::setQuality)
        DropdownSelector("Rating", state.rating, listOf("all", "9", "8", "7", "6", "5", "4", "3", "2", "1"), viewModel::setRating)
        DropdownSelector(
            "Genre",
            state.genre,
            listOf(
                "all", "action", "adventure", "animation", "biography", "comedy", "crime",
                "documentary", "drama", "family", "fantasy", "film-noir", "history",
                "horror", "music", "mystery", "romance", "sci-fi", "sport", "thriller",
                "war", "western"
            ),
            viewModel::setGenre
        )
    }
}

@Composable
private fun DropdownSelector(label: String, selected: String, values: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: ${optionLabel(selected)}", modifier = Modifier.weight(1f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(
                    text = { Text(optionLabel(value)) },
                    onClick = {
                        expanded = false
                        onSelect(value)
                    }
                )
            }
        }
    }
}

@Composable
private fun MovieList(
    movies: List<Movie>,
    isLoadingMore: Boolean,
    onOpen: (Int) -> Unit,
    onNearBottom: () -> Unit,
    onScrollDown: () -> Unit,
    viewModel: MovieViewModel
) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState, onScrollDown) {
        var previousPosition = 0
        snapshotFlow {
            listState.firstVisibleItemIndex * 100000 + listState.firstVisibleItemScrollOffset
        }.collect { position ->
            if (position > previousPosition) {
                onScrollDown()
            }
            previousPosition = position
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(movies, key = { _, movie -> movie.id }) { index, movie ->
            if (index >= movies.lastIndex - 4) {
                LaunchedEffect(movies.size) { onNearBottom() }
            }
            MovieListRow(movie, onOpen, viewModel)
        }
        if (isLoadingMore) {
            item { LoadingRow() }
        }
    }
}

@Composable
private fun MovieGrid(
    movies: List<Movie>,
    columns: Int,
    isLoadingMore: Boolean,
    onOpen: (Int) -> Unit,
    onNearBottom: () -> Unit,
    onScrollDown: () -> Unit,
    viewModel: MovieViewModel
) {
    val gridState = rememberLazyGridState()
    LaunchedEffect(gridState, onScrollDown) {
        var previousPosition = 0
        snapshotFlow {
            gridState.firstVisibleItemIndex * 100000 + gridState.firstVisibleItemScrollOffset
        }.collect { position ->
            if (position > previousPosition) {
                onScrollDown()
            }
            previousPosition = position
        }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns.coerceIn(1, 5)),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(movies, key = { _, movie -> movie.id }) { index, movie ->
            if (index >= movies.lastIndex - (columns * 2)) {
                LaunchedEffect(movies.size) { onNearBottom() }
            }
            MovieGridCard(movie, onOpen, viewModel)
        }
        if (isLoadingMore) {
            item { LoadingRow() }
        }
    }
}

@Composable
private fun MovieGridCard(movie: Movie, onOpen: (Int) -> Unit, viewModel: MovieViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(movie.id) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f).background(Color.Black)) {
                CachedImage(
                    url = movie.mediumCoverImage,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = String.format("%.1f", movie.rating),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color(0xAA1E7F5C), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(movie.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text("${movie.year}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MovieListRow(movie: Movie, onOpen: (Int) -> Unit, viewModel: MovieViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth().height(134.dp).clickable { onOpen(movie.id) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            CachedImage(
                url = movie.mediumCoverImage,
                viewModel = viewModel,
                modifier = Modifier.width(92.dp).fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(12.dp).weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(movie.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text("${movie.year} - ${String.format("%.1f", movie.rating)}/10")
                if (movie.language.isNotBlank()) {
                    Text(movie.language.uppercase(), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun FavoritesScreen(state: MovieUiState, viewModel: MovieViewModel) {
    when {
        state.isLoadingFavorites -> CenterMessage(loading = true, text = "Loading favorites")
        state.favoriteMovies.isEmpty() -> CenterMessage(loading = false, text = "No bookmarks yet")
        else -> MovieList(
            movies = state.favoriteMovies,
            isLoadingMore = false,
            onOpen = viewModel::openMovie,
            onNearBottom = {},
            onScrollDown = {},
            viewModel = viewModel
        )
    }
}

@Composable
private fun SettingsScreen(state: MovieUiState, viewModel: MovieViewModel) {
    var baseUrl by remember(state.settings.baseUrl) { mutableStateOf(state.settings.baseUrl) }
    var pageSize by remember(state.settings.pageSize) { mutableStateOf(state.settings.pageSize) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API base URL") },
            singleLine = true
        )

        SettingSlider("Page size", pageSize, 10, 50) { pageSize = it }

        Button(
            onClick = {
                val next = BrowseSettings().apply {
                    this.baseUrl = baseUrl
                    this.browseMode = state.settings.browseMode
                    this.gridColumns = state.settings.gridColumns
                    this.pageSize = pageSize
                }
                viewModel.saveSettings(next)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save settings")
        }
    }
}

@Composable
private fun SettingSlider(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("$value")
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt().coerceIn(min, max)) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = (max - min - 1).coerceAtLeast(0)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MovieDetailsScreen(
    movie: Movie,
    bookmarked: Boolean,
    viewModel: MovieViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CachedImage(
                url = movie.mediumCoverImage,
                viewModel = viewModel,
                modifier = Modifier
                    .width(130.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(movie.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${movie.year} - ${String.format("%.1f", movie.rating)}/10")
                Text("${movie.likeCount} likes")
                Text(movie.genres.joinToString(" - "), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = viewModel::toggleBookmark) {
                    Icon(
                        if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (bookmarked) "Bookmarked" else "Bookmark")
                }
            }
        }

        if (movie.torrents.isNotEmpty()) {
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // FlowRow naturally places elements side-by-side and wraps them if they exceed the width
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                movie.torrents.forEachIndexed { index, torrent ->
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                viewModel.magnetUrl(movie, index)?.let { magnet ->
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Magnet URL", magnet))
                                    Toast.makeText(context, "Download URL copied", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Download")
                        Spacer(Modifier.width(8.dp))
                        Text(torrent.label(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Text("Plot Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(movie.summary.ifBlank { "No summary available." })

        if (movie.cast.isNotEmpty()) {
            Text("Cast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            movie.cast.forEach { member ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CachedImage(
                        url = member.smallImageUrl,
                        viewModel = viewModel,
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(21.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text("${member.name} as ${member.characterName}")
                }
            }
        }

        // Similar movies
        SimilarMovies(movie, viewModel , onOpen = viewModel::openMovie)
    }
}

@Composable
fun SimilarMovies(movie: Movie, viewModel: MovieViewModel, onOpen: (Int) -> Unit) {
    // 1. Fetch suggestions when the movie ID changes (safe from recomposition loops)
    LaunchedEffect(key1 = movie.id) {
        viewModel.getSimilarMovies(movie.id)
    }

    // 2. Safely collect the StateFlow from the ViewModel
    val state by viewModel.state.collectAsState()
    val movies: List<Movie> = state.suggestedMovies;

    if (movies.isNotEmpty()){
        LazyHorizontalGrid(
            rows = GridCells.Fixed(1),
            // 3. IMPORTANT: LazyHorizontalGrid must have a specified height constraint to render correctly!
            modifier = Modifier.fillMaxWidth().height(320.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = movies,
                key = { it.id }
            ) { suggestedMovie ->
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight()
                ) {
                    MovieGridCard(
                        movie = suggestedMovie,
                        onOpen = onOpen,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

}

@Composable
private fun CachedImage(
    url: String?,
    viewModel: MovieViewModel,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var bitmapPath by remember(url) { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        bitmapPath = if (url.isNullOrBlank()) null else viewModel.cachedImageFile(url)?.absolutePath
    }

    val bitmap = remember(bitmapPath) {
        bitmapPath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
    }

    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier, contentScale = contentScale)
    } else {
        Box(
            modifier = modifier.background(Color(0xFFE1E9E5)),
            contentAlignment = Alignment.Center
        ) {
            Text("No image", style = MaterialTheme.typography.labelSmall, color = Color(0xFF52635D))
        }
    }
}

@Composable
private fun CenterMessage(loading: Boolean, text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (loading) {
                CircularProgressIndicator()
            }
            Text(text)
        }
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(10.dp))
        Text("Loading more")
    }
}

private fun titleFor(tab: AppTab): String {
    return when (tab) {
        AppTab.Browse -> "Browse Movies"
        AppTab.Favorites -> "Favorites"
        AppTab.Settings -> "Settings"
    }
}

private fun optionLabel(value: String): String {
    return if (value == "all") "All" else value.uppercase()
}
