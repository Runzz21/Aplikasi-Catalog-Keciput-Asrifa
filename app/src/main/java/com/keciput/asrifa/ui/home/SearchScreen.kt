package com.keciput.asrifa.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.keciput.asrifa.domain.model.Snack
import com.keciput.asrifa.ui.components.formatRupiah
import com.keciput.asrifa.ui.components.shimmerEffect
import com.keciput.asrifa.ui.components.KeciputSnackbarData
import com.keciput.asrifa.ui.components.KeciputSnackbarHost
import com.keciput.asrifa.ui.components.SnackbarType
import com.keciput.asrifa.ui.components.rememberKeciputSnackbarState
import com.keciput.asrifa.ui.components.ScallopedBottomShape
import com.keciput.asrifa.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    initialQuery: String?,
    onBack: () -> Unit,
    onSnackClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = rememberKeciputSnackbarState()
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf(initialQuery ?: "") }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(300)
            viewModel.search(searchQuery)
        } else if (searchQuery.isEmpty()) {
            viewModel.search("")
        }
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = {},
        topBar = {
            SearchTopBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onBack = onBack,
                onClearQuery = { searchQuery = "" }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (searchQuery.isEmpty()) {
                    // State: Empty Query (History / Suggestions)
                    SearchInitialState(
                        history = uiState.searchHistory,
                        onSuggestionClick = { searchQuery = it },
                        onDeleteHistory = { item ->
                            viewModel.deleteHistoryItem(item)
                            snackbarState.show(
                                KeciputSnackbarData(
                                    message = "\"$item\" dihapus dari riwayat",
                                    actionLabel = "Urungkan",
                                    type = SnackbarType.UNDO
                                )
                            )
                        },
                        onClearHistory = {
                            viewModel.clearHistory()
                            snackbarState.show(
                                KeciputSnackbarData(
                                    message = "Semua riwayat dihapus",
                                    actionLabel = "Urungkan",
                                    type = SnackbarType.UNDO
                                )
                            )
                        }
                    )
                } else {
                    // Results / Loading / Empty / Error
                    SearchContent(
                        uiState = uiState,
                        query = searchQuery,
                        onSnackClick = onSnackClick,
                        onAddToCart = { snack ->
                            viewModel.addToCart(snack)
                            snackbarState.show(
                                KeciputSnackbarData(
                                    message = "${snack.name} ditambahkan ke pesanan!",
                                    type = SnackbarType.SUCCESS,
                                    icon = Icons.Default.ShoppingBag
                                )
                            )
                        },
                        onRetry = { viewModel.search(searchQuery) }
                    )
                }
            }

            // Custom Snackbar overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                KeciputSnackbarHost(
                    state = snackbarState,
                    onAction = { viewModel.undoHistoryRemoval() }
                )
            }
        }
    }
}

@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onClearQuery: () -> Unit
) {
    val scallopedBottom = remember { ScallopedBottomShape() }
    Surface(
        color = CoralDark,
        shape = scallopedBottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(CoralDark, CoralMid)))
                .statusBarsPadding()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 42.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                "Cari snack favoritmu...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.5f))
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            cursorBrush = SolidColor(Color.White),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClearQuery, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Hapus",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchInitialState(
    history: List<String>,
    onSuggestionClick: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Riwayat Pencarian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                TextButton(onClick = onClearHistory) {
                    Text("Hapus Semua", color = CoralMid, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                history.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Ink.copy(alpha = 0.1f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Area Klik untuk Mencari
                            Box(
                                modifier = Modifier
                                    .clickable { onSuggestionClick(item) }
                                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
                            ) {
                                Text(item, style = MaterialTheme.typography.bodyMedium, color = Ink)
                            }
                            
                            // Pembatas tipis visual
                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Ink.copy(alpha = 0.1f)))

                            // Tombol hapus khusus
                            IconButton(
                                onClick = { onDeleteHistory(item) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close, 
                                    contentDescription = "Hapus", 
                                    tint = InkMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            InitialIllustration()
        }
    }
}

@Composable
private fun InitialIllustration() {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = CoralSoft.copy(0.15f), 
            shape = CircleShape, 
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                Icons.Default.Search, 
                null, 
                tint = CoralMid, 
                modifier = Modifier.padding(32.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Cari snack favoritmu", 
            style = MaterialTheme.typography.titleLarge, 
            fontWeight = FontWeight.Bold, 
            color = Ink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Contoh: keciput, rambak, kacang, atau kue", 
            style = MaterialTheme.typography.bodyMedium, 
            color = InkMuted, 
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SearchContent(
    uiState: SearchUiState,
    query: String,
    onSnackClick: (Int) -> Unit,
    onAddToCart: (Snack) -> Unit,
    onRetry: () -> Unit
) {
    val error = uiState.error
    if (error != null) {
        SearchValidationState(
            icon = Icons.Default.ErrorOutline,
            title = "Ups, Terjadi Kesalahan",
            subtitle = error,
            actionText = "Coba Lagi",
            onAction = onRetry
        )
    } else if (uiState.isLoading) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(6) { SearchShimmerItem() }
        }
    } else if (uiState.searchResults.isEmpty()) {
        SearchValidationState(
            icon = Icons.Default.SearchOff,
            title = "Produk tidak ditemukan",
            subtitle = "Tidak ada hasil untuk \"$query\".\nCoba kata kunci lain seperti nama snack atau kategori.",
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.searchResults, key = { it.id }) { snack ->
                SearchSnackCard(
                    snack = snack,
                    onClick = { onSnackClick(snack.id) },
                    onAddToCart = { onAddToCart(snack) }
                )
            }
        }
    }
}

@Composable
fun SearchValidationState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = CoralSoft.copy(0.15f),
            shape = CircleShape,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(32.dp),
                tint = CoralMid
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Ink,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(36.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = CoralDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(52.dp).padding(horizontal = 32.dp)
            ) {
                Text(actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SearchShimmerItem() {
    Column(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = CoralMid.copy(0.1f),
                spotColor = CoralMid.copy(0.1f)
            )
            .background(Color.White, RoundedCornerShape(18.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(135.dp).shimmerEffect())
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).shimmerEffect())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).shimmerEffect())
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.width(80.dp).height(24.dp).shimmerEffect())
        }
    }
}

@Composable
fun SearchSnackCard(snack: Snack, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = CoralMid.copy(0.2f),
                spotColor = CoralMid.copy(0.2f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(snack.imageUrl).size(135).crossfade(true).build(),
                    contentDescription = snack.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                )
                
                if (snack.originalPrice != null) {
                    val discount = ((snack.originalPrice - snack.price) / snack.originalPrice * 100).toInt()
                    Surface(
                        color = CoralMid,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            "-$discount%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .shadow(4.dp, CircleShape),
                    shape = CircleShape,
                    color = Color.White.copy(0.9f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Tambah",
                        tint = CoralMid,
                        modifier = Modifier.padding(6.dp).size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    snack.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    snack.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CoralMid,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    "Rp${snack.price.toInt().formatRupiah()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CoralDark,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
