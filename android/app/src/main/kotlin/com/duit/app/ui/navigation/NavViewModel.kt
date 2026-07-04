package com.duit.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.duit.app.data.local.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(val tokenStorage: TokenStorage) : ViewModel()
