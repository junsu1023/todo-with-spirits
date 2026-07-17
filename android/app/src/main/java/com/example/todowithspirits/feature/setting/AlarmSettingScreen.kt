package com.example.todowithspirits.feature.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.setting.component.AlarmToggleRow
import com.example.todowithspirits.feature.setting.viewmodel.AlarmSettingViewModel

@Composable
fun AlarmSettingScreen(
    alarmSettingViewModel: AlarmSettingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by alarmSettingViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            TitleHeader(
                leftIconRes = R.drawable.todo_back1,
                title = stringResource(R.string.alarm_setting),
                onLeftIconClick = onBack
            )

            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                AlarmToggleRow(
                    label = stringResource(R.string.inactivity_reminder_alarm),
                    checked = uiState.isOnActiveRemind,
                    onCheckedChange = { alarmSettingViewModel.setActiveRemindState(it) }
                )

                AlarmToggleRow(
                    label = stringResource(R.string.periodic_reminder_postponed_alarm),
                    checked = uiState.isOnPostponePlan,
                    onCheckedChange = { alarmSettingViewModel.setPostponePlanState(it) }
                )

                AlarmToggleRow(
                    label = stringResource(R.string.routine_alarm),
                    checked = uiState.isOnRoutineGuide,
                    onCheckedChange = { alarmSettingViewModel.setRoutineGuideState(it) }
                )

                AlarmToggleRow(
                    label = stringResource(R.string.strick_save_alarm),
                    checked = uiState.isOnStreakSave,
                    onCheckedChange = { alarmSettingViewModel.setStreakSaveState(it) }
                )

                AlarmToggleRow(
                    label = stringResource(R.string.receive_night_time_push_alarm),
                    checked = uiState.isOnNightPush,
                    onCheckedChange = { alarmSettingViewModel.setNightPushState(it) }
                )

                AlarmToggleRow(
                    label = stringResource(R.string.consent_to_receive_promotions),
                    checked = uiState.isOnPromotionConsent,
                    onCheckedChange = { alarmSettingViewModel.setPromotionConsentState(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}