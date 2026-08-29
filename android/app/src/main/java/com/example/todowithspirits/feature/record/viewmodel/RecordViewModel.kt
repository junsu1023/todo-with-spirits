package com.example.todowithspirits.feature.record.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.GetDailyRecordUseCase
import com.example.todowithspirits.feature.record.state.RecordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val getDailyRecordUseCase: GetDailyRecordUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> get() = _uiState.asStateFlow()

    init {
        loadDailyRecord()
    }

    fun loadDailyRecord() {
        viewModelScope.launchWithLoading {
            getDailyRecordUseCase()
                .onSuccess { record ->
                    Log.d(TAG, "loadDailyRecord = $record")
                    _uiState.update { it.copy(dailyRecord = record) }
                }
                .onFailure {
                    Log.e(TAG, "loadDailyRecord failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "기록을 불러오지 못했습니다")
                }
        }
    }
}
