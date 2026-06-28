package com.keciput.asrifa.ui.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.keciput.asrifa.R
import com.keciput.asrifa.domain.model.Snack
import com.keciput.asrifa.ui.components.formatRupiah
import com.keciput.asrifa.ui.components.KeciputSnackbarData
import com.keciput.asrifa.ui.components.KeciputSnackbarHost
import com.keciput.asrifa.ui.components.KeciputSnackbarState

import com.keciput.asrifa.ui.components.SnackbarType
import com.keciput.asrifa.ui.components.rememberKeciputSnackbarState
import com.keciput.asrifa.ui.components.ScallopedBottomShape
import com.keciput.asrifa.ui.components.sesamePattern
import com.keciput.asrifa.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

// ── Shimmer Effect Modifier ──────────────────
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    background(brush)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CatalogScreen(
    initialCategory: String? = null,
    initialFilterType: String? = null,
    onBackClick: () -> Unit,
    onSnackClick: (Int) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = rememberKeciputSnackbarState()
    val scope = rememberCoroutineScope()
    var pendingUndoHistory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialCategory, initialFilterType) {
        if (!initialCategory.isNullOrBlank()) {
            viewModel.onCategorySelected(initialCategory)
        }
        if (!initialFilterType.isNullOrBlank()) {
            viewModel.onFilterTypeSelected(initialFilterType)
        }
    }

    Scaffold(
        containerColor = Cream,
        snackbarHost = {},
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())) {

            val headerContent = @Composable {
                CatalogHeader(
                    query              = uiState.searchQuery,
                    onQueryChange      = viewModel::onSearchQuery,
                    onSearchSubmit     = { viewModel.onSearchSubmit(it) },
                    searchHistory      = uiState.searchHistory,
                    onHistoryClick     = {
                        viewModel.onSearchQuery(it)
                        viewModel.onSearchSubmit(it)
                    },
                    onRemoveHistory    = { query ->
                        viewModel.removeSearchHistory(query)
                        pendingUndoHistory = query
                        snackbarState.show(
                            KeciputSnackbarData(
                                message = "Riwayat \"$query\" dihapus",
                                actionLabel = "Urungkan",
                                type = SnackbarType.UNDO,
                                icon = androidx.compose.material.icons.Icons.Default.History
                            )
                        )
                    },
                    onClearHistory     = {
                        viewModel.clearSearchHistory()
                        snackbarState.show(
                            KeciputSnackbarData(
                                message = "Semua riwayat pencarian dihapus",
                                type = SnackbarType.INFO,
                                icon = androidx.compose.material.icons.Icons.Default.DeleteSweep
                            )
                        )
                    },
                    categories         = uiState.categories,
                    selectedCategory   = uiState.selectedCategory,
                    onCategorySelected = viewModel::onCategorySelected,
                    selectedSort       = uiState.sortOption,
                    onSortChange       = viewModel::onSortChange,
                    viewMode           = uiState.viewMode,
                    onViewToggle       = viewModel::onViewToggle
                )
            }

            Crossfade(targetState = uiState.viewMode, label = "CatalogViewMode") { mode ->
                when (mode) {
                    ViewMode.GRID -> CatalogGridContent(
                        state         = uiState.dataState,
                        onSnackClick  = onSnackClick,
                        onAddToCart   = { snack ->
                            viewModel.addToCart(snack)
                            snackbarState.show(
                                KeciputSnackbarData(
                                    message = "${snack.name} ditambahkan ke pesanan!",
                                    type = SnackbarType.SUCCESS,
                                    icon = androidx.compose.material.icons.Icons.Default.ShoppingBag
                                )
                            )
                        },
                        onReset       = viewModel::onResetFilter,
                        headerContent = headerContent
                    )
                    ViewMode.LIST -> CatalogListContent(
                        state         = uiState.dataState,
                        onSnackClick  = onSnackClick,
                        onAddToCart   = { snack ->
                            viewModel.addToCart(snack)
                            snackbarState.show(
                                KeciputSnackbarData(
                                    message = "${snack.name} ditambahkan ke pesanan!",
                                    type = SnackbarType.SUCCESS,
                                    icon = androidx.compose.material.icons.Icons.Default.ShoppingBag
                                )
                            )
                        },
                        onReset       = viewModel::onResetFilter,
                        headerContent = headerContent
                    )
                }
            }

            // Custom Snackbar overlay di bawah layar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                KeciputSnackbarHost(
                    state = snackbarState,
                    onAction = {
                        pendingUndoHistory?.let { viewModel.undoRemoveSearchHistory() }
                        pendingUndoHistory = null
                    }
                )
            }
        }
    }
}

