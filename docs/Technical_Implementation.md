# Техническое описание реализации проекта "НеПросрочь!"

## 1. Общая архитектура приложения

### 1.1 Архитектурный паттерн

Приложение реализовано на основе архитектурного паттерна **MVVM (Model-View-ViewModel)** с применением принципов **Clean Architecture**, что обеспечивает четкое разделение ответственности, тестируемость кода и упрощение сопровождения проекта.

**Структура слоев:**

```
┌─────────────────────────────────────────────────────┐
│                 UI Layer (Presentation)              │
│  Compose UI + ViewModels + Navigation                │
└──────────────────┬──────────────────────────────────┘
                   │ Events / State
                   ▼
┌─────────────────────────────────────────────────────┐
│              Domain Layer (Business Logic)           │
│  Models + Use Cases + Business Rules                 │
└──────────────────┬──────────────────────────────────┘
                   │ Entities / Operations
                   ▼
┌─────────────────────────────────────────────────────┐
│              Data Layer (Data Management)            │
│  Repositories + Data Sources (Local DB + Remote API)│
└─────────────────────────────────────────────────────┘
```

### 1.2 Структура проекта

Проект организован следующим образом:

```
app/src/main/java/com/example/godbless/
├── data/                          # Слой данных
│   ├── local/                     # Локальное хранилище
│   │   ├── AppDatabase.kt         # Room БД
│   │   ├── ProductDao.kt          # DAO для продуктов
│   │   ├── ShoppingItemDao.kt     # DAO для списка покупок
│   │   └── Converters.kt          # Type Converters для Room
│   ├── remote/                    # Удаленные источники данных
│   │   ├── OpenFoodFactsApi.kt    # Retrofit API интерфейс
│   │   ├── OpenFoodFactsModels.kt # Модели API
│   │   └── RetrofitClient.kt      # Конфигурация Retrofit
│   ├── repository/                # Репозитории
│   │   ├── ProductRepository.kt
│   │   ├── ShoppingRepository.kt
│   │   ├── AuthRepository.kt
│   │   ├── PreferencesRepository.kt
│   │   └── OpenFoodFactsRepository.kt
│   └── SettingsManager.kt         # Управление настройками
├── domain/                        # Доменный слой
│   └── model/                     # Доменные модели
│       ├── Product.kt             # Модель продукта
│       ├── ShoppingItem.kt        # Модель товара в списке покупок
│       └── UserPreferences.kt     # Модель настроек пользователя
├── ui/                            # Слой представления
│   ├── components/                # Переиспользуемые UI компоненты
│   │   ├── CategoryFilter.kt
│   │   └── DateInputSection.kt
│   ├── navigation/                # Навигация
│   │   └── Screen.kt              # Определения экранов
│   ├── screens/                   # Экраны приложения
│   │   ├── auth/                  # Экран авторизации
│   │   ├── home/                  # Главный экран
│   │   ├── scanner/               # Сканер штрихкодов
│   │   ├── shopping/              # Список покупок
│   │   ├── profile/               # Профиль и настройки
│   │   └── addproduct/            # Добавление продукта
│   ├── theme/                     # Тема оформления
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── utils/                     # Утилиты UI
├── workers/                       # Фоновые задачи
│   └── ExpiryNotificationWorker.kt
├── utils/                         # Общие утилиты
│   ├── CategoryMapper.kt
│   └── LocaleHelper.kt
├── NeprosrochApp.kt              # Application класс
└── MainActivity.kt                # Главная активность
```

## 2. Технологический стек

### 2.1 Основные технологии

**Язык программирования:**
- **Kotlin 1.9+** - современный язык для Android разработки с поддержкой корутин, null-safety, extension functions

**Платформа:**
- **Android SDK** - minSdk 29 (Android 10), targetSdk 35 (Android 14+), compileSdk 35
- **Java 11** - совместимость с современными JVM функциями

