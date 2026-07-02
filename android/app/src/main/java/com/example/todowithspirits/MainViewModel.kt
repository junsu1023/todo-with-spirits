package com.example.todowithspirits

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.CheckSystemHealthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val checkSystemHealthUseCase: CheckSystemHealthUseCase
): BaseViewModel() {
    private val _systemHealth = MutableStateFlow("")
    val systemHealth: StateFlow<String> get() = _systemHealth.asStateFlow()

    init {
        checkSystemHealth()
    }

    fun checkSystemHealth() {
        viewModelScope.launch {
            _systemHealth.update { checkSystemHealthUseCase() }
        }
    }
}