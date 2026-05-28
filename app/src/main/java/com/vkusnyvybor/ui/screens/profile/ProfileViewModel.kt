package com.vkusnyvybor.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.vkusnyvybor.data.repository.AuthSession
import com.vkusnyvybor.data.repository.AuthSessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionStore: AuthSessionStore
) : ViewModel() {

    val session: StateFlow<AuthSession?> = sessionStore.session

    fun logout() = sessionStore.clear()
}