### 2.2 Библиотеки и фреймворки

#### UI Framework
- **Jetpack Compose** (BOM 2024.+) - декларативный UI toolkit от Google
  - `androidx.compose.ui` - базовые компоненты
  - `androidx.compose.material3` - Material Design 3 компоненты
  - `androidx.compose.material.icons` - набор иконок Material Icons
  - `androidx.activity.compose` - интеграция Compose с Activity

**Преимущества Jetpack Compose:**
- Декларативный синтаксис (описываем "что" отображать, а не "как")
- Автоматическая перерисовка UI при изменении состояния (recomposition)
- Меньше boilerplate кода по сравнению с XML layouts
- Поддержка тем, анимаций, жестов из коробки
- Превью в Android Studio для быстрой разработки

#### Навигация
- **Navigation Compose** (androidx.navigation.compose)
  - Типобезопасная навигация между экранами
  - Поддержка back stack
  - Deep links и передача аргументов

#### База данных
- **Room Database** (androidx.room)
  - ORM для SQLite с compile-time проверкой SQL
  - Поддержка Flow для реактивных запросов
  - Type converters для сложных типов (Date)

**Структура БД:**
```sql
TABLE products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    brand TEXT,
    category TEXT NOT NULL,
    storageLocation TEXT NOT NULL,
    expiryDate INTEGER NOT NULL,
    barcode TEXT,
    imageUrl TEXT,
    notes TEXT,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
)

TABLE shopping_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    quantity TEXT,
    isPurchased INTEGER NOT NULL DEFAULT 0
)
```

#### Работа с камерой и ML
- **CameraX** (androidx.camera) - современный API для работы с камерой
  - camera-core, camera-camera2 - ядро CameraX
  - camera-lifecycle - интеграция с Lifecycle
  - camera-view - готовые View компоненты

- **ML Kit Barcode Scanning** (com.google.mlkit:barcode-scanning)
  - Распознавание штрихкодов и QR-кодов без подключения к интернету
  - Поддержка форматов: EAN-13, EAN-8, UPC-A, UPC-E, Code-39, Code-93, Code-128, QR, DataMatrix

#### Сетевые запросы
- **Retrofit 2** - HTTP клиент для REST API
  - retrofit-converter-gson - сериализация JSON через Gson
  - okhttp-logging - логирование HTTP запросов для отладки

**Интеграция с OpenFoodFacts API:**
```
GET https://world.openfoodfacts.org/api/v0/product/{barcode}.json
```

#### Асинхронность
- **Kotlin Coroutines** (kotlinx.coroutines.android)
  - suspend функции для асинхронных операций
  - Flow для реактивных потоков данных
  - viewModelScope, lifecycleScope для управления жизненным циклом

#### Управление состоянием
- **ViewModel** (androidx.lifecycle.viewmodel.compose)
  - Сохранение состояния при смене конфигурации (поворот экрана)
  - LiveData / Flow для обновления UI

#### Аутентификация
- **Firebase Authentication** (firebase-auth)
  - Email/Password аутентификация
  - Управление сессиями пользователей

#### Фоновые задачи
- **WorkManager** (androidx.work.runtime)
  - Периодическая проверка сроков годности
  - Отправка уведомлений
  - Гарантированное выполнение даже после перезагрузки устройства

#### Загрузка изображений
- **Coil** (coil-compose)
  - Асинхронная загрузка изображений для Compose
  - Кэширование в памяти и на диске

#### Сериализация
- **Gson** - JSON парсинг для SharedPreferences и Retrofit

#### Тестирование
- **JUnit** - unit тесты
- **Espresso** - UI тесты
- **AndroidX Test** - тестовые утилиты

## 3. Детальная реализация компонентов

### 3.1 Точка входа приложения

#### NeprosrochApp.kt - Application класс