@Composable
fun CatalogGridContent(
    state: CatalogDataState,
    onSnackClick: (Int) -> Unit,
    onAddToCart: (Snack) -> Unit,
    onReset: () -> Unit,
    headerContent: @Composable () -> Unit
) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        contentPadding        = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement   = Arrangement.spacedBy(12.dp),
        modifier              = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(2) }) {
            headerContent()
        }

        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (state) {
            is CatalogDataState.Loading -> {
                items(8) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                        SnackGridSkeleton()
                    }
                }
            }
            is CatalogDataState.Success -> {
                items(state.snacks, key = { it.id }) { snack ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                        SnackGridCard(
                            snack = snack,
                            onClick = { onSnackClick(snack.id) },
                            onAddToCart = { onAddToCart(snack) }
                        )
                    }
                }
            }
            is CatalogDataState.Empty -> {
                item(span = { GridItemSpan(2) }) { EmptyCatalogState(onReset) }
            }
            is CatalogDataState.Error -> {
                item(span = { GridItemSpan(2) }) { /* Error UI */ }
            }
        }
    }
}

@Composable
fun CatalogListContent(
    state: CatalogDataState,
    onSnackClick: (Int) -> Unit,
    onAddToCart: (Snack) -> Unit,
    onReset: () -> Unit,
    headerContent: @Composable () -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier            = Modifier.fillMaxSize()
    ) {
        item {
            headerContent()
        }

        when (state) {
            is CatalogDataState.Loading -> {
                items(6) { SnackListSkeleton() }
            }
            is CatalogDataState.Success -> {
                items(state.snacks, key = { it.id }) { snack ->
                    SnackListCard(
                        snack = snack,
                        onClick = { onSnackClick(snack.id) },
                        onAddToCart = { onAddToCart(snack) }
                    )
                }
            }
            is CatalogDataState.Empty -> {
                item { EmptyCatalogState(onReset) }
            }
            is CatalogDataState.Error -> { /* Error UI */ }
        }
    }
}

@Composable
fun SnackGridSkeleton() {
    Column(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = CoralMid.copy(0.1f), spotColor = CoralMid.copy(0.1f))
            .background(Color.White, RoundedCornerShape(18.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(130.dp).shimmerEffect())
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).shimmerEffect())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).shimmerEffect())
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.width(70.dp).height(20.dp).shimmerEffect())
        }
    }
}

@Composable
fun SnackListSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = CoralMid.copy(0.1f), spotColor = CoralMid.copy(0.1f))
            .background(Color.White, RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(110.dp).shimmerEffect())
        Column(modifier = Modifier.weight(1f).padding(14.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(18.dp).shimmerEffect())
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.3f).height(14.dp).shimmerEffect())
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.width(90.dp).height(22.dp).shimmerEffect())
        }
    }
}


