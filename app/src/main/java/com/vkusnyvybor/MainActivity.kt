package com.vkusnyvybor

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vkusnyvybor.ui.navigation.AppNavGraph
import com.vkusnyvybor.ui.navigation.BottomNavItem
import com.vkusnyvybor.ui.navigation.Screen
import com.vkusnyvybor.ui.screens.home.HomeViewModel
import com.vkusnyvybor.ui.theme.VkusnyVyborTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VkusnyVyborTheme(dynamicColor = true) {
                SplashScreenWrapper {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun SplashScreenWrapper(content: @Composable () -> Unit) {
    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        isReady = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isReady) {
            content()
        }

        AnimatedVisibility(
            visible = !isReady,
            enter = fadeIn(),
            exit = fadeOut(tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "splash")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(0.8f)
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = BottomNavItem.entries.map { it.route }
    val showBottomBar = currentDestination?.route in bottomBarScreens

    val context = LocalContext.current
    val homeViewModel: HomeViewModel = hiltViewModel(context as ViewModelStoreOwner)

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    tonalElevation = 8.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    BottomNavItem.entries.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        
                        NavigationBarItem(
                            icon = { Icon(imageVector = if (selected) item.selectedIcon else item.unselectedIcon, contentDescription = null) },
                            label = { 
                                Text(
                                    text = item.label, 
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    softWrap = false
                                ) 
                            },
                            selected = selected,
                            onClick = {
                                if (item == BottomNavItem.RESTAURANTS) {
                                    homeViewModel.clearSearch()
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        // ФИКС: Используем отступ снизу, чтобы контент не перекрывался нижней навигацией
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            AppNavGraph(navController = navController)
        }
    }
}
