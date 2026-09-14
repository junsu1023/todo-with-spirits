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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.noRippleClickable
import com.example.todowithspirits.feature.alarm.component.AlarmBanner
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
            .background(SpiritTodoTheme.color.surfaceColor4)
    ) {
        TitleHeader(
            leftIconRes = R.drawable.todo_back1,
            title = stringResource(R.string.alarm),
            rightIconRes = R.drawable.todo_setting,
            onLeftIconClick = leaveScreen,
            onRightIconClick = {
                alarmViewModel.markAllAsRead()
                onSettingClick()
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            AlarmBanner()

            Spacer(Modifier.height(14.dp))

            if (uiState.notifications.isEmpty()) {
                if (!isLoading) {
                    EmptyAlarmState()
                }
            } else {
                if (newAlarms.isNotEmpty()) {
                    AlarmSectionHeader(title = stringResource(R.string.new_alarm))

                    Spacer(Modifier.height(12.dp))

                    newAlarms.forEach { alarm ->
                        AlarmItem(alarm = alarm)
                    }

                    Spacer(Modifier.height(38.dp))
                }

                if (pastAlarms.isNotEmpty()) {
                    AlarmSectionHeader(title = stringResource(R.string.old_alarm))

                    Spacer(Modifier.height(12.dp))

                    pastAlarms.forEach { alarm ->
                        AlarmItem(alarm = alarm)
                    }
                }

                if (uiState.hasNext) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.more),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SpiritTodoTheme.color.systemGrey,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRippleClickable { alarmViewModel.loadMore() }
                            .padding(vertical = 12.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    LoadingOverlay(isLoading = isLoading)
}

@Composable
private fun EmptyAlarmState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.todo_empty),
            contentDescription = null
        )

        Text(
            text = stringResource(R.string.empty_alarm),
            fontSize = 14.sp,
            color = SpiritTodoTheme.color.systemGrey
        )
    }
}
