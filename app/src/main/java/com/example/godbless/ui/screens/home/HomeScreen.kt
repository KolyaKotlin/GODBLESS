package com.example.godbless.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.godbless.NeprosrochApp
import com.example.godbless.R
import com.example.godbless.domain.model.Product
import com.example.godbless.domain.model.ProductCategory
import com.example.godbless.domain.model.ProductStatus
import com.example.godbless.domain.model.StorageLocation
import com.example.godbless.ui.components.CategoryFilter
import com.example.godbless.ui.components.DateInputSection
import com.example.godbless.ui.utils.getLocalizedName
import com.example.godbless.ui.screens.shopping.ShoppingViewModel
import com.example.godbless.ui.screens.shopping.ShoppingViewModelFactory
import com.example.godbless.ui.theme.StatusGreen
import com.example.godbless.ui.theme.StatusRed
import com.example.godbless.ui.theme.StatusYellow
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(NeprosrochApp.instance.productRepository)
    ),
    shoppingViewModel: ShoppingViewModel = viewModel(
        factory = ShoppingViewModelFactory(NeprosrochApp.instance.shoppingRepository)
    )
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddProductDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }

    val defaultQuantity = stringResource(R.string.default_quantity)

    // Фильтрация продуктов
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == null) {
            products
        } else {
            products.filter { it.category == selectedCategory }
        }
    }

    // Анимация для FAB
    val infiniteTransition = rememberInfiniteTransition(label = "fab")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            com.example.godbless.ui.theme.GradientStart,
                            com.example.godbless.ui.theme.GradientMiddle,
                            com.example.godbless.ui.theme.GradientEnd.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.home_title),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${products.size} ${if (products.size == 1) stringResource(R.string.products_count_single) else stringResource(R.string.products_count_plural)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .scale(if (products.isEmpty()) fabScale else 1f)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = MaterialTheme.colorScheme.primary
                        )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_product),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        ) { padding ->
        if (isLoading && products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Animated empty state icon
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = -10f,
                        targetValue = 10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "rotation"
                    )

                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .rotate(rotation),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(R.string.empty_products),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Нажмите + чтобы добавить первый продукт",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Фильтр по категориям
                item {
                    CategoryFilter(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                }

                // Список продуктов
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onDelete = { viewModel.deleteProduct(product) },
                        onAddToShopping = {
                            shoppingViewModel.addShoppingItem(
                                name = product.name,
                                quantity = defaultQuantity
                            )
                        }
                    )
                }
            }
        }
        }
    }

    // Диалог добавления продукта
    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onAddProduct = { name, category, location, expiryDate ->
                viewModel.addProduct(
                    name = name,
                    brand = null,
                    category = category,
                    storageLocation = location,
                    expiryDate = expiryDate,
                    barcode = null,
                    notes = null
                )
                showAddProductDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAddProduct: (String, ProductCategory, StorageLocation, java.util.Date) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.OTHER) }
    var selectedLocation by remember { mutableStateOf(StorageLocation.FRIDGE) }
    var calculatedDays by remember { mutableStateOf(7) }
    var expiryDate by remember { mutableStateOf<java.util.Date?>(null) }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showLocationMenu by remember { mutableStateOf(false) }

    // Рассчитываем дату окончания срока годности на основе дней
    LaunchedEffect(calculatedDays) {
        if (expiryDate == null) {
            expiryDate = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, calculatedDays)
            }.time
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.add_product_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text(stringResource(R.string.product_name_label)) },
                    placeholder = { Text(stringResource(R.string.example_product_milk)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Категория
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.getLocalizedName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.category_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        ProductCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.getLocalizedName()) },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Место хранения
                ExposedDropdownMenuBox(
                    expanded = showLocationMenu,
                    onExpandedChange = { showLocationMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedLocation.getLocalizedName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.storage_location_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLocationMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showLocationMenu,
                        onDismissRequest = { showLocationMenu = false }
                    ) {
                        StorageLocation.values().forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location.getLocalizedName()) },
                                onClick = {
                                    selectedLocation = location
                                    showLocationMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ввод даты
                DateInputSection(
                    onDaysCalculated = { days ->
                        calculatedDays = days
                        expiryDate = java.util.Calendar.getInstance().apply {
                            add(java.util.Calendar.DAY_OF_YEAR, days)
                        }.time
                    },
                    onExpiryDateChanged = { date ->
                        if (date != null) {
                            expiryDate = date
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productName.isNotBlank() && expiryDate != null) {
                        onAddProduct(productName, selectedCategory, selectedLocation, expiryDate!!)
                    }
                },
                enabled = productName.isNotBlank() && expiryDate != null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_button), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ProductCard(
    product: Product,
    onDelete: () -> Unit,
    onAddToShopping: () -> Unit
) {
    val statusColor = when (product.getStatusColor()) {
        ProductStatus.GOOD -> StatusGreen
        ProductStatus.WARNING -> StatusYellow
        ProductStatus.EXPIRED -> StatusRed
    }

    val statusIcon = when (product.getStatusColor()) {
        ProductStatus.GOOD -> Icons.Default.CheckCircle
        ProductStatus.WARNING -> Icons.Default.Warning
        ProductStatus.EXPIRED -> Icons.Default.Error
    }

    val statusText = when {
        product.isExpired() -> stringResource(R.string.status_expired)
        product.getDaysUntilExpiry() == 0 -> stringResource(R.string.status_today)
        product.getDaysUntilExpiry() <= 3 -> stringResource(R.string.status_urgent)
        else -> stringResource(R.string.status_ok)
    }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    var isExpanded by remember { mutableStateOf(false) }

    // Анимация пульсации для просроченных продуктов
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (product.isExpired()) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Анимация свечения для критических продуктов
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (product.isExpired() || product.getDaysUntilExpiry() <= 1) 0.7f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (product.isExpired()) pulseScale else 1f)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = statusColor.copy(alpha = glowAlpha),
                ambientColor = statusColor.copy(alpha = 0.2f)
            )
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 500f)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Левая часть с иконкой статуса
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    // Статус иконка с анимацией и градиентом
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = statusColor.copy(alpha = 0.5f)
                            )
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        statusColor.copy(alpha = 0.25f),
                                        statusColor.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Информация о продукте
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (product.brand != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = product.brand,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Срок годности
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = statusColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dateFormat.format(product.expiryDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = statusColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Дни до истечения
                        val daysLeft = product.getDaysUntilExpiry()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = statusColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (daysLeft < 0) {
                                    stringResource(R.string.expired_days_ago, -daysLeft)
                                } else if (daysLeft == 0) {
                                    stringResource(R.string.expires_today)
                                } else {
                                    stringResource(R.string.days_left, daysLeft)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                // Кнопки действий
                Column {
                    // Кнопка добавления в список покупок
                    IconButton(
                        onClick = onAddToShopping,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = stringResource(R.string.cd_add_to_shopping)
                        )
                    }

                    // Кнопка удаления
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }

            // Дополнительная информация (раскрывается при клике)
            if (isExpanded && (product.notes != null || product.storageLocation != null)) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = product.storageLocation.getLocalizedName(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (product.notes != null) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = product.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ViewModelFactory
class HomeViewModelFactory(
    private val repository: com.example.godbless.data.repository.ProductRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
