package com.example.godbless.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.godbless.NeprosrochApp
import com.example.godbless.domain.model.ProductCategory
import com.example.godbless.domain.model.StorageLocation
import com.example.godbless.ui.navigation.Screen
import com.example.godbless.ui.screens.home.HomeViewModel
import com.example.godbless.ui.screens.home.HomeViewModelFactory
import com.example.godbless.utils.CategoryMapper
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.Calendar
import java.util.Date
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

                // Overlay с прицелом
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Наведите камеру на штрихкод",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Прицел для сканирования
                        Box(
                            modifier = Modifier
                                .size(250.dp, 150.dp)
                                .border(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Штрихкод будет отсканирован автоматически",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.7f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
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
                        Text("Предоставить разрешение")
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
    var daysUntilExpiry by remember { mutableStateOf("7") }

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
                    label = { Text("Название продукта") },
                    placeholder = { Text("Например: Молоко") },
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
                                "Выберите из найденных:",
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
                                        product.productName ?: "Неизвестно",
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
                        value = getCategoryName(selectedCategory),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
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
                                text = { Text(getCategoryName(category)) },
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
                        value = getLocationName(selectedLocation),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Место хранения") },
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
                                text = { Text(getLocationName(location)) },
                                onClick = {
                                    selectedLocation = location
                                    showLocationMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = daysUntilExpiry,
                    onValueChange = { if (it.all { char -> char.isDigit() }) daysUntilExpiry = it },
                    label = { Text("Срок годности (дней)") },
                    placeholder = { Text("7") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productName.isNotBlank() && daysUntilExpiry.isNotBlank()) {
                        val days = daysUntilExpiry.toIntOrNull() ?: 7
                        val expiryDate = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, days)
                        }.time

                        onAddProduct(productName, selectedCategory, selectedLocation, expiryDate)
                    }
                },
                enabled = productName.isNotBlank() && daysUntilExpiry.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Добавить", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Отмена")
            }
        }
    )
}

fun getCategoryName(category: ProductCategory): String = when (category) {
    ProductCategory.DAIRY -> "Молочные продукты"
    ProductCategory.MEAT -> "Мясо"
    ProductCategory.FISH -> "Рыба"
    ProductCategory.VEGETABLES -> "Овощи"
    ProductCategory.FRUITS -> "Фрукты"
    ProductCategory.BAKERY -> "Хлеб и выпечка"
    ProductCategory.BEVERAGES -> "Напитки"
    ProductCategory.FROZEN -> "Замороженное"
    ProductCategory.CANNED -> "Консервы"
    ProductCategory.OTHER -> "Другое"
}

fun getLocationName(location: StorageLocation): String = when (location) {
    StorageLocation.FRIDGE -> "Холодильник"
    StorageLocation.FREEZER -> "Морозилка"
    StorageLocation.PANTRY -> "Кладовая"
    StorageLocation.COUNTER -> "На столе"
}