```kotlin
class NeprosrochApp : Application() {
    lateinit var database: AppDatabase
    lateinit var productRepository: ProductRepository
    lateinit var shoppingRepository: ShoppingRepository
    lateinit var authRepository: AuthRepository
    lateinit var preferencesRepository: PreferencesRepository
    lateinit var openFoodFactsRepository: OpenFoodFactsRepository
    lateinit var settingsManager: SettingsManager

    companion object {
        lateinit var instance: NeprosrochApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Инициализация базы данных
        database = AppDatabase.getDatabase(this)

        // Инициализация репозиториев
        productRepository = ProductRepository(database.productDao())
        shoppingRepository = ShoppingRepository(database.shoppingItemDao())
        authRepository = AuthRepository(FirebaseAuth.getInstance())
        preferencesRepository = PreferencesRepository(this)
        openFoodFactsRepository = OpenFoodFactsRepository()
        settingsManager = SettingsManager(this)

        // Запуск периодической проверки сроков годности
        scheduleExpiryCheck()
    }

    private fun scheduleExpiryCheck() {
        val workRequest = PeriodicWorkRequestBuilder<ExpiryNotificationWorker>(
            1, TimeUnit.DAYS  // Проверка раз в сутки
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ExpiryNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
```

**Реализация паттерна Singleton для репозиториев:**
- Application класс создается один раз при запуске приложения
- Все репозитории инициализируются здесь и доступны глобально через `NeprosrochApp.instance`
- Избегаем множественного создания экземпляров БД и репозиториев

#### MainActivity.kt - Главная активность

**Основные функции:**
1. Установка темы оформления (светлая/темная/системная)
2. Применение выбранного языка интерфейса
3. Запрос разрешений (камера, уведомления)
4. Настройка навигации

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsManager = NeprosrochApp.instance.settingsManager

        // Применяем выбранный язык
        LocaleHelper.applyLanguage(this, settingsManager.getLanguage())

        setContent {
            AppThemeWrapper {
                MainApp()
            }
        }
    }
}
```

**Управление темой:**
```kotlin
@Composable
fun AppThemeWrapper(content: @Composable () -> Unit) {
    val settingsManager = NeprosrochApp.instance.settingsManager
    var savedTheme by remember { mutableStateOf(settingsManager.getTheme()) }
    val isSystemDark = isSystemInDarkTheme()

    val darkTheme = when (savedTheme) {
        SettingsManager.THEME_LIGHT -> false
        SettingsManager.THEME_DARK -> true
        else -> isSystemDark  // Следуем системной теме
    }

    GODBLESSTheme(darkTheme = darkTheme) {
        content()
    }
}
```

**Навигация:**
Двухуровневая навигация:
1. **Первый уровень:** Auth Screen → Main Screen
2. **Второй уровень (внутри Main Screen):** Home, Scanner, Shopping, Profile

```kotlin
NavHost(navController, startDestination = Screen.Auth.route) {
    composable(Screen.Auth.route) {
        AuthScreen(
            onAuthSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
            }
        )
    }
    composable(Screen.Home.route) {
        MainScreen(navController)  // Внутренняя навигация
    }
}
```

### 3.2 Слой данных (Data Layer)

#### 3.2.1 Локальная база данных

**AppDatabase.kt - Room Database**

Реализация паттерна **Singleton** для базы данных:

```kotlin
@Database(
    entities = [Product::class, ShoppingItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neprosroch_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**@Volatile** - обеспечивает видимость изменений INSTANCE между потоками
**synchronized** - предотвращает одновременное создание нескольких экземпляров БД
**fallbackToDestructiveMigration()** - при изменении схемы БД пересоздает таблицы (для разработки)

**Converters.kt - Type Converters**

Room не поддерживает Date из коробки, поэтому создаем конвертеры:

```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromProductCategory(value: ProductCategory): String {
        return value.name
    }

    @TypeConverter
    fun toProductCategory(value: String): ProductCategory {
        return ProductCategory.valueOf(value)
    }

    @TypeConverter
    fun fromStorageLocation(value: StorageLocation): String {
        return value.name
    }

    @TypeConverter
    fun toStorageLocation(value: String): StorageLocation {
        return StorageLocation.valueOf(value)
    }
}
```

**ProductDao.kt - Data Access Object для продуктов**

```kotlin
@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Insert
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}
```

**Flow<List<Product>>** - реактивный поток данных, автоматически обновляется при изменениях в БД
**suspend** - функция может быть приостановлена (работает в корутинах)

#### 3.2.2 Удаленные источники данных

**OpenFoodFactsApi.kt - Retrofit интерфейс**

```kotlin
interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): ProductResponse
}
```

**RetrofitClient.kt - Конфигурация Retrofit**

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: OpenFoodFactsApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenFoodFactsApi::class.java)
}
```

**Логирование HTTP запросов** для отладки
**Таймауты** предотвращают зависание при медленном интернете
**GsonConverterFactory** автоматически преобразует JSON в Kotlin объекты

#### 3.2.3 Репозитории

Репозитории инкапсулируют логику доступа к данным и предоставляют единый API для ViewModel.

**ProductRepository.kt**

```kotlin
class ProductRepository(private val productDao: ProductDao) {
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
}
```

**Паттерн Repository:**
- Скрывает источник данных (БД, сеть, кэш)
- Позволяет легко заменить реализацию (например, с Room на другую БД)
- Упрощает тестирование (можно создать mock repository)

**OpenFoodFactsRepository.kt**

```kotlin
class OpenFoodFactsRepository {
    private val api = RetrofitClient.api

