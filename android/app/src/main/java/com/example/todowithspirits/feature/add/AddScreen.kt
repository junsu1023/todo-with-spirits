package com.example.todowithspirits.feature.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.todowithspirits.R
import com.example.todowithspirits.component.SelectionTabs
import com.example.todowithspirits.component.TabItems
import com.example.todowithspirits.component.TitleHeader
import com.example.todowithspirits.component.VerticalSpacer20
import com.example.todowithspirits.feature.add.component.SearchArea

@Composable
fun AddScreen() {
    val scheduleText = stringResource(R.string.schedule)
    val todoText = stringResource(R.string.todo)
    val routineText = stringResource(R.string.routine)
    val selectedTab = remember { mutableStateOf(scheduleText) }
    val tabItems = remember { TabItems(items = listOf(scheduleText, todoText, routineText)) }

    Column(modifier = Modifier.fillMaxSize()) {
        TitleHeader(
            leftIconRes = R.drawable.temp_app_icon,
            title = stringResource(R.string.add_todo, selectedTab.value),
            rightIconRes =  R.drawable.alarm_icon
        )

        VerticalSpacer20()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SearchArea()

            VerticalSpacer20()

            SelectionTabs(
                tabItems = tabItems,
                selectedItem = selectedTab.value,
                onItemSelected = { selectedTab.value = it }
            )

            VerticalSpacer20()

            when(selectedTab.value) {
                scheduleText -> ScheduleForm()
                todoText -> TodoForm()
                else -> { }
            }


            VerticalSpacer20()
        }
    }
}
