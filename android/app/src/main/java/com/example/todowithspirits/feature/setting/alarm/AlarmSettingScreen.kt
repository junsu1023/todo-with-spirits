package com.example.todowithspirits.feature.setting.alarm

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todowithspirits.R
import com.example.todowithspirits.component.LoadingOverlay
import com.example.todowithspirits.component.SpiritsTodoPrimaryButton
import com.example.todowithspirits.component.TopNaviBar
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingEffect
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingEvent
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingType
import com.example.todowithspirits.feature.setting.alarm.state.AlarmSettingUiState
import com.example.todowithspirits.feature.setting.alarm.viewmodel.AlarmSettingViewModel
import com.example.todowithspirits.feature.setting.component.AlarmToggleRow
import com.example.todowithspirits.util.ObserveAsEffect
import com.example.todowithspirits.util.ToastUtil

@Composable
fun AlarmSettingRoute(
    navigateBack: () -> Unit,
    alarmSettingViewModel: AlarmSettingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by alarmSettingViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by alarmSettingViewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(alarmSettingViewModel) {
        alarmSettingViewModel.errorMsg.collect { message -> ToastUtil.show(context, message) }
    }

    ObserveAsEffect(alarmSettingViewModel.effect) { effect ->
        when (effect) {
            AlarmSettingEffect.NavigateBack -> navigateBack()
        }
    }

    AlarmSettingScreen(
        uiState = uiState,
        isLoading = isLoading,
        onEvent = alarmSettingViewModel::onEvent
    )
}

@Composable
fun AlarmSettingScreen(
    uiState: AlarmSettingUiState,
    isLoading: Boolean,
    onEvent: (AlarmSettingEvent) -> Unit
) {
    BackHandler { onEvent(AlarmSettingEvent.BackClicked) }

    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            TopNaviBar(
                leftIconRes = R.drawable.todo_back1,
                title = stringResource(R.string.alarm_setting),
                onLeftIconClick = { onEvent(AlarmSettingEvent.BackClicked) },
                modifier = Modifier.padding(horizontal = 17.dp)
            )

            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AlarmSettingType.entries.forEach { type ->
                    AlarmToggleRow(
                        label = stringResource(type.labelRes),
                        checked = uiState.isOn(type),
                        onCheckedChange = { onEvent(AlarmSettingEvent.AlarmToggled(type, it)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        SpiritsTodoPrimaryButton(
            text = stringResource(R.string.check),
            onClick = { onEvent(AlarmSettingEvent.ConfirmClicked) },
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(Modifier.height(48.dp))
    }

    LoadingOverlay(isLoading = isLoading)
}
