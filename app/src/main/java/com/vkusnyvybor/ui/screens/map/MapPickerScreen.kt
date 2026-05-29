package com.vkusnyvybor.ui.screens.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.vkusnyvybor.data.repository.AuthSessionStore
import com.vkusnyvybor.data.repository.SelectedLocation
import com.vkusnyvybor.data.repository.SelectedLocationStore
import com.vkusnyvybor.ui.localization.LocalStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Экран выбора предприятия на карте. Показывает встроенную WebView-карту,
 * сохраняет выбор в [SelectedLocationStore] и закрывается.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    onClose: () -> Unit,
    viewModel: MapPickerViewModel = hiltViewModel()
) {
    val s = LocalStrings.current

    BackHandler { onClose() }

    // Спрашиваем геолокацию один раз при входе — чтобы кнопка «найти меня»
    // на карте сразу работала. Отказ не критичен: карта работает и без неё.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* результат не блокирует карту */ }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.mapPickerTitle) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = s.close)
                    }
                }
            )
        }
    ) { padding ->
        MapPickerWebView(
            onResult = { location ->
                viewModel.save(location)
                onClose()
            },
            authToken = viewModel.authToken,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@HiltViewModel
class MapPickerViewModel @Inject constructor(
    private val store: SelectedLocationStore,
    authSessionStore: AuthSessionStore
) : ViewModel() {
    /** user_hash из сессии — отдаём карте как токен авторизации для шлюза. */
    val authToken: String? = authSessionStore.session.value?.userHash

    fun save(location: SelectedLocation) = store.save(location)
}
