package com.example.todowithspirits.feature.setting.data.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.core.viewmodel.BaseViewModel
import com.example.todowithspirits.feature.setting.data.state.DataSettingEffect
import com.example.todowithspirits.feature.setting.data.state.DataSettingEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 화면에 보여줄 상태가 없어 UiState는 두지 않는다.
@HiltViewModel
class DataSettingViewModel @Inject constructor() : BaseViewModel() {
    private val _effect = Channel<DataSettingEffect>(Channel.BUFFERED)
    val effect: Flow<DataSettingEffect> = _effect.receiveAsFlow()

    fun onEvent(event: DataSettingEvent) {
        when (event) {
            DataSettingEvent.BackClicked,
            DataSettingEvent.ConfirmClicked -> sendEffect(DataSettingEffect.NavigateBack)
            // TODO 백업/동기화/데이터 내보내기 기능이 생기면 연결
            DataSettingEvent.BackupClicked,
            DataSettingEvent.SyncClicked,
            DataSettingEvent.ExportDataClicked -> Unit
        }
    }

    private fun sendEffect(effect: DataSettingEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
