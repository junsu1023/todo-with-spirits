package com.example.todowithspirits.feature.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.mypage.component.AlarmSwitch
import com.example.todowithspirits.feature.mypage.component.MyPageSettingHeader
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun NotificationSettingScreen(onBack: () -> Unit = {}) {
    var serviceAlarm by remember { mutableStateOf(true) }
    var inactivityReminder by remember { mutableStateOf(true) }
    var periodicPostponed by remember { mutableStateOf(false) }
    var routineAlarm by remember { mutableStateOf(true) }
    var strickSave by remember { mutableStateOf(true) }
    var nightPush by remember { mutableStateOf(false) }
    var promotionConsent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.colors.white)
            .verticalScroll(rememberScrollState())
    ) {
        MyPageSettingHeader(onBack = onBack, title = stringResource(R.string.alarm_setting))

        Spacer(Modifier.height(17.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.service_alarm),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.colors.mainTextColor
                )

                AlarmSwitch(
                    checked = serviceAlarm,
                    onCheckedChange = { serviceAlarm = it }
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.service_alarm_desc, 5),
                fontSize = 16.sp,
                color = SpiritTodoTheme.colors.onSurfaceColor2
            )
        }

        Spacer(Modifier.height(20.dp))

        HorizontalDivider(thickness = 6.dp, color = SpiritTodoTheme.colors.surfaceColor4)

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.detail_alarm_setting),
            fontSize = 14.sp,
            color = SpiritTodoTheme.colors.onSurfaceColor2,
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        Spacer(Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            AlarmToggleRow(
                label = stringResource(R.string.inactivity_reminder_alarm),
                checked = inactivityReminder,
                onCheckedChange = { inactivityReminder = it }
            )

            AlarmToggleRow(
                label = stringResource(R.string.periodic_reminder_postponed_alarm),
                checked = periodicPostponed,
                onCheckedChange = { periodicPostponed = it }
            )

            AlarmToggleRow(
                label = stringResource(R.string.routine_alarm),
                checked = routineAlarm,
                onCheckedChange = { routineAlarm = it }
            )

            AlarmToggleRow(
                label = stringResource(R.string.strick_save_alarm),
                checked = strickSave,
                onCheckedChange = { strickSave = it }
            )

            AlarmToggleRow(
                label = stringResource(R.string.receive_night_time_push_alarm),
                checked = nightPush,
                onCheckedChange = { nightPush = it }
            )

            AlarmToggleRow(
                label = stringResource(R.string.consent_to_receive_promotions),
                checked = promotionConsent,
                onCheckedChange = { promotionConsent = it }
            )
        }

        Spacer(Modifier.height(21.dp))
    }
}

@Composable
private fun AlarmToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = SpiritTodoTheme.colors.mainTextColor,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )

        AlarmSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}