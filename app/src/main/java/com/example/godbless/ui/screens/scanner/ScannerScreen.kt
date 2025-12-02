package com.example.godbless.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.godbless.NeprosrochApp
import com.example.godbless.R
import com.example.godbless.domain.model.ProductCategory
import com.example.godbless.domain.model.StorageLocation
import com.example.godbless.ui.navigation.Screen
import com.example.godbless.ui.utils.getLocalizedName
import com.example.godbless.ui.screens.home.HomeViewModel
import com.example.godbless.ui.screens.home.HomeViewModelFactory
import com.example.godbless.utils.CategoryMapper
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(NeprosrochApp.instance.productRepository)
    ),
    scannerViewModel: ScannerViewModel = viewModel(
        factory = ScannerViewModelFactory(NeprosrochApp.instance.openFoodFactsRepository)
    )
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val scannedProduct by scannerViewModel.scannedProduct.collectAsState()
    val isLoading by scannerViewModel.isLoading.collectAsState()
    val error by scannerViewModel.error.collectAsState()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Сканер штрихкодов",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (hasCameraPermission) {
                        IconButton(onClick = { torchEnabled = !torchEnabled }) {
                            Icon(
                                if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Вкл/выкл фонарик"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    torchEnabled = torchEnabled,
                    onBarcodeScanned = { barcode ->
                        scannedBarcode = barcode
                        scannerViewModel.searchByBarcode(barcode)
                        showAddProductDialog = true
                    }
                )

                // Futuristic overlay с анимированным прицелом
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated scan line
                    val infiniteTransition = rememberInfiniteTransition(label = "scan")
                    val scanLineY by infiniteTransition.animateFloat(
                        initialValue = -75f,
                        targetValue = 75f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "scan_line"
                    )

                    val cornerAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "corner_alpha"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Наведите камеру на штрихкод",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(
                                    Color.Black.copy(alpha = 0.8f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(20.dp)
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Futuristic scan target with animated corners
                        Box(
                            modifier = Modifier.size(280.dp, 180.dp)
                        ) {
                            // Corner brackets
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cornerSize = 40f
                                val strokeWidth = 8f
                                val color = androidx.compose.ui.graphics.Color(0xFF6366F1)
                                    .copy(alpha = cornerAlpha)

                                // Top-left corner
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(0f, cornerSize),
                                    end = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(cornerSize, 0f),
                                    strokeWidth = strokeWidth
                                )

                                // Top-right corner
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(size.width - cornerSize, 0f),
                                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                    end = androidx.compose.ui.geometry.Offset(size.width, cornerSize),
                                    strokeWidth = strokeWidth
                                )

                                // Bottom-left corner
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(0f, size.height - cornerSize),
                                    end = androidx.compose.ui.geometry.Offset(0f, size.height),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                    end = androidx.compose.ui.geometry.Offset(cornerSize, size.height),
                                    strokeWidth = strokeWidth
                                )

                                // Bottom-right corner
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(size.width - cornerSize, size.height),
                                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(size.width, size.height - cornerSize),
                                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }

                            // Animated scan line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .offset(y = scanLineY.dp)
                                    .align(Alignment.Center)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                com.example.godbless.ui.theme.Primary.copy(alpha = 0.8f),
                                                com.example.godbless.ui.theme.Secondary.copy(alpha = 0.8f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Text(
                            text = "Штрихкод будет отсканирован автоматически",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    Color.Black.copy(alpha = 0.8f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        )
                    }
                }
            } else {
                // Сообщение о необходимости разрешения
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Для сканирования штрихкодов необходимо разрешение камеры",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
        }
    }

    // Диалог добавления продукта после сканирования
    if (showAddProductDialog && scannedBarcode != null) {
        AddScannedProductDialog(
            barcode = scannedBarcode!!,
            scannedProduct = scannedProduct,
            isLoading = isLoading,
            error = error,
            scannerViewModel = scannerViewModel,
            onDismiss = {
                showAddProductDialog = false
                scannedBarcode = null
                scannerViewModel.clearSearch()
            },
            onAddProduct = { name, category, location, expiryDate ->
                viewModel.addProduct(
                    name = name,
                    brand = null,
                    category = category,
                    storageLocation = location,
                    expiryDate = expiryDate,
                    barcode = scannedBarcode,
                    notes = null
                )
                showAddProductDialog = false
                scannedBarcode = null
                scannerViewModel.clearSearch()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun CameraPreview(
    torchEnabled: Boolean,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(torchEnabled) {
        camera?.cameraControl?.enableTorch(torchEnabled)
        onDispose { }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, BarcodeAnalyzer { barcode ->
                            onBarcodeScanned(barcode)
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )

                    if (torchEnabled && camera?.cameraInfo?.hasFlashUnit() == true) {
                        camera?.cameraControl?.enableTorch(true)
                    }
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var lastScannedTime = 0L
    private val SCAN_DELAY = 2000L // 2 секунды между сканами

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastScannedTime > SCAN_DELAY) {
                            barcode.rawValue?.let { value ->
                                onBarcodeDetected(value)
                                lastScannedTime = currentTime
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScannedProductDialog(
    barcode: String,
    scannedProduct: com.example.godbless.data.remote.OpenFoodProduct?,
    isLoading: Boolean,
    error: String?,
    scannerViewModel: ScannerViewModel,
    onDismiss: () -> Unit,
    onAddProduct: (String, ProductCategory, StorageLocation, Date) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.OTHER) }
    var selectedLocation by remember { mutableStateOf(StorageLocation.FRIDGE) }

    // Режим ввода даты: 0 = дни, 1 = производство+истечение, 2 = только истечение
    var dateInputMode by remember { mutableStateOf(0) }

    var daysUntilExpiry by remember { mutableStateOf("7") }
    var productionDate by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showLocationMenu by remember { mutableStateOf(false) }

    val searchResults by scannerViewModel.searchResults.collectAsState()

    // Автозаполнение из API
    LaunchedEffect(scannedProduct) {
        scannedProduct?.let { product ->
            productName = product.productName ?: ""
            // Автоматически определяем категорию из данных OpenFoodFacts
            selectedCategory = CategoryMapper.mapFromOpenFoodFacts(product.categories)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    "Отсканирован штрихкод",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    barcode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Статус загрузки/ошибки
                when {
                    isLoading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Поиск в базе OpenFoodFacts...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    scannedProduct != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "✓",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Продукт найден в базе!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    error != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                "Продукт не найден. Введите название вручную.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Поле ввода названия с автокомплитом
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        if (it.length >= 2) {
                            scannerViewModel.searchProducts(it)
                        }
                    },
                    label = { Text(stringResource(R.string.product_name_label)) },
                    placeholder = { Text(stringResource(R.string.example_product_milk)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                // Результаты автокомплита
                if (searchResults.isNotEmpty() && productName.length >= 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                stringResource(R.string.select_from_found),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            searchResults.take(3).forEach { product ->
                                TextButton(
                                    onClick = {
                                        productName = product.productName ?: ""
                                        scannerViewModel.clearSearch()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        product.productName ?: stringResource(R.string.unknown),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }

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

                // Выбор режима ввода даты
                Text(
                    "Способ указания срока годности:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Кнопка "Дни"
                    FilterChip(
                        selected = dateInputMode == 0,
                        onClick = { dateInputMode = 0 },
                        label = { Text(stringResource(R.string.days)) },
                        modifier = Modifier.weight(1f)
                    )
                    // Кнопка "Даты"
                    FilterChip(
                        selected = dateInputMode == 1,
                        onClick = { dateInputMode = 1 },
                        label = { Text(stringResource(R.string.dates)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Поля ввода в зависимости от режима
                when (dateInputMode) {
                    0 -> {
                        // Режим "Дни"
                        OutlinedTextField(
                            value = daysUntilExpiry,
                            onValueChange = { if (it.all { char -> char.isDigit() }) daysUntilExpiry = it },
                            label = { Text(stringResource(R.string.expiry_days)) },
                            placeholder = { Text(stringResource(R.string.placeholder_7)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                    1 -> {
                        // Режим "Даты производства и истечения"
                        OutlinedTextField(
                            value = productionDate,
                            onValueChange = { productionDate = it },
                            label = { Text(stringResource(R.string.production_date_label)) },
                            placeholder = { Text(stringResource(R.string.date_example)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = expirationDate,
                            onValueChange = { expirationDate = it },
                            label = { Text(stringResource(R.string.expiration_date)) },
                            placeholder = { Text(stringResource(R.string.date_example_end)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.EventAvailable,
                                    contentDescription = null,
                                    tint = com.example.godbless.ui.theme.StatusRed
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productName.isNotBlank()) {
                        val finalExpiryDate: Date? = when (dateInputMode) {
                            0 -> {
                                // Режим дней
                                if (daysUntilExpiry.isNotBlank()) {
                                    val days = daysUntilExpiry.toIntOrNull() ?: 7
                                    Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, days)
                                    }.time
                                } else null
                            }
                            1 -> {
                                // Режим дат
                                if (expirationDate.isNotBlank()) {
                                    parseDateString(expirationDate)
                                } else null
                            }
                            else -> null
                        }

                        finalExpiryDate?.let { date ->
                            onAddProduct(productName, selectedCategory, selectedLocation, date)
                        }
                    }
                },
                enabled = when (dateInputMode) {
                    0 -> productName.isNotBlank() && daysUntilExpiry.isNotBlank()
                    1 -> productName.isNotBlank() && expirationDate.isNotBlank()
                    else -> false
                },
                shape = RoundedCornerShape(12.dp)
            ) {
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

fun parseDateString(dateStr: String): Date? {
    return try {
        val formats = listOf(
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        )
        formats.firstNotNullOfOrNull { format ->
            try {
                format.parse(dateStr)
            } catch (e: Exception) {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}