    suspend fun searchProduct(barcode: String): Result<ProductInfo> {
        return try {
            val response = api.getProduct(barcode)
            if (response.status == 1 && response.product != null) {
                Result.success(response.product)
            } else {
                Result.failure(Exception("Product not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Result<T>** - стандартный Kotlin класс для обработки успеха/ошибки
**try-catch** - обрабатываем сетевые ошибки, таймауты, парсинг JSON

### 3.3 Доменный слой (Domain Layer)

#### Product.kt - Модель продукта

```kotlin
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val category: ProductCategory,
    val storageLocation: StorageLocation,
    val expiryDate: Date,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    // Бизнес-логика расчета дней до истечения
    fun getDaysUntilExpiry(): Int {
        val nowCal = Calendar.getInstance().apply {
            time = Date()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val expiryCal = Calendar.getInstance().apply {
            time = expiryDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diff = expiryCal.timeInMillis - nowCal.timeInMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun isExpired(): Boolean = getDaysUntilExpiry() < 0

    fun isExpiringSoon(): Boolean {
        val days = getDaysUntilExpiry()
        return days in 0..7
    }

    fun getStatusColor(): ProductStatus {
        return when {
            isExpired() -> ProductStatus.EXPIRED
            getDaysUntilExpiry() <= 3 -> ProductStatus.WARNING
            else -> ProductStatus.GOOD
        }
    }
}
```

**Обнуление времени:**
Сравниваем только даты без учета времени (часы, минуты, секунды обнуляются).
Это обеспечивает корректный подсчет: если выбрали 7 дней, всегда отображается 7 дней, независимо от времени создания.

**Enum классы:**

```kotlin
enum class ProductCategory(val displayName: String) {
    DAIRY("Молочные продукты"),
    MEAT("Мясо и птица"),
    FISH("Рыба и морепродукты"),
    VEGETABLES("Овощи"),
    FRUITS("Фрукты"),
    BAKERY("Хлебобулочные изделия"),
    FROZEN("Замороженные продукты"),
    CANNED("Консервы"),
    BEVERAGES("Напитки"),
    OTHER("Другое")
}

enum class StorageLocation(val displayName: String) {
    FRIDGE("Холодильник"),
    FREEZER("Морозильник"),
    PANTRY("Кладовая"),
    COUNTER("На столе")
}

enum class ProductStatus {
    GOOD,      // Зеленый - срок годности > 3 дней
    WARNING,   // Желтый - срок годности 1-3 дня
    EXPIRED    // Красный - срок истек
}
```

### 3.4 Слой представления (Presentation Layer / UI Layer)

#### 3.4.1 ViewModel

**HomeViewModel.kt**

```kotlin
class HomeViewModel(private val productRepository: ProductRepository) : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getAllProducts()
                .collect { productList ->
                    _products.value = productList.sortedWith(
                        compareBy<Product> { it.getStatusColor() }
                            .thenBy { it.getDaysUntilExpiry() }
                    )
                    _isLoading.value = false
                }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            productRepository.insertProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }
}
```

**StateFlow** - холодный поток состояния (всегда имеет текущее значение)
**viewModelScope** - корутины привязаны к жизненному циклу ViewModel
**collect** - подписка на изменения Flow (реактивность)
**Сортировка:** Expired (красные) → Warning (желтые) → Good (зеленые)

**ViewModelFactory:**

```kotlin
class HomeViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

Фабрика нужна для передачи зависимостей (repository) в ViewModel при создании.

#### 3.4.2 Compose UI Screens

**HomeScreen.kt - Главный экран**

**Основные компоненты:**

1. **Градиентный фон:**
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    GradientStart,
                    GradientMiddle,
                    GradientEnd.copy(alpha = 0.3f)
                )
            )
        )
)
```

2. **Фильтры по категориям:**
```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(horizontal = 16.dp)
) {
    items(ProductCategory.values()) { category ->
        FilterChip(
            selected = selectedCategory == category,
            onClick = { selectedCategory = if (selectedCategory == category) null else category },
            label = { Text(category.getLocalizedName()) },
            leadingIcon = { Icon(getCategoryIcon(category)) }
        )
    }
}
```

3. **Список продуктов:**
```kotlin
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(filteredProducts, key = { it.id }) { product ->
        ProductCard(
            product = product,
            onDelete = { viewModel.deleteProduct(product) },
            onAddToShopping = { shoppingViewModel.addShoppingItem(product.name) }
        )
    }
}
```

**LazyColumn** - эффективный список с ленивой загрузкой (рендерятся только видимые элементы)
**key = { it.id }** - уникальный ключ для оптимизации recomposition

4. **Floating Action Button:**
```kotlin
FloatingActionButton(
    onClick = { showAddDialog = true },
    containerColor = MaterialTheme.colorScheme.primary,
    shape = RoundedCornerShape(20.dp),
    modifier = Modifier.scale(if (products.isEmpty()) fabScale else 1f)
) {
    Icon(Icons.Default.Add, modifier = Modifier.size(32.dp))
}
```

**Анимация пульсации** при пустом списке привлекает внимание:
```kotlin
val fabScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.1f,
    animationSpec = infiniteRepeatable(
        animation = tween(1500, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )
)
```

**ScannerScreen.kt - Сканер штрихкодов**

**Интеграция CameraX и ML Kit:**

```kotlin
@Composable
fun ScannerScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var detectedBarcode by remember { mutableStateOf<String?>(null) }

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraExecutor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // ImageAnalysis use case для ML Kit
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                            detectedBarcode = barcode
                        })
                    }

                // Привязываем к жизненному циклу
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    Log.e("Camera", "Error", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

**BarcodeAnalyzer - ML Kit анализатор:**

```kotlin
private class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let { barcode ->
                        onBarcodeDetected(barcode)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
```

**Поиск продукта в OpenFoodFacts:**

```kotlin
suspend fun searchProductByBarcode(barcode: String): ProductInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val result = openFoodFactsRepository.searchProduct(barcode)
            result.getOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
```

**ShoppingScreen.kt - Список покупок**

**Группировка на купленные и некупленные:**

```kotlin
val unpurchasedItems = shoppingItems.filter { !it.isPurchased }
val purchasedItems = shoppingItems.filter { it.isPurchased }

// Секция "Нужно купить"
if (unpurchasedItems.isNotEmpty()) {
    item {
        Text(
            text = "Нужно купить (${unpurchasedItems.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    items(unpurchasedItems) { item ->
        ShoppingItemCard(
            item = item,
            onToggle = { viewModel.togglePurchased(item) },
            onDelete = { viewModel.deleteItem(item) }
        )
    }
}

// Секция "Куплено"
if (purchasedItems.isNotEmpty()) {
    item {
        Text(
            text = "Куплено (${purchasedItems.size})",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    items(purchasedItems) { item ->
        ShoppingItemCard(
            item = item,
            onToggle = { viewModel.togglePurchased(item) },
            onDelete = { viewModel.deleteItem(item) }
        )
    }
}
```

**Анимация купленных товаров:**

```kotlin
val alpha by animateFloatAsState(
    targetValue = if (item.isPurchased) 0.6f else 1f,
    animationSpec = tween(300)
)

Card(
    modifier = Modifier.alpha(alpha)
) {
    Text(
        text = item.name,
        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
    )
}
```

**ProfileScreen.kt - Настройки**

**Управление темой:**

```kotlin
var selectedTheme by remember { mutableStateOf(settingsManager.getTheme()) }

RadioButton(
    selected = selectedTheme == SettingsManager.THEME_LIGHT,
    onClick = {
        selectedTheme = SettingsManager.THEME_LIGHT
        settingsManager.saveTheme(SettingsManager.THEME_LIGHT)
    }
)
```

**Управление языком:**

```kotlin
var selectedLanguage by remember { mutableStateOf(settingsManager.getLanguage()) }

DropdownMenu(expanded = showLanguageMenu) {
    DropdownMenuItem(
        text = { Text("Русский") },
        onClick = {
            selectedLanguage = "ru"
            settingsManager.saveLanguage("ru")
            LocaleHelper.applyLanguage(context, "ru")
            activity.recreate()  // Перезапуск для применения языка
        }
    )
}
```

### 3.5 Фоновые задачи и уведомления

#### ExpiryNotificationWorker.kt - WorkManager Worker

**Периодическая проверка сроков годности:**

```kotlin
class ExpiryNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val productRepository = (applicationContext as NeprosrochApp).productRepository
        val preferencesRepository = (applicationContext as NeprosrochApp).preferencesRepository

        // Получаем настройки уведомлений
        val preferences = preferencesRepository.userPreferences.first()

        // Получаем все продукты
        val products = productRepository.getAllProducts().first()

        createNotificationChannel()

        products.forEach { product ->
            val daysUntilExpiry = product.getDaysUntilExpiry()

            // Проверяем, нужно ли отправлять уведомление
            val notificationType = when {
                daysUntilExpiry in 6..7 && preferences.notifySevenDays -> 7
                daysUntilExpiry in 2..3 && preferences.notifyThreeDays -> 3
                daysUntilExpiry in 0..1 && preferences.notifyOneDay -> 1
                else -> null
            }

            if (notificationType != null) {
                sendNotification(product.name, notificationType, product.id.toInt())
            }
        }

        return Result.success()
    }
}
```

**Создание каналов уведомлений (Android 8.0+):**

```kotlin
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // Канал для уведомлений за 7 дней
        createChannelWithSound(
            notificationManager,
            CHANNEL_ID_7_DAYS,
            "Уведомления за 7 дней",
            R.raw.notification_7days
        )

        // Канал для уведомлений за 3 дня
        createChannelWithSound(
            notificationManager,
            CHANNEL_ID_3_DAYS,
            "Уведомления за 3 дня",
            R.raw.notification_3days
        )

        // Канал для уведомлений за 1 день
        createChannelWithSound(
            notificationManager,
            CHANNEL_ID_1_DAY,
            "Уведомления за 1 день",
            R.raw.notification_1day
        )
    }
}
```

**Отправка уведомления:**

```kotlin
private fun sendNotification(
    productName: String,
    daysLeft: Int,
    notificationId: Int
) {
    val channelId = when (daysLeft) {
        7 -> CHANNEL_ID_7_DAYS
        3 -> CHANNEL_ID_3_DAYS
        1 -> CHANNEL_ID_1_DAY
        else -> CHANNEL_ID_7_DAYS
    }

    val notification = NotificationCompat.Builder(applicationContext, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Истекает срок годности!")
        .setContentText("$productName - осталось $daysLeft дней")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(applicationContext)
        .notify(notificationId, notification)
}
```

**Запуск периодической задачи:**

В `NeprosrochApp.onCreate()`:

```kotlin
private fun scheduleExpiryCheck() {
    val workRequest = PeriodicWorkRequestBuilder<ExpiryNotificationWorker>(
        1, TimeUnit.DAYS  // Проверка каждые 24 часа
    ).build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        ExpiryNotificationWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,  // Не создавать дубликаты
        workRequest
    )
}
```

**WorkManager гарантирует:**
- Выполнение даже при закрытом приложении
- Выполнение после перезагрузки устройства
- Автоматические повторы при сбоях
- Соблюдение ограничений батареи Android (Doze mode, App Standby)

### 3.6 Локализация

#### LocaleHelper.kt - Управление языком

```kotlin
object LocaleHelper {
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLanguage(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = activity.resources.configuration
        config.setLocale(locale)

        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }
}
```

**Структура ресурсов:**

```
res/
├── values/            # Русский (по умолчанию)
│   └── strings.xml
├── values-en/         # Английский
│   └── strings.xml
└── values-zh/         # Китайский
    └── strings.xml
```

**Использование в Compose:**

```kotlin
Text(stringResource(R.string.home_title))
```

**stringResource()** автоматически выбирает нужную локализацию на основе текущего Locale.

### 3.7 Аутентификация

#### AuthRepository.kt - Firebase Auth

```kotlin
class AuthRepository(private val auth: FirebaseAuth) {

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.let {
                        continuation.resume(Result.success(it))
                    } ?: continuation.resume(Result.failure(Exception("User is null")))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.let {
                        continuation.resume(Result.success(it))
                    } ?: continuation.resume(Result.failure(Exception("User is null")))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
```

**suspendCancellableCoroutine** - преобразует callback-based API Firebase в корутины

## 4. Особенности реализации

### 4.1 Реактивное программирование

**Flow вместо LiveData:**

```kotlin
// Repository
fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

// ViewModel
private val _products = MutableStateFlow<List<Product>>(emptyList())
val products: StateFlow<List<Product>> = _products.asStateFlow()

init {
    viewModelScope.launch {
        productRepository.getAllProducts().collect { products ->
            _products.value = products.sortedBy { it.getDaysUntilExpiry() }
        }
    }
}

// UI
val products by viewModel.products.collectAsState()
```

**Преимущества Flow:**
- Поддержка suspend функций
- Операторы трансформации (map, filter, combine)
- Отмена при уничтожении корутины
- Лучшая интеграция с Compose

### 4.2 Корутины и асинхронность

**Все IO операции выполняются в фоновых потоках:**

```kotlin
viewModelScope.launch {
    withContext(Dispatchers.IO) {
        // Сетевой запрос
        val result = api.getProduct(barcode)

        // Запись в БД
        productDao.insertProduct(product)
    }

    // Обновление UI на Main потоке
    _uiState.value = UiState.Success(product)
}
```

**Dispatchers:**
- **Dispatchers.Main** - UI операции
- **Dispatchers.IO** - БД, сеть, файлы
- **Dispatchers.Default** - CPU-интенсивные операции

### 4.3 Состояние UI (UI State)

**Sealed class для состояний:**

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

**Использование:**

```kotlin
when (val state = uiState.value) {
    is UiState.Idle -> { /* Пустое состояние */ }
    is UiState.Loading -> CircularProgressIndicator()
    is UiState.Success -> ProductList(state.data)
    is UiState.Error -> ErrorMessage(state.message)
}
```

### 4.4 Dependency Injection (Ручная реализация)

Вместо Dagger/Hilt используется простой паттерн Service Locator через Application класс:

```kotlin
// В NeprosrochApp
companion object {
    lateinit var instance: NeprosrochApp
}

// Использование в любом месте
val repository = NeprosrochApp.instance.productRepository
```

**Преимущества:**
- Простота (не нужно изучать Dagger)
- Быстрая компиляция

**Недостатки:**
- Нет compile-time проверок зависимостей
- Не подходит для больших проектов

### 4.5 Обработка ошибок

**Централизованная обработка в ViewModel:**

```kotlin
suspend fun addProduct(product: Product) {
    _isLoading.value = true
    _error.value = null

    try {
        productRepository.insertProduct(product)
        _successMessage.value = "Продукт добавлен"
    } catch (e: Exception) {
        _error.value = e.message ?: "Неизвестная ошибка"
    } finally {
        _isLoading.value = false
    }
}
```

**Отображение ошибок в UI:**

```kotlin
val error by viewModel.error.collectAsState()

error?.let { errorMessage ->
    Snackbar(
        action = {
            TextButton(onClick = { viewModel.clearError() }) {
                Text("OK")
            }
        }
    ) {
        Text(errorMessage)
    }
}
```

## 5. Производительность и оптимизация

### 5.1 Оптимизация списков

**LazyColumn вместо RecyclerView:**
- Автоматический view recycling
- Ленивая загрузка элементов
- Стабильные ключи для оптимизации recomposition

```kotlin
LazyColumn {
    items(products, key = { it.id }) { product ->
        ProductCard(product)
    }
}
```

### 5.2 Кэширование изображений

**Coil автоматически кэширует:**

```kotlin
AsyncImage(
    model = product.imageUrl,
    contentDescription = product.name,
    modifier = Modifier.size(80.dp),
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error)
)
```

### 5.3 Оптимизация БД запросов

**Индексы на часто запрашиваемые поля:**

```kotlin
@Entity(
    tableName = "products",
    indices = [Index(value = ["expiryDate"])]
)
```

**Flow вместо LiveData** - меньше overhead при recomposition

## 6. Безопасность

### 6.1 Хранение данных

- **Room Database** - данные хранятся в зашифрованной БД приложения
- **SharedPreferences** - настройки хранятся локально (не содержат чувствительных данных)
- **Firebase Auth** - пароли не хранятся локально, только токены сессии

### 6.2 Сетевая безопасность

- **HTTPS** для всех сетевых запросов
- **ProGuard/R8** в release сборке для обфускации кода

## 7. Заключение

Приложение "НеПросрочь!" реализовано с использованием современного Android stack:

**Ключевые технологии:**
- Jetpack Compose для UI
- MVVM + Clean Architecture
- Room для локальной БД
- Retrofit для API
- CameraX + ML Kit для сканирования
- WorkManager для фоновых задач
- Firebase для аутентификации

**Архитектурные решения:**
- Слоистая архитектура (Data, Domain, UI)
- Реактивное программирование (Flow, StateFlow)
- Корутины для асинхронности
- Репозитории для абстракции источников данных
- ViewModel для управления состоянием UI

**Качество кода:**
- Разделение ответственности (SRP)
- Dependency Inversion (зависимости от абстракций)
- Модульность (легко тестировать, расширять)
- Type safety (Kotlin, Room compile-time проверки)

Приложение готово к масштабированию: легко добавить новые экраны, источники данных, функции без изменения существующей архитектуры.
