package com.example.todowithspirits.feature.alarm

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.NotificationItem
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.feature.alarm.component.AlarmItem
import com.example.todowithspirits.feature.alarm.component.AlarmSectionHeader
import com.example.todowithspirits.feature.alarm.state.AlarmEffect
import com.example.todowithspirits.feature.alarm.state.AlarmEvent
import com.example.todowithspirits.feature.alarm.state.AlarmUiState
import com.example.todowithspirits.feature.alarm.viewmodel.AlarmViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil

@Composable
fun AlarmRoute(
    navigateBack: () -> Unit,
    navigateToSetting: () -> Unit,
    alarmViewModel: AlarmViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by alarmViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by alarmViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(alarmViewModel) {
        alarmViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(alarmViewModel.effect) { effect ->
        when (effect) {
            AlarmEffect.NavigateBack -> navigateBack()
            AlarmEffect.NavigateToSetting -> navigateToSetting()
        }
    }

    AlarmScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = alarmViewModel::onEvent
    )
}

@Composable
fun AlarmScreen(
    uiState: AlarmUiState,
    isLoading: Boolean,
    onEvent: (AlarmEvent) -> Unit
) {
    BackHandler { onEvent(AlarmEvent.BackClicked) }

    val scrollState = rememberScrollState()
    // 목록이 스크롤되면 헤더에 그림자를 넣어 콘텐츠와 구분한다.
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }
    val headerElevation by animateDpAsState(
        targetValue = if (isScrolled) 4.dp else 0.dp,
        label = "alarmHeaderElevation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.surfaceColor4)
    ) {
        AlarmHeader(
            onBackClick = { onEvent(AlarmEvent.BackClicked) },
            onSettingClick = { onEvent(AlarmEvent.SettingClicked) },
            // 아래 목록보다 위에 그려야 그림자가 가려지지 않는다.
            modifier = Modifier
                .zIndex(1f)
                .shadow(headerElevation)
                .background(SpiritTodoTheme.color.surfaceColor4)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.notifications.isEmpty()) {
                if (!isLoading) {
                    EmptyAlarmState()
                }
            } else {
                if (uiState.newAlarms.isNotEmpty()) {
                    AlarmContents(
                        header = stringResource(R.string.new_alarm),
                        alarms = uiState.newAlarms
                    )

                    Spacer(Modifier.height(34.dp))
                }

                if (uiState.pastAlarms.isNotEmpty()) {
                    AlarmContents(
                        header = stringResource(R.string.past_alarm),
                        alarms = uiState.pastAlarms
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    LoadingOverlay(isLoading = isLoading)
}

@Composable
private fun AlarmHeader(
    onBackClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopNaviBar(
        leftIconRes = R.drawable.widgetly_back_24,
        title = stringResource(R.string.alarm),
        rightIconRes = R.drawable.widgetly_setting_24,
        onLeftIconClick = onBackClick,
        onRightIconClick = onSettingClick,
        modifier = modifier.padding(horizontal = 17.dp)
    )
}

@Composable
private fun AlarmContents(
    header: String,
    alarms: List<NotificationItem>
) {
    AlarmSectionHeader(title = header)

    Spacer(modifier = Modifier.height(6.dp))

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        alarms.forEach { alarm ->
            AlarmItem(alarm = alarm)
        }
    }
}

@Composable
private fun EmptyAlarmState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.widgetly_empty),
            contentDescription = null
        )

        Text(
            text = stringResource(R.string.empty_alarm),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.font02
        )
    }
}
