package com.example.godbless

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.godbless.data.SettingsManager
import com.example.godbless.ui.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.godbless.ui.screens.auth.AuthScreen
import com.example.godbless.ui.screens.home.HomeScreen
import com.example.godbless.ui.screens.profile.ProfileScreen
import com.example.godbless.ui.screens.scanner.ScannerScreen
import com.example.godbless.ui.screens.shopping.ShoppingScreen
import com.example.godbless.ui.screens.shopping.ShoppingViewModel
import com.example.godbless.ui.screens.shopping.ShoppingViewModelFactory
import com.example.godbless.ui.theme.GODBLESSTheme
import com.example.godbless.utils.LocaleHelper
import android.content.Context

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Применяем сохранённый язык
        val settingsManager = NeprosrochApp.instance.settingsManager
        LocaleHelper.applyLanguage(this, settingsManager.getLanguage())

        setContent {
            AppThemeWrapper {
                MainApp()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val settingsManager = NeprosrochApp.instance.settingsManager
        val context = newBase?.let {
            LocaleHelper.setLocale(it, settingsManager.getLanguage())
        }
        super.attachBaseContext(context ?: newBase)
    }
}

@Composable
fun AppThemeWrapper(content: @Composable () -> Unit) {
    val settingsManager = NeprosrochApp.instance.settingsManager
    var savedTheme by remember { mutableStateOf(settingsManager.getTheme()) }
    val isSystemDark = isSystemInDarkTheme()

    // Обновляем тему каждый раз при ребилде
    LaunchedEffect(Unit) {
        savedTheme = settingsManager.getTheme()
    }

    val darkTheme = when (savedTheme) {
        SettingsManager.THEME_LIGHT -> false
        SettingsManager.THEME_DARK -> true
        else -> isSystemDark // THEME_SYSTEM
    }

    GODBLESSTheme(darkTheme = darkTheme) {
        content()
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    var permissionsGranted by remember { mutableStateOf(false) }

    // Запрос разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
    }

    // Запрашиваем разрешения при первом запуске
    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.CAMERA
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
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
            MainScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shoppingViewModel: ShoppingViewModel = viewModel(
        factory = ShoppingViewModelFactory(NeprosrochApp.instance.shoppingRepository)
    )
    val shoppingItems by shoppingViewModel.shoppingItems.collectAsState()

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Scanner.route,
                    Screen.Shopping.route,
                    Screen.Profile.route
                )
            ) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    shoppingItemsCount = shoppingItems.size
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Scanner.route) {
                ScannerScreen(navController)
            }
            composable(Screen.Shopping.route) {
                ShoppingScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onSignOut = {
                    rootNavController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?,
    shoppingItemsCount: Int = 0
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_scanner)) },
            selected = currentRoute == Screen.Scanner.route,
            onClick = {
                navController.navigate(Screen.Scanner.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        )
        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (shoppingItemsCount > 0) {
                            Badge {
                                Text(shoppingItemsCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                }
            },
            label = { Text(stringResource(R.string.nav_shopping)) },
            selected = currentRoute == Screen.Shopping.route,
            onClick = {
                navController.navigate(Screen.Shopping.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_profile)) },
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        )
    }
}
