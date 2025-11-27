package com.example.godbless

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.godbless.ui.navigation.Screen
import com.example.godbless.ui.screens.home.HomeScreen
import com.example.godbless.ui.screens.profile.ProfileScreen
import com.example.godbless.ui.screens.shopping.ShoppingScreen
import com.example.godbless.ui.theme.GODBLESSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GODBLESSTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Scanner.route,
                    Screen.Shopping.route,
                    Screen.Profile.route
                )
            ) {
                BottomNavigationBar(navController, currentRoute)
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
                ScannerPlaceholderScreen()
            }
            composable(Screen.Shopping.route) {
                ShoppingScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
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
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
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

@Composable
fun ScannerPlaceholderScreen() {
    Surface {
        Text(
            text = "Экран сканера будет здесь. Требуется разрешение камеры.",
            modifier = Modifier.padding(16.dp)
        )
    }
}
