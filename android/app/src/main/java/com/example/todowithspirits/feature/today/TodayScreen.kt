package com.example.todowithspirits.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.feature.today.component.BadgeAndAchievementRow
import com.example.todowithspirits.feature.today.component.SpiritSection
import com.example.todowithspirits.feature.today.component.TodayPlanSection
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun TodayScreen(navigateToAlarm: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.colors.homeColor)
                .padding(horizontal = 16.dp)
        ) {
            TitleHeader(
                leftIconRes = R.drawable.temp_app_icon,
                rightIconRes = R.drawable.todo_alarm,
                onRightIconClick = navigateToAlarm,
                isAlarm = true
            )

            SpiritSection()

            BadgeAndAchievementRow()

            Spacer(Modifier.height(20.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpiritTodoTheme.colors.white)
                .padding(horizontal = 16.dp)
        ) {
            TodayPlanSection()
        }
    }
}
