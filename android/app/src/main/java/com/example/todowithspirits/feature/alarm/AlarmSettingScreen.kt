package com.example.todowithspirits.feature.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.todowithspirits.component.SplitsTodoSwitch
import com.example.todowithspirits.theme.SpiritTodoTheme
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.alarm.component.AlarmToggleRow

@Composable
fun AlarmSettingScreen(onBack: () -> Unit) {
    var serviceAlarm by remember { mutableStateOf(true) }
    var inactiveRemind by remember { mutableStateOf(true) }
    var postponedPlan by remember { mutableStateOf(false) }
    var routineGuide by remember { mutableStateOf(true) }
    var streakSave by remember { mutableStateOf(true) }
    var nightPush by remember { mutableStateOf(false) }
    var promotionConsent by remember { mutableStateOf(false) }

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

                    SplitsTodoSwitch(
                        checked = serviceAlarm,
                        onCheckedChange = { serviceAlarm = it }
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.service_alarm_desc, 99),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor2
                )

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(SpiritTodoTheme.colors.surfaceColor4)
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.custom_alarm_setting),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpiritTodoTheme.colors.mainTextColor
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.custom_alarm_setting_desc),
                    fontSize = 12.sp,
                    color = SpiritTodoTheme.colors.onSurfaceColor2
                )

                Spacer(Modifier.height(20.dp))

                AlarmToggleRow(
                    label = stringResource(R.string.inactivity_reminder_alarm),
                    checked = inactiveRemind,
                    onCheckedChange = { inactiveRemind = it }
                )

                Spacer(Modifier.height(29.dp))

                AlarmToggleRow(
                    label = stringResource(R.string.periodic_reminder_postponed_alarm),
                    checked = postponedPlan,
                    onCheckedChange = { postponedPlan = it }
                )

                Spacer(Modifier.height(29.dp))

                AlarmToggleRow(
                    label = stringResource(R.string.routine_alarm),
                    checked = routineGuide,
                    onCheckedChange = { routineGuide = it }
                )

                Spacer(Modifier.height(29.dp))

                AlarmToggleRow(
                    label = stringResource(R.string.strick_save_alarm),
                    checked = streakSave,
                    onCheckedChange = { streakSave = it }
                )

                Spacer(Modifier.height(29.dp))

                AlarmToggleRow(
                    label = stringResource(R.string.receive_night_time_push_alarm),
                    checked = nightPush,
                    onCheckedChange = { nightPush = it }
                )

                Spacer(Modifier.height(29.dp))

                AlarmToggleRow(
                    label = stringResource(R.string.consent_to_receive_promotions),
                    checked = promotionConsent,
                    onCheckedChange = { promotionConsent = it }
                )

                Spacer(Modifier.height(29.dp))
            }
        }
    }
}