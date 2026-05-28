package com.vkusnyvybor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.vkusnyvybor.data.repository.AuthMode
import com.vkusnyvybor.data.repository.AuthSession
import com.vkusnyvybor.data.repository.AuthSessionStore
import com.vkusnyvybor.data.repository.OrdersStore
import com.vkusnyvybor.ui.navigation.AppNavGraph
import com.vkusnyvybor.ui.navigation.Screen
import com.vkusnyvybor.ui.theme.VkusnyVyborTheme
import com.vkusnyvybor.ui.theme.engine.LocalThemeDecorations
import com.vkusnyvybor.ui.theme.engine.ThemeEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var ordersStore: OrdersStore

    @Inject
    lateinit var authSessionStore: AuthSessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeEngine.init(this)

        // Перехватываем возврат из браузера после Telegram-входа
        // (deep link vkusnyvybor://auth/callback?...). Делаем это ДО setContent,
        // чтобы стартовый экран сразу стал Home, если сессия уже сохранена.
        handleAuthDeepLink(intent)

        setContent {
            VkusnyVyborTheme(dynamicColor = true) {
                SplashScreenWrapper {
                    val startDestination = if (authSessionStore.isAuthenticated()) {
                        Screen.Home.route
                    } else {
                        Screen.Auth.route
                    }
                    MainApp(ordersStore, startDestination)
                }
            }
        }
    }

    /**
     * Activity объявлена как singleTask, поэтому при возврате из браузера
     * (приложение уже в памяти) система зовёт onNewIntent, а не пересоздаёт
     * экран. Обновляем intent и обрабатываем ссылку — AuthViewModel подписан
     * на стор сессии и сам переведёт на Home.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthDeepLink(intent)
    }

    /**
     * Разбирает deep link вида
     * `vkusnyvybor://auth/callback?success=true&user_hash=...&username=...`
     * и сохраняет НАСТОЯЩИЕ user_hash и имя из ответа шлюза в локальную сессию.
     */
    private fun handleAuthDeepLink(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        if (data.scheme != "vkusnyvybor" || data.host != "auth") return

        val success = data.getQueryParameter("success")
        val userHash = data.getQueryParameter("user_hash")
        val username = data.getQueryParameter("username")

        if (success == "true" && !userHash.isNullOrBlank()) {
            authSessionStore.save(
                AuthSession(
                    userHash = userHash,
                    username = username?.takeIf { it.isNotBlank() } ?: "Telegram-пользователь",
                    mode = AuthMode.TELEGRAM
                )
            )
        }
    }
}

@Composable
fun SplashScreenWrapper(content: @Composable () -> Unit) {
    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(1500); isReady = true }

    val themeLogo = LocalThemeDecorations.current.themeLogo

    Box(modifier = Modifier.fillMaxSize()) {
        if (isReady) content()
        AnimatedVisibility(visible = !isReady, exit = fadeOut(tween(500))) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "splash")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "pulse"
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(160.dp).scale(pulseScale),
                        contentAlignment = Alignment.Center
                    ) {
                        if (themeLogo != null) {
                            themeLogo()
                        } else {
                            // Стандартный логотип, если в теме нет своего
                            Surface(Modifier.size(120.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MainApp(ordersStore: OrdersStore, startDestination: String) {
    val navController = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AppNavGraph(
            navController = navController,
            ordersStore = ordersStore,
            startDestination = startDestination
        )
    }
}
