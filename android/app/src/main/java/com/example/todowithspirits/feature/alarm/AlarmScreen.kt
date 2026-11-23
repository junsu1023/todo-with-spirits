package com.example.todowithspirits.feature.alarm

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.NotificationItem
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.feature.alarm.component.AlarmItem
import com.example.todowithspirits.feature.alarm.component.AlarmSectionHeader
import com.example.todowithspirits.feature.alarm.viewmodel.AlarmViewModel
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.util.ToastUtil

@Composable
fun AlarmScreen(
    alarmViewModel: AlarmViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSettingClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by alarmViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by alarmViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(alarmViewModel) {
        alarmViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    val leaveScreen: () -> Unit = {
        alarmViewModel.markAllAsRead()
        onBack()
    }

    BackHandler(onBack = leaveScreen)

    val newAlarms = uiState.notifications.filter { !it.read }
    val pastAlarms = uiState.notifications.filter { it.read }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(SpiritTodoTheme.color.surfaceColor4)
    ) {
        AlarmHeader(
            onLeaveScreen = leaveScreen,
            markAllAsRead = { alarmViewModel.markAllAsRead() },
            onSettingClick = onSettingClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (uiState.notifications.isEmpty()) {
                if (!isLoading) {
                    EmptyAlarmState()
                }
            } else {
                if (newAlarms.isNotEmpty()) {
                    AlarmContents(
                        header = stringResource(R.string.new_alarm),
                        alarms = newAlarms
                    )

                    Spacer(Modifier.height(34.dp))
                }

                if (pastAlarms.isNotEmpty()) {
                    AlarmContents(
                        header = stringResource(R.string.past_alarm),
                        alarms = pastAlarms
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
    onLeaveScreen: () -> Unit,
    markAllAsRead: () -> Unit,
    onSettingClick: () -> Unit
) {
    TopNaviBar(
        leftIconRes = R.drawable.widgetly_back_24,
        title = stringResource(R.string.alarm),
        rightIconRes = R.drawable.widgetly_setting_24,
        onLeftIconClick = onLeaveScreen,
        onRightIconClick = {
            markAllAsRead()
            onSettingClick()
        },
        modifier = Modifier.padding(horizontal = 17.dp)
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