@Composable
fun CatalogHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    searchHistory: List<String>,
    onHistoryClick: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    viewMode: ViewMode,
    onViewToggle: () -> Unit
) {
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val scallopedBottom = remember { ScallopedBottomShape() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(CoralDark, CoralMid)),
                shape = scallopedBottom
            )
            .sesamePattern()
    ) {
        Spacer(Modifier.statusBarsPadding())

        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 48.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logokeciputasrifa),
                    contentDescription = "Logo Aplikasi",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Katalog Produk", style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
                    Text("Temukan snack favoritmu", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)))
                }
            }
            Spacer(Modifier.height(24.dp))

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(
                        width = if (isSearchFocused) 2.dp else 1.5.dp,
                        color = if (isSearchFocused) CoralMid else CoralSoft.copy(0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = if (isSearchFocused) CoralMid else InkMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text("Cari snack...", style = MaterialTheme.typography.bodyMedium.copy(color = InkMuted))
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                        cursorBrush = SolidColor(CoralMid),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onSearchSubmit(query)
                                focusManager.clearFocus()
                                isSearchFocused = false
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange(""); isSearchFocused = false; focusManager.clearFocus() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = InkMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Riwayat Pencarian — muncul hanya saat ada riwayat & query kosong
            AnimatedVisibility(
                visible = searchHistory.isNotEmpty() && query.isEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Header riwayat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.History,
                                null,
                                tint = Color.White.copy(0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Riwayat Pencarian",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(0.8f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Surface(
                            onClick = onClearHistory,
                            color = Color.White.copy(0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Hapus semua",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CoralSoft,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // List chip riwayat
                    searchHistory.forEach { historyItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(0.08f))
                                .clickable {
                                    onHistoryClick(historyItem)
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                null,
                                tint = CoralSoft.copy(0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                historyItem,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { onRemoveHistory(historyItem) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = Color.White.copy(0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().background(Cream).padding(top = 12.dp)) {
            Text(
                text = "Pilih Kategori",
                style = MaterialTheme.typography.titleSmall.copy(color = Ink.copy(alpha = 0.8f)),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(categories) { cat ->
                    val isOn = cat == selectedCategory
                    val categoryIcon = getCategoryIcon(cat)
                    Surface(
                        modifier = Modifier
                            .shadow(if (isOn) 6.dp else 1.dp, RoundedCornerShape(14.dp), ambientColor = CoralMid.copy(0.15f), spotColor = CoralMid.copy(0.15f))
                            .clickable { onCategorySelected(cat) },
                        color = if (isOn) CoralDark else Color.White,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            if (categoryIcon != null) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = if (isOn) Color.White else CoralMid,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isOn) Color.White else Ink,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = CoralMid.copy(0.1f), spotColor = CoralMid.copy(0.1f))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Outlined.Sort, null, tint = CoralMid, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(SortOption.entries.toTypedArray()) { sort ->
                        val isOn = sort == selectedSort
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isOn) CoralSoft.copy(0.2f) else Color.Transparent)
                                .clickable { onSortChange(sort) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sort.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isOn) CoralDark else InkMuted,
                                    fontWeight = if (isOn) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Cream).padding(3.dp)
                ) {
                    val gridOn = viewMode == ViewMode.GRID
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (gridOn) CoralMid else Color.Transparent)
                            .clickable { if (!gridOn) onViewToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.GridView, null, tint = if (gridOn) Color.White else InkMuted, modifier = Modifier.size(18.dp))
                    }
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (!gridOn) CoralMid else Color.Transparent)
                            .clickable { if (gridOn) onViewToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.List, null, tint = if (!gridOn) Color.White else InkMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun getCategoryIcon(name: String): ImageVector? {
    return when {
        name == "Semua" -> Icons.Default.AllInclusive
        name.contains("Kerupuk") -> Icons.Default.BakeryDining
        name.contains("Snack") -> Icons.Default.Icecream
        name.contains("Kacang") -> Icons.Default.Grain
        name.contains("Singkong") -> Icons.Default.LunchDining
        name.contains("Tempe") -> Icons.Default.BreakfastDining
        name.contains("Kue") -> Icons.Default.Cookie
        name.contains("Tahu") -> Icons.Default.Kitchen
        else -> null
    }
}

@Composable
fun SnackGridCard(snack: Snack, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = CoralMid.copy(0.2f), spotColor = CoralMid.copy(0.2f))
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
                        Text("-$discount%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Surface(
                    onClick = onAddToCart,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).shadow(4.dp, CircleShape),
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
                Text(snack.name, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(snack.category, style = MaterialTheme.typography.labelSmall.copy(color = CoralMid, fontWeight = FontWeight.Bold))
                Text("Rp${snack.price.toInt().formatRupiah()}", style = MaterialTheme.typography.titleMedium.copy(color = CoralDark, fontWeight = FontWeight.ExtraBold), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun SnackListCard(snack: Snack, onClick: () -> Unit, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = CoralMid.copy(0.2f), spotColor = CoralMid.copy(0.2f))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(110.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(snack.imageUrl).size(110).crossfade(true).build(),
                    contentDescription = snack.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (snack.originalPrice != null) {
                    val discount = ((snack.originalPrice - snack.price) / snack.originalPrice * 100).toInt()
                    Surface(
                        color = CoralMid,
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text("$discount%", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).padding(14.dp)) {
                Text(snack.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(snack.category, style = MaterialTheme.typography.bodySmall.copy(color = CoralMid, fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rp${snack.price.toInt().formatRupiah()}", style = MaterialTheme.typography.titleLarge.copy(color = CoralDark, fontWeight = FontWeight.ExtraBold), modifier = Modifier.padding(top = 4.dp))
                    
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onAddToCart() }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            null, 
                            tint = CoralMid, 
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Tambah", 
                            color = CoralMid, 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCatalogState(onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = CoralSoft.copy(0.15f),
            shape = CircleShape,
            modifier = Modifier.size(120.dp)
        ) {
            Icon(Icons.Default.SearchOff, null, tint = CoralMid, modifier = Modifier.padding(32.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Produk tidak ditemukan", style = MaterialTheme.typography.titleLarge, color = Ink)
        Text("Coba cari kata kunci lain.", style = MaterialTheme.typography.bodyMedium, color = InkMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onReset, 
            colors = ButtonDefaults.buttonColors(containerColor = CoralDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(52.dp).padding(horizontal = 32.dp)
        ) {
            Text("Reset Filter", fontWeight = FontWeight.Bold)
        }
    }
}
