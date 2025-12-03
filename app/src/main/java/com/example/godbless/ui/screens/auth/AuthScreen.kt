package com.example.godbless.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.godbless.NeprosrochApp
import com.example.godbless.R
import com.example.godbless.ui.screens.profile.ProfileViewModel
import com.example.godbless.ui.screens.profile.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            NeprosrochApp.instance.authRepository,
            NeprosrochApp.instance.preferencesRepository
        )
    )
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Если пользователь авторизован, переходим на главный экран
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onAuthSuccess()
        }
    }

    // Показываем сообщение об успехе
    val successMessage by viewModel.successMessage.collectAsState()
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onAuthSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated gradient background
        val infiniteTransition = rememberInfiniteTransition(label = "background")
        val colorShift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "color_shift"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            com.example.godbless.ui.theme.GradientStart.copy(alpha = 0.3f + colorShift * 0.2f),
                            com.example.godbless.ui.theme.GradientMiddle.copy(alpha = 0.4f),
                            com.example.godbless.ui.theme.GradientEnd.copy(alpha = 0.5f - colorShift * 0.2f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Логотип/Иконка приложения с анимацией
                val logoScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "logo_scale"
                )

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(logoScale)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Название приложения
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Форма авторизации с glassmorphism
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = if (isSignUp) stringResource(R.string.auth_title_register) else stringResource(R.string.auth_title_login),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Email поле
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(stringResource(R.string.email)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Пароль
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(R.string.password)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Подтверждение пароля (только при регистрации)
                        AnimatedVisibility(visible = isSignUp) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text(stringResource(R.string.confirm_password)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                            Icon(
                                                if (confirmPasswordVisible) Icons.Default.Visibility
                                                else Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    visualTransformation = if (confirmPasswordVisible)
                                        VisualTransformation.None
                                    else
                                        PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isLoading,
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        // Ошибка
                        if (error != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Кнопка входа/регистрации с градиентным фоном
                        Button(
                            onClick = {
                                if (isSignUp) {
                                    viewModel.signUp(email, password, confirmPassword)
                                } else {
                                    viewModel.signIn(email, password)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 10.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUp) stringResource(R.string.auth_button_register) else stringResource(R.string.auth_button_login),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.1f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Переключение режима
                        TextButton(
                            onClick = {
                                isSignUp = !isSignUp
                                viewModel.clearError()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text(
                                text = if (isSignUp)
                                    stringResource(R.string.auth_switch_to_login)
                                else
                                    stringResource(R.string.auth_switch_to_register),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Информация о приложении
                Text(
                    text = "Отслеживайте сроки годности продуктов\nПолучайте уведомления вовремя\nНе выбрасывайте деньги на ветер!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
                )
            }
        }
        }
    }
}
