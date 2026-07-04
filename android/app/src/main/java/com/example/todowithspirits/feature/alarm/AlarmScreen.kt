package com.example.todowithspirits.feature.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.feature.alarm.component.AlarmBanner
import com.example.todowithspirits.feature.alarm.component.AlarmHeader
import com.example.todowithspirits.feature.alarm.component.AlarmItem
import com.example.todowithspirits.feature.alarm.component.AlarmSectionHeader
import com.example.todowithspirits.theme.SpiritTodoTheme

data class AlarmData(
    val type: String,
    val message: String,
    val timeLabel: String
)

private val newAlarms = listOf(
    AlarmData("시스템 알림", "오늘 마감인 할 일이 {N}개 남아있어요.\n차근차근 끝내볼까요? 💪", "1시간 전"),
    AlarmData("이벤트", "루틴 5연속 성공 시 경험치가 두배", "1시간 전")
)

private val pastAlarms = listOf(
    AlarmData("시스템 알림", "오늘 마감인 할 일이 {N}개 남아있어요.\n차근차근 끝내볼까요? 💪", "1일 전"),
    AlarmData("정령의 숲", "루미가 OO정령으로 진화했어요~\n지금 바로 만나러 가볼까요?", "2일 전"),
    AlarmData("이벤트", "루틴 5연속 성공 시 경험치가 두배", "06. 01"),
    AlarmData("이벤트", "루틴 5연속 성공 시 경험치가 두배", "06. 01")
)

@Composable
fun AlarmScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpiritTodoTheme.colors.homeColor)
    ) {
        AlarmHeader(onBack = onBack)

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            AlarmBanner()

            Spacer(Modifier.height(14.dp))

            AlarmSectionHeader(title = stringResource(R.string.new_alarm))

            Spacer(Modifier.height(12.dp))

            newAlarms.forEach { alarm ->
                AlarmItem(
                    alarm = alarm,
                    isNew = true
                )
            }

            Spacer(Modifier.height(38.dp))

            AlarmSectionHeader(title = stringResource(R.string.old_alarm))

            Spacer(Modifier.height(12.dp))

            pastAlarms.forEachIndexed { index, alarm ->
                AlarmItem(
                    alarm = alarm,
                    isNew = false
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}


