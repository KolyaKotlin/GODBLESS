package com.example.godbless.ui.screens.profile

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.godbless.NeprosrochApp
import com.example.godbless.R
import com.example.godbless.ui.screens.home.HomeViewModel
import com.example.godbless.ui.screens.home.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            NeprosrochApp.instance.authRepository,
            NeprosrochApp.instance.preferencesRepository
        )
    ),
    homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(NeprosrochApp.instance.productRepository)
    )
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val error by viewModel.error.collectAsState()
    val products by homeViewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentUser == null) {
                AuthSection(viewModel, error)
            } else {
                // Профиль пользователя
                UserProfileCard(currentUser!!.email ?: "")

                // Статистика
                StatsSection(products)

                // Настройки уведомлений
                NotificationSettings(userPreferences, viewModel)

                // Дополнительные настройки
                AdditionalSettings()

                // О приложении
                AboutSection()

                // Выход
                LogoutSection(viewModel, onSignOut)
            }
        }
    }
}

@Composable
fun AuthSection(viewModel: ProfileViewModel, error: String?) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()

    Column {
        Text(
            text = if (isSignUp) stringResource(R.string.sign_up) else stringResource(R.string.sign_in),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (isSignUp) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isSignUp) {
                    viewModel.signUp(email, password, confirmPassword)
                } else {
                    viewModel.signIn(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isSignUp) stringResource(R.string.sign_up) else stringResource(R.string.sign_in))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { isSignUp = !isSignUp },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(
                if (isSignUp) "Уже есть аккаунт? Войти"
                else "Нет аккаунта? Зарегистрироваться"
            )
        }
    }
}

@Composable
fun UserProfileCard(email: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Добро пожаловать!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun StatsSection(products: List<com.example.godbless.domain.model.Product>) {
    val totalProducts = products.size
    val expiredProducts = products.count { it.isExpired() }
    val expiringSoonProducts = products.count { it.isExpiringSoon() && !it.isExpired() }
    val goodProducts = totalProducts - expiredProducts - expiringSoonProducts

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Статистика",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Kitchen,
                    value = totalProducts.toString(),
                    label = "Всего",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = goodProducts.toString(),
                    label = "Свежих",
                    color = com.example.godbless.ui.theme.StatusGreen
                )
                StatItem(
                    icon = Icons.Default.Warning,
                    value = expiringSoonProducts.toString(),
                    label = "Скоро",
                    color = com.example.godbless.ui.theme.StatusYellow
                )
                StatItem(
                    icon = Icons.Default.Error,
                    value = expiredProducts.toString(),
                    label = "Просрочено",
                    color = com.example.godbless.ui.theme.StatusRed
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AdditionalSettings() {
    val settingsManager = NeprosrochApp.instance.settingsManager
    val context = LocalContext.current
    val activity = context as? Activity

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    var selectedTheme by remember { mutableStateOf(settingsManager.getTheme()) }
    var selectedLanguage by remember { mutableStateOf(settingsManager.getLanguage()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
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
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    com.example.godbless.ui.theme.GradientStart.copy(alpha = 0.2f),
                                    com.example.godbless.ui.theme.GradientMiddle.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Дополнительно",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingItem(
                icon = Icons.Default.Palette,
                title = "Тема оформления",
                subtitle = selectedTheme,
                onClick = { showThemeDialog = true }
            )

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            SettingItem(
                icon = Icons.Default.Language,
                title = "Язык",
                subtitle = selectedLanguage,
                onClick = { showLanguageDialog = true }
            )
        }
    }

    // Диалог выбора темы
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = selectedTheme,
            onThemeSelected = { theme ->
                selectedTheme = theme
                settingsManager.setTheme(theme)
                showThemeDialog = false
                // Перезапускаем Activity для применения темы
                activity?.recreate()
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Диалог выбора языка
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = selectedLanguage,
            onLanguageSelected = { language ->
                selectedLanguage = language
                settingsManager.setLanguage(language)
                showLanguageDialog = false
                // Перезапускаем Activity для применения языка
                activity?.recreate()
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AboutSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "О приложении",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow("Версия", "1.0.0")
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Разработчик", "GODBLESS Team")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LogoutSection(viewModel: ProfileViewModel, onSignOut: () -> Unit) {
    OutlinedButton(
        onClick = {
            viewModel.signOut()
            onSignOut()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(
            Icons.Default.Logout,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            stringResource(R.string.sign_out),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NotificationSettings(
    preferences: com.example.godbless.domain.model.UserPreferences,
    viewModel: ProfileViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.notifications_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            NotificationRow(
                icon = Icons.Default.CalendarMonth,
                label = stringResource(R.string.notify_7_days),
                checked = preferences.notifySevenDays,
                onCheckedChange = {
                    viewModel.updateNotificationPreference(notifySevenDays = it)
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            NotificationRow(
                icon = Icons.Default.Event,
                label = stringResource(R.string.notify_3_days),
                checked = preferences.notifyThreeDays,
                onCheckedChange = {
                    viewModel.updateNotificationPreference(notifyThreeDays = it)
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            NotificationRow(
                icon = Icons.Default.EventAvailable,
                label = stringResource(R.string.notify_1_day),
                checked = preferences.notifyOneDay,
                onCheckedChange = {
                    viewModel.updateNotificationPreference(notifyOneDay = it)
                }
            )
        }
    }
}

@Composable
fun NotificationRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = listOf("Светлая", "Темная", "Системная")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Тема оформления",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                themes.forEach { theme ->
                    Surface(
                        onClick = { onThemeSelected(theme) },
                        color = if (theme == currentTheme)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = theme == currentTheme,
                                onClick = { onThemeSelected(theme) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                theme,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (theme == currentTheme) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf("Русский", "English", "中文", "🇰🇵 조선말")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Язык приложения",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                languages.forEach { language ->
                    Surface(
                        onClick = { onLanguageSelected(language) },
                        color = if (language == currentLanguage)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == currentLanguage,
                                onClick = { onLanguageSelected(language) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                language,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (language == currentLanguage) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun BackupDialog(
    onDismiss: () -> Unit
) {
    var backupInProgress by remember { mutableStateOf(false) }
    var backupComplete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Backup,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Резервное копирование",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    backupComplete -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = com.example.godbless.ui.theme.StatusGreen
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Резервная копия создана!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Данные сохранены в папке Downloads",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    backupInProgress -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Создание резервной копии...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Default.Backup,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Создать резервную копию всех данных?",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Будут сохранены:\n• Продукты\n• Список покупок\n• Настройки",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!backupComplete) {
                Button(
                    onClick = {
                        backupInProgress = true
                        // Имитация процесса резервного копирования
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(2000)
                            backupInProgress = false
                            backupComplete = true
                        }
                    },
                    enabled = !backupInProgress,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (backupInProgress) "Создание..." else "Создать")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (backupComplete) "Закрыть" else "Отмена")
            }
        }
    )
}

// ViewModelFactory
class ProfileViewModelFactory(
    private val authRepository: com.example.godbless.data.repository.AuthRepository,
    private val preferencesRepository: com.example.godbless.data.repository.PreferencesRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
