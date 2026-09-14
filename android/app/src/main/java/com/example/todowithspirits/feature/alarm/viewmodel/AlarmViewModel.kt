package com.example.todowithspirits.feature.alarm.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.core.tag.TAG
import com.example.core.viewmodel.BaseViewModel
import com.example.domain.usecase.GetNotificationsUseCase
import com.example.todowithspirits.feature.alarm.state.AlarmUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/*
TODO 알림 실시간성 미지원 상태 (보류):
현재는 AlarmScreen 진입 시 loadNotifications()로 목록을 한 번만 fetch한다. 폴링/구독 로직이 없어서
화면에 머무는 동안 서버에 새 알림이 생겨도 반영되지 않는다(화면을 벗어났다 재진입해야 갱신됨).
알림은 비동기로 아무 때나 발생하는 특성상 실시간 반영이 중요하므로, 추후 아래 중 하나로 보완이 필요하다.
- 화면이 보이는 동안 짧은 주기(예: 15~30초)로 재조회하는 폴링
- FCM 푸시 연동(백엔드의 푸시 발송 지원 필요) 후 수신 시 목록 갱신
*/
@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> get() = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launchWithLoading {
            getNotificationsUseCase()
                .onSuccess { page ->
                    Log.d(TAG, "loadNotifications = $page")
                    _uiState.update {
                        it.copy(
                            notifications = page.items,
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext
                        )
                    }
                }
                .onFailure {
                    Log.e(TAG, "loadNotifications failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "알림을 불러오지 못했습니다")
                }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        val cursor = state.nextCursor
        if (!state.hasNext || cursor == null) return

        viewModelScope.launchWithLoading {
            getNotificationsUseCase(cursor = cursor)
                .onSuccess { page ->
                    Log.d(TAG, "loadMore notifications = $page")
                    _uiState.update {
                        it.copy(
                            notifications = it.notifications + page.items,
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext
                        )
                    }
                }
                .onFailure {
                    Log.e(TAG, "loadMore notifications failed!", it)
                    emitErrorMsg(it.localizedMessage ?: "알림을 불러오지 못했습니다")
                }
        }
    }
}
