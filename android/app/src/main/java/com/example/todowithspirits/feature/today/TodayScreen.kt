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
import com.example.todowithspirits.feature.today.component.BadgeAndAchievementRow
import com.example.todowithspirits.feature.today.component.SpiritSection
import com.example.todowithspirits.feature.today.component.TodayHeader
import com.example.todowithspirits.feature.today.component.TodayPlanSection
import com.example.todowithspirits.theme.SpiritTodoTheme

@Composable
fun TodayScreen() {
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
            TodayHeader()

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